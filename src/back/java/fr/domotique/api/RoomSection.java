package fr.domotique.api;

import fr.domotique.*;
import fr.domotique.base.*;
import fr.domotique.base.Validation;
import fr.domotique.base.apidocs.*;
import fr.domotique.base.data.*;
import fr.domotique.data.*;
import io.vertx.core.Future;
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

        roomRoutes.route().handler(ctx -> {
            Authenticator auth = Authenticator.get(ctx);
            auth.requireAuth(Level.BEGINNER);
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

    record RoomsResponse(List<Room> rooms) {}

    Future<RoomsResponse> getAll(RoutingContext context) {
        return server.db().rooms().getAll()
            .map(RoomsResponse::new);
    }
    // endregion

    // region GET /api/rooms/:roomId | Get room by ID
    static final RouteDoc GET_ROOM_DOC = new RouteDoc("getRoomById")
        .summary("Get room by ID")
        .description("Gets a room by its ID.")
        .pathParam("roomId", int.class, "The ID of the room.")
        .response(200, Room.class, "The room data.")
        .response(204, "Room not found.");

    Future<Room> getRoomById(RoutingContext context) {
        int roomId = readIntPathParam(context, "roomId");
        return server.db().rooms().get(roomId);
    }
    // endregion

    // region POST /api/rooms | Create room
    static final RouteDoc CREATE_ROOM_DOC = new RouteDoc("createRoom")
        .summary("Create room")
        .description("Creates a new room. The room will be owned by the current user unless specified otherwise.")
        .requestBody(CreateRoomInput.class, new CreateRoomInput("Living Room", 0x2E86C1, null))
        .response(201, Room.class, "The room was created successfully.")
        .response(422, ErrorResponse.class, "Some fields are invalid or the room name already exists.");

    record CreateRoomInput(String name, int color, @Nullable Integer ownerId) {
        public CreateRoomInput {
            name = Sanitize.string(name);
        }
    }

    Room createRoom(RoutingContext context) {
        Authenticator auth = Authenticator.get(context);

        CreateRoomInput input = readBody(context, CreateRoomInput.class);

        // Validate room input
        try (var block = Validation.start()) {
            Validation.nonBlank(block, "name", input.name, "Le nom de la pièce ne peut pas être vide.");
            Validation.lengthIn(block, "name", input.name, 0, 128, "Le nom de la pièce est trop long.");
        }

        // Determine room ownership
        Integer ownerId = input.ownerId;
        if (ownerId == null) {
            // If no owner specified, set to current user
            ownerId = auth.getUserId();
        } else {
            // Only users with sufficient privileges can create rooms for others
            auth.requireAuth(Level.EXPERT);
        }

        // Create the room
        Room room = new Room(0, input.name, input.color, ownerId);

        try {
            server.db().rooms().create(room).await();
            log.info("Room created with id {} and name {}", room.getId(), room.getName());

            context.response().setStatusCode(201);
            return room;
        } catch (DuplicateException ex) {
            throw new RequestException("Une pièce avec ce nom existe déjà.", 422, "ROOM_NAME_EXISTS");
        }
    }
    // endregion

    // region POST /api/rooms/:roomId | Update room
    static final RouteDoc UPDATE_ROOM_DOC = new RouteDoc("updateRoom")
        .summary("Update room")
        .description("Updates an existing room. Users can only update rooms they own, unless they have expert privileges.")
        .pathParam("roomId", int.class, "The ID of the room to update.")
        .requestBody(UpdateRoomInput.class, new UpdateRoomInput("Updated Room", 0x8E44AD, null))
        .response(200, Room.class, "The room was updated successfully.")
        .response(404, ErrorResponse.class, "Room not found.")
        .response(422, ErrorResponse.class, "Some fields are invalid or the room name already exists.");

    record UpdateRoomInput(String name, int color, @Nullable Integer ownerId) {
        public UpdateRoomInput {
            name = Sanitize.string(name);
        }
    }

    Room updateRoom(RoutingContext context) {
        Authenticator auth = Authenticator.get(context);
        auth.requireAuth(Level.ADVANCED);

        int roomId = readIntPathParam(context, "roomId");
        UpdateRoomInput input = readBody(context, UpdateRoomInput.class);

        // Validate room input
        try (var block = Validation.start()) {
            Validation.nonBlank(block, "name", input.name, "Le nom de la pièce ne peut pas être vide.");
            Validation.lengthIn(block, "name", input.name, 0, 128, "Le nom de la pièce est trop long.");
        }

        // Get the room
        Room room = server.db().rooms().get(roomId).await();
        if (room == null) {
            throw new RequestException("Pièce introuvable.", 404, "ROOM_NOT_FOUND");
        }

        // Check permissions - only room owner or experts can modify rooms
        int userId = auth.getUserId();
        Integer roomOwnerId = room.getOwnerId();

        if (roomOwnerId != null) {
            throw new RequestException("Vous n'avez pas la permission de modifier cette pièce.", 403, "INSUFFICIENT_PERMISSIONS");
        }

        // Update room properties
        room.setName(input.name);
        room.setColor(input.color);

        try {
            server.db().rooms().update(room).await();
            log.info("Room updated with id {} and name {}", room.getId(), room.getName());
            return room;
        } catch (DuplicateException ex) {
            throw new RequestException("Une pièce avec ce nom existe déjà.", 422, "ROOM_NAME_EXISTS");
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
        Authenticator auth = Authenticator.get(context);
        auth.requireAuth(Level.ADVANCED);

        int roomId = readIntPathParam(context, "roomId");

        // Get the room
        Room room = server.db().rooms().get(roomId).await();
        if (room == null) {
            throw new RequestException("Cette salle n'existe pas.", 404, "NOT_FOUND");
        }

        // Delete room
        server.db().rooms().delete(roomId).await();
        log.info("Room deleted with id {}", roomId);
    }
    // endregion
}