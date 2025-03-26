package fr.domotique.data;

import fr.domotique.base.data.*;
import lombok.*;

import java.time.*;

/// An occurrence of a user logging in to the application.
@Getter
@Setter
@AllArgsConstructor
public class LoginLog {
    /// The unique identifier of the login log.
    int id;
    /// The user ID of the user who logged in.
    int userId;
    /// The time at which the user logged in.
    Instant time;

    public static final EntityInfo<LoginLog> ENTITY = new EntityInfo<>(
        LoginLog.class,
        (r, s) -> new LoginLog(
            r.getInteger(s.next()),
            r.getInteger(s.next()),
            r.getLocalDateTime(s.next()).toInstant(ZoneOffset.UTC)
        ),
        new EntityColumn<>("id", LoginLog::getId, ColumnType.GENERATED_KEY),
        new EntityColumn<>("userId", LoginLog::getUserId),
        new EntityColumn<>("time", LoginLog::timeToDB)
    );

    private LocalDateTime timeToDB() {
        return LocalDateTime.ofInstant(time, ZoneOffset.UTC);
    }
}
