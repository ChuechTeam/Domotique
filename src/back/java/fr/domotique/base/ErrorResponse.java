package fr.domotique.base;

import fr.domotique.base.apidocs.*;
import org.jetbrains.annotations.*;

import java.util.*;

/// A response given by the API when something goes wrong. Takes the same data as [RequestException].
///
/// ## Example JSON
/// ```json
///{
///    "message": "Exception's message",
///    "code": "EXCEPTION_ERR_CODE", // or null if errorCode is null
///    "details": {...} // or null if details is null
///}
///```
@ApiDoc("A response given by the API when something goes wrong.")
public record ErrorResponse<T>(
    @ApiDoc("The error message. Is localized (in French) and can be shown to the client for all errors except 400.")
    String message,

    @ApiDoc("The error code. Can be used to identify the error in the client.")
    @Nullable String code,

    @ApiDoc("Additional details about the error. Can be really useful for validation errors most of the time.")
    @Nullable T data
) {
    public ErrorResponse(String message) {
        this(message, null, null);
    }

    public ErrorResponse(String message, @Nullable String code) {
        this(message, code, null);
    }

    static final ErrorResponse<?> EXAMPLE
        = new ErrorResponse<>("Something went really bad!", "ERROR_CODE", null);

    /// Returns an error response for validation errors. Used to show API examples
    ///
    /// ## Example
    /// ```java
    /// var error = ErrorResponse.validationError(Map.of(
    ///    "firstName", new String[]{"Le prénom ne peut pas être vide."},
    ///    "lastName", new String[]{"Le nom est trop long."}
    /// ));
    /// ```
    public static ErrorResponse<Map<String, Object>> validationError(Map<String, Object> details) {
        return new ErrorResponse<>("Les données entrées sont invalides.", "VALIDATION_ERROR", details);
    }
}
