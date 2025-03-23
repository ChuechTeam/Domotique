package fr.domotique.api.rooms;

import fr.domotique.*;
import fr.domotique.base.*;
import fr.domotique.base.Validation;
import fr.domotique.base.apidocs.*;
import fr.domotique.base.data.*;
import fr.domotique.data.*;
import io.vertx.core.Future;
import io.vertx.core.http.*;
import io.vertx.ext.web.*;
import org.jetbrains.annotations.Nullable;
import org.slf4j.*;

import java.util.*;

/// All API endpoints to access room data
///
/// NOTE: All endpoints
public class RoomSection extends Section {
    /// Creates a new RoomSection with the given server.
    public RoomSection(Server server) {
        super(server);
    }

    // All routes of this section will begin with /api/rooms
    static final String PATH_PREFIX = "/api/rooms*";

    // Logger for this section
    static final Logger log = LoggerFactory.getLogger(RoomSection.class);

    @Override
    public void register(Router router) {
        // Create a sub-router for all room routes
        var roomRoutes = newRouter();

        roomRoutes.post("/test")
            .respond(ctx -> {
                return server.db().rooms().create(new Room(0,
                    "Test Room",
                    0x2E86C1,
                    null
                ));
            });

        // When:
        // - A user requests data, they must be AT LEAST a BEGINNER (= confirmed email)
        // - A user modifies data, they must be AT LEAST an ADVANCED user
        roomRoutes.route().handler(ctx -> {
            Authenticator auth = Authenticator.get(ctx);

            if (ctx.request().method() == HttpMethod.GET) {
                auth.requireAuth(Level.BEGINNER);
            } else {
                auth.requireAuth(Level.ADVANCED);
            }

            // Don't stop there! Continue the request.
            ctx.next();
        });

        // Register all room-related endpoints
        roomRoutes.get("/").respond(this::getAll).putMetadata(RouteDoc.KEY, GET_ROOMS_DOC);
        roomRoutes.post("/").respond(vt(this::createRoom)).putMetadata(RouteDoc.KEY, CREATE_ROOM_DOC);

        // Routes with parameters come last
        roomRoutes.get("/:roomId").respond(this::getRoomById).putMetadata(RouteDoc.KEY, GET_ROOM_DOC);
        roomRoutes.post("/:roomId").respond(vt(this::updateRoom)).putMetadata(RouteDoc.KEY, UPDATE_ROOM_DOC);
        roomRoutes.delete("/:roomId").respond(vt(this::deleteRoom)).putMetadata(RouteDoc.KEY, DELETE_ROOM_DOC);

        // Register the sub-router with the main router
        // And add common authorization responses to the documentation.
        doc(router.route(PATH_PREFIX).subRouter(roomRoutes))
            .tag("Rooms")
            .response(401, ErrorResponse.class, "You are not logged in.")
            .response(403, ErrorResponse.class, "You don't have permission to access this resource.");
    }

    // region GET /api/rooms | Get all rooms
    static final RouteDoc GET_ROOMS_DOC = new RouteDoc("getRooms")
        .summary("Get rooms")
        .description("Gets all rooms from the database.")
        .response(200, RoomsResponse.class, "The list of all rooms.");

    record RoomsResponse(List<CompleteRoom> rooms) {}

    Future<RoomsResponse> getAll(RoutingContext context) {
        return server.db().rooms().getAllComplete().map(RoomsResponse::new);
    }
    // endregion

    // region GET /api/rooms/:roomId | Get room by ID
    static final RouteDoc GET_ROOM_DOC = new RouteDoc("getRoomById")
        .summary("Get room by ID")
        .description("Gets a room by its ID.")
        .pathParam("roomId", int.class, "The ID of the room.")
        .response(200, CompleteRoom.class, "The room data.")
        .response(204, "Room not found.");

    Future<CompleteRoom> getRoomById(RoutingContext context) {
        int roomId = readIntPathParam(context, "roomId");
        return server.db().rooms().getComplete(roomId);
    }
    // endregion

    @ApiDoc("Data for both INSERT and UPDATE operations on a room.")
    record RoomInput(String name, int color, @Nullable @ApiDoc(optional = true) Integer ownerId) {
        public RoomInput {
            name = Sanitize.string(name);
        }

        /// Runs validation for this input.
        public void validate() {
            try (var block = Validation.start()) {
                Validation.lengthIn(block, "name", name, 0, 128,
                    "Le nom est vide.",
                    "Le nom est trop long.");
            }
        }
    }

    // region POST /api/rooms | Create room
    static final RouteDoc CREATE_ROOM_DOC = new RouteDoc("createRoom")
        .summary("Create room")
        .description("Creates a new room.")
        .requestBody(RoomInput.class, new RoomInput("Toilettes", 0x2E86C1, null))
        .response(201, CompleteRoom.class, "The room was created successfully.")
        .response(422, ErrorResponse.class, "Some fields are invalid or the room name already exists.");

    CompleteRoom createRoom(RoutingContext context) {
        RoomInput input = readBody(context, RoomInput.class);

        // Validate data
        input.validate();

        // Create the room
        Room room = new Room(0, input.name, input.color, input.ownerId);

        try {
            server.db().rooms().create(room).await();
            log.info("Room created with id {} and name {}", room.getId(), room.getName());

            context.response().setStatusCode(201);
            return server.db().rooms().getComplete(room.getId()).await();
        } catch (ForeignException ex) {
            throw new RequestException("Le propriétaire de la salle n'existe pas.", 422, "OWNER_NOT_FOUND");
        }
    }
    // endregion

    // region POST /api/rooms/:roomId | Update room
    static final RouteDoc UPDATE_ROOM_DOC = new RouteDoc("updateRoom")
        .summary("Update room")
        .description("Updates an existing room.")
        .pathParam("roomId", int.class, "The ID of the room to update.")
        .requestBody(RoomInput.class, new RoomInput("Updated Room", 0x8E44AD, null))
        .response(200, CompleteRoom.class, "The room was updated successfully.")
        .response(404, ErrorResponse.class, "Room not found.")
        .response(422, ErrorResponse.class, "Some fields are invalid.");

    CompleteRoom updateRoom(RoutingContext context) {
        int roomId = readIntPathParam(context, "roomId");
        RoomInput input = readBody(context, RoomInput.class);

        // Validate room input
        input.validate();

        // Get the room
        Room room = server.db().rooms().get(roomId).await();
        if (room == null) {
            throw new RequestException("Salle introuvable.", 404, "ROOM_NOT_FOUND");
        }

        // Update room properties
        room.setName(input.name);
        room.setColor(input.color);
        room.setOwnerId(input.ownerId);

        try {
            server.db().rooms().update(room).await();
            log.info("Room updated with id {} and name {}", room.getId(), room.getName());
            return server.db().rooms().getComplete(roomId).await();
        } catch (ForeignException ex) {
            throw new RequestException("Le propriétaire de la salle n'existe pas.", 422, "OWNER_NOT_FOUND");
        }
    }
    // endregion

    // region DELETE /api/rooms/:roomId | Delete room
    static final RouteDoc DELETE_ROOM_DOC = new RouteDoc("deleteRoom")
        .summary("Delete room")
        .description("Deletes a room.")
        .pathParam("roomId", int.class, "The ID of the room to delete.")
        .response(204, "The room was deleted successfully.")
        .response(404, ErrorResponse.class, "Room not found.");

    void deleteRoom(RoutingContext context) {
        int roomId = readIntPathParam(context, "roomId");

        // Get the room
        Room room = server.db().rooms().get(roomId).await();
        if (room == null) {
            throw new RequestException("Cette salle n'existe pas.", 404, "NOT_FOUND");
        }

        // Delete room
        // TODO: What happens for devices with this room?
        server.db().rooms().delete(roomId).await();
        log.info("Room deleted with id {}", roomId);
    }
    // endregion
}