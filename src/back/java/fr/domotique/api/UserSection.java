package fr.domotique.api;

import com.fasterxml.jackson.annotation.*;
import fr.domotique.*;
import fr.domotique.apidocs.*;
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
        var userRoutes = newRouter();

        // Then, put all of our functions to the corresponding URLs!
        // REST guide (simplified):
        // - get : read data
        // - post: create/modify data
        //
        // The "this::func" syntax is equivalent to x -> this.func(x)
        userRoutes.get("/me").respond(this::me).putMetadata(RouteDoc.KEY, ME_DOC);
        userRoutes.post("/").respond(this::register).putMetadata(RouteDoc.KEY, REGISTER_DOC);
        userRoutes.post("/logout").respond(this::logout).putMetadata(RouteDoc.KEY, LOGOUT_DOC);
        userRoutes.post("/login").respond(this::login).putMetadata(RouteDoc.KEY, LOGIN_DOC);
        userRoutes.get("/:userId").respond(this::getUser).putMetadata(RouteDoc.KEY, GET_USER_DOC);

        // Finally, register that router to the main router, prefixed by /api/users
        doc(router.route(PATH_PREFIX).subRouter(userRoutes))
            .tag("Users");
    }

    // GET /api/users/:userId
    static final RouteDoc GET_USER_DOC = new RouteDoc("findUser")
        .summary("Get user by ID")
        .description("Gets a user by their ID, and return their public data.")
        .param("userId", int.class, "The ID of the user.")
        .response(200, UserProfile.class, "The user's data.")
        .response(204, "User not found.");

    Future<UserProfile> getUser(RoutingContext context) {
        // Read the user id from the path parameter (/api/users/:userId)
        int userId = readIntPathParam(context, "userId");

        // Return the user from the database, and convert it to a UserProfile to not leak passwords!
        return userTable.get(userId).map(UserProfile::fromUser);
    }

    // POST /api/users
    static final RouteDoc REGISTER_DOC = new RouteDoc("register")
        .summary("Register")
        .description("Register a new user with an email and a password.")
        .requestBody(RegisterInput.class, new RegisterInput("mail_de_ouf@gmail.com", Gender.FEMALE, "mot2passe"))
        .response(201, CompleteUser.class, "The user was created.")
        .response(400, ErrorResponse.class, "You are already logged in.")
        .response(422, ErrorResponse.class, "Invalid email or password.",
            new ErrorResponse<>("Mot de passe invalide !", null, null));

    record RegisterInput(String email, Gender gender, String password) {}

    Future<CompleteUser> register(RoutingContext context) {
        // Grab the Authenticator
        Authenticator auth = Authenticator.get(context);

        // Check if the user is already logged in; if so, don't register a user who's already registered!
        if (auth.isLoggedIn()) {
            throw new RequestException("Vous êtes déjà connecté !", 400, "ALREADY_LOGGED_IN");
        }

        // Read the JSON from the request:
        // {
        //     "email": "hello@abc.fr"
        //     "gender": "FEMALE"
        //     "password": "password123"
        // }
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
            .map(CompleteUser::fromUser); // Make sure to strip the password before sending user data!
    }

    // POST /api/users/login
    static final RouteDoc LOGIN_DOC = new RouteDoc("login")
        .summary("Log in")
        .description("Log in a user with an email and a password.")
        .requestBody(LoginInput.class, new LoginInput("mail_de_ouf@gmail.com", "mot2passe"))
        .response(200, CompleteUser.class,
            "The user was logged in. Is also a success when the user's already logged, even when the credentials are invalid.")
        .response(401, ErrorResponse.class,
            "Credentials are incorrect: the password doesn't match the one found on the server.",
            new ErrorResponse<>("E-mail ou mot de passe incorrect."))
        .response(422, ErrorResponse.class,
            "Some fields are invalid (empty/too short).",
            new ErrorResponse<>("L'e-mail est invalide."));

    // The input JSON taken by this endpoint.
    record LoginInput(String email, String password) {}

    Future<CompleteUser> login(RoutingContext context) {
        // Get the Authenticator, and check if the user is already logged in.
        Authenticator auth = Authenticator.get(context);
        if (auth.isLoggedIn()) {
            // Already logged in! Just send the user back.
            return auth.getUserOrFail().map(CompleteUser::fromUser);
        }

        // Read the JSON from the request
        // {
        //     "email": "hello@def.fr"
        //     "password": "password123"
        // }
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

                // Return the user, using CompleteUser to avoid leaking the password.
                return CompleteUser.fromUser(user);
            });
    }

    // POST /api/users/logout
    static final RouteDoc LOGOUT_DOC = new RouteDoc("logout")
        .summary("Log out")
        .description("Log out the current user from the app.")
        .response(204, "You are now logged out, even if you weren't logged in before.");

    Future<Void> logout(RoutingContext context) {
        // Get the Authenticator.
        Authenticator auth = Authenticator.get(context);

        // Log out the user. Does nothing if the user's not logged in anyway!
        auth.logout();

        // Return nothing, so it's always a 204 No Content status code (which is a success code).
        return Future.succeededFuture(null);
    }

    // GET /api/users/me
    static final RouteDoc ME_DOC = new RouteDoc("me")
        .summary("Get my user profile")
        .description("Gets the currently authenticated user's complete data.")
        .response(200, CompleteUser.class, "The current user's data.")
        .response(401, ErrorResponse.class, "You are not logged in.");

    Future<CompleteUser> me(RoutingContext context) {
        // Get the Authenticator.
        Authenticator auth = Authenticator.get(context);

        // Get the user from the Authenticator, and remove its password from the response.
        return auth.getUserOrFail()
            .map(CompleteUser::fromUser);
    }
}
