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
    public static CompleteUser fromUser(User u) {
        if (u == null) {return null;}
        return new CompleteUser(UserProfile.fromUser(u), UserSecrets.fromUser(u));
    }

    public static final CompleteUser EXAMPLE = new CompleteUser(UserProfile.EXAMPLE, UserSecrets.EXAMPLE);
}
