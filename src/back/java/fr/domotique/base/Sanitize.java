package fr.domotique.base;

import org.jetbrains.annotations.*;

/// Tools to sanitize strings and avoid abuse: stuff like having a lot of spaces
/// or funny people going crazy on emojis and invisible characters.
public final class Sanitize {
    private Sanitize() {
    }

    /// Sanitizes the string by removing leading and trailing whitespace.
    ///
    /// Returns `null` if the string is null, or when the string is blank.
    public static @Nullable String string(@Nullable String s) {
        if (s == null) {
            return null;
        }

        s = s.trim();
        if (s.isEmpty()) {
            return null;
        } else {
            return s;
        }
    }
}
