package fr.domotique.base;

import fr.domotique.*;
import fr.domotique.base.apidocs.*;
import io.vertx.core.*;
import io.vertx.core.Future;
import io.vertx.core.buffer.*;
import io.vertx.core.json.*;
import io.vertx.ext.web.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

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
///@Override
///publicvoid register(Router router){
///                   // Create a sub-router for product endpoints
///                   Router productRoutes = Router.router(server.vertx());
///
///                   // Define routes
///                   productRoutes.get("/").respond(this::getAllProducts);
///                   productRoutes.get("/:productId").respond(this::getProduct);
///
///                   // Register the sub-router under /api/products
///                   router.route("/api/products*").subRouter(productRoutes);
///}
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

    /// Parses the integer of the given value. Returns `null` if either:
    /// - the value isn't a valid integer
    /// - the value is `null`
    public static Integer readIntOrNull(String value) {
        try {
            if (value == null) {return null;}
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /// Parses the unsigned long of the given value. Returns `null` if either:
    /// - the value isn't a valid unsigned long
    /// - the value is `null`
    public static Long readUnsignedLongOrNull(String value) {
        try {
            if (value == null) {return null;}
            return Long.parseUnsignedLong(value);
        } catch (NumberFormatException e) {
            return null;
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
            throw new RequestException("Invalid JSON body.", 400, "INVALID_JSON");
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
    ///}
    ///})
    ///```
    ///
    /// **After**
    /// ```java
    /// someFuture.andThen(whenOk(x -> {
    ///     doThing(x);
    ///}));
    ///```
    public static <T> Handler<AsyncResult<T>> whenOk(Handler<T> handler) {
        return x -> {
            if (x.succeeded()) {
                handler.handle(x.result());
            }
        };
    }

    /// When encountering a particular type of exception, transforms it into another one.
    ///
    /// Made to be used in conjunction with [Future#recover(java.util.function.Function)]
    ///
    /// ## Example
    ///
    /// ```java
    /// someFuture.recover(errMap(IllegalArgumentException.class, e -> new RequestException("Invalid input", 400)));
    /// ```
    public static <E extends Throwable, T> Function<Throwable, Future<T>> errMap(Class<E> exceptionClass,
                                                                                 Function<E, ? extends Throwable> action) {
        return ex -> {
            if (exceptionClass.isInstance(ex)) {
                return Future.failedFuture(action.apply(exceptionClass.cast(ex)));
            } else {
                return Future.failedFuture(ex);
            }
        };
    }

    /// Add API documentation to a route using [RouteDoc]
    ///
    /// ## Example
    /// ```java
    /// doc(router.get("/api/users/:id")).summary("Get a user");
    ///```
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
    ///```
    protected Router newRouter() {
        return Router.router(server.vertx());
    }

    /// \[Experimental!] Executor for virtual threads to do async/await
    public static final ExecutorService vtExecutor = Executors.newVirtualThreadPerTaskExecutor();

    /// Allows you to create a routing function using async/await thanks to virtual threads.
    protected <T> Function<RoutingContext, Future<T>> vt(Function<RoutingContext, T> func) {
        return ctx -> {
            Promise<T> promise = Promise.promise();
            // A bit hacky... We can't easily transfer variables of the context to a custom thread.
            var prng = ctx.vertx().getOrCreateContext().get("__vertx.VertxContextPRNG");
            vtExecutor.execute(() -> {
                if (prng != null) {
                    ctx.vertx().getOrCreateContext().put("__vertx.VertxContextPRNG", prng);
                }
                try {
                    promise.complete(func.apply(ctx));
                } catch (Throwable e) {
                    promise.fail(e);
                }
            });
            return promise.future();
        };
    }

    /// Allows you to create a routing function using async/await thanks to virtual threads.
    ///
    /// This always returns 204 No Content on success.
    protected <T> Function<RoutingContext, Future<T>> vt(Consumer<RoutingContext> func) {
        return vt(ctx -> {
            func.accept(ctx);
            return null;
        });
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
    ///@paramStringuser
    ///@paramIntegermoney
    ///
    /// <h1>Welcome, ${user}!</h1>
    /// <p>You have ${money} euros in your account.</p>
    ///@if(money<0)
    /// <p>You have a negative balance.</p>
    /// <p><b>TIP:</b> Consider getting a job.</p>
    ///@endif
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
    ///);
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
