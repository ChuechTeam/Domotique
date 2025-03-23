package fr.domotique.data;

/// The database of the entire app, with all tables.
///
/// @param users the user table, containing all [User] rows
/// @param rooms the room table, containing all [Room] rows
public record Database(
    UserTable users,
    RoomTable rooms
) {

}
