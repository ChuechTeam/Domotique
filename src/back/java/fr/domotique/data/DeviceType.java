package fr.domotique.data;

import fr.domotique.base.data.*;
import io.vertx.core.json.*;
import lombok.*;

import java.util.*;

/// A typical type of device. Stuff like Watch, Phone, Washing Machine, etc.
@Getter
@Setter
@AllArgsConstructor
public class DeviceType {
    /// The attributes that a device of this type can have
    int id;
    /// The name of the device type
    String name;
    /// The category assigned to this device type. Can be set to [DeviceCategory#OTHER] to tell that it has no category.
    DeviceCategory category;
    /// All attributes that this device can display.
    EnumSet<AttributeType> attributes;

    /// Information associating the SQL database table to the Java class
    public static final EntityInfo<DeviceType> ENTITY = new EntityInfo<>(
        DeviceType.class,
        (r, s) -> new DeviceType(
            r.getInteger(s.next()),
            r.getString(s.next()),
            DeviceCategory.fromByte(r.get(Byte.class, s.next())),
            attributesFromDB(r.getJsonArray(s.next()))
        ),
        new EntityColumn<>("id", DeviceType::getId, ColumnType.GENERATED_KEY),
        new EntityColumn<>("name", DeviceType::getName),
        new EntityColumn<>("category", DeviceType::getCategory),
        new EntityColumn<>("attributes", x -> attributesToDB(x.getAttributes()))
    );

    /// Converts the EnumSet into JSON for the database
    public static JsonArray attributesToDB(EnumSet<AttributeType> attributes) {
        // Make an array with integers (= ordinal of the enum)
        // It's Integer because boxing sucks :(
        var attrArray = new Integer[attributes.size()];

        // Put all AttributeType's ordinal into the array
        int i = 0;
        for (AttributeType attr : attributes) {
            attrArray[i++] = attr.ordinal();
        }

        // Make it a JSON array: [1, 2, 3, ...]
        return new JsonArray(Arrays.asList(attrArray));
    }

    /// Converts the JSON from the database into an EnumSet
    public static EnumSet<AttributeType> attributesFromDB(JsonArray ar) {
        // Extract ordinals from the JSON array
        var enums = new AttributeType[ar.size()];
        for (int i = 0; i < ar.size(); i++) {
            enums[i] = AttributeType.values()[ar.getInteger(i)];
        }
        if (enums.length != 0) {
            return EnumSet.copyOf(Arrays.asList(enums));
        } else {
            return EnumSet.noneOf(AttributeType.class);
        }
    }
}
