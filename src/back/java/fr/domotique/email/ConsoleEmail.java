package fr.domotique.email;

import io.vertx.core.*;
import org.slf4j.*;

import java.util.*;

/// An [EmailSender] that writes emails to the console.
///
/// Used temporarily while we don't have some cloud service that sends email.
public class ConsoleEmail implements EmailSender {
    private static final Logger log = LoggerFactory.getLogger(ConsoleEmail.class);

    @Override
    public Future<Void> send(String recipientEmail, String subject, String body) {
        Objects.requireNonNull(recipientEmail);
        Objects.requireNonNull(subject);
        Objects.requireNonNull(body);

        log.info("Email to {}: \nSubject: {}\nBody:\n{}", recipientEmail, subject, body);

        return Future.succeededFuture();
    }
}
