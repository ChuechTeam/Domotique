package fr.domotique.data;

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

}
