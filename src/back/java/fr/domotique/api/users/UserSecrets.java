package fr.domotique.api.users;

import fr.domotique.base.apidocs.*;
import fr.domotique.data.*;

@ApiDoc("All confidential information only the user account's owner can see, or an admin.")
public record UserSecrets(
    @ApiDoc("The email address of the user. Must be unique. Is used to log in.")
    String email,

    @ApiDoc("True when this user's email has been confirmed.")
    boolean emailConfirmed
) {
    /// Converts a database User to a UserSecrets for client API consumption.
    /// Returns `null` when the user is null.
    public static UserSecrets fromUser(User u) {
        if (u == null) {return null;}
        return new UserSecrets(u.getEmail(), u.isEmailConfirmed());
    }

    public static final UserSecrets EXAMPLE = new UserSecrets("coucou@email.fr", true);
}
