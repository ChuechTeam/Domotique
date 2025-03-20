package fr.domotique.api;

import fr.domotique.apidocs.*;
import fr.domotique.data.*;

@DocDesc("Complete information about a user: its public data and its secrets.")
public record CompleteUser(
    @DocDesc("The public data of the user.")
    UserProfile profile,

    @DocDesc("The confidential data of the user.")
    UserSecrets secret
) {
    public static CompleteUser fromUser(User u) {
        if (u == null) {return null;}
        return new CompleteUser(UserProfile.fromUser(u), UserSecrets.fromUser(u));
    }

    public static final CompleteUser EXAMPLE = new CompleteUser(UserProfile.EXAMPLE, UserSecrets.EXAMPLE);
}
