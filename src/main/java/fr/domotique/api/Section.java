package fr.domotique.api;

import fr.domotique.*;
import io.vertx.core.*;
import io.vertx.core.json.*;
import io.vertx.ext.web.*;

/// A section contains many API endpoints, all grouped under the same URL prefix (usually).
///
/// These routes can be registered using the [#register(Router)] function, which will add all routes
/// related to this section. Usually, that's done under [fr.domotique.RouterVerticle].
///
/// A section lives for the entirety of the server's lifetime.
///
/// Sections can also be nested: one section might register a "subsection" within [#register(Router)].
///
/// Subclasses can access the [Server] easily by simply using the [#server] field.
///
/// ## Example
///
/// ```java
/// /**
///  * Example section that handles product-related API endpoints.
///  *
///  * This example shows:
///  * 1. How to create a basic Section
///  * 2. How to define API endpoints
///  * 3. How to register routes
///  * 4. How to use Future and side effects
///  */
/// public class ProductSection extends Section {
///     // Simple in-memory list of products for demo purposes
///     private final String[] products = {"Lamp", "Chair", "Table", "Bookshelf"};
///
///     /**
///      * Creates a new ProductSection with the given server.
///      */
///     public ProductSection(Server server) {
///         super(server);
///     }
///
///     /**
///      * Handles GET request to fetch all products.
///      */
///     private Future<String[]> getAllProducts(RoutingContext context) {
///         // Log the request without changing the response
///         return Future.succeededFuture(products)
///                 .andThen(whenOk(result -> {
///                     // This is a side effect that doesn't change the result
///                     System.out.println("Products list requested, returning " + result.length + " items");
///            }));
///     }
///
///     /**
///      * Handles GET request to fetch a single product by ID.
///      */
///     private Future<String> getProduct(RoutingContext context) {
///         // Read the product ID from path parameter
///         int productId = readIntPathParam(context, "productId");
///
///         // Check if product ID is valid
///         if (productId < 0 || productId >= products.length) {
///             throw new RequestProblemException("Product not found", 404);
///         }
///
///         return Future.succeededFuture(products[productId]);
///     }
///
///     @Override
///     public void register(Router router) {
///         // Create a sub-router for product endpoints
///         Router productRoutes = Router.router(server.vertx());
///
///         // Define routes
///         productRoutes.get("/").respond(this::getAllProducts);
///         productRoutes.get("/:productId").respond(this::getProduct);
///
///         // Register the sub-router under /api/products
///         router.route("/api/products*").subRouter(productRoutes);
///     }
/// }
/// ```
///
/// @see Router
/// @see Server
public abstract class Section {
    /// The server instance, with various services to access (database, mail, etc.).
    protected final Server server;

    /// Creates a new Section with the given server.
    ///
    /// When using this class, you usually just copy-paste this constructor.
    ///
    /// @param server the server instance to use
    public Section(Server server) {
        this.server = server;
    }

    /// Registers all routes of this section to the given router.
    public abstract void register(Router router);

    // Various utility functions. Will maybe put these somewhere else one day.

    /// Reads a [path parameter](https://vertx.io/docs/5.0.0.CR5/vertx-web/java/#_capturing_path_parameters) as an integer.
    ///
    /// Ends the request with a 400 status code if the parameter is not an integer, or just missing.
    ///
    /// @throws RequestProblemException if the parameter is not an integer
    public static int readIntPathParam(RoutingContext context, String paramName) {
        String paramVal = context.pathParam(paramName);
        if (paramVal == null) {
            throw new RequestProblemException("Parameter " + paramName + " is missing.", 400);
        }

        try {
            return Integer.parseInt(paramVal);
        } catch (NumberFormatException e) {
            throw new RequestProblemException("Parameter " + paramName + " is not an integer.", 400);
        }
    }

    /// Reads the body of the request in JSON format, with the given class.
    ///
    /// Ends the request with a 400 status code if the body is not valid JSON, or when the data is invalid/missing.
    ///
    /// ## Example
    ///
    /// ```java
    /// record DeviceCreationInput(String name, String type) {}
    ///
    /// public Future<Device> createDevice(RoutingContext context) {
    ///     DeviceCreationInput input = readBody(context, DeviceCreationInput.class);
    ///
    ///     // Do stuff with the input
    ///
    ///     return deviceStore.createNewDevice(input);
    /// }
    /// ```
    public static <T> T readBody(RoutingContext context, Class<T> clazz) {
        try {
            return context.body().asPojo(clazz);
        } catch (DecodeException e) {
            throw new RequestProblemException("Invalid JSON body.", 400);
        }
    }

    /// Little utility for [Future#andThen(io.vertx.core.Completable)] that only runs the function on success.
    ///
    /// ## Usage
    /// **Before**
    /// ```java
    /// someFuture.andThen(x -> {
    ///     if (x.succeeded()) {
    ///         doThing(x.result());
    ///     }
    /// })
    /// ```
    ///
    /// **After**
    /// ```java
    /// someFuture.andThen(whenOk(x -> {
    ///     doThing(x);
    /// }));
    /// ```
    public static <T> Handler<AsyncResult<T>> whenOk(Handler<T> handler) {
        return x -> {
            if (x.succeeded()) {
                handler.handle(x.result());
            }
        };
    }
}
