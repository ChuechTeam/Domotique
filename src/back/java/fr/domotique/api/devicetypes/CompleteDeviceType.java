package fr.domotique.api.devicetypes;

import fr.domotique.base.apidocs.*;
import fr.domotique.base.data.*;
import fr.domotique.data.*;

import java.util.*;

@ApiDoc("A device type with all its attributes.")
public record CompleteDeviceType(
    @ApiDoc("The unique identifier of the device type.")
    int id,
    @ApiDoc("The name of the device type.")
    String name,
    @ApiDoc("The category of the device type.")
    DeviceCategory category,
    @ApiDoc("All attributes that devices of this type will have.")
    EnumSet<AttributeType> attributes
) {
    public static final Mapper<CompleteDeviceType> MAP = Mapper.of(
        4,
        (r, s) -> new CompleteDeviceType(
            r.getInteger(s.next()),
            r.getString(s.next()),
            DeviceCategory.fromByte(r.get(Byte.class, s.next())),
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
            deviceType.getCategory(),
            deviceType.getAttributes()
        );
    }

    public static String columnList(String tableName) {
        return QueryUtils.columnList(tableName, "id", "name", "category", "attributes");
    }
}
