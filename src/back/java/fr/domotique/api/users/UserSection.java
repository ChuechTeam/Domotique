package fr.domotique.api.users;

import fr.domotique.*;
import fr.domotique.base.*;
import fr.domotique.base.Validation;
import fr.domotique.base.apidocs.*;
import fr.domotique.base.data.*;
import fr.domotique.data.*;
import io.vertx.core.Future;
import io.vertx.ext.auth.prng.*;
import io.vertx.ext.web.*;
import org.jetbrains.annotations.*;
import org.slf4j.*;

import java.time.*;
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
        userRoutes.get("/level-info").respond(this::getLevelInfo).putMetadata(RouteDoc.KEY, LEVEL_INFO_DOC);
        userRoutes.get("/confirmEmail").handler(this::confirmEmail).putMetadata(RouteDoc.KEY, CONFIRM_EMAIL_DOC);


        // Paths with parameters come last.
        userRoutes.patch("/:userId/profile").respond(vt(this::updateProfile)).putMetadata(RouteDoc.KEY, UPDATE_PROFILE_DOC);
        userRoutes.put("/:userId/password").respond(vt(this::changePassword)).putMetadata(RouteDoc.KEY, CHANGE_PASSWORD_DOC);
        userRoutes.delete("/:userId").respond(vt(this::deleteUser)).putMetadata(RouteDoc.KEY, DELETE_USER_DOC);
        userRoutes.get("/:userId").respond(this::getUser).putMetadata(RouteDoc.KEY, GET_USER_DOC);
        userRoutes.get("/:userId/full").respond(this::getFullUser).putMetadata(RouteDoc.KEY, GET_FULL_USER_DOC);

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

    // region GET /api/users/:userId/full
    static final RouteDoc GET_FULL_USER_DOC = new RouteDoc("findFullUser")
        .summary("Get a full user by ID")
        .description("Gets a user by their ID, and return their public AND private data. Only available for admins.")
        .pathParam("userId", int.class, "The ID of the user.")
        .response(200, UserProfile.class, "The user's data.")
        .response(204, "User not found.")
        .response(401, ErrorResponse.class, "You are not logged in.")
        .response(403, ErrorResponse.class, "You don't have the rights to access this resource.");

    Future<CompleteUser> getFullUser(RoutingContext context) {
        // Make sure we're at least an expert
        Authenticator.get(context).requireAuth(Level.EXPERT);

        // Read the user id from the path parameter (/api/users/:userId)
        int userId = readIntPathParam(context, "userId");

        // Return the user from the database, and convert it to a UserProfile to not leak passwords!
        return server.db().users().get(userId).map(CompleteUser::fromUser);
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
            input.role == Role.ADMIN,
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
            auth.login(user);

            // Add this to the login logs
            // TODO: Do it in the background instead, maybe move it to auth.login?
            server.db().loginLogs().insert(new LoginLog(0, user.getId(), Instant.now())).await();

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
        auth.login(user);

        // Add this to the login logs
        // TODO: Do it in the background instead, maybe move it to auth.login?
        server.db().loginLogs().insert(new LoginLog(0, user.getId(), Instant.now())).await();

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
        .summary("Get users")
        .description("""
            Find users by either:
            - a list of ids, using `ids`
            - their full name, using `fullName`.""")
        .optionalQueryParam("fullName", String.class, "The full name to search for.")
        .optionalQueryParam("ids", RouteDoc.listType(Integer.class), "A list of identifiers of users to find.")
        .response(200, ProfileSearchOutput.class, "The list of users matching the query.")
        .response(400, ErrorResponse.class, "The full name query parameter is blank.");

    record ProfileSearchOutput(List<UserProfile> profiles) {}

    Future<ProfileSearchOutput> searchUsers(RoutingContext context) {
        // Make sure the user is logged in and has their email confirmed.
        Authenticator.get(context).requireAuth(Level.BEGINNER);

        // Try doing a by-id search.
        List<Integer> ids = readIntListFromQueryParams(context, "ids");
        if (!ids.isEmpty()) {
            return server.db().users().getAllProfiles(ids).map(ProfileSearchOutput::new);
        }

        String fullName = context.queryParams().get("fullName");
        if (fullName == null || fullName.isBlank()) {
            throw new RequestException("La recherche est vide.", 400);
        }
        return server.db().users().getAllProfilesByFullName(fullName).map(ProfileSearchOutput::new);
    }
    // endregion

    // --- Updates ---

    static final ParamDoc USERID_PARAM = new ParamDoc().name("userId").valueType(String.class)
        .desc("The ID of the user. Use 'me' to refer to the currently authenticated user.");

    // region POST /api/users/:userId/profile | Update profile
    static final RouteDoc UPDATE_PROFILE_DOC = new RouteDoc("updateProfile")
        .summary("Update profile")
        .description("Update the profile of the currently authenticated user. Each value can be omitted or set to `null` to not change it.")
        .param(USERID_PARAM)
        .requestBody(UpdateProfileInput.class, new UpdateProfileInput("Gérard", "Poulet", Gender.MALE, null, null, null))
        .response(200, UserProfile.class, "The new updated profile.");

    @ApiDoc(value = "The data to update the user profile. Each value can be omitted or set to `null` to not change it.", optional = true)
    record UpdateProfileInput(
        @ApiDoc("The first name of the user")
        String firstName,
        @ApiDoc("The last name of the user")
        String lastName,
        @ApiDoc("The gender of the user")
        Gender gender,
        @ApiDoc("The role of the user. Modifiable only by an admin.")
        Role role,
        @ApiDoc("The level of the user. Modifiable only by an admin.")
        Level level,
        @ApiDoc("The number of points the user has. Modifiable only by an admin.")
        Integer points
    ) {
        public UpdateProfileInput {
            // Sanitize strings before doing anything else.
            firstName = Sanitize.string(firstName);
            lastName = Sanitize.string(lastName);
        }

        public void validate() {
            try (var block = Validation.start()) {
                if (firstName != null) UserValidation.firstName(block, firstName);
                if (lastName != null) UserValidation.lastName(block, lastName);
                if (points != null && points < 0) {
                    block.addError("points", "Le nombre de points ne peut pas être négatif.");
                }
            }
        }
    }

    UserProfile updateProfile(RoutingContext context) {
        Authenticator auth = Authenticator.get(context);

        // Make sure we're logged in!
        auth.requireAuth();

        // Read the user id. Me = current user
        int userId = readUserId(context);

        // If we're trying to modify another user's profile, make sure we're an admin.
        boolean isMe = userId == auth.getUserId();
        if (isMe) {
            auth.requireAuth(Level.EXPERT);
        }

        // Read the JSON from the request
        UpdateProfileInput input = readBody(context, UpdateProfileInput.class);

        // Validate incoming values
        input.validate();

        // Get the user from the database. Take the user from the authenticator first since it might be cached.
        var user = isMe ? auth.getUser().await() : server.db().users().get(userId).await();
        if (user == null) {
            throw new RequestException("L'utilisateur n'existe pas.", 404, "USER_NOT_FOUND");
        }

        // Are we trying to modify any "sensitive" profile information?
        if ((input.role != null && input.role != user.getRole())
            || (input.level != null && input.level != user.getLevel())
            || (input.points != null && input.points != user.getPoints())) {
            // See if we have the rights to do so.
            if (!auth.hasLevel(Level.EXPERT)) {
                throw new RequestException("Vous n'avez pas les droits pour modifier les valeurs de rôle, niveau ou de points.",
                    403, "INSUFFICIENT_PERMISSIONS");
            }

            // Only ADMINs can edit other ADMINs, not just "experts". So enforce that.
            if (!isMe && user.getRole() == Role.ADMIN && !auth.isOfRole(Role.ADMIN)) {
                throw new RequestException("Seuls les administrateurs peuvent modifier les administrateurs.", 403, "ADMIN_EDIT_ADMIN");
            }

            // Prevent admins from stepping down, to avoid the "no-admin situation".
            // (Maybe we should check numAdmins > 0 but too complicated)
            if (isMe && input.role != null && input.role != Role.ADMIN && user.getRole() == Role.ADMIN) {
                throw new RequestException("Les administrateurs ne peuvent pas s'auto-rétrograder.", 422, "AUTO_DEMOTE_ADMIN");
            }
        }

        // Set all the values, only if they are present.
        if (input.firstName != null) user.setFirstName(input.firstName);
        if (input.lastName != null) user.setLastName(input.lastName);
        if (input.gender != null) user.setGender(input.gender);
        if (input.role != null) user.setRole(input.role);
        if (input.level != null) user.setLevel(input.level);
        if (input.points != null) user.setPoints(input.points);

        // Admins are always expert. Would be dumb to have a non-expert admin.
        // Maybe we should enforce this better but for now it works(tm).
        if (user.getRole() == Role.ADMIN) {
            user.setLevel(Level.EXPERT);
        }

        // Update the user in the database.
        server.db().users().update(user).await();

        // Done! Return the public data of the user.
        return UserProfile.fromUser(user);
    }
    // endregion

    // region POST /api/users/me/password | Change password
    static final RouteDoc CHANGE_PASSWORD_DOC = new RouteDoc("changePassword")
        .summary("Change password")
        .description("Change the password of the given user.")
        .param(USERID_PARAM)
        .requestBody(ChangePasswordInput.class, new ChangePasswordInput("oldPassword123", "newPassword456"))
        .response(204, "Password successfully changed.")
        .response(401, ErrorResponse.class, "You are not logged in.")
        .response(403, ErrorResponse.class, "The old password is incorrect, or you don't have rights to change this user's password.")
        .response(422, ErrorResponse.class, "The new password is invalid.");

    record ChangePasswordInput(
        String oldPassword,
        String newPassword
    ) {}

    void changePassword(RoutingContext context) {
        Authenticator auth = Authenticator.get(context);

        // Make sure the user is logged in
        auth.requireAuth();

        // Read the user id. Me = current user
        int userId = readUserId(context);

        // If we're trying to modify another user's profile, make sure we're an admin.
        boolean isMe = userId == auth.getUserId();
        if (isMe) {
            auth.requireAuth(Level.EXPERT);
        }

        // Read the JSON from the request
        ChangePasswordInput input = readBody(context, ChangePasswordInput.class);

        // Validate new password
        try (var block = Validation.start()) {
            UserValidation.password(block, "newPassword", input.newPassword);
        }

        // Get the user from the database. Take the user from the authenticator first since it might be cached.
        var user = isMe ? auth.getUser().await() : server.db().users().get(userId).await();
        if (user == null) {
            throw new RequestException("L'utilisateur n'existe pas.", 404, "USER_NOT_FOUND");
        }

        // Verify old password only when we aren't editing our own password
        if (!isMe || !auth.checkPassword(input.oldPassword, user.getPassHash())) {
            throw new RequestException("L'ancien mot de passe est incorrect.", 403);
        }

        // Hash the new password, and update it.
        String newPasswordHashed = auth.hashPassword(input.newPassword);
        user.setPassHash(newPasswordHashed);

        // Update user in database (synchronously using await)
        server.db().users().update(user).await();

        // Finally, log it to the console!
        log.info("User {}'s password was successfully changed (initiator: {})", user.getId(), auth.getUserId());
    }
    // endregion

    // region DELETE /api/users/:userId | Delete user
    static final RouteDoc DELETE_USER_DOC = new RouteDoc("deleteUser")
        .summary("Delete user")
        .description("Deletes a user from the app. When a user deletes their account, " +
                     "the password must be given to confirm the deletion.")
        .param(USERID_PARAM)
        .requestBody(DeleteUserInput.class, new DeleteUserInput("password234"))
        .response(204, "User successfully deleted.")
        .response(401, ErrorResponse.class, "You are not logged in.")
        .response(403, ErrorResponse.class, "You don't have the rights to delete this user.")
        .response(422, ErrorResponse.class, "The password is incorrect or missing.");

    record DeleteUserInput(
        @ApiDoc(value = "When a user deletes their account, they must provide their password. This does not apply to" +
                        " admins deleting other users.", optional = true)
        @Nullable String password
    ) {}

    void deleteUser(RoutingContext context) {
        Authenticator auth = Authenticator.get(context);

        // Make sure the user is logged in
        auth.requireAuth();

        // Read the user id. Me = current user
        int userId = readUserId(context);

        // If we're trying to delete another user, make sure we're an admin.
        boolean isMe = userId == auth.getUserId();
        if (isMe) {
            auth.requireAuth(Level.EXPERT);
        }

        // Read the JSON from the request
        DeleteUserInput input = readBody(context, DeleteUserInput.class);

        // The password must not be blank when deleting our own user
        if (!isMe) {
            try (var block = Validation.start()) {
                Validation.nonBlank(block, "password", input.password, "Le mot de passe est vide.");
            }
        }

        // Find the user from the database
        User user = isMe ? auth.getUserOrFail().await() : server.db().users().get(userId).await();
        if (user == null) {
            throw new RequestException("L'utilisateur n'existe pas.", 404, "USER_NOT_FOUND");
        }

        // Prevent admins from deleting themselves
        if (isMe && user.getRole() == Role.ADMIN) {
            throw new RequestException("L'administrateur ne peut pas s'auto-détruire.", 422, "ADMIN_SELF_DELETE");
        }

        // Only admins can delete other admins, not just "experts". So enforce that.
        if (!isMe && user.getRole() == Role.ADMIN && !auth.isOfRole(Role.ADMIN)) {
            throw new RequestException("Seuls les administrateurs peuvent supprimer les administrateurs.", 403, "ADMIN_DELETE_ADMIN");
        }

        // When a user deletes themselves, check their password first. Admins don't need to input other users' passwords!
        if (isMe && !auth.checkPassword(input.password, user.getPassHash())) {
            throw new RequestException("Le mot de passe est incorrect.", 422, "INCORRECT_PASSWORD");
        }

        // Finally, begin deleting the user. The following will happen to associated data:
        //   - Rooms owned by this user:    the owner will be set to NULL
        //   - Login logs about this user:  the logs will be deleted
        server.db().users().delete(userId).await();

        // If we deleted ourselves, log out of the app now.
        if (isMe) {
            auth.logout();
        }

        // Complete the request with the 204 CREATED status code.
        context.response().setStatusCode(204);
    }
    // endregion

    // --- Point information ---

    // region GET /api/users/level-info
    static final RouteDoc LEVEL_INFO_DOC = new RouteDoc("getLevelInfo")
        .summary("Get level upgrade information")
        .description("Gives the amount of points necessary to reach all levels.")
        .response(200, UpgradeInfo.class, "The information about the next upgrade.");

    record UpgradeInfo(Map<Level, Integer> pointsRequired) {
        /// Current value global to the program.
        static final UpgradeInfo CURRENT = new UpgradeInfo(
            new EnumMap<>(Map.of(
                Level.BEGINNER, 0,
                Level.INTERMEDIATE, UserOperations.INTERMEDIATE_POINTS,
                Level.ADVANCED, UserOperations.ADVANCED_POINTS,
                Level.EXPERT, UserOperations.EXPERT_POINTS
            ))
        );

        /// Example for the API docs
        static final UpgradeInfo EXAMPLE = CURRENT;
    }

    Future<UpgradeInfo> getLevelInfo(RoutingContext context) {
        // Return the information, no need to check for authentication honestly it's not a big deal.
        return Future.succeededFuture(UpgradeInfo.CURRENT);
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
                                auth.login(u);
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

    /// Reads the userId from the path parameter.
    ///
    /// If it's "me", then returns the logged-in user id
    /// Else, returns the user id as an integer.
    private int readUserId(RoutingContext context) {
        if (context.pathParam("userId").equals("me")) {
            Authenticator auth = Authenticator.get(context);
            var id = auth.getUserId();
            if (id == 0) {
                throw Authenticator.UNAUTHORIZED_EXCEPTION;
            }

            return id;
        } else {
            return readIntPathParam(context, "userId");
        }
    }
}
