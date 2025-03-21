package fr.domotique.base;

/// Thrown when an error occurred during the request, customizing the error response.
///
/// ## Error response format
/// This exception stores helpful information for a useful HTTP error response:
/// - the [status code][#getStatusCode()] (4xx usually);
/// - the [error code][#getErrorCode()] (optional), a string to identify the error programmatically;
/// - the [details][#getDetails()] (optional), an object with anything useful.
///
/// This exception has a special treament from the [fr.domotique.RouterVerticle],
/// which is going to read those properties to generate a proper HTTP response, in the following JSON format:
/// ```json
/// {
///     "message": "Exception's message",
///     "code": "EXCEPTION_ERR_CODE", // or null if errorCode is null
///     "details": {...} // or null if details is null
/// }
/// ```
///
/// ## Typical scenarios to use this exception
///  - **when receiving an invalid request** (missing parameters, invalid types, etc.)
///  - **when being in an incorrect state** (trying to delete a non-existing resource)
///
/// ## Example
///
/// ```java
/// private Future<Device> findDeviceByName(RoutingContext context) {
///     String deviceName = context.pathParam("deviceName");
///
///     if (deviceName == null || deviceName.isBlank()) {
///        throw new RequestException("Device name is invalid!", 400);
///     }
///
///     return deviceStore.findByName(deviceName);
/// }
/// ```
///
/// ## Performance considerations
///
/// This exception does NOT record the stack trace; this gives a gigantic performance boost over
/// plain exceptions, as we don't need that information.
///
/// Using this exception with [io.vertx.core.Future#failedFuture(java.lang.Throwable)] is preferred,
/// but you can also `throw` it, with an acceptable performance cost for most cases.
///
/// @author Dynamic
/// @see fr.domotique.RouterVerticle
public class RequestException extends RuntimeException {
    private final int statusCode;
    private final String errorCode; // Nullable!
    private final Object details; // Nullable!

    /// Makes a new exception with the given message, status code, error code, and details.
    /// @param message the exception message
    /// @param statusCode the HTTP status code
    /// @param errorCode the error code (optional; can be null)
    /// @param details the details object (optional; can be null)
    public RequestException(String message, int statusCode, String errorCode, Object details) {
        super(message, null, false, false);
        this.statusCode = statusCode;
        this.details = details;
        this.errorCode = errorCode;
    }

    /// Makes a new exception with the given message, with a default status code of 400. No error code, no details.
    /// @param message the exception message
    public RequestException(String message) {
        this(message, 400, null, null);
    }

    /// Makes a new exception with the given message and status code. No error code, no details.
    /// @param message the exception message
    /// @param statusCode the HTTP status code
    public RequestException(String message, int statusCode) {
        this(message, statusCode, null, null);
    }

    /// Makes a new exception with the given message, status code, and error code. No details.
    /// @param message the exception message
    /// @param statusCode the HTTP status code
    /// @param errorCode the error code
    public RequestException(String message, int statusCode, String errorCode) {
        this(message, statusCode, errorCode, null);
    }

    /// Makes a new exception with the given message, status code, and details. No error code.
    /// @param message the exception message
    /// @param statusCode the HTTP status code
    /// @param details the details object
    public RequestException(String message, int statusCode, Object details) {
        this(message, statusCode, null, details);
    }

    /// Returns the HTTP status code, to be put on the response.
    public int getStatusCode() {
        return statusCode;
    }

    /// Returns the programmer-friendly error code. Can be null.
    public String getErrorCode() {
        return errorCode;
    }

    /// Returns the details objects, added to the response. Can be null.
    public Object getDetails() {
        return details;
    }
}
