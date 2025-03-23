package fr.domotique.base.data;

/// Thrown when a row couldn't be inserted as it breaks a FOREIGN KEY SQL constraint.
public class ForeignException extends RuntimeException {
    public ForeignException(String message) {
        super(message);
    }

    public ForeignException(String message, Throwable cause) {
        super(message, cause);
    }

    public ForeignException(Throwable cause) {
        super(cause);
    }
}
