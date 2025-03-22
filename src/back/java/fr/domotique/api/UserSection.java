package fr.domotique.api;

import fr.domotique.*;
import fr.domotique.base.*;
import fr.domotique.base.Validation;
import fr.domotique.base.apidocs.*;
import fr.domotique.base.data.*;
import fr.domotique.data.*;
import io.vertx.core.Future;
import io.vertx.ext.auth.prng.*;
import io.vertx.ext.web.*;
import io.vertx.sqlclient.*;
import org.slf4j.*;

import java.util.*;

/// All API endpoints to access user data, and do authentication.
public class UserSection extends Section {
    /// Creates a new UserSection with the given server.
    public UserSection(Server server) {
        super(server);
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
        userRoutes.get("/").respond(this::searchUsers).putMetadata(RouteDoc.KEY, SEARCH_USERS_DOC);
        userRoutes.post("/").respond(vt(this::registerUser)).putMetadata(RouteDoc.KEY, REGISTER_DOC);
        userRoutes.post("/logout").respond(this::logout).putMetadata(RouteDoc.KEY, LOGOUT_DOC);
        userRoutes.post("/login").respond(vt(this::login)).putMetadata(RouteDoc.KEY, LOGIN_DOC);
        userRoutes.get("/confirmEmail").handler(this::confirmEmail).putMetadata(RouteDoc.KEY, CONFIRM_EMAIL_DOC);
        userRoutes.post("/me/profile").respond(vt(this::updateProfile)).putMetadata(RouteDoc.KEY, UPDATE_PROFILE_DOC);
        userRoutes.post("/me/password").respond(vt(this::changePassword)).putMetadata(RouteDoc.KEY, CHANGE_PASSWORD_DOC);

        // Paths with parameters come last.
        userRoutes.get("/:userId").respond(this::getUser).putMetadata(RouteDoc.KEY, GET_USER_DOC);


        // Finally, register that router to the main router, prefixed by /api/users
        doc(router.route(PATH_PREFIX).subRouter(userRoutes))
            .tag("Users");

    }

    // region GET /api/users/:userId
    static final RouteDoc GET_USER_DOC = new RouteDoc("findUser")
        .summary("Get user profile by ID")
        .description("Gets a user by their ID, and return their public data.")
        .pathParam("userId", int.class, "The ID of the user.")
        .response(200, UserProfile.class, "The user's data.")
        .response(204, "User not found.");

    Future<UserProfile> getUser(RoutingContext context) {
        // Read the user id from the path parameter (/api/users/:userId)
        int userId = readIntPathParam(context, "userId");

        // Return the user from the database, and convert it to a UserProfile to not leak passwords!
        return server.db().users().get(userId).map(UserProfile::fromUser);
    }
    // endregion

    // region POST /api/users | Register
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
                 - Some values are invalid in the form. (code: VALIDATION_ERROR)
                 - This e-mail is already in use. (code: EMAIL_IN_USE)
                 - You're already logged in. (code: ALREADY_LOGGED_IN)""",
            ErrorResponse.validationError(Map.of(
                "email", new String[]{"L'e-mail est invalide."},
                "firstName", new String[]{"Le prénom est trop long."}
            )));

    record RegisterInput(String email,
                         String firstName,
                         String lastName,
                         Gender gender,
                         Role role,
                         String password) {
        public RegisterInput {
            // Sanitize all sensitive strings (not password)
            email = Sanitize.string(email);
            firstName = Sanitize.string(firstName);
            lastName = Sanitize.string(lastName);
        }
    }

    // Uses virtual threads; we can use .await()!
    CompleteUser registerUser(RoutingContext context) {
        // Grab the Authenticator
        Authenticator auth = Authenticator.get(context);

        // Check if the user is already logged in; if so, don't register a user who's already registered!
        if (auth.isLoggedIn()) {
            throw new RequestException("Vous êtes déjà connecté !", 422, "ALREADY_LOGGED_IN");
        }

        // Read the JSON from the request
        RegisterInput input = readBody(context, RegisterInput.class);

        // Do a series of validation (including password strength)
        try (var block = Validation.start()) {
            // Validate the email
            Validation.email(block, "email", input.email);

            // Validate first and last names
            UserValidation.firstName(block, input.firstName);
            UserValidation.lastName(block, input.lastName);

            // Validate the password (for registration; login requirements are different)
            UserValidation.password(block, "password", input.password);
        }

        // Hash the password, and create the User object
        String passwordHashed = auth.hashPassword(input.password);

        // Create a new email confirmation token.
        // Remove the Most Significant Bit, since we only have 63 bits because no unsigned :(
        long confirmationToken = VertxContextPRNG.current(context.vertx()).nextLong() & ~(1L << 63);

        // Now, create the user!
        var user = new User(0,
            input.email,
            confirmationToken,
            false,
            passwordHashed,
            input.firstName,
            input.lastName,
            input.gender,
            input.role,
            input.role == Role.RESIDENT ? Level.BEGINNER : Level.EXPERT,
            0);

        try {
            // Add the user to the database.
            server.db().users().create(user).await();

            // Report that registration to the log (-> the console)
            log.info("User registered with id {} and email {}", user.getId(), user.getEmail());

            // Log the user in, and send a HTTP 201 Created status code.
            auth.login(user.getId());

            // Send the confirmation e-mail, and wait for it to be sent.
            sendConfirmationEmail(user, context).await();

            // Success, now register the status code and send the CompleteUser
            context.response().setStatusCode(201);
            return CompleteUser.fromUser(user);
        } catch (DuplicateException ex) {
            throw new RequestException("Un utilisateur est déjà inscrit avec cet e-mail.", 422, "EMAIL_IN_USE");
        }
    }
    // endregion

    // region POST /api/users/login | Log in
    static final RouteDoc LOGIN_DOC = new RouteDoc("login")
        .summary("Log in")
        .description("Log in a user with an email and a password.")
        .requestBody(LoginInput.class, new LoginInput("mail_de_ouf@gmail.com", "mot2passe"))
        .response(200, CompleteUser.class,
            "The user was logged in. Is also a success when the user's already logged, even when the credentials are invalid.")
        .response(401, ErrorResponse.class,
            "Credentials are incorrect: the password doesn't match the one found on the server. Error code: INVALID_CREDENTIALS.",
            new ErrorResponse<>("E-mail ou mot de passe incorrect.", "INVALID_CREDENTIALS"))
        .response(422, ErrorResponse.class,
            "Some fields are invalid (empty/too short).",
            ErrorResponse.validationError(Map.of(
                "email", new String[]{"L'e-mail est invalide."},
                "password", new String[]{"Le mot de passe est vide."}
            )));

    // The input JSON taken by this endpoint.
    record LoginInput(String email, String password) {
        public LoginInput {
            email = Sanitize.string(email);
        }
    }

    // Uses virtual threads; we can use .await()!
    CompleteUser login(RoutingContext context) {
        // Get the Authenticator, and check if the user is already logged in.
        Authenticator auth = Authenticator.get(context);
        if (auth.isLoggedIn()) {
            // Already logged in! Just send the user back.
            User user = auth.getUserOrFail().await();
            return CompleteUser.fromUser(user);
        }

        // Read the JSON from the request
        LoginInput input = readBody(context, LoginInput.class);

        // Validate stuff
        try (var block = Validation.start()) {
            Validation.email(block, "email", input.email, "L'e-mail est invalide.");
            Validation.nonBlank(block, "password", input.password, "Le mot de passe est vide.");
        }

        // Get a user by mail from the database.
        var user = server.db().users().getByEmail(input.email).await();

        // Make sure the given password matches the user's password, and that the user exists!
        if (user == null || !auth.checkPassword(input.password, user.getPassHash())) {
            throw new RequestException("E-mail ou mot de passe incorrect.", 401, "INVALID_CREDENTIALS");
        }

        // Log in the user to the current session.
        auth.login(user.getId());

        // Return the user, using CompleteUser to avoid leaking the password.
        return CompleteUser.fromUser(user);
    }
    // endregion

    // region POST /api/users/logout | Log out
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
    // endregion

    // region GET /api/users/me | Get my user profile
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
    // endregion

    // region GET /api/users | Search users
    static final RouteDoc SEARCH_USERS_DOC = new RouteDoc("searchUsers")
        .summary("Search users")
        .description("Search for users by their first name, last name, or email.")
        .queryParam("fullName", String.class, "The full name to search for.")
        .response(200, ProfileSearchOutput.class, "The list of users matching the query.")
        .response(400, ErrorResponse.class, "The full name query parameter is missing.");

    record ProfileSearchOutput(List<UserProfile> profiles) {}

    Future<ProfileSearchOutput> searchUsers(RoutingContext context) {
        // todo: add auth
        var fullName = context.queryParams().get("fullName");
        if (fullName == null || fullName.isBlank()) {
            throw new RequestException("La recherche est vide.", 400);
        }

        return server.sql().preparedQuery("""
                SELECT id, first_name, last_name, role, level, gender FROM user
                WHERE INSTR(first_name, ?) > 0 OR INSTR(last_name, ?) > 0
                """)
            .mapping(UserProfile::fromRow) // Convert the SQL rows into UserProfiles
            .execute(Tuple.of(fullName, fullName)) // Execute the query with both '?' replaced by the full name
            .map(Table::toList) // Convert the RowSet into a List of rows
            .map(ProfileSearchOutput::new); // Wrap the list into a ProfileSearchOutput object
    }
    // endregion

    // --- Updates ---

    // region POST /api/users/me/profile | Update profile
    static final RouteDoc UPDATE_PROFILE_DOC = new RouteDoc("updateProfile")
        .summary("Update profile")
        .description("Update the profile of the currently authenticated user.")
        .requestBody(UpdateProfileInput.class, new UpdateProfileInput("Gérard", "Poulet", Gender.MALE))
        .response(200, UserProfile.class, "The new updated profile.");

    record UpdateProfileInput(
        String firstName,
        String lastName,
        Gender gender
    ) {
        public UpdateProfileInput {
            // Sanitize strings before doing anything else.
            firstName = Sanitize.string(firstName);
            lastName = Sanitize.string(lastName);
        }
    }

    UserProfile updateProfile(RoutingContext context) {
        Authenticator auth = Authenticator.get(context);

        // Make sure we're logged in!
        auth.requireAuth();

        // Read the JSON from the request
        UpdateProfileInput input = readBody(context, UpdateProfileInput.class);

        // Validate incoming values
        try (var block = Validation.start()) {
            UserValidation.firstName(block, input.firstName);
            UserValidation.lastName(block, input.lastName);
        }

        // Get the user from the database.
        var user = auth.getUserOrFail().await();

        // Set all the values
        user.setFirstName(input.firstName);
        user.setLastName(input.lastName);
        user.setGender(input.gender);

        // Update the user in the database.
        server.db().users().update(user).await();

        // Done! Return the public data of the user.
        return UserProfile.fromUser(user);
    }
    // endregion

    // region POST /api/users/me/password | Change password
    static final RouteDoc CHANGE_PASSWORD_DOC = new RouteDoc("changePassword")
        .summary("Change password")
        .description("Change the password of the currently authenticated user.")
        .requestBody(ChangePasswordInput.class, new ChangePasswordInput("oldPassword123", "newPassword456"))
        .response(204, "Password successfully changed.")
        .response(401, ErrorResponse.class, "You are not logged in.")
        .response(403, ErrorResponse.class, "The old password is incorrect.")
        .response(422, ErrorResponse.class, "The new password is invalid.");

    record ChangePasswordInput(
        String oldPassword,
        String newPassword
    ) {}

    void changePassword(RoutingContext context) {
        Authenticator auth = Authenticator.get(context);

        // Make sure the user is logged in
        auth.requireAuth();

        // Read the JSON from the request
        ChangePasswordInput input = readBody(context, ChangePasswordInput.class);

        // Validate new password
        try (var block = Validation.start()) {
            UserValidation.password(block, "newPassword", input.newPassword);
        }

        // Get the logged-in user
        User user = auth.getUserOrFail().await();

        // Verify old password
        if (!auth.checkPassword(input.oldPassword, user.getPassHash())) {
            throw new RequestException("L'ancien mot de passe est incorrect.", 403);
        }

        // Hash the new password, and update it.
        String newPasswordHashed = auth.hashPassword(input.newPassword);
        user.setPassHash(newPasswordHashed);

        // Update user in database (synchronously using await)
        server.db().users().update(user).await();

        // Finally, log it to the console!
        log.info("User {} successfully changed their password", user.getId());
    }
    // endregion

    // --- Email confirmation ---

    // region GET /api/users/confirmEmail?token=xxx&user=xxx | Confirm email
    static final RouteDoc CONFIRM_EMAIL_DOC = new RouteDoc("confirmEmail")
        .tag("Users")
        .summary("Confirm email")
        .queryParam("token", long.class, "The email confirmation token sent by... mail")
        .queryParam("user", int.class, "ID of the user whose account is getting confirmed")
        .response(302, """
            Redirects to the home page of the website, with the URL being either:
            - `/?confirmResult=ok` -- when the email has been successfully confirmed
            - `/?confirmResult=err` -- when the email has NOT been confirmed
            """);

    void confirmEmail(RoutingContext context) {
        Authenticator auth = Authenticator.get(context);

        // Grab both query parameters: ?token=xxx&user=xxx
        String tokenParam = context.queryParams().get("token");
        String userParam = context.queryParams().get("user");

        // Read them
        Long tokenLong = readUnsignedLongOrNull(tokenParam);
        Integer userId = readIntOrNull(userParam);

        // If any is invalid, redirect to error
        if (tokenLong == null || userId == null) {
            context.redirect("/?confirmResult=err");
            return;
        }

        // Query the database for that user
        server.db().users().get(userId)
            .onSuccess(u -> {
                // If the token is right, and the user is not already confirmed, confirm the email
                if (u != null && u.getEmailConfirmationToken() == tokenLong && !u.isEmailConfirmed()) {
                    // Confirm the email
                    u.setEmailConfirmed(true);

                    // Update the user in the database
                    server.db().users().update(u)
                        .onSuccess(v -> {
                            log.info("User {} successfully confirmed their email", u.getId());

                            // Now also authenticate the user if not done already
                            if (!auth.isLoggedIn()) {
                                auth.login(userId);
                            }

                            // Redirect to the home page with a success message
                            context.redirect("/?confirmResult=ok");
                        })
                        .onFailure(ex -> context.redirect("/?confirmResult=err"));
                } else {
                    // Nope, redirect to the home page with an error
                    context.redirect("/?confirmResult=err");
                }
            })
            .onFailure(context::fail);
    }
    // endregion

    /// Sends the confirmation email to User `u`.
    private Future<User> sendConfirmationEmail(User u, RoutingContext ctx) {
        // Send the confirmation email
        // Get base URL (e.g., "http://localhost:7777")
        String baseUrl = ctx.request().scheme() + "://"
            + ctx.request().authority().host()
            + ":" + ctx.request().authority().port();

        // Construct the confirmation URL
        String confirmUrl = baseUrl + "/api/users/confirmEmail?token=" + u.getEmailConfirmationToken() + "&user=" + u.getId();

        // Prepare template data for the email
        Map<String, Object> htmlArguments = Map.of(
            "firstName", u.getFirstName(),
            "lastName", u.getLastName(),
            "url", confirmUrl
        );
        final String emailSubject = "Confirmation de votre e-mail";

        // Render the template using the email.jte file then send it
        return server.templateEngine().render(htmlArguments, "email.jte")
            .compose(emailBody ->
                server.email().send(u.getEmail(), emailSubject, emailBody.toString()))
            .andThen(whenOk(_ -> {
                log.info("Confirmation e-mail sent to user {}", u.getId());
            }))
            .map(u);
    }
}
