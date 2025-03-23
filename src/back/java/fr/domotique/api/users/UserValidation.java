package fr.domotique.api.users;

import fr.domotique.base.*;

/// Various validation functions for user data. Most functions expect sanitized data.
public final class UserValidation {
    private UserValidation() {}

    /// Validates the first name of the user.
    public static void firstName(ValidationBlock block, String value) {
        Validation.lengthIn(block, "firstName", value, 0, 128,
            "Le prénom ne peut pas être vide.",
            "Le prénom est trop long.");
    }

    /// Validates the last name of the user.
    public static void lastName(ValidationBlock block, String value) {
        Validation.lengthIn(block, "lastName", value, 0, 128,
            "Le nom ne peut pas être vide.",
            "Le nom est trop long.");
    }

    /// Validates a new password, making it sure it is secure.
    public static void password(ValidationBlock block, String key, String value) {
        if (value == null) {
            block.addError(key, "Le mot de passe ne peut pas être vide.");
            return;
        }

        if (value.length() < 8) {
            block.addError(key, "Le mot de passe doit faire au moins 8 caractères.");
        }

        if (value.length() > 128) {
            block.addError(key, "Le mot de passe est trop long.");
        }
    }
}
