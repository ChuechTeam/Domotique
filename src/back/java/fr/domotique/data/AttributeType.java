package fr.domotique.data;

import fr.domotique.base.apidocs.*;
import lombok.*;

/// Type of device attribute
///
/// **WARNING:** Don't remove enum elements, mark them as deprecated instead!!
@Getter
@AllArgsConstructor
@ApiDoc("A type of attribute for devices.")
public enum AttributeType {
    @ApiDoc("Calories burned during the entire lifetime of the device.")
    CALORIES_BURNED("Calories brûlées", Content.DOUBLE),

    @ApiDoc("The total amount of physical activity recorded by the device, during its entire lifetime, in minutes.")
    ACTIVITY_DURATION("Durée d'activité physique (minutes)", Content.DOUBLE),

    @ApiDoc("The total distance traveled by the device, during its entire lifetime, in meters.")
    TEMPERATURE("Température (°C)", Content.DOUBLE),

    @ApiDoc("The percentage of humidity at this current point in time.")
    HUMIDITY("Humidité (%)", Content.DOUBLE);

    /// The displayed name of the attribute.
    final String name;

    /// The value type of the attribute.
    final Content content;

    /// Returns the default value this attribute will be initialized with.
    public Object defaultValue() {
        return switch (content) {
            case BOOLEAN -> false;
            case DOUBLE -> 0.0;
            case STRING -> "";
        };
    }

    /// Converts a byte value to a [AttributeType] enum.
    public static AttributeType fromByte(byte b) {
        return values()[b];
    }

    /// The type of value this attribute can hold.
    enum Content {
        BOOLEAN,
        DOUBLE,
        STRING
    }
}
