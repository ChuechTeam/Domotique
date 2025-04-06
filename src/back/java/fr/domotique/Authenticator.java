package fr.domotique;

import fr.domotique.base.*;
import fr.domotique.data.*;
import io.vertx.core.*;
import io.vertx.core.Future;
import io.vertx.ext.auth.hashing.*;
import io.vertx.ext.auth.prng.*;
import io.vertx.ext.web.*;
import org.jetbrains.annotations.*;

import javax.validation.constraints.*;
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

    /// Information about the currently logged-in user.
    private @Nullable SessionData session;

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

    /// Exception thrown when the user has not enough rights.
    public static final RequestException INSUFFICIENT_EXCEPTION
        = new RequestException("Vous n'avez pas les droits nécessaires pour effectuer cette action.", 403, "INSUFFICIENT_PERMISSIONS");

    /// Exception thrown when the user's e-mail is not confirmed.
    public static final RequestException EMAIL_NOT_CONFIRMED_EXCEPTION
        = new RequestException("Vous devez confirmer votre adresse e-mail pour effectuer cette action.", 403, "EMAIL_NOT_CONFIRMED");

    /// Creates a new authenticator for the current request, and the given logged-in user.
    private Authenticator(RoutingContext ctx, Server server, @Nullable User loggedUser) {
        // Initialize boring dependencies
        this.ctx = ctx;
        this.server = server;

        // If the user is logged in; use it!
        if (loggedUser != null) {
            this.user = loggedUser;
            this.session = new SessionData(loggedUser.getId(),
                loggedUser.getLevel(),
                loggedUser.getRole(),
                loggedUser.isEmailConfirmed());
        }
    }

    /// Contains necessary information about a user authorization, stored in a session.
    public record SessionData(int userId, Level level, Role role, boolean emailConfirmed) {}

    /// Creates a new authenticator for the current request, that will request the user from the database.
    public static Future<Authenticator> create(RoutingContext ctx, Server server) {
        // TODO: Make this less dumb. Right now, we're querying the user FOR EVERY REQUEST.
        //       There's multiple solutions we can implement:
        //       1. Store level & emailConfirmed in the session, and refresh sessions
        //          on level/email change. But we need to have a userId->sessionId map.
        //       2. Have a separate level/emailConfirmed cache, and refresh it on level/email change.
        //       3. Cache the users completely. But now this will require extensive changes to how we query users
        //       ------------
        //       I'd say that solution 1 is preferred, since it has the best data locality.
        //       But we'll implement that later!

        // Get the user id from the session
        Integer sesUid = ctx.session().get(SESSION_UID_KEY);
        if (sesUid == null) {
            // No user -> create it directly
            return Future.succeededFuture(new Authenticator(ctx, server, null));
        }

        return server.db().users().get(sesUid)
            .map(u -> new Authenticator(ctx, server, u));
    }

    /// Gets the authenticator from the current request. Guaranteed to succeed.
    ///
    /// ## Example
    /// ```java
    /// // Get the authenticator from the current request
    /// Authenticator auth = Authenticator.get(context);
    ///```
    public static Authenticator get(RoutingContext ctx) {
        return ctx.get(KEY);
    }

    /// Logs the user in with the given user. Replaces the logged-in user if there is one.
    ///
    /// @param user the user to log in
    /// @throws IllegalArgumentException when the user id is 0
    public void login(User user) {
        // Prevent logging in with an invalid user id!
        if (user.getId() == 0) {
            throw new IllegalArgumentException("User id can't be 0");
        }

        // Add the user id to the session data.
        ctx.session().put(SESSION_UID_KEY, user.getId());

        // Set the user id in the authenticator.
        this.user = user;
        this.session = new SessionData(user.getId(), user.getLevel(), user.getRole(), user.isEmailConfirmed());
    }

    /// Logs the user out of the app. Does nothing when the user isn't logged in.
    public void logout() {
        // Destroy the session, which removes the user id
        ctx.session().destroy();

        // Remove any leftovers.
        user = null;
        session = null;
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
    /// @return `true` if the password is correct, `false` otherwise.
    public boolean checkPassword(String password, String hashed) {
        return hashAlgo.verify(hashed, password);
    }

    /// Ensures that the user is logged in at this point in the request.
    ///
    /// Allows users with unconfirmed e-mails to access the route.
    ///
    /// If the user is not logged in, throws a 401 "Unauthorized" status code.
    ///
    /// ## Usage
    /// ```java
    /// Authenticator auth = Authenticator.get(ctx);
    /// auth.requireAuth();
    ///```
    public void requireAuth() {
        if (session == null) {
            throw UNAUTHORIZED_EXCEPTION;
        }
    }

    /// Ensures that the user is logged in, with its level greater or equal to `level`.
    ///
    /// Does not allow users with unconfirmed e-mails to access the route.
    ///
    /// If the user is not logged in, throws a 401 "Unauthorized" status code.
    ///
    /// If the user's level is not enough, throws a 403 "Forbidden" status code.
    ///
    /// If the user's e-mail is not confirmed, throws a 403 "Forbidden" status code.
    ///
    /// ## Usage
    ///
    /// ```java
    /// Authenticator auth = Authenticator.get(ctx);
    /// auth.requireAuth(Level.INTERMEDIATE);
    /// ```
    public void requireAuth(Level level) {
        if (session == null) {
            throw UNAUTHORIZED_EXCEPTION;
        } else if (!hasLevel(level)) {
            throw INSUFFICIENT_EXCEPTION;
        } else if (!session.emailConfirmed) {
            throw EMAIL_NOT_CONFIRMED_EXCEPTION;
        }
    }

    /// Returns true when the user is logged and has a level of at least `level`.
    public boolean hasLevel(Level level) {
        return session != null && session.level.compareTo(level) >= 0;
    }

    /// Returns true when the user is logged and has a role of `role`.
    public boolean isOfRole(Role role) {
        return session != null && session.role == role;
    }

    /// Returns true if a user is currently logged in.
    public boolean isLoggedIn() {
        return session != null;
    }

    /// Returns the currently logged-in user id. Returns 0 if no user is logged.
    public int getUserId() {
        return session == null ? 0 : session.userId;
    }

    /// Returns the session of the currently logged user. If logged out, returns `null`.
    public @Nullable SessionData getSession() {
        return session;
    }

    /// Returns the currently logged-in user from the database.
    ///
    /// @return the logged-in user, or `null` if no user is logged in
    public Future<@Nullable User> getUser() {
        // When the user is not logged in, return null directly.
        if (session == null) {
            return Future.succeededFuture(null);
        }

        // When the user is already cached, return it directly.
        if (user != null) {
            return Future.succeededFuture(user);
        }

        // Query the database for the user, then store it in the Authenticator.
        return server.db().users().get(session.userId).andThen(x -> {
            user = x.result();
            if (user == null) {
                // Then the user has been deleted. Remove the session!
                logout();
            }
        });
    }

    /// Returns the currently logged-in user from the database.
    ///
    /// If no user is logged in, ends the request with a 401 "Unauthorized" status code.
    ///
    /// @return the logged-in user, never `null`
    public Future<User> getUserOrFail() {
        // When the user is not logged in, return a failure with the UNAUTHORIZED exception.
        if (session == null) {
            return Future.failedFuture(UNAUTHORIZED_EXCEPTION);
        }

        // Else, just return the user using getUser(); we know it can't be null.
        return getUser();
    }
}
