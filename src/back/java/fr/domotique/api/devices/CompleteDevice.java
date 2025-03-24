package fr.domotique.api.devices;

import fr.domotique.api.devicetypes.*;
import fr.domotique.api.rooms.*;
import fr.domotique.base.apidocs.*;
import fr.domotique.base.data.*;
import fr.domotique.data.*;
import org.jetbrains.annotations.*;

import java.util.*;

@ApiDoc("A device including its type, and the room its in.")
public record CompleteDevice(
    int id,
    String name,
    @Nullable @ApiDoc(optional = true) String description,
    EnumMap<AttributeType, Object> attributes,
    boolean powered,
    double energyConsumption,
    CompleteDeviceType type,
    CompleteRoom room
) {
    public static final Mapper<CompleteDevice> MAP = Mapper.of(
        6
        + CompleteDeviceType.MAP.getColumns()
        + CompleteRoom.MAP.getColumns(),
        (r, s) -> new CompleteDevice(
            r.getInteger(s.next()),
            r.getString(s.next()),
            r.getString(s.next()),
            Device.attributesFromDB(r.getJsonArray(s.next())),
            r.getBoolean(s.next()),
            r.getDouble(s.next()),
            CompleteDeviceType.MAP.apply(r, s),
            CompleteRoom.MAP.apply(r, s)
        )
    );

    public static String columnList(String tableName) {
        return QueryUtils.columnList(tableName, "id", "name", "description", "attributes", "powered", "energyConsumption");
    }
}
