package fr.domotique.api.rooms;

import fr.domotique.api.users.*;
import fr.domotique.base.apidocs.*;
import fr.domotique.base.data.*;
import org.jetbrains.annotations.*;

@ApiDoc("A room in the EHPAD.")
public record CompleteRoom(
    @ApiDoc("The room's ID.")
    int id,
    @ApiDoc("The room's name.")
    String name,
    @ApiDoc("The room's color, in RGB format. The last 8 bits can be ignored.")
    int color,
    @ApiDoc(value = "The room's owner, or `null` if it has none.", optional = true)
    @Nullable UserProfile owner
) {
    /// Transforms a SQL row into a CompleteRoom.
    public static Mapper<CompleteRoom> MAP = Mapper.of(3 + UserProfile.MAP.getColumns(),
        (row, session) -> new CompleteRoom(
            row.getInteger(session.next()),
            row.getString(session.next()),
            row.getInteger(session.next()),
            UserProfile.MAP.apply(row, session)
        ));

    /// Returns the id, name, color list of columns for the room table, but prefixed with `tableName`.
    ///
    /// ## Example
    /// ```
    /// CompleteRoom.columnList("room") // "room.id, room.name, room.color"
    /// ```
    public static String columnList(String tableName) {
        return QueryUtils.columnList(tableName, "id", "name", "color");
    }

    /// An example for API documentation.
    public static final CompleteRoom EXAMPLE = new CompleteRoom(5, "Room 5", 0x2E86C1, UserProfile.EXAMPLE);
}
