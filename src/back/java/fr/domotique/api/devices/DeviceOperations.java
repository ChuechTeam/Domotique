package fr.domotique.api.devices;

import fr.domotique.data.*;

import java.util.*;

/// Contains common operations for devices.
public final class DeviceOperations {
    private DeviceOperations() {}

    /// Adapts the input attributes to match the device type:
    /// - Removes all attributes that are not in the device type
    /// - Adds default values for all attributes that are missing
    ///
    /// The `attributes` map will be updated.
    public static void fixAttributes(EnumMap<AttributeType, Object> attributes, DeviceType type) {
        // Remove all attributes that are NOT present in the device type
        attributes.keySet().removeIf(key -> !type.getAttributes().contains(key));

        // Add default values for all attributes that are missing
        for (AttributeType attrType : type.getAttributes()) {
            if (!attributes.containsKey(attrType)) {
                attributes.put(attrType, attrType.defaultValue());
            }
        }
    }
}
