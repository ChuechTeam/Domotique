package fr.domotique.api;

import fr.domotique.apidocs.*;
import fr.domotique.data.*;

@DocDesc("Public information about a user.")
public record UserProfile(
    @DocDesc("The unique identifier of the user. A value of 0 is invalid.")
    int id,

    @DocDesc("The first name of the user.")
    String firstName,

    @DocDesc("The last name of the user.")
    String lastName,

    @DocDesc("The role of the user in the EHPAD.")
    Role role,

    @DocDesc("The level of the user.")
    Level level,

    @DocDesc("The gender of the user.")
    Gender gender
) {
    /// Converts a database User to a PublicUser for client API consumption.
    /// Returns `null` when the user is null.
    public static UserProfile fromUser(User u) {
        if (u == null) {return null;}
        return new UserProfile(u.getId(), u.getFirstName(), u.getLastName(), u.getRole(), u.getLevel(), u.getGender());
    }

    public static final UserProfile EXAMPLE = new UserProfile(10, "Juréma", "Deveri",
        Role.RESIDENT, Level.ADVANCED, Gender.FEMALE);
}
