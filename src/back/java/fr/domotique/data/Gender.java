package fr.domotique.data;

import fr.domotique.base.apidocs.*;

/**
 * The gender of a user.
 * <p>
 * Has a third (!) value {@link #UNDISCLOSED} in case the user would rather not communicate that information.
 *
 * @author Dynamic
 */
@ApiDoc("The gender of a user, with a third UNDISCLOSED option for unknown genders.")
public enum Gender {
    /**
     * The male gender.
     */
    MALE,
    /**
     * The female gender.
     */
    FEMALE,
    /**
     * The user prefers not to tell us their gender. Why? Who knows.
     */
    UNDISCLOSED;

    /// Converts a byte value to a Gender enum.
    public static Gender fromByte(byte b) {
        return values()[b];
    }
}
