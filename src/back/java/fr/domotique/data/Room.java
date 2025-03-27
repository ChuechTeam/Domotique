package fr.domotique.data;

import fr.domotique.base.data.*;
import lombok.*;
import org.jetbrains.annotations.*;

/// A room that can host multiple devices.
@Getter
@Setter
@AllArgsConstructor
public class Room {
    /// The identifier of the room.
    int id;
    /// The name of the room.
    String name;
    /// Color in RGB format (0xRRGGBB)
    int color;
    /// The user ID of the owner of this room. Either :
    /// - this room has a user id -> it's a personal living room
    /// - this room has no user id -> it's a common room (or a room where multiple people live)
    @Nullable Integer ownerId;

    public static final EntityInfo<Room> ENTITY = new EntityInfo<>(
        Room.class,
        (r, s) -> new Room(
            r.getInteger(s.next()),
            r.getString(s.next()),
            r.getInteger(s.next()),
            r.getInteger(s.next())
        ),
        new EntityColumn<>("id", Room::getId, ColumnType.GENERATED_KEY),
        new EntityColumn<>("name", Room::getName),
        new EntityColumn<>("color", Room::getColor),
        new EntityColumn<>("userId", Room::getOwnerId)
    );
}
