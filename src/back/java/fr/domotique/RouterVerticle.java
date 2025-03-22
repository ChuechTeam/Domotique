package fr.domotique;

import fr.domotique.api.*;
import fr.domotique.base.*;
import fr.domotique.base.apidocs.*;
import io.vertx.core.*;
import io.vertx.core.http.*;
import io.vertx.core.json.*;
import io.vertx.ext.auth.prng.*;
import io.vertx.ext.web.*;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.proxy.handler.*;
import io.vertx.httpproxy.*;
import org.slf4j.*;

/**
 * A RouterVerticle handles incoming HTTP requests by directing them to the right place.
 *
 * <p>
 * When someone visits our website, this class:
 * <ul>
 *   <li>creates a server that listens for visitors
 *   <li>serves static files (like images, CSS, JavaScript) from the 'assets' folder
 *   <li>shows different pages depending on the URL they visit
 * </ul>
 *
 * <p>
 * For example:
 * <ul>
 *   <li>if someone visits "/", they see the homepage
 *   <li>if someone visits "/assets/images/cat.jpg", they get that image from the 'assets/images' folder
 * </ul>
 *
 * <p>
 * This class is part of our web server and works together with {@link MainVerticle}
 * to handle website traffic.
 *
 * @see MainVerticle
 * @see Server
 */
public class RouterVerticle extends VerticleBase {
    /**
     * The server settings, like port number and database connection
     */
    final Server server;

    /**
     * Logger to help us track what's happening in the server
     */
    static final Logger log = LoggerFactory.getLogger(RouterVerticle.class);

    /**
     * Creates a new RouterVerticle with the given server settings.
     *
     * @param server contains important settings like the port number
     */
    public RouterVerticle(Server server) {
        this.server = server;
    }

    /// The array with all [sections][Section] we should activate.
    Section[] allSections() {
        return new Section[]{
            new UserSection(server)
        };
    }

    /**
     * Starts the web server and sets up all the routes.
     * This method runs when the verticle is deployed.
     *
     * @return a Future that completes when the server is ready
     * @throws Exception if something goes wrong while starting
     */
    @Override
    public Future<?> start() throws Exception {
        // Create a new router to handle different URLs
        var r = Router.router(vertx);

        // Serve files from the 'assets' folder when someone requests them from the assets/ folder
        r.route("/assets/*").handler(StaticHandler.create("assets"));

        // Serve API documentation at the /api-docs/ URL when we're in development mode
        if (server.config().isDevelopment()) {
            serveApiDocumentation(r);
        }

        // Register the session handler, which is going to manage our user session in the cookie for us.
        r.route("/api/*").handler(SessionHandler.create(server.sessionStore()));

        // Register the Authenticator to do user authentication.
        r.route("/api/*").handler(ctx -> {
            ctx.put(Authenticator.KEY, new Authenticator(ctx, server));
            ctx.next();
        });

        // Add an error handler that sends correct responses when we throw a RequestException.
        r.route("/api/*").failureHandler(RouterVerticle::handleRequestProblems);

        // Add an error handler to show a nice error page when something UNEXPECTED goes wrong
        r.route("/api/*").failureHandler(ErrorHandler.create(vertx));

        // Limit the incoming requests to 128KB of data, for better security.
        // Could also be used later on to add support for file uploads.
        r.route("/api/*").handler(BodyHandler.create().setBodyLimit(128 * 1024 * 1024));

        // Make sure the client does NOT cache requests to the API.
        r.route("/api/*").handler(ctx -> {
            ctx.response().putHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
            ctx.next();
        });

        // Register all sections.
        for (Section section : allSections()) {
            section.register(r);
        }

        // If nothing works, and we're in dev mode; redirect all GET requests to the vue dev server
        if (server.config().isDevelopment()) {
            HttpClient client = vertx.createHttpClient();
            HttpProxy proxy = HttpProxy.reverseProxy(client);
            proxy.origin(5173, "localhost");

            r.get().handler(ProxyHandler.create(proxy));
        }

        // note to self: ctx.addEndHandlers can be useful for middlewares

        // Create and start the HTTP server
        return vertx.createHttpServer()
            .requestHandler(r)
            .listen(server.config().port());
    }

    /// This function is called when an exception is thrown during a request,
    /// and will adapt the response accordingly.
    private static void handleRequestProblems(RoutingContext ctx) {
        // Is this exception one of those special RequestExceptions?
        if (ctx.failure() instanceof RequestException pb) {
            // Yes! Now make sure we can write to the response.
            if (ctx.response().headWritten()) {
                log.error("Can't write problem details; response has already begun! ({})", pb.getMessage());
                return;
            }

            // And now, write a JSON response with information about the error
            var responseToSend = new ErrorResponse<>(pb.getMessage(), pb.getErrorCode(), pb.getDetails());
            ctx.response()
                .setStatusCode(pb.getStatusCode())
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                .end(Json.encode(responseToSend));
        } else {
            // Nope, give it to the other error handler
            ctx.next();
        }
    }

    /// Serves the generated openapi.yml file in development mode at the /api-docs/openapi.yml URL,
    /// and serves the Swagger UI page as well.
    private void serveApiDocumentation(Router r) {
        // First serve the files for the Scalar API Docs
        r.route("/api-docs*").handler(StaticHandler.create("scalar"));

        // Generate the docs in background. It's a bit inefficient for every core to calculate it,
        // but it's so insignifiant that we don't care.
        Future<String> ymlTask = vertx.executeBlocking(() -> DocsGen.generate(r));

        // Then serve it when ready
        r.route("/api-docs/openapi.yml").handler(ctx -> ymlTask
            .onSuccess(yml -> ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "text/yaml").end(yml))
            .onFailure(ctx::fail));
    }
}
