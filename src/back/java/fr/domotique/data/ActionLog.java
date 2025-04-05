package fr.domotique.data;

import fr.domotique.base.data.*;
import lombok.*;
import org.jetbrains.annotations.*;

import java.time.*;
import java.util.*;

@Getter
@Setter
@AllArgsConstructor
public class ActionLog {
    int id;
    @Nullable Integer userId;
    /// Can reference inexistant identifiers in table described by targetType. Zero for conceptual targets.
    int targetId;
    ActionLogTarget targetType; // stored as TINYINT
    ActionLogOperation operation; // stored as TINYINT
    EnumSet<ActionLogFlags> flags; // stored as BIGINT
    Instant time;

    public ActionLog(int userId, int targetId, ActionLogTarget targetType, ActionLogOperation operation) {
        this(0, userId, targetId, targetType, operation, EnumSet.noneOf(ActionLogFlags.class), Instant.now());
    }

    public ActionLog(int userId, int targetId, ActionLogTarget targetType, ActionLogOperation operation, EnumSet<ActionLogFlags> flags) {
        this(0, userId, targetId, targetType, operation, flags, Instant.now());
    }

    public static final EntityInfo<ActionLog> ENTITY = new EntityInfo<>(
        ActionLog.class,
        (r, s) -> new ActionLog(
            r.getInteger(s.next()),
            r.getInteger(s.next()),
            r.getInteger(s.next()),
            ActionLogTarget.values()[(r.get(Byte.class, s.next()))],
            ActionLogOperation.values()[(r.get(Byte.class, s.next()))],
            ActionLogFlags.fromLong(r.getLong(s.next())),
            r.getLocalDateTime(s.next()).toInstant(ZoneOffset.UTC)
        ),
        new EntityColumn<>("id", ActionLog::getId, ColumnType.GENERATED_KEY),
        new EntityColumn<>("userId", ActionLog::getUserId),
        new EntityColumn<>("targetId", ActionLog::getTargetId),
        new EntityColumn<>("targetType", ActionLog::getTargetType),
        new EntityColumn<>("operation", ActionLog::getOperation),
        new EntityColumn<>("flags", x -> ActionLogFlags.toLong(x.getFlags())),
        new EntityColumn<>("time", ActionLog::timeToDB)
    );

    private LocalDateTime timeToDB() {
        return LocalDateTime.ofInstant(time, ZoneOffset.UTC);
    }
}

