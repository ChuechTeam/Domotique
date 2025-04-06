package fr.domotique.api.actionlogs;

import fr.domotique.api.users.*;
import fr.domotique.base.data.*;
import fr.domotique.data.*;
import org.jetbrains.annotations.*;

import java.time.*;
import java.util.*;

public record CompleteActionLog(
    int id,
    int targetId,
    ActionLogTarget targetType,
    ActionLogOperation operation,
    EnumSet<ActionLogFlags> flags,
    Instant time,
    String targetName,
    @Nullable UserProfile user
) {
    public static String columnList(String tableName) {
        return QueryUtils.columnList(tableName, "id", "targetId", "targetType", "operation", "flags", "time");
    }

    public static final Mapper<CompleteActionLog> MAP = Mapper.of(7 + UserProfile.MAP.getColumns(), (r, s) -> new CompleteActionLog(
        r.getInteger(s.next()),
        r.getInteger(s.next()),
        ActionLogTarget.values()[r.getInteger(s.next())],
        ActionLogOperation.values()[r.getInteger(s.next())],
        ActionLogFlags.fromLong(r.getLong(s.next())),
        r.getLocalDateTime(s.next()).toInstant(ZoneOffset.UTC),
        r.getString(s.next()),
        UserProfile.MAP.apply(r, s)
    ));
}
