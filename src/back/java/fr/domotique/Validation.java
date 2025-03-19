package fr.domotique;

/// Contains functions that validate user values.
///
/// All functions end the request with a 422/400 status code if the value is invalid.
///
/// TODO: Extend this class to accumulate validation errors, with an API like .throw() or .into(x).
///
/// ## Example
///
/// ```java
/// // Example API handler method that requires authentication
/// private Future<Device> getDeviceByName(RoutingContext context) {
///     // Get the name in the URL: /api/devices?name=...
///     String name = context.queryParam("name");
///
///     Validation.nonBlank(name, "The name must not be blank!");
///
///     // Do other stuff...
/// }
/// ```
public final class Validation {
    // Forbid new Validation(), it's useless!
    private Validation() {
    }

    /// Validates an email address. Ends the request when the email is invalid.
    /// ## Example
    ///
    /// ```java
    /// // Example API handler method that requires authentication
    /// private Future<Void> changeUserMail(RoutingContext context) {
    ///     // Get the name in the body of the request.
    ///     String newMail = context.body().asString();
    ///
    ///     Validation.email(newMail, "The mail must be valid!");
    ///
    ///     // Do other stuff...
    /// }
    /// ```
    public static void email(String value, String errMessage) {
        if (value == null) {
            throw new RequestException(errMessage, 422);
        }
        if (!value.contains("@") || value.length() < 3) {
            throw new RequestException(errMessage, 422);
        }
    }

    /// Ensures that a string is not blank (i.e. contains only spaces or null).
    ///
    /// Ends the request with a 422 status code if the string is blank.
    ///
    /// ## Example
    ///
    /// ```java
    /// // Example API handler method that requires authentication
    /// private Future<Device> getDeviceByName(RoutingContext context) {
    ///     // Get the name in the URL: /api/devices?name=...
    ///     String name = context.queryParam("name");
    ///
    ///     Validation.nonBlank(name, "The name must not be blank!");
    ///
    ///     // Do other stuff...
    /// }
    /// ```
    ///
    /// @see String#isBlank()
    public static void nonBlank(String value, String errMessage) {
        if (value == null || value.isBlank()) {
            throw new RequestException(errMessage, 422);
        }
    }
}
