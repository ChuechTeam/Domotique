package fr.domotique.base;

/// Contains functions to validate user values in a [ValidationBlock].
///
/// ## Example
///
/// ```java
/// ValidationBlock block = Validation.start();
///
/// Validation.email(block, "email", user.email, "The mail must be valid!");
/// Validation.nonBlank(block, "name", user.name, "The name must not be blank!");
///
/// // Will stop the request if there's at least one error
/// block.end();
///```
public final class Validation {
    // Forbid new Validation(), it's useless!
    private Validation() {
    }

    /// Creates a new root validation block.
    public static ValidationBlock start() {
        return new ValidationBlock();
    }

    /// Validates an email address.
    /// ## Example
    ///
    /// ```java
    /// Validation.email(block, "email", user.email, "The mail must be valid!");
    ///```
    public static void email(ValidationBlock block, String key, String value, String errMessage) {
        if (value == null) {
            block.addError(key, errMessage);
        } else if (!value.contains("@") || value.length() < 3) {
            block.addError(key, errMessage);
        }
    }

    /// Validates an email address with the default error message.
    /// ## Example
    ///
    /// ```java
    /// Validation.email(block, "email", user.email);
    ///```
    public static void email(ValidationBlock block, String key, String value) {
        email(block, key, value, "L'adresse e-mail est invalide.");
    }

    /// Ensures that a string is not blank (i.e. contains only spaces or null).
    ///
    /// ## Example
    ///
    /// ```java
    /// Validation.nonBlank(block, "name", user.name, "The name must not be blank!");
    /// ```
    public static void nonBlank(ValidationBlock block, String key, String value, String errMessage) {
        if (value == null || value.isBlank()) {
            block.addError(key, errMessage);
        }
    }

    /// Ensures that a string's length is between `min` and `max` (inclusive).
    ///
    /// ## Example
    ///
    /// ```java
    /// Validation.lengthIn(block, "name", user.name, 3, 20, "The name must be between 3 and 20 characters long!");
    /// ```
    public static void lengthIn(ValidationBlock block, String key, String value, int min, int max, String errMessage) {
        if (value == null || value.length() < min || value.length() > max) {
            block.addError(key, errMessage);
        }
    }
}
