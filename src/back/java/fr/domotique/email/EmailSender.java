package fr.domotique.email;

import io.vertx.core.*;

/// Sends emails to users.
public interface EmailSender {
    /// Sends an email to `recipientEmail` with the given `subject` and `body`.
    ///
    /// @param recipientEmail the email address of the recipient
    /// @param subject the subject of the email
    /// @param body the body of the email (in HTML format)
    Future<Void> send(String recipientEmail, String subject, String body);
}
