package fr.domotique.email;

import io.vertx.core.*;
import io.vertx.core.http.*;
import io.vertx.core.json.*;
import org.slf4j.*;

/// Sends email using the SendGrid mail API.
public class SendGridEmail implements EmailSender {
    private static final Logger log = LoggerFactory.getLogger(SendGridEmail.class);
    private final HttpClientAgent httpClient;
    private final String apiKey;
    private final Email senderEmail;

    public SendGridEmail(Vertx vertx, String apiKey, String senderEmail) {
        this.httpClient = vertx.createHttpClient();
        this.apiKey = apiKey;
        this.senderEmail = new Email(senderEmail);
    }

    // Many records to create JSON requests properly
    record Request(Email from, Personalization[] personalizations, String subject, Content[] content) {}
    record Content(String type, String value) {}
    record Personalization(Email[] to) {}
    record Email(String email) {}

    @Override
    public Future<Void> send(String recipientEmail, String subject, String body) {
        // Do the request manually. We do this because SendGrid's provided Java library is terrible as it
        // doesn't support JPMS in big 2025, and relies on its custom REST client which does NOT use Vert.x,
        // so that's a double no-no.
        var req = new Request(
            senderEmail,
            new Personalization[] {
                new Personalization(new Email[] { new Email(recipientEmail) })
            },
            subject,
            new Content[] {new Content("text/html", body)}
        );
        var json = Json.encode(req);
        log.debug("Sending JSON to the SendGrid API: {}", json);

        // TODO: better error handling instead of weak "expecting"
        return httpClient.request(new RequestOptions()
            .setMethod(HttpMethod.POST)
            .setAbsoluteURI("https://api.sendgrid.com/v3/mail/send")
            .addHeader("Authorization", "Bearer " + apiKey)
            .addHeader("Content-Type", "application/json"))
            .compose(r -> r.send(json))
            .expecting(HttpResponseExpectation.status(200, 300))
            .andThen(x -> {
                if (x.succeeded()) {
                    log.info("Email sent to {}: '{}'", recipientEmail, subject);
                } else {
                    log.error("Failed to send email to {}", recipientEmail, x.cause());
                }
            })
            .mapEmpty();
    }
}
