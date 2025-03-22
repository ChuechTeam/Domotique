package fr.domotique.api;

import fr.domotique.base.apidocs.*;
import fr.domotique.data.*;

@ApiDoc("Complete information about a user: its public data and its secrets.")
public record CompleteUser(
    @ApiDoc("The public data of the user.")
    UserProfile profile,

    @ApiDoc("The confidential data of the user.")
    UserSecrets secret
) {
    /// Convert a database user to a [CompleteUser]
    public static CompleteUser fromUser(User u) {
        if (u == null) {return null;}
        return new CompleteUser(UserProfile.fromUser(u), UserSecrets.fromUser(u));
    }

    /// An example of a complete user for API documentation
    public static final CompleteUser EXAMPLE = new CompleteUser(UserProfile.EXAMPLE, UserSecrets.EXAMPLE);
}
