package fr.domotique.api.users;

import fr.domotique.base.apidocs.*;
import fr.domotique.base.data.*;
import fr.domotique.data.*;

@ApiDoc("Public information about a user.")
public record UserProfile(
    @ApiDoc("The unique identifier of the user. A value of 0 is invalid.")
    int id,

    @ApiDoc("The first name of the user.")
    String firstName,

    @ApiDoc("The last name of the user.")
    String lastName,

    @ApiDoc("The role of the user in the EHPAD.")
    Role role,

    @ApiDoc("The level of the user.")
    Level level,

    @ApiDoc("The gender of the user.")
    Gender gender
) {
    /// Converts a database User to a PublicUser for client API consumption.
    /// Returns `null` when the user is null.
    public static UserProfile fromUser(User u) {
        if (u == null) {return null;}
        return new UserProfile(u.getId(), u.getFirstName(), u.getLastName(), u.getRole(), u.getLevel(), u.getGender());
    }

    /// Transforms a SQL row into a UserProfile.
    public static Mapper<UserProfile> MAP = Mapper.of(6, (r, s) -> new UserProfile(
        r.getInteger(s.next()),
        r.getString(s.next()),
        r.getString(s.next()),
        Role.fromByte(r.get(Byte.class, s.next())),
        Level.fromByte(r.get(Byte.class, s.next())),
        Gender.fromByte(r.get(Byte.class, s.next()))
    ));

    public static String columnList(String tableName) {
        return QueryUtils.columnList(tableName, "id", "firstName", "lastName", "role", "level", "gender");
    }

    /// Example user profile for API documentation.
    public static final UserProfile EXAMPLE = new UserProfile(10, "Juréma", "Deveri",
        Role.RESIDENT, Level.ADVANCED, Gender.FEMALE);
}
