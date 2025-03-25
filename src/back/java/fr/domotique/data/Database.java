package fr.domotique.data;

import io.vertx.sqlclient.*;

/// The database of the entire app, with all tables.
///
/// @param users the user table, containing all [User] rows
/// @param rooms the room table, containing all [Room] rows
/// @param deviceTypes the device type table, containing all [DeviceType] rows
public record Database(
    UserTable users,
    RoomTable rooms,
    DeviceTypeTable deviceTypes,
    DeviceTable devices,
    LoginLogTable loginLogs
) {
    /// Makes a database object with all tables ready
    public Database(SqlClient client) {
        this(
            new UserTable(client),
            new RoomTable(client),
            new DeviceTypeTable(client),
            new DeviceTable(client),
            new LoginLogTable(client)
        );
    }
}
