package fr.domotique.api;

import fr.domotique.apidocs.*;
import fr.domotique.data.*;

@DocDesc("Public information about a user.")
public record UserProfile(
    @DocDesc("The unique identifier of the user. A value of 0 is invalid.")
    int id,

    @DocDesc("The gender of the user.")
    Gender gender
) {
    /// Converts a database User to a PublicUser for client API consumption.
    /// Returns `null` when the user is null.
    public static UserProfile fromUser(User u) {
        if (u == null) {return null;}
        return new UserProfile(u.getId(), u.getGender());
    }

    public static final UserProfile EXAMPLE = new UserProfile(10, Gender.MALE);
}
