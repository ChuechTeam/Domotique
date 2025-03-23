package fr.domotique.data;

import fr.domotique.base.data.*;
import org.jetbrains.annotations.*;

/// A room that can host multiple devices.
public class Room {
    private int id;
    private String name;
    /// Color in RGB format (0xRRGGBB)
    private int color;
    /// The user ID of the owner of this room. Either :
    /// - this room has a user id -> it's a personal living room
    /// - this room has no user id -> it's a common room (or a room where multiple people live)
    private @Nullable Integer ownerId;

    public Room(int id, String name, int color, @Nullable Integer ownerId) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.ownerId = ownerId;
    }

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
        new EntityColumn<>("ownerId", Room::getOwnerId)
    );

    // Getters and setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public @Nullable Integer getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(@Nullable Integer ownerId) {
        this.ownerId = ownerId;
    }
}
