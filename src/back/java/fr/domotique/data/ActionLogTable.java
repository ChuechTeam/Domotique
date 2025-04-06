package fr.domotique.data;

import fr.domotique.api.actionlogs.*;
import fr.domotique.api.users.*;
import fr.domotique.base.data.*;
import io.vertx.core.*;
import io.vertx.sqlclient.*;
import org.jetbrains.annotations.*;

import java.util.*;

import static fr.domotique.data.ActionLog.ENTITY;

public class ActionLogTable extends Table {
    public ActionLogTable(SqlClient client) {
        super(client);
    }

    public record Query(
        @Nullable Integer userId,
        @Nullable ActionLogOperation operation,
        @Nullable ActionLogTarget target,
        boolean desc
    ) {}

    static final String QUERY_BASE_SQL = makeModularSQL("""
        SELECT %s, (
            CASE a.targetType
                WHEN 0 THEN targetDevice.name
                WHEN 1 THEN CONCAT(CONCAT(targetUser.firstName, ' '), targetUser.lastName)
                WHEN 2 THEN targetRoom.name
                WHEN 3 THEN targetDeviceType.name
            END
        ) AS targetName, %s
        FROM ActionLog a
        LEFT JOIN User doerUser ON a.userId = doerUser.id
        LEFT JOIN User targetUser ON a.targetId = targetUser.id
        LEFT JOIN Device targetDevice ON a.targetId = targetDevice.id
        LEFT JOIN DeviceType targetDeviceType ON targetDevice.typeId = targetDeviceType.id
        LEFT JOIN Room targetRoom ON targetDevice.roomId = targetRoom.id
        WHERE 1=1
        """, CompleteActionLog.columnList("a"), UserProfile.columnList("doerUser"));

    public Future<List<CompleteActionLog>> query(Query q) {
        var sql = new StringBuilder(QUERY_BASE_SQL);
        var args = new ArrayList<>();

        if (q.userId != null) {
            sql.append(" AND a.userId = ?");
            args.add(q.userId);
        }
        if (q.operation != null) {
            sql.append(" AND a.operation = ?");
            args.add(q.operation.ordinal());
        }
        if (q.target != null) {
            sql.append(" AND a.targetType = ?");
            args.add(q.target.ordinal());
        }

        sql.append(" ORDER BY a.time ").append(q.desc ? "DESC" : "ASC");

        return queryMany(CompleteActionLog.MAP, sql.toString(), args.toArray());
    }

    public Future<ActionLog> get(int id) {
        return querySingle(ENTITY.mapper(), "SELECT * FROM ActionLog WHERE id = ?", id);
    }

    public Future<ActionLog> insert(ActionLog deviceType) {
        return insert(ENTITY, deviceType, ActionLog::getId, ActionLog::setId);
    }

    public Future<ActionLog> update(ActionLog deviceType) {
        return update(ENTITY, deviceType);
    }

    public Future<Boolean> delete(int id) {
        return delete(ENTITY, id);
    }
}
