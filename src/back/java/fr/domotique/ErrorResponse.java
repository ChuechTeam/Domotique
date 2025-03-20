package fr.domotique;

import fr.domotique.apidocs.*;
import org.jetbrains.annotations.*;

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
public record ErrorResponse<T>(
    @DocDesc("The error message. Is localized (in French) and can be shown to the client for all errors except 400.")
    String message,

    @DocDesc("The error code. Can be used to identify the error in the client.")
    @Nullable String code,

    @DocDesc("Additional details about the error. Can be really useful for validation errors most of the time.")
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
}
