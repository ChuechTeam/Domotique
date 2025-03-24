package fr.domotique.data;

import fr.domotique.base.data.*;
import io.vertx.core.json.*;
import lombok.*;
import org.jetbrains.annotations.*;

import java.util.*;

/// A device used within the EHPAD.
@Getter
@Setter
@AllArgsConstructor
public class Device {
    /// The identifiant for this device
    int id;

    /// The name given to this device.
    /// **MAX LENGTH:** 128
    String name;

    /// The description of this device. Can be null, but not an empty string.
    /// **MAX LENGTH:** 16000
    @Nullable String description;

    /// The [DeviceType] of this device. Required!
    int typeId;

    /// The [Room] in which this device is located. Required!
    int roomId;

    /// Attributes assigned to this device
    ///
    /// ## JSON storage
    /// This one is a bit weird. See, JSON doesn't allow integer to index objects... So, instead, we use two arrays:
    /// - One for the keys, as enum [ordinals][Enum#ordinal()] ([AttributeType])
    /// - One for the values ([Object])
    ///
    /// ### Example
    ///
    /// ```json
    /// [
    ///    [1, 5], // keys
    ///    ["pouet", true]  // values
    /// ]
    /// ```
    EnumMap<AttributeType, Object> attributes;

    /// True if the device is powered on, false otherwise
    boolean powered;

    /// Energy consumption in Watts of this device while powered on
    double energyConsumption;

    /// Information associating the SQL database table to the Java class
    public static final EntityInfo<Device> ENTITY = new EntityInfo<>(
        Device.class,
        (r, s) -> new Device(
            r.getInteger(s.next()),
            r.getString(s.next()),
            r.getString(s.next()),
            r.getInteger(s.next()),
            r.getInteger(s.next()),
            attributesFromDB(r.getJsonArray(s.next())),
            r.getBoolean(s.next()),
            r.getDouble(s.next())
        ),
        new EntityColumn<>("id", Device::getId, ColumnType.GENERATED_KEY),
        new EntityColumn<>("name", Device::getName),
        new EntityColumn<>("description", Device::getDescription),
        new EntityColumn<>("typeId", Device::getTypeId),
        new EntityColumn<>("roomId", Device::getRoomId),
        new EntityColumn<>("attributes", Device::attributesToDB),
        new EntityColumn<>("powered", Device::isPowered),
        new EntityColumn<>("energyConsumption", Device::getEnergyConsumption)
    );

    private JsonArray attributesToDB() {
        // Convert the enum keys to integers (using ordinal)
        ArrayList<Integer> keys = new ArrayList<>(attributes.size());
        for (var key : attributes.keySet()) {
            keys.add(key.ordinal());
        }

        // We can leave this as-is, String, Double and Boolean are all supported.
        ArrayList<Object> values = new ArrayList<>(attributes.values());

        // Return the two arrays as a JSON array
        return new JsonArray(List.of(new JsonArray(keys), new JsonArray(values)));
    }

    public static EnumMap<AttributeType, Object> attributesFromDB(JsonArray array) {
        // Find the array pair from the JSON
        // [
        //     [1, 5], // keys
        //     ["pouet", true]  // values
        // ]
        JsonArray keys = array.getJsonArray(0);
        JsonArray values = array.getJsonArray(1);

        if (keys.size() != values.size()) {
            throw new IllegalArgumentException("Keys and values must have the same size!");
        }

        // Take one element from each array and put them into a map
        EnumMap<AttributeType, Object> map = new EnumMap<>(AttributeType.class);
        for (int i = 0; i < keys.size(); i++) {
            // Take the ordinal from the keys array and convert it to an enum
            Integer keyOrdinal = keys.getInteger(i);
            AttributeType key = AttributeType.values()[keyOrdinal];

            // Take the value from the values array
            Object val = values.getValue(i);

            // Put it into the map!
            map.put(key, val);
        }

        return map;
    }
}
