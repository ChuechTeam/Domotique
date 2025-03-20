package fr.domotique.api;

import fr.domotique.apidocs.*;
import fr.domotique.data.*;

@DocDesc("All confidential information only the user account's owner can see, or an admin.")
public record UserSecrets(
    @DocDesc("The email address of the user. Must be unique. Is used to log in.")
    String email
) {
    /// Converts a database User to a UserSecrets for client API consumption.
    /// Returns `null` when the user is null.
    public static UserSecrets fromUser(User u) {
        if (u == null) {return null;}
        return new UserSecrets(u.getEmail());
    }

    public static final UserSecrets EXAMPLE = new UserSecrets("coucou@email.fr");
}
