package fr.domotique.data;

import fr.domotique.base.data.*;
import io.vertx.core.*;
import io.vertx.sqlclient.*;
import org.jetbrains.annotations.*;

import java.util.List;

/// Contains functions to interact with the [Room] table in the database.
///
/// ## Example
///
/// ```java
/// // Get the room with ID 5
/// server.db().rooms().get(5).onSuccess(room -> { ... });
///```
public class RoomTable extends Table {
    public RoomTable(SqlClient client) {
        super(client);
    }

    /// Gets the room with the given ID. Can return `null`.
    public Future<@Nullable Room> get(int id) {
        return querySingle(Room::map, "SELECT * FROM room WHERE id = ?", id);
    }

    /// Gets all rooms in the database.
    public Future<List<Room>> getAll() {
        return queryMany(Room::map, "SELECT * FROM room");
    }

    /// Gets all rooms owned by a specific user. Returns an empty list if none found.
    public Future<List<Room>> getByOwner(int ownerId) {
        return queryMany(Room::map, "SELECT * FROM room WHERE owner_id = ?", ownerId);
    }

    /// Creates a room in the database.
    ///
    /// @param room the room to create, must have an id of 0
    /// @throws IllegalArgumentException if the room has a non-zero id
    /// @return the same room, with its new ID
    public Future<Room> create(Room room) {
        if (room.getId() != 0) {
            return Future.failedFuture(new IllegalArgumentException("Room has a non-zero id!"));
        }

        return insert(room, room::setId, "INSERT INTO room (name, color, owner_id) VALUES (?, ?, ?)",
            room.getName(),
            room.getColor(),
            room.getOwnerId());
    }

    /// Updates an existing room in the database.
    public Future<Room> update(Room room) {
        return query("UPDATE room SET name = ?, color = ?, owner_id = ? WHERE id = ?",
            room.getName(),
            room.getColor(),
            room.getOwnerId(),
            room.getId())
            .map(room);
    }

    /// Deletes a room from the database.
    ///
    /// @param id the id of the room to delete
    /// @return a Future that completes when the room is deleted
    public Future<Boolean> delete(int id) {
        return query("DELETE FROM room WHERE id = ?", id).map(x -> x.rowCount() != 0);
    }
}