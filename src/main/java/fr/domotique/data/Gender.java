package fr.domotique.data;

/**
 * The gender of a user.
 * <p>
 * Has a third (!) value {@link #UNDISCLOSED} in case the user would rather not communicate that information.
 *
 * @author Dynamic
 */
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
    UNDISCLOSED
}
