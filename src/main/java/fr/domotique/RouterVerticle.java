package fr.domotique;

import fr.domotique.api.*;
import fr.domotique.site.*;
import io.vertx.core.*;
import io.vertx.core.buffer.*;
import io.vertx.core.http.*;
import io.vertx.core.json.*;
import io.vertx.ext.web.*;
import io.vertx.ext.web.handler.*;
import org.slf4j.*;

import java.io.*;

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
    private final Server server;

    /**
     * Logger to help us track what's happening in the server
     */
    private static final Logger log = LoggerFactory.getLogger(RouterVerticle.class);

    /**
     * Creates a new RouterVerticle with the given server settings.
     *
     * @param server contains important settings like the port number
     */
    public RouterVerticle(Server server) {
        this.server = server;
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
        // NOTE: This is registered *before* /assets/* so don't check the session when loading frontend data.
        r.route().handler(SessionHandler.create(server.sessionStore()));

        // Register the Authenticator to do user authentication.
        r.route().handler(ctx -> {
            ctx.put(Authenticator.KEY, new Authenticator(ctx, server));
            ctx.next();
        });

        // Add an error handler that sends correct responses when we throw a RequestException.
        r.route().failureHandler(RouterVerticle::handleRequestProblems);

        // Add an error handler to show a nice error page when something UNEXPECTED goes wrong
        r.route().failureHandler(ErrorHandler.create(vertx));

        // Limit the incoming requests to 128KB of data, for better security.
        // Could also be used later on to add support for file uploads.
        r.route().handler(BodyHandler.create().setBodyLimit(128 * 1024 * 1024));

        // Register all sections of our API.
        var userApi = new UserSection(server);
        userApi.register(r);

        // Register all sections of our website.
        var homeSection = new HomeSection(server);
        homeSection.register(r);

        // note to self: ctx.addEndHandlers can be useful for middlewares

        // Create and start the HTTP server
        return vertx.createHttpServer()
                .requestHandler(r)
                .listen(server.config().port());
    }

    /// This function is called when an exception is thrown during a request,
    /// and will adapt the response accordingly.
    private static void handleRequestProblems(RoutingContext ctx) {
        // Declare a little record so we can send JSON with these properties:
        // {
        //    "message": "Exception's message",
        //    "code": "EXCEPTION_ERR_CODE", // or null if errorCode is null
        //    "details": {...} // or null if details is null
        // }
        record ErrorResponse(String message, String code, Object data) {}

        // Is this exception one of those special RequestExceptions?
        if (ctx.failure() instanceof RequestException pb) {
            // Yes! Now make sure we can write to the response.
            if (ctx.response().headWritten()) {
                log.error("Can't write problem details; response has already begun! ({})", pb.getMessage());
                return;
            }

            // And now, write a JSON response with information about the error
            var responseToSend = new ErrorResponse(pb.getMessage(), pb.getErrorCode(), pb.getDetails());
            ctx.response()
                    .setStatusCode(pb.getStatusCode())
                    .putHeader(HttpHeaders.CONTENT_TYPE, "text/json; charset=utf-8")
                    .end(Json.encode(responseToSend));
        } else {
            // Nope, give it to the other error handler
            ctx.next();
        }
    }

    /// Serves the openapi.yml file in development mode at the /api-docs/openapi.yml URL,
    /// and serves the Swagger UI page as well.
    private void serveApiDocumentation(Router r) throws IOException {
        // First read the entirety of the openapi.yml file in a "yml" buffer.
        final Buffer yml;
        try (InputStream stream = RouterVerticle.class.getResourceAsStream("/openapi.yml")) {
            if (stream != null) {
                yml = Buffer.buffer(stream.readAllBytes());
            } else {
                yml = null;
            }
        }

        // The serve it! (if possible...)
        if (yml != null) {
            // First serve the files for the Scalar API Docs
            r.route("/api-docs*").handler(StaticHandler.create("scalar"));

            // Then serve the openapi.yml file from the buffer
            r.route("/api-docs/openapi.yml").handler(ctx -> {
                ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "text/yaml").end(yml);
            });
        } else {
            log.warn("No openapi.yml file found, openapi.yml will not be available.");
        }
    }
}
