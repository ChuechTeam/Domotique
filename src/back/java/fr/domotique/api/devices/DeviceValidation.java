package fr.domotique.api.devices;

import fr.domotique.base.*;
import fr.domotique.data.*;

import java.util.*;

/// Various validation functions for device data. Most functions expect sanitized data.
public final class DeviceValidation {
    private DeviceValidation() {}

    /// Validates the name of the device.
    public static void name(ValidationBlock block, String value) {
        Validation.lengthIn(block, "name", value, 1, 128,
            "Le nom est vide.",
            "Le nom est trop long.");
    }

    /// Validates the description of the device.
    public static void description(ValidationBlock block, String value) {
        if (value != null) {
            Validation.lengthIn(block, "description", value, 0, 16000,
                "La description est trop longue.");
        }
    }

    /// Validates the energy consumption of the device.
    public static void energyConsumption(ValidationBlock block, double value) {
        if (value < 0) {
            block.addError("energyConsumption", "La consommation d'énergie ne peut pas être négative.");
        }
    }

    /// Validates the attributes of the device against its device type.
    public static void attributes(ValidationBlock block, EnumMap<AttributeType, Object> attributes) {
        var attrBlock = block.child("attributes");
        for (var kv : attributes.entrySet()) {
            AttributeType key = kv.getKey();
            Object value = kv.getValue();

            if (!key.validate(value)) {
                // Note: dependant on JSON serialization choices
                attrBlock.addError(key.name(), "L'attribut « " + key.getName() + " » possède une valeur invalide.");
            }
        }
    }
}
