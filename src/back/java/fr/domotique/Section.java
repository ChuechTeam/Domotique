package fr.domotique;

import fr.domotique.apidocs.*;
import io.vertx.core.*;
import io.vertx.core.buffer.*;
import io.vertx.core.json.*;
import io.vertx.ext.web.*;

import java.util.*;

/// A section contains many Web endpoints (API or Views), all grouped under the same URL prefix (usually).
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
///     public ProductSection(Server server){
///         super(server);
///}
///
///     /**
///      * Handles GET request to fetch all products.
///      */
///     private Future<String[]> getAllProducts(RoutingContext context){
///         // Log the request without changing the response
///         return Future.succeededFuture(products)
///                 .andThen(whenOk(result -> {
///                     // This is a side effect that doesn't change the result
///                     System.out.println("Products list requested, returning " + result.length + " items");
///}));
///}
///
///     /**
///      * Handles GET request to fetch a single product by ID.
///      */
///     private Future<String> getProduct(RoutingContext context){
///         // Read the product ID from path parameter
///         int productId = readIntPathParam(context, "productId");
///
///         // Check if product ID is valid
///         if (productId < 0 || productId >= products.length){
///             throw new RequestException("Product not found", 404);
///}
///
///         return Future.succeededFuture(products[productId]);
///}
///
///     @Override
///     public void register(Router router) {
///                   // Create a sub-router for product endpoints
///                   Router productRoutes = Router.router(server.vertx());
///
///                   // Define routes
///                   productRoutes.get("/").respond(this::getAllProducts);
///                   productRoutes.get("/:productId").respond(this::getProduct);
///
///                   // Register the sub-router under /api/products
///                   router.route("/api/products*").subRouter(productRoutes);
///     }
///}
///```
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
    /// @throws RequestException if the parameter is not an integer
    public static int readIntPathParam(RoutingContext context, String paramName) {
        String paramVal = context.pathParam(paramName);
        if (paramVal == null) {
            throw new RequestException("Parameter " + paramName + " is missing.", 400);
        }

        try {
            return Integer.parseInt(paramVal);
        } catch (NumberFormatException e) {
            throw new RequestException("Parameter " + paramName + " is not an integer.", 400);
        }
    }

    /// Reads the body of the request in JSON format, with the given class.
    ///
    /// Ends the request with a 400 status code if the body is not valid JSON, or when the data is invalid/missing.
    ///
    /// ## Example
    ///
    /// ```java
    /// record DeviceCreationInput(String name, String type){}
    ///
    /// public Future<Device> createDevice(RoutingContext context){
    ///     DeviceCreationInput input = readBody(context, DeviceCreationInput.class);
    ///
    ///     // Do stuff with the input
    ///
    ///     return deviceStore.createNewDevice(input);
    ///}
    ///```
    public static <T> T readBody(RoutingContext context, Class<T> clazz) {
        try {
            return context.body().asPojo(clazz);
        } catch (DecodeException e) {
            throw new RequestException("Invalid JSON body.", 400);
        }
    }

    /// Little utility for [Future#andThen(io.vertx.core.Completable)] that only runs the function on success.
    ///
    /// ## Usage
    /// **Before**
    /// ```java
    /// someFuture.andThen(x -> {
    ///     if (x.succeeded()){
    ///         doThing(x.result());
    ///     }
    ///})
    ///```
    ///
    /// **After**
    /// ```java
    /// someFuture.andThen(whenOk(x -> {
    ///     doThing(x);
    /// }));
    ///```
    public static <T> Handler<AsyncResult<T>> whenOk(Handler<T> handler) {
        return x -> {
            if (x.succeeded()) {
                handler.handle(x.result());
            }
        };
    }

    public static RouteDoc doc(Route route) {
        var rd = new RouteDoc();
        route.putMetadata(RouteDoc.KEY, rd);
        return rd;
    }


    /// Creates a new router (using the server's vertx instance).
    ///
    /// This is equivalent to
    /// ```java
    /// Router.router(server.vertx());
    /// ```
    protected Router newRouter() {
        return Router.router(server.vertx());
    }

    /// Renders an HTML JTE template from the `views/` folder, having the given `name`.
    ///
    /// ## Example
    ///
    /// ```java
    /// Future<Buffer> dashboard(RoutingContext ctx){
    ///    // Render the views/dashboard.jte template.
    ///    return view(ctx, "dashboard.jte");
    ///}
    ///```
    protected final Future<Buffer> view(RoutingContext context, String name) {
        return server.templateEngine().render(Map.of(), name)
                .andThen(_ -> context.response().putHeader("Content-Type", "text/html"));
    }

    /// Renders an HTML JTE template from the `views/` folder, having the given `name`, with arguments.
    ///
    /// Arguments are taken in form of a [map][Map] (a dictionary, if you prefer).
    ///
    /// Each argument will be sent to the JTE template, which can use them through `@param` statements.
    ///
    /// ## Example
    ///
    /// **Java Section**
    /// ```java
    /// Future<Buffer> dashboard(RoutingContext ctx){
    ///    // Create a Map to pass arguments to the JTE template.
    ///    Map<String, Object> map = new HashMap<String, Object>();
    ///
    ///    // Add in some interesting arguments
    ///    map.put("user", "Alice");
    ///    map.put("money", -300);
    ///
    ///    // Render the views/dashboard.jte template with the arguments.
    ///    return view(ctx, "dashboard.jte", map);
    ///}
    ///```
    ///
    /// **JTE Template** at `views/dashboard.jte`
    /// ```html
    /// @param String user
    /// @param Integer money
    ///
    /// <h1>Welcome, ${user}!</h1>
    /// <p>You have ${money} euros in your account.</p>
    /// @if(money<0)
    /// <p>You have a negative balance.</p>
    /// <p><b>TIP:</b> Consider getting a job.</p>
    /// @endif
    /// ```
    protected final Future<Buffer> view(RoutingContext context, String name, Map<String, Object> argumentMap) {
        return server.templateEngine().render(argumentMap, name)
                .andThen(_ -> context.response().putHeader("Content-Type", "text/html"));
    }

    /// Renders an HTML JTE template from the `views/` folder, having the given `name`, with arguments.
    ///
    /// Arguments are given using the [#viewArg] method, which is much more practical to use.
    ///
    /// ## Example
    ///
    /// ```java
    /// Future<Buffer> dashboard(RoutingContext ctx){
    ///    // Render the views/dashboard.jte template with arguments using varargs syntax.
    ///    return view(ctx, "dashboard.jte",
    ///        viewArg("user", "Alice"),
    ///        viewArg("money", -300),
    ///        viewArg("isAdmin", false)
    ///    );
    ///}
    ///```
    ///
    /// **JTE Template** at `views/dashboard.jte`
    /// ```html
    ///@param String user
    ///@param Integer money
    ///@param Boolean isAdmin
    ///
    /// <h1>Welcome, ${user}!</h1>
    /// <p>You have ${money} euros in your account.</p>
    ///@if(money<0)
    ///     <p>You have a negative balance.</p>
    ///@endif
    ///@if(isAdmin)
    ///     <a href="/admin">Admin Panel</a>
    ///@endif
    /// ```
    @SafeVarargs
    protected final Future<Buffer> view(RoutingContext context, String name, Map.Entry<String, Object>... argumentList) {
        return server.templateEngine().render(Map.ofEntries(argumentList), name)
                .andThen(_ -> context.response().putHeader("Content-Type", "text/html"));
    }

    /// Create a view argument to be used with
    /// [the view function][Section#view(RoutingContext, String, Map.Entry\[\])].
    protected final Map.Entry<String, Object> viewArg(String key, Object value) {
        return Map.entry(key, value);
    }
}
