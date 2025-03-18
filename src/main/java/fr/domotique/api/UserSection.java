package fr.domotique.api;

import fr.domotique.*;
import fr.domotique.data.*;
import io.vertx.core.Future;
import io.vertx.ext.web.*;
import org.slf4j.*;

/// All API endpoints to access user data, and do authentication.
public class UserSection extends Section {
    // The store to access the User table in the database
    final UserTable userTable;

    /// Creates a new UserSection with the given server.
    public UserSection(Server server) {
        super(server);

        // Create the UserTable to access... the user table of the database easily.
        this.userTable = new UserTable(server.db());
    }

    // All routes of this section will begin by /api/users
    static final String PATH_PREFIX = "/api/users*";

    // The logger used to print messages to the console with nice colors.
    static final Logger log = LoggerFactory.getLogger(UserSection.class);

    @Override
    public void register(Router router) {
        // First, create a "sub-router" to put all of our routes in.
        var userRoutes = Router.router(server.vertx());

        // Then, put all of our functions to the corresponding URLs!
        // REST guide (simplified):
        // - get : read data
        // - post: create/modify data
        //
        // The "this::func" syntax is equivalent to x -> this.func(x)
        userRoutes.get("/me").respond(this::me);
        userRoutes.post("/").respond(this::register);
        userRoutes.post("/logout").respond(this::logout);
        userRoutes.post("/login").respond(this::login);
        userRoutes.get("/:userId").respond(this::getUser);

        // Finally, register that router to the main router, prefixed by /api/users
        router.route(PATH_PREFIX).subRouter(userRoutes);
    }

    /// All of a user's data, sent to the client, without the password!
    record PublicUser(int id, String email, Gender gender) {
        /// Converts a database User to a PublicUser for client API consumption.
        /// Returns `null` when the user is null.
        static PublicUser fromUser(User u) {
            if (u == null) { return null; }
            return new PublicUser(u.getId(), u.getEmail(), u.getGender());
        }
    }

    // GET /api/users/:userId
    Future<PublicUser> getUser(RoutingContext context) {
        // Read the user id from the path parameter (/api/users/:userId)
        int userId = readIntPathParam(context, "userId");

        // Return the user from the database, and convert it to a PublicUser to not leak passwords!
        return userTable.get(userId).map(PublicUser::fromUser);
    }

    // POST /api/users
    Future<PublicUser> register(RoutingContext context) {
        // The input JSON taken by this endpoint.
        // {
        //     "email": "hello@abc.fr"
        //     "gender": "FEMALE"
        //     "password": "password123"
        // }
        record RegisterInput(String email, Gender gender, String password) {}

        // Grab the Authenticator
        Authenticator auth = Authenticator.get(context);

        // Check if the user is already logged in; if so, don't register a user who's already registered!
        if (auth.isLoggedIn()) {
            throw new RequestException("Vous êtes déjà connecté !", 400, "ALREADY_LOGGED_IN");
        }

        // Read the JSON from the request
        RegisterInput input = readBody(context, RegisterInput.class);

        // Do a series of validation (including password strength)
        Validation.email(input.email, "L'e-mail est invalide.");
        Validation.nonBlank(input.password, "Le mot de passe est vide.");
        if (input.password.length() < 8) {
            throw new RequestException("Le mot de passe doit faire au moins 8 caractères.", 422);
        }

        // Hash the password, and create the User object
        String passwordHashed = auth.hashPassword(input.password);
        var user = new User(0, input.email, input.gender, passwordHashed);

        // Add the user to the database. Once that's done successfully, log in the user.
        return userTable.create(user)
                .andThen(whenOk(u -> {
                    // Report that registration to the log (-> the console)
                    log.info("User registered with id {} and email {}", u.getId(), u.getEmail());

                    // Log the user in, and send a HTTP 201 Created status code.
                    auth.login(u.getId());
                    context.response().setStatusCode(201);
                }))
                .map(PublicUser::fromUser); // Make sure to strip the password before sending user data!
    }

    // POST /api/users/login
    Future<PublicUser> login(RoutingContext context) {
        // The input JSON taken by this endpoint.
        // {
        //     "email": "hello@def.fr"
        //     "password": "password123"
        // }
        record LoginInput(String email, String password) {}

        // Get the Authenticator, and check if the user is already logged in.
        Authenticator auth = Authenticator.get(context);
        if (auth.isLoggedIn()) {
            // Already logged in! Just send the user back.
            return auth.getUserOrFail().map(PublicUser::fromUser);
        }

        // Read the JSON from the request
        LoginInput input = readBody(context, LoginInput.class);

        // Validate stuff
        Validation.email(input.email, "L'e-mail est invalide.");
        Validation.nonBlank(input.password, "Le mot de passe est vide.");

        // Get a user by mail from the database.
        return userTable.getByEmail(input.email)
                .map(user -> {
                    // Make sure the given password matches the user's password, and that the user exists!
                    if (user == null || !auth.checkPassword(input.password, user.getPassHash())) {
                        throw new RequestException("E-mail ou mot de passe incorrect.", 401);
                    }

                    // Log in the user to the current session.
                    auth.login(user.getId());

                    // Return the user, using PublicUser to avoid leaking the password.
                    return PublicUser.fromUser(user);
                });
    }

    // POST /api/users/logout
    Future<Void> logout(RoutingContext context) {
        // Get the Authenticator.
        Authenticator auth = Authenticator.get(context);

        // Log out the user. Does nothing if the user's not logged in anyway!
        auth.logout();

        // Return nothing, so it's always a 204 No Content status code (which is a success code).
        return Future.succeededFuture(null);
    }

    // GET /api/users/me
    Future<PublicUser> me(RoutingContext context) {
        // Get the Authenticator.
        Authenticator auth = Authenticator.get(context);

        // Get the user from the Authenticator, and remove its password from the response.
        return auth.getUserOrFail()
                .map(PublicUser::fromUser);
    }
}
