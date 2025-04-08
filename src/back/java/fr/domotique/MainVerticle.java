package fr.domotique;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.jdk8.*;
import fr.domotique.api.users.*;
import fr.domotique.base.*;
import fr.domotique.base.data.*;
import fr.domotique.data.*;
import fr.domotique.email.*;
import io.vertx.core.*;
import io.vertx.core.http.*;
import io.vertx.core.json.jackson.*;
import io.vertx.ext.auth.prng.*;
import io.vertx.ext.web.*;
import io.vertx.ext.web.sstore.*;
import io.vertx.ext.web.templ.jte.*;
import io.vertx.mysqlclient.*;
import io.vertx.sqlclient.*;
import io.vertx.sqlclient.PoolOptions;
import liquibase.exception.*;
import org.openapitools.jackson.nullable.*;
import org.slf4j.*;

import java.io.*;
import java.util.stream.*;

/// This is the starting point of our web server application.
/// It's the main control center that sets everything up.
///
/// In the Vert.x framework, we use "verticles":
/// - verticles are **pieces of code** that can run simultaneously with other verticles
/// - we can **start** them, by deploying them
/// - we can **stop** them, usually when the app stops
/// - they are very similar to **processes** or **threads**, but much more lightweight
///
/// More info on [Vert.X's documentation](https://vertx.io/docs/5.0.0.CR5/vertx-core/java/#_verticles)
///
/// This verticle's role is to:
/// - **initialize** the services we use (databases, configuration...)
/// - **ensure** that the database is operational
/// - **deploy** [router verticles][RouterVerticle], receiving every HTTP request from the browser
///
/// Services we've initialized are all stored in a [Server] object, which
/// is used everywhere in the project, so we can quickly access the database for example.
///
/// This verticle deploys many [RouterVerticle] instances, one per CPU core.
/// This allows for every core of the processor to handle incoming HTTP requests, making
/// it very efficient!
///
/// @author Dynamic
/// @see VerticleBase
/// @see Server
public class MainVerticle extends VerticleBase {
    // The logger used to print messages to the console with nice colors.
    private static final Logger log = LoggerFactory.getLogger(MainVerticle.class);

    static {
        // Register the Java 8 module for JSON serialization so we can use Optional<T>.
        DatabindCodec.mapper().registerModule(new Jdk8Module().configureReadAbsentAsNull(true));
        DatabindCodec.mapper().registerModule(new JsonNullableModule());
    }

    // The starting point of our main verticle.
    // Should be started using VIRTUAL_THREADS, so we can use await()
    @Override
    public Future<?> start() {
        // Load the configuration from the various config-xxx.properties files.
        Config config = Config.load();

        // Create a new SqlClient to connect to the MySQL database
        SqlClient client = MySQLBuilder.client().with(new PoolOptions().setMaxSize(100)).connectingTo(config.databaseUri()).using(vertx).build();

        // Run a test query to make sure the database is correctly connected.
        try {
            // Here ".await()" allows us to wait that the future is complete.
            // This is only possible because we're in a VIRTUAL_THREAD mode, which gives practicality at
            // the expense of performance.
            client.query("SELECT 1 AS patate").execute().await();
            log.info("Database connection successful!");
        } catch (Exception e) {
            log.error("""
                Couldn't connect to the database!
                Make sure the database is configured properly; use the 'gradle updateDatabase'/liquibase command!
                """, e);
            return Future.failedFuture(e);
        }

        if (config.seedDatabase()) {
            seedDatabase(config, client).await();
        }

        // Create the Database object containing all our tables for easy access.
        Database db = new Database(client);

        // Create the session store, storing user sessions for logged-in users, currently in-memory.
        LocalSessionStore sessionStore = LocalSessionStore.create(vertx);

        // Make the JTE templating engine, so we can render HTML
        JteTemplateEngine templateEngine = JteTemplateEngine.create(vertx, "views");

        // Create the e-mail client to send mails using SendGrid or the console (depending on the configuration)
        EmailSender email;
        if (config.sendGridToken() != null) {
            email = new SendGridEmail(vertx, config.sendGridToken(), config.sendGridEmail());
            log.info("Using SendGrid email service using email {}", config.sendGridEmail());
        } else {
            email = new ConsoleEmail();
            log.info("Using console email service");
        }

        // Create the server object, with our new SqlClient and the configuration
        var server = new Server(client, db, sessionStore, templateEngine, email, config, vertx);

        // Set how many instances of RouterVerticle to deploy (one per CPU)
        var options = new DeploymentOptions().setInstances(Runtime.getRuntime().availableProcessors()).setThreadingModel(ThreadingModel.EVENT_LOOP);

        // Launch the Vue Development Server in dev mode, and if it's not yet launched of course.
        if (config.isDevelopment()) {
            launchVueDevServer();
        }

        // Deploy RouterVerticles and log the server address when ready
        // Also deploy other verticles, like the PointsVerticle
        return vertx.deployVerticle(() -> new RouterVerticle(server), options)
            .andThen(x -> {
                log.info("Server ready at http://localhost:{} ({} instances)", config.port(), options.getInstances());
                if (config.isDevelopment()) {
                    log.info("API documentation is available at http://localhost:{}/api-docs", config.port());
                }
            })
            .compose(_ -> vertx.deployVerticle(new PointsVerticle(server)));
    }

    @Override
    public Future<?> stop() {
        // Close the [experimental!] Virtual Thread Executor
        Section.vtExecutor.close();
        return Future.succeededFuture();
    }

    private void launchVueDevServer() {
        // TODO: Make port configurable
        vertx.createHttpClient().connect(new HttpConnectOptions().setHost("localhost").setPort(5173).setSsl(false))
            .onSuccess(HttpClientConnection::close)
            .onFailure(x -> {
                log.info("Vue.js dev server not running. Starting it...");
                vertx.executeBlocking(() -> {
                    try {
                        // Create process builder to run npm command
                        ProcessBuilder processBuilder = new ProcessBuilder();

                        // Run npm run dev in a separate process
                        if (System.getProperty("os.name").toLowerCase().contains("win")) {
                            processBuilder.command("cmd.exe", "/c", "start cmd.exe /k npm run dev");
                        } else {
                            processBuilder.command("setsid", "gnome-terminal", "--", "bash", "-c", "npm run dev");
                        }

                        // Set working directory to the front folder
                        processBuilder.directory(new File("./../front"));

                        // Start it!
                        Process process = processBuilder.start();
                        log.info("Vue.js dev server started in a separate process");

                        return process;
                    } catch (Exception e) {
                        log.error("""
                            Failed to start Vue.js dev server! Make sure:
                            - NPM is installed
                            - You have run "npm install" in the src/front/ folder""", e);
                        return null;
                    }
                });
            });
    }

    private Future<Void> seedDatabase(Config config, SqlClient client) {
        try {
            log.info("Running any migration before seeding the database...");

            try {
                MigrationRunner.update(config);
            } catch (CommandExecutionException e) {
                log.warn("Failed to run migrations before seeding the database", e);
            }

            log.info("Seeding the database...");
            InputStream stream = getClass().getResourceAsStream("/seed.sql");
            if (stream == null) {
                throw new RuntimeException("Failed to find the seed.sql file in the classpath.");
            }

            try (var streamReader = new BufferedReader(new InputStreamReader(stream))) {
                String sql = streamReader.lines().collect(Collectors.joining("\n"));

                String seedQuery = "START TRANSACTION;\n" + sql + "\nCOMMIT;";
                log.debug("Executing seed SQL query: {}", seedQuery);
                return client.query(seedQuery).execute().mapEmpty();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load seed.sql", e);
        }
    }
}
