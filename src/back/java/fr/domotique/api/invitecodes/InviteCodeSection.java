package fr.domotique.api.invitecodes;

import fr.domotique.*;
import fr.domotique.base.*;
import fr.domotique.base.Validation;
import fr.domotique.base.apidocs.*;
import fr.domotique.data.*;
import io.vertx.core.Future;
import io.vertx.ext.auth.prng.*;
import io.vertx.ext.web.*;
import org.slf4j.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/// API endpoints to manage invite codes
public class InviteCodeSection extends Section {
    /// Creates a new InviteCodeSection with the given server.
    public InviteCodeSection(Server server) {
        super(server);
    }

    // All routes of this section will begin with /api/invite-codes
    static final String PATH_PREFIX = "/api/invite-codes*";

    // Logger for this section
    static final Logger log = LoggerFactory.getLogger(InviteCodeSection.class);

    @Override
    public void register(Router router) {
        // Create a sub-router for all invite code routes
        var inviteCodeRoutes = newRouter();

        // All operations require EXPERT level
        inviteCodeRoutes.route().handler(ctx -> {
            Authenticator auth = Authenticator.get(ctx);
            auth.requireAuth(Level.EXPERT);
            ctx.next();
        });

        // Register endpoints
        inviteCodeRoutes.get("/").respond(this::getAll).putMetadata(RouteDoc.KEY, GET_INVITE_CODES_DOC);
        inviteCodeRoutes.post("/").respond(vt(this::createInviteCode)).putMetadata(RouteDoc.KEY, CREATE_INVITE_CODE_DOC);
        inviteCodeRoutes.delete("/:id").respond(vt(this::deleteInviteCode)).putMetadata(RouteDoc.KEY, DELETE_INVITE_CODE_DOC);

        // Register the sub-router with the main router
        // And add common authorization responses to the documentation.
        doc(router.route(PATH_PREFIX).subRouter(inviteCodeRoutes))
            .tag("Invite Codes")
            .response(401, ErrorResponse.class, "You are not logged in.")
            .response(403, ErrorResponse.class, "You don't have permission to access this resource.");
    }

    // region GET /api/invite-codes | Get all invite codes
    static final RouteDoc GET_INVITE_CODES_DOC = new RouteDoc("getInviteCodes")
        .summary("Get invite codes")
        .description("Gets all invite codes from the database.")
        .response(200, InviteCodesResponse.class, "The list of all invite codes.");

    record InviteCodesResponse(List<InviteCode> inviteCodes) {}

    Future<InviteCodesResponse> getAll(RoutingContext context) {
        return server.db().inviteCodes().getAll().map(InviteCodesResponse::new);
    }
    // endregion

    // region POST /api/invite-codes | Create invite code
    static final RouteDoc CREATE_INVITE_CODE_DOC = new RouteDoc("createInviteCode")
        .summary("Create invite code")
        .description("Creates a new invite code with a randomly generated ID.")
        .requestBody(InviteCodeInput.class, new InviteCodeInput(5, Role.CAREGIVER))
        .response(201, InviteCode.class, "The invite code was created successfully.")
        .response(422, ErrorResponse.class, "Some fields are invalid.");

    @ApiDoc("Data for creating a new invite code")
    record InviteCodeInput(int usagesLeft, Role role) {
        /// Runs validation for this input.
        public void validate() {
            try (var block = Validation.start()) {
                if (usagesLeft <= 0) {
                    block.addError("usagesLeft", "Le nombre d'utilisations doit être supérieur à 0.");
                }
            }
        }
    }

    InviteCode createInviteCode(RoutingContext context) {
        InviteCodeInput input = readBody(context, InviteCodeInput.class);

        // Validate data
        input.validate();

        // Generate a random ID (6 characters)
        String id = generateRandomId(context, 6);

        // Get the current user ID
        Authenticator auth = Authenticator.get(context);
        Integer creatorId = auth.getUserId();

        // Create the invite code
        InviteCode inviteCode = new InviteCode(
            id, 
            input.usagesLeft, 
            input.role, 
            creatorId, 
            Instant.now()
        );

        server.db().inviteCodes().insert(inviteCode).await();
        log.info("Invite code created with id {}", inviteCode.getId());

        context.response().setStatusCode(201);
        return inviteCode;
    }

    // Helper method to generate a random alphanumeric ID
    private String generateRandomId(RoutingContext context, int length) {
        VertxContextPRNG prng = VertxContextPRNG.current(context.vertx());
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        var output = new char[length];
        for (int i = 0; i < length; i++) {
            int index = prng.nextInt(chars.length());
            output[i] = chars.charAt(index);
        }
        return String.valueOf(output);
    }
    // endregion

    // region DELETE /api/invite-codes/:id | Delete invite code
    static final RouteDoc DELETE_INVITE_CODE_DOC = new RouteDoc("deleteInviteCode")
        .summary("Delete invite code")
        .description("Deletes an invite code.")
        .pathParam("id", String.class, "The ID of the invite code to delete.")
        .response(204, "The invite code was deleted successfully.")
        .response(404, ErrorResponse.class, "Invite code not found.");

    void deleteInviteCode(RoutingContext context) {
        String id = context.pathParam("id");

        // Get the invite code
        InviteCode inviteCode = server.db().inviteCodes().get(id).await();
        if (inviteCode == null) {
            throw new RequestException("Ce code d'invitation n'existe pas.", 404, "NOT_FOUND");
        }

        // Delete invite code
        server.db().inviteCodes().delete(id).await();
        
        log.info("Invite code deleted with id {}", id);
        context.response().setStatusCode(204).end();
    }
    // endregion
}
