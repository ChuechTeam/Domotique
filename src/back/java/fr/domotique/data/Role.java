package fr.domotique.data;

import fr.domotique.base.apidocs.*;

@ApiDoc("The user's role in the EHPAD")
public enum Role {
    /// The user is a resident in the EHPAD
    RESIDENT,
    /// The user is a caregiver in the EHPAD
    CAREGIVER,
    /// The user is an admin in the EHPAD
    ADMIN;

    /// Converts a byte value to a Role enum.
    public static Role fromByte(byte b) {
        return values()[b];
    }
}
