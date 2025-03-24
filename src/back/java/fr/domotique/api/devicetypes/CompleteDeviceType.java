package fr.domotique.api.devicetypes;

import fr.domotique.base.data.*;
import fr.domotique.data.*;

import java.util.*;

public record CompleteDeviceType(
    int id,
    String name,
    EnumSet<AttributeType> attributes
) {
    public static final Mapper<CompleteDeviceType> MAP = Mapper.of(
        3,
        (r, s) -> new CompleteDeviceType(
            r.getInteger(s.next()),
            r.getString(s.next()),
            DeviceType.attributesFromDB(r.getJsonArray(s.next()))
        )
    );

    public static CompleteDeviceType fromDeviceType(DeviceType deviceType) {
        if (deviceType == null) {
            return null;
        }
        return new CompleteDeviceType(
            deviceType.getId(),
            deviceType.getName(),
            deviceType.getAttributes()
        );
    }

    public static String columnList(String tableName) {
        return QueryUtils.columnList(tableName, "id", "name", "attributes");
    }
}
