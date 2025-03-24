package fr.domotique.data;

import fr.domotique.api.devices.*;
import fr.domotique.api.devicetypes.*;
import fr.domotique.api.rooms.*;
import fr.domotique.api.users.*;
import fr.domotique.base.data.*;
import io.vertx.core.*;
import io.vertx.sqlclient.*;
import org.jetbrains.annotations.*;
import org.slf4j.*;

import java.util.*;

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

    public Future<List<Device>> getAll() {
        return queryMany(ENTITY.mapper(), "SELECT * FROM Device");
    }

    public Future<Device> get(int id) {
        return querySingle(ENTITY.mapper(), "SELECT * FROM Device WHERE id = ?", id);
    }

    static final String COMPLETE_MANY_SQL = makeModularSQL("""
            SELECT %s, %s, %s, %s
            FROM Device d
            INNER JOIN DeviceType dt ON d.typeId = dt.id
            INNER JOIN Room r ON d.roomId = r.id
            LEFT JOIN User u ON r.ownerId = u.id""",
        CompleteDevice.columnList("d"),
        CompleteDeviceType.columnList("dt"),
        CompleteRoom.columnList("r"),
        UserProfile.columnList("u"));

    static final String COMPLETE_SINGLE_SQL = COMPLETE_MANY_SQL + " WHERE d.id = ?";

    public Future<@Nullable CompleteDevice> getComplete(int id) {
        return querySingle(CompleteDevice.MAP, COMPLETE_SINGLE_SQL, id);
    }

    public Future<List<CompleteDevice>> getCompleteAll() {
        return queryMany(CompleteDevice.MAP, COMPLETE_MANY_SQL);
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
}
