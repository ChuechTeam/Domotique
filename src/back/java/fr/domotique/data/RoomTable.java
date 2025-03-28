package fr.domotique.data;

import fr.domotique.api.rooms.*;
import fr.domotique.api.users.*;
import fr.domotique.base.data.*;
import io.vertx.core.*;
import io.vertx.sqlclient.*;
import org.jetbrains.annotations.*;
import org.slf4j.*;

import java.util.*;

import static fr.domotique.data.Room.ENTITY;

/// Contains functions to interact with the [Room] table in the database.
///
/// ## Example
///
/// ```java
/// // Get the room with ID 5
/// server.db().rooms().get(5).onSuccess(room -> { ... });
///```
public class RoomTable extends Table {
    private static final Logger log = LoggerFactory.getLogger(RoomTable.class);

    public RoomTable(SqlClient client) {
        super(client);
    }

    /// Gets the room with the given ID. Can return `null`.
    public Future<@Nullable Room> get(int id) {
        return querySingle(ENTITY.mapper(), "SELECT * FROM Room WHERE id = ?", id);
    }

    /// Gets all rooms in the database.
    public Future<List<Room>> getAll() {
        return queryMany(ENTITY.mapper(), "SELECT * FROM Room");
    }

    static String COMPLETE_SINGLE_SQL = makeModularSQL("""
            SELECT %s, %s
            FROM Room r
            LEFT JOIN User u on u.id = r.ownerId
            WHERE r.id = ?
            """,
        CompleteRoom.columnList("r"),   // -> "r.id, r.name, r.color"
        UserProfile.columnList("u")); // -> "u.id, u.firstName, u.lastName, ..."

    public Future<@Nullable CompleteRoom> getComplete(int id) {
        return querySingle(CompleteRoom.MAP, COMPLETE_SINGLE_SQL, id);
    }

    static String COMPLETE_MANY_SQL = makeModularSQL("""
            SELECT %s, %s
            FROM Room r
            LEFT JOIN User u on u.id = r.ownerId
            """,
        CompleteRoom.columnList("r"),   // -> "r.id, r.name, r.color"
        UserProfile.columnList("u")); // -> "u.id, u.firstName, u.lastName, ..."

    /// Gets all [CompleteRoom] in the database.
    public Future<List<CompleteRoom>> getAllComplete() {
        return queryMany(CompleteRoom.MAP, COMPLETE_MANY_SQL);
    }

    /// Gets all [CompleteRoom] in the database with the given IDs.
    public Future<List<CompleteRoom>> getAllComplete(Collection<Integer> ids) {
        if (ids.isEmpty()) {
            return Future.succeededFuture(Collections.emptyList());
        }

        return queryMany(CompleteRoom.MAP,
            COMPLETE_MANY_SQL + "\n WHERE r.id IN " + paramList(ids.size()),
            ids.toArray()
        );
    }

    /// Creates a room in the database.
    ///
    /// @param room the room to create, must have an id of 0
    /// @throws IllegalArgumentException if the room has a non-zero id
    /// @return the same room, with its new ID
    public Future<Room> create(Room room) {
        return insert(ENTITY, room, Room::getId, Room::setId);
    }

    /// Updates an existing room in the database.
    public Future<Room> update(Room room) {
        return update(ENTITY, room);
    }

    /// Deletes a room from the database.
    ///
    /// @param id the id of the room to delete
    /// @return a Future that completes when the room is deleted
    public Future<Boolean> delete(int id) {
        return delete(ENTITY, id);
    }
}