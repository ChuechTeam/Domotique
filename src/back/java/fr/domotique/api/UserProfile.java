package fr.domotique.api;

import fr.domotique.base.apidocs.*;
import fr.domotique.data.*;
import io.vertx.sqlclient.*;

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

    /// Converts a database row to a PublicUser for client API consumption.
    public static UserProfile fromRow(Row u) {
        return new UserProfile(
            u.getInteger(0),
            u.getString(1),
            u.getString(2),
            Role.fromByte(u.get(Byte.class, 3)),
            Level.fromByte(u.get(Byte.class, 4)),
            Gender.fromByte(u.get(Byte.class, 5))
        );
    }

    public static final UserProfile EXAMPLE = new UserProfile(10, "Juréma", "Deveri",
        Role.RESIDENT, Level.ADVANCED, Gender.FEMALE);
}
