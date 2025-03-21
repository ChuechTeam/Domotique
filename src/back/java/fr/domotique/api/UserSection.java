package fr.domotique.api;

import fr.domotique.*;
import fr.domotique.apidocs.*;
import fr.domotique.data.*;
import io.vertx.core.Future;
import io.vertx.ext.auth.prng.*;
import io.vertx.ext.web.*;
import org.slf4j.*;

import java.util.*;

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

        // Add register the email confirmation on the root
        router.get("/confirmEmail").handler(this::confirmEmail).putMetadata(RouteDoc.KEY, CONFIRM_EMAIL_DOC);
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
        .requestBody(RegisterInput.class, new RegisterInput("mail_de_ouf@gmail.com",
            "Juréma",
            "Deveri",
            Gender.FEMALE,
            Role.RESIDENT,
            "mot2passe"))
        .response(201, CompleteUser.class, "The user was created.")
        .response(422, ErrorResponse.class, """
            Either:
             - Some values are invalid in the form.
             - This e-mail is already in use.
             - You're already logged in.""");

    record RegisterInput(String email,
                         String firstName,
                         String lastName,
                         Gender gender,
                         Role role,
                         String password) {}

    Future<CompleteUser> register(RoutingContext context) {
        // Grab the Authenticator
        Authenticator auth = Authenticator.get(context);

        // Check if the user is already logged in; if so, don't register a user who's already registered!
        if (auth.isLoggedIn()) {
            throw new RequestException("Vous êtes déjà connecté !", 422, "ALREADY_LOGGED_IN");
        }

        // Read the JSON from the request:
        // {
        //     "email": "hello@abc.fr"
        //     "gender": "FEMALE"
        //     "password": "password123"
        // }
        RegisterInput input = readBody(context, RegisterInput.class);

        // Trim the strings to remove extra spaces
        String email = input.email.trim();
        String firstName = input.firstName.trim();
        String lastName = input.lastName.trim();

        // Do a series of validation (including password strength)
        Validation.email(email, "L'e-mail est invalide.");
        Validation.nonBlank(firstName, "Le prénom est vide.");
        Validation.nonBlank(lastName, "Le nom est vide.");
        Validation.nonBlank(input.password, "Le mot de passe est vide.");
        if (input.password.length() < 8) {
            throw new RequestException("Le mot de passe doit faire au moins 8 caractères.", 422);
        }

        // Hash the password, and create the User object
        String passwordHashed = auth.hashPassword(input.password);

        // Create a new email confirmation token.
        // Remove the Most Significant Bit, since we only have 63 bits because no unsigned :(
        long confirmationToken = VertxContextPRNG.current(context.vertx()).nextLong() & ~(1L << 63);

        var user = new User(0,
            email,
            confirmationToken,
            false,
            passwordHashed,
            firstName,
            lastName,
            input.gender,
            input.role,
            input.role == Role.RESIDENT ? Level.BEGINNER : Level.EXPERT,
            0);

        // Add the user to the database. Once that's done successfully, log in the user.
        return userTable.create(user)
            .compose(u -> {
                // Report that registration to the log (-> the console)
                log.info("User registered with id {} and email {}", u.getId(), u.getEmail());

                // Log the user in, and send a HTTP 201 Created status code.
                auth.login(u.getId());

                // Send the confirmation e-mail
                return sendConfirmationEmail(u, context);
            })
            .map(u -> {
                // Success, now register the status code and send the CompleteUser
                context.response().setStatusCode(201);
                return CompleteUser.fromUser(u);
            })
            .recover(ex -> {
                if (ex instanceof DuplicateException) {
                    return Future.failedFuture(new RequestException("Un utilisateur est déjà enregistré avec cet e-mail.", 422));
                } else {
                    return Future.failedFuture(ex);
                }
            });
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

    // --- Email confirmation ---

    // GET /confirmEmail?token=xxx&user=xxx
    static final RouteDoc CONFIRM_EMAIL_DOC = new RouteDoc("confirmEmail")
        .tag("Users")
        .summary("Confirm email")
        .param(ParamDoc.Location.QUERY, "token", long.class, "The email confirmation token sent by... mail")
        .param(ParamDoc.Location.QUERY, "user", int.class, "ID of the user whose account is getting confirmed")
        .response(302, """
            Redirects to the home page of the website, with the URL being either:
            - `/?confirmResult=ok` -- when the email has been successfully confirmed
            - `/?confirmResult=err` -- when the email has NOT been confirmed
            """);

    void confirmEmail(RoutingContext context) {
        Authenticator auth = Authenticator.get(context);

        String tokenParam = context.queryParams().get("token");
        String userParam = context.queryParams().get("user");

        Long tokenLong = readUnsignedLongOrNull(tokenParam);
        Integer userId = readIntOrNull(userParam);

        if (tokenLong == null || userId == null) {
            context.redirect("/?confirmResult=ok");
            return;
        }

        userTable.get(userId)
            .onSuccess(u -> {
                if (u != null && u.getEmailConfirmationToken() == tokenLong) {
                    u.setEmailConfirmed(true);

                    userTable.update(u)
                        .onSuccess(v -> context.redirect("/?confirmResult=ok"))
                        .onFailure(ex -> context.redirect("/?confirmResult=err"));
                } else {
                    context.redirect("/?confirmResult=err");
                }
            })
            .onFailure(context::fail);
    }

    /// Sends the confirmation email to User `u`.
    private Future<User> sendConfirmationEmail(User u, RoutingContext ctx) {
        // Send the confirmation email
        // Get base URL (e.g., "http://localhost:7777")
        String baseUrl = ctx.request().scheme() + "://"
                         + ctx.request().authority().host()
                         + ":" + ctx.request().authority().port();

        // Construct the confirmation URL
        String confirmUrl = baseUrl + "/confirmEmail?token=" + u.getEmailConfirmationToken() + "&user=" + u.getId();

        // Render the template then send it
        return server.templateEngine().render(Map.of(
                "firstName", u.getFirstName(),
                "lastName", u.getLastName(),
                "url", confirmUrl
            ), "email.jte")
            .compose(emailBody ->
                server.email().send(u.getEmail(), "Confirmation de votre e-mail", emailBody.toString()))
            .andThen(whenOk(_ -> {
                log.info("Confirmation e-mail sent to user {}", u.getId());
            }))
            .map(u);
    }
}
