package fr.domotique;

import fr.domotique.data.*;
import io.vertx.core.*;
import io.vertx.ext.auth.hashing.*;
import io.vertx.ext.auth.prng.*;
import io.vertx.ext.web.*;
import org.jetbrains.annotations.*;

import java.util.*;

/// Takes care of user authentication during requests: logging in, logging out, password hashing, etc.
///
/// ## Retrieving it
/// Simply use the [Authenticator#get(RoutingContext)] function. The [RouterVerticle] creates it
/// automatically for each request.
///
/// ## Example
///
/// ```java
/// // Example API handler method that requires authentication
/// private Future<Device> getDeviceForUser(RoutingContext context) {
///     // Get the authenticator from the current request
///     Authenticator auth = Authenticator.get(context);
///
///     // Check if user is logged in
///     if (!auth.isLoggedIn()) {
///         throw new RequestException("You must be logged in", 401);
///     }
///
///     // Get the user id of the current user
///     int userId = auth.getUserId();
///
///     // Do something with it, like this!
///     return deviceStore.getForUser(userId);
///}
///```
///
/// @author Dynamic
public class Authenticator {
    // Various utilities we use
    private final RoutingContext ctx; // To get info about the request
    private final Server server;
    private final UserTable userTable; // To access the User table in the database

    // Id of the logged-in user; 0 means no user logged in
    private int userId = 0;
    /// Cached logged-in user object; can be null!
    private @Nullable User user = null;

    /// The key used for [#get(RoutingContext)]
    public static final String KEY = "Auth";

    /// The key used for the user id in the session.
    public static final String SESSION_UID_KEY = "uid";

    /// Password hashing algorithm
    private static final HashingStrategy hashAlgo = HashingStrategy.load();

    /// Exception thrown when the user is not logged in.
    public static final RequestException UNAUTHORIZED_EXCEPTION
            = new RequestException("Vous devez être connecté pour effectuer cette action.", 401, "UNAUTHORIZED");

    /// Creates a new authenticator for the current request.
    public Authenticator(RoutingContext ctx, Server server) {
        // Initialize boring dependencies
        this.ctx = ctx;
        this.server = server;
        this.userTable = new UserTable(server.db());

        // Get the user id from the session, or 0 if not logged in.
        Integer sesUid = ctx.session().get(SESSION_UID_KEY);
        if (sesUid == null) {
            this.userId = 0;
        } else {
            this.userId = sesUid;
        }
    }

    /// Gets the authenticator from the current request. Guaranteed to succeed.
    ///
    /// ## Example
    /// ```java
    /// // Get the authenticator from the current request
    /// Authenticator auth = Authenticator.get(context);
    /// ```
    public static Authenticator get(RoutingContext ctx) {
        return ctx.get(KEY);
    }

    /// Allows only authenticated users to access the route.
    ///
    /// When not logged in, the request will fail with a 401 "Unauthorized" status code.
    ///
    /// ## Usage
    /// ```java
    /// // All routes under /api/devices/* require authentication
    /// router.route("/api/devices/*").handler(Authenticator.requireAuthHandler());
    ///```
    public static Handler<RoutingContext> requireAuthHandler() {
        return ctx -> {
            Authenticator auth = Authenticator.get(ctx);
            if (!auth.isLoggedIn()) {
                ctx.fail(UNAUTHORIZED_EXCEPTION);
            } else {
                ctx.next();
            }
        };
    }

    /// Ensures that the user is logged in at this point in the request.
    ///
    /// If the user is not logged in, throws a 401 "Unauthorized" status code.
    ///
    /// ## Usage
    /// ```java
    /// Authenticator auth = Authenticator.get(ctx);
    /// auth.requireAuth();
    ///```
    public void requireAuth() {
        if (!isLoggedIn()) {
            throw UNAUTHORIZED_EXCEPTION;
        }
    }

    /// Hashes the given password with the standard PBKDF2 algorithm, using a random salt.
    ///
    /// @param password The password to hash.
    /// @return The hashed password, containing the salt.
    public String hashPassword(String password) {
        return hashAlgo.hash("pbkdf2",
                Collections.emptyMap(),
                VertxContextPRNG.current(server.vertx()).nextString(16),
                password);
    }

    /// Checks the password against the hashed password.
    ///
    /// @param password The password to check.
    /// @param hashed   The hashed password to check against.
    /// @return `true` if the password is correct,`false` otherwise.
    public boolean checkPassword(String password, String hashed) {
        return hashAlgo.verify(hashed, password);
    }

    /// Returns true if a user is currently logged in.
    public boolean isLoggedIn() {
        return userId != 0;
    }

    /// Returns the currently logged-in user id. Returns 0 if no user is logged.
    public int getUserId() {
        return userId;
    }

    /// Returns the currently logged-in user id.
    ///
    /// If no user is logged in, ends the request with a 401 "Unauthorized" status code.
    ///
    /// @return the user id; can't be 0
    /// @throws RequestException when the user is not logged in
    public int getUserIdOrFail() {
        requireAuth();
        return userId;
    }

    /// Returns the currently logged-in user from the database.
    ///
    /// @return the logged-in user, or `null` if no user is logged in
    public Future<@Nullable User> getUser() {
        // When the user is not logged in, return null directly.
        if (userId == 0) {
            return Future.succeededFuture(null);
        }

        // When the user is already cached, return it directly.
        if (user != null) {
            return Future.succeededFuture(user);
        }

        // Query the database for the user, then store it in the Authenticator.
        return userTable.get(userId).andThen(x -> user = x.result());
    }

    /// Returns the currently logged-in user from the database.
    ///
    /// If no user is logged in, ends the request with a 401 "Unauthorized" status code.
    ///
    /// @return the logged-in user, never `null`
    public Future<User> getUserOrFail() {
        // When the user is not logged in, return a failure with the UNAUTHORIZED exception.
        if (userId == 0) {
            return Future.failedFuture(UNAUTHORIZED_EXCEPTION);
        }

        // Else, just return the user using getUser(); we know it can't be null.
        return getUser();
    }

    /// Logs the user out of the app. Does nothing when the user isn't logged in.
    public void logout() {
        // Destroy the session, which removes the user id
        ctx.session().destroy();

        // Remove any leftovers.
        user = null;
        userId = 0;
    }

    /// Logs the user in with the given user id. Replaces the logged-in user if there is one.
    ///
    /// @param id the user id, can't be 0
    /// @throws IllegalArgumentException when the user id is 0
    public void login(int id) {
        // Prevent logging in with an invalid user id!
        if (id == 0) {
            throw new IllegalArgumentException("User id can't be 0");
        }

        // Add the user id to the session data.
        ctx.session().put(SESSION_UID_KEY, id);

        // Set the user id in the authenticator.
        userId = id;
    }
}
