package fr.domotique.base;

/// Contains functions to validate user values in a [ValidationBlock].
///
/// ## Example (simple)
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
///
/// ## Example (auto-end)
///
/// ```java
/// try (ValidationBlock block = Validation.start()){
///     Validation.email(block, "email", user.email);
///     Validation.nonBlank(block, "password", user.password, "Password cannot be empty");
///
///     // Custom validation logic
///     if (user.age < 18){
///         block.addError("age", "User must be at least 18 years old");
///}
///
///     // Will automatically call block.end() when exiting the try block
///}
///```
///
/// ## Example (child block)
///
/// ```java
/// try (ValidationBlock parentBlock = Validation.start()){
///     // Validate user basic info
///     Validation.nonBlank(parentBlock, "username", user.username);
///
///     // Create a child block for address validation
///     ValidationBlock addressBlock = parentBlock.createChild("address");
///     Validation.nonBlank(addressBlock, "street", user.address.street);
///     Validation.nonBlank(addressBlock, "city", user.address.city);
///
///     // Parent block's end() will be called automatically
///}
///```
///
/// ## Example (child array)
///
/// ```java
/// ValidationBlock block = Validation.start();
///
/// List<User> users = getUserList();
/// block.childArray("users", users,(user, b) -> {
///     Validation.nonBlank(b, "name", user.getName(), "Name is required");
///     Validation.email(b, "email", user.getEmail(), "Valid email is required");
///     if (user.getAge() < 18){
///         b.addError("age", "Must be at least 18 years old");
///}
///});
///
/// block.end();
///```
///
/// The output will contain validation errors for each user in the array:
///
/// ```json
///{
///     "users": [
///{
///             "name": ["Name is required"]
///},
///{
///             "email": ["Valid email is required"],
///             "age": ["Must be at least 18 years old"]
///}
///]
///}
///```
///
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
    ///```
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
    ///```
    public static void lengthIn(ValidationBlock block, String key, String value, int min, int max, String errMessage) {
        if (value == null || value.length() < min || value.length() > max) {
            block.addError(key, errMessage);
        }
    }

    /// Ensures that a string's length is between `min` and `max` (inclusive), with different error messages when:
    /// - the message is too short
    /// - the message is too long
    ///
    /// ## Example
    ///
    /// ```java
    /// Validation.lengthIn(block, "name", user.name, 3, 20,
    ///     "The name is too short!",
    ///     "The name is too long!");
    ///```
    public static void lengthIn(ValidationBlock block, String key, String value, int min, int max,
                                String shortMsg,
                                String longMsg) {
        if (value == null || value.length() < min) {
            block.addError(key, shortMsg);
        } else if (value.length() > max) {
            block.addError(key, longMsg);
        }
    }
}
