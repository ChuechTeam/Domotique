package fr.domotique.base.data;

/// Thrown when a row couldn't be inserted as it breaks a UNIQUE SQL constraint.
///
/// The message should be the same as the MySQL error.
public class DuplicateException extends RuntimeException {
    public DuplicateException(String message) {
        super(message);
    }

    public DuplicateException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateException(Throwable cause) {
        super(cause);
    }
}
