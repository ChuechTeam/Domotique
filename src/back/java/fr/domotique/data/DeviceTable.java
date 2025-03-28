package fr.domotique.data;

import fr.domotique.api.devices.*;
import fr.domotique.api.devicetypes.*;
import fr.domotique.api.rooms.*;
import fr.domotique.api.users.*;
import fr.domotique.base.data.*;
import io.vertx.core.Future;
import io.vertx.sqlclient.*;
import org.jetbrains.annotations.*;
import org.slf4j.*;

import java.util.*;
import java.util.function.*;

import static fr.domotique.data.Device.ENTITY;

public class DeviceTable extends Table {
    private static final Logger log = LoggerFactory.getLogger(DeviceTable.class);

    public DeviceTable(SqlClient client) {
        super(client);
    }

    /// The foreign key to [DeviceType].
    public static final String TYPE_FK = "fk_device_type";
    /// The foreign key to [Room].
    public static final String ROOM_FK = "fk_device_room";
    /// The foreign key to [User].
    public static final String USER_FK = "fk_device_user";

    public Future<List<Device>> getAll() {
        return queryMany(ENTITY.mapper(), "SELECT * FROM Device");
    }

    public Future<Device> get(int id) {
        return querySingle(ENTITY.mapper(), "SELECT * FROM Device WHERE id = ?", id);
    }

    static final String COMPLETE_MANY_SQL = makeModularSQL("""
            SELECT %s, %s, %s, %s, %s
            FROM Device d
            INNER JOIN DeviceType dt ON d.typeId = dt.id
            LEFT JOIN Room r ON d.roomId = r.id
            LEFT JOIN User roomUser ON r.ownerId = roomUser.id
            LEFT JOIN User deviceUser ON d.userId = deviceUser.id""",
        CompleteDevice.columnList("d"),
        CompleteDeviceType.columnList("dt"),
        CompleteRoom.columnList("r"),
        UserProfile.columnList("roomUser"),
        UserProfile.columnList("deviceUser"));

    static final String COMPLETE_SINGLE_SQL = COMPLETE_MANY_SQL + " WHERE d.id = ?";

    public Future<@Nullable CompleteDevice> getComplete(int id) {
        return querySingle(CompleteDevice.MAP, COMPLETE_SINGLE_SQL, id);
    }

    public Future<List<CompleteDevice>> getCompleteAll() {
        return queryMany(CompleteDevice.MAP, COMPLETE_MANY_SQL);
    }

    public Future<List<CompleteDevice>> getCompleteAll(Collection<Integer> ids) {
        if (ids.isEmpty()) {
            return Future.succeededFuture(Collections.emptyList());
        }

        return queryMany(
            CompleteDevice.MAP,
            COMPLETE_MANY_SQL + " WHERE d.id IN " + paramList(ids.size()),
            ids.toArray()
        );
    }

    public record CompleteQuery(
        @Nullable String name,
        @Nullable Integer typeId,
        @Nullable Integer roomId,
        @Nullable Integer userId,
        @Nullable Boolean powered) {}

    public Future<List<CompleteDevice>> queryComplete(CompleteQuery completeQuery) {
        var sql = new StringBuilder(COMPLETE_MANY_SQL);
        var args = new ArrayList<>();

        sql.append("\nWHERE 1=1");

        if (completeQuery.name != null) {
            sql.append("\nAND INSTR(d.name, ?) > 0");
            args.add(completeQuery.name);
        }
        if (completeQuery.typeId != null) {
            sql.append("\nAND d.typeId = ?");
            args.add(completeQuery.typeId);
        }
        if (completeQuery.roomId != null) {
            sql.append("\nAND d.roomId = ?");
            args.add(completeQuery.roomId);
        }
        if (completeQuery.userId != null) {
            sql.append("\nAND r.userId = ?");
            args.add(completeQuery.userId);
        }
        if (completeQuery.powered != null) {
            sql.append("\nAND d.powered = ?");
            args.add(completeQuery.powered);
        }

        log.debug("Running search query {} with SQL\n{}", completeQuery, sql);

        return queryMany(CompleteDevice.MAP, sql.toString(), args.toArray());
    }

    public Future<List<DeviceStat>> queryStats(DeviceStatsQuery statsQuery) {
        // First, make sure that attribute type & function are compatible
        if (!statsQuery.validQuery()) {
            return Future.failedFuture("Invalid stats query");
        }

        var sql = new StringBuilder("SELECT ");
        var args = new ArrayList<>();

        var groupId = switch (statsQuery.grouping()) {
            case DEVICE -> "d.id";
            case DEVICE_TYPE -> "d.typeId";
            case ROOM -> "d.roomId";
            case USER -> "d.userId";
            case CATEGORY -> "d.category";
        };
        sql.append(groupId);

        sql.append(", ");
        sql.append(statsQuery.function().name());
        sql.append("(attrValues.value) AS val\n");

        var sqlAttrType = switch (statsQuery.attribute().getContent()) {
            case BOOLEAN -> "BOOLEAN";
            case STRING -> "VARCHAR(128)";
            case NUMBER -> "DOUBLE";
        };
        sql.append("""
            FROM Device d,
                 JSON_TABLE(attributes, '$[0][*]' COLUMNS (
                     idx FOR ORDINALITY,
                     value INT PATH '$'
                     )) AS attrKeys,
                 JSON_TABLE(attributes, '$[1][*]' COLUMNS (
                     idx FOR ORDINALITY,
                     value\s""");
        sql.append(sqlAttrType);
        sql.append(" PATH '$')) AS attrValues\n");

        sql.append("WHERE attrKeys.idx = attrValues.idx AND attrKeys.value = ?\n");
        args.add(statsQuery.attribute().ordinal());

        sql.append("GROUP BY ");
        sql.append(groupId);
        sql.append("\n");

        if (statsQuery.ascendingOrder()) {
            sql.append("ORDER BY val ASC");
        } else {
            sql.append("ORDER BY val DESC");
        }

        Function<Row, DeviceStat> mappingFunction;
        if (statsQuery.grouping() == DeviceStatsQuery.Grouping.CATEGORY) {
            mappingFunction = x -> new DeviceStat(DeviceCategory.fromByte(x.get(Byte.class, 0)), x.getDouble(1));
        } else { // something with an id
            mappingFunction = x -> new DeviceStat(x.getInteger(0), x.getDouble(1));
        }

        log.debug("Running stats query {} with SQL\n{}", statsQuery, sql);

        return queryMany(mappingFunction, sql.toString(), args.toArray());
    }

    /// Returns true when there's at least one device using this device type.
    public Future<Boolean> hasAnyWithDeviceType(int typeId) {
        return querySingle(x -> x.getBoolean(0), "SELECT EXISTS(SELECT 1 FROM Device WHERE typeId = ?)", typeId);
    }

    /// Returns true when there's at least one device in this room.
    public Future<Boolean> hasAnyWithRoom(int roomId) {
        return querySingle(x -> x.getBoolean(0), "SELECT EXISTS(SELECT 1 FROM Device WHERE roomId = ?)", roomId);
    }

    public Future<Device> insert(Device device) {
        return insert(ENTITY, device, Device::getId, Device::setId);
    }

    public Future<Device> update(Device device) {
        return update(ENTITY, device);
    }

    public Future<Boolean> delete(int id) {
        return delete(ENTITY, id);
    }
    // --- Special functions ---

    public Future<List<DeviceAndAttributes>> getAllAttribsOfDeviceType(int deviceTypeId) {
        return queryMany(DeviceAndAttributes::fromTuple, "SELECT id, attributes FROM Device WHERE typeId = ?", deviceTypeId);
    }

    public Future<Void> updateAttributesBatch(List<DeviceAndAttributes> updates) {
        return client.preparedQuery("""
                        UPDATE Device SET
                            attributes = ?
                        WHERE id = ?
                """)
            .executeBatch(updates.stream().map(DeviceAndAttributes::toTuple).toList())
            .andThen(res -> {
                if (res.succeeded()) {
                    var result = res.result();
                    while (result != null) {
                        if (result.rowCount() == 0) {
                            log.warn("Some devices were not updated by updateAttributesBatch");
                        }
                        result = result.next();
                    }
                } else {
                    log.error("Failed to update device attributes", res.cause());
                }
            })
            .mapEmpty();
    }

    public record DeviceAndAttributes(int deviceId, EnumMap<AttributeType, Object> attributes) {
        Tuple toTuple() {
            return Tuple.of(
                Device.attributesToDB(attributes),
                deviceId
            );
        }

        static DeviceAndAttributes fromTuple(Row row) {
            return new DeviceAndAttributes(
                row.getInteger(0),
                Device.attributesFromDB(row.getJsonArray(1))
            );
        }
    }
}
