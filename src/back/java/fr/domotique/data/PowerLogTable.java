package fr.domotique.data;

import fr.domotique.base.data.*;
import io.vertx.core.*;
import io.vertx.sqlclient.*;

import java.time.*;
import java.util.*;

import static fr.domotique.data.PowerLog.ENTITY;

public class PowerLogTable extends Table {

    public PowerLogTable(SqlClient client) {
        super(client);
    }

    public Future<List<PowerLog>> getAllSorted() {
        return queryMany(ENTITY.mapper(), "SELECT * FROM PowerLog ORDER BY time DESC");
    }

    public Future<PowerLog> get(int id) {
        return querySingle(ENTITY.mapper(), "SELECT * FROM PowerLog WHERE id = ?", id);
    }

    public Future<List<PowerLog>> queryForDeviceSorted(int deviceId) {
        return queryMany(ENTITY.mapper(), "SELECT * FROM PowerLog WHERE deviceId = ? ORDER BY time DESC", deviceId);
    }

    public Future<List<Consumption>> queryTotalConsumption(Instant start, Instant end) {
        // Argument list:
        // 1. startTime: Instant
        // 2. startTime: Instant
        // 3. endTime: Instant
        // 4. endTime: Instant
        // 5. endTime: Instant
        LocalDateTime startLocal = LocalDateTime.ofInstant(start, ZoneOffset.UTC);
        LocalDateTime endLocal = LocalDateTime.ofInstant(end, ZoneOffset.UTC);
        return queryMany(x -> new Consumption(x.getInteger(0), x.getDouble(1)),
            """
                SELECT c.deviceId, SUM(c.consumption)
                FROM (WITH FirstRelevantLogs AS (SELECT p.deviceId, MAX(p.time) AS time
                                                 FROM PowerLog p
                                                 WHERE p.time <= ?
                                                 GROUP BY p.deviceId)
                      SELECT p.deviceId, IF(p.status = 'POWER_ON', TIMESTAMPDIFF(SECOND,
                                                                     GREATEST(p.time, ?),
                                                                     LEAST(COALESCE(LEAD(p.time, 1) OVER w, ?), ?))
                                                           * p.energyConsumption / 3600, 0) AS consumption
                      FROM PowerLog p
                      LEFT JOIN FirstRelevantLogs firstRel ON p.deviceId = firstRel.deviceId
                      WHERE ((firstRel.time IS NULL or p.time >= firstRel.time) AND p.time < ?)
                      WINDOW w AS (PARTITION BY p.deviceId ORDER BY p.time)) as c
                GROUP BY c.deviceId;
                """,
            startLocal, startLocal, endLocal, endLocal, endLocal);

    }

    public record Consumption(int deviceId, double consumption) {}

    public Future<PowerLog> insert(PowerLog log) {
        return insert(ENTITY, log, PowerLog::getId, PowerLog::setId);
    }

    public Future<PowerLog> update(PowerLog log) {
        return update(ENTITY, log);
    }

    public Future<Boolean> delete(int id) {
        return delete(ENTITY, id);
    }
}