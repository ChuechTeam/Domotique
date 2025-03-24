package fr.domotique;

import fr.domotique.base.*;
import fr.domotique.data.*;
import fr.domotique.email.*;
import io.vertx.core.*;
import io.vertx.core.http.*;
import io.vertx.ext.auth.prng.*;
import io.vertx.ext.web.*;
import io.vertx.ext.web.sstore.*;
import io.vertx.ext.web.templ.jte.*;
import io.vertx.mysqlclient.*;
import io.vertx.sqlclient.*;
import io.vertx.sqlclient.PoolOptions;
import org.slf4j.*;

import java.io.*;

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

        // Create the Database object containing all our tables for easy access.
        Database db = new Database(
            new UserTable(client),
            new RoomTable(client),
            new DeviceTypeTable(client),
            new DeviceTable(client)
        );

        // Create the session store, storing user sessions for logged-in users, currently in-memory.
        LocalSessionStore sessionStore = LocalSessionStore.create(vertx);

        // Make the JTE templating engine, so we can render HTML
        JteTemplateEngine templateEngine = JteTemplateEngine.create(vertx, "views");

        // Create the e-mail client to send mails to the console (for now)
        EmailSender email = new ConsoleEmail();

        // Create the server object, with our new SqlClient and the configuration
        var server = new Server(client, db, sessionStore, templateEngine, email, config, vertx);

        // Set how many instances of RouterVerticle to deploy (one per CPU)
        var options = new DeploymentOptions().setInstances(Runtime.getRuntime().availableProcessors()).setThreadingModel(ThreadingModel.EVENT_LOOP);

        // Launch the Vue Development Server in dev mode, and if it's not yet launched of course.
        if (config.isDevelopment()) {
            launchVueDevServer();
        }

        // Deploy RouterVerticles and log the server address when ready
        return vertx.deployVerticle(() -> new RouterVerticle(server), options).andThen(x -> {
            log.info("Server ready at http://localhost:{} ({} instances)", config.port(), options.getInstances());
            if (config.isDevelopment()) {
                log.info("API documentation is available at http://localhost:{}/api-docs", config.port());
            }
        });
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
}
