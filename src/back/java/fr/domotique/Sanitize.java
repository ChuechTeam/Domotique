package fr.domotique;

/// Tools to sanitize strings and avoid abuse: stuff like having a lot of spaces
/// or funny people going crazy on emojis and invisible characters.
public final class Sanitize {
    private Sanitize() {
    }

    /// Sanitizes the string by removing leading and trailing whitespace.
    public static String string(String s) {
        if (s == null) {
            return null;
        }

        return s.trim();
    }
}
