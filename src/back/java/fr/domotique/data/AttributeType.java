package fr.domotique.data;

import fr.domotique.base.apidocs.*;
import lombok.*;

import java.util.function.*;

/// Type of device attribute
///
/// **WARNING:** Don't remove enum elements, mark them as deprecated instead!!
@Getter
@AllArgsConstructor
@ApiDoc("A type of attribute for devices.")
public enum AttributeType {
    @ApiDoc("Calories burned during the entire lifetime of the device.")
    CALORIES_BURNED("Calories brûlées", Content.NUMBER, (Number x) -> x.doubleValue() >= 0),

    @ApiDoc("The total amount of physical activity recorded by the device, during its entire lifetime, in minutes.")
    ACTIVITY_DURATION("Durée d'activité physique (minutes)", Content.NUMBER, (Number x) -> x.doubleValue() >= 0),

    @ApiDoc("The total distance traveled by the device, during its entire lifetime, in meters.")
    TEMPERATURE("Température (°C)", Content.NUMBER, _ -> true),

    @ApiDoc("The percentage of humidity at this current point in time.")
    HUMIDITY("Humidité (%)", Content.NUMBER, (Number x) -> x.doubleValue() >= 0 && x.doubleValue() <= 100);

    /// The displayed name of the attribute.
    public final String name;

    /// The value type of the attribute.
    public final Content content;

    /// A function that validates the incoming values for this attribute.
    private final Predicate<?> validator;

    /// Returns the default value this attribute will be initialized with.
    public Object defaultValue() {
        // TODO: What if 0.0 is an invalid value?
        return switch (content) {
            case BOOLEAN -> false;
            case NUMBER -> 0.0;
            case STRING -> "";
        };
    }

    /// Validates the value of this attribute. Returns `true` when the value is considered valid.
    @SuppressWarnings("unchecked")
    public final boolean validate(Object value) {
        boolean ofRightType = switch (content) {
            case BOOLEAN -> value instanceof Boolean;
            case NUMBER -> value instanceof Number;
            case STRING -> value instanceof String;
        };

        if (!ofRightType) {
            return false;
        }

        return ((Predicate<Object>) validator).test(value);
    }

    /// Converts a byte value to a [AttributeType] enum.
    public static AttributeType fromByte(byte b) {
        return values()[b];
    }

    /// The type of value this attribute can hold.
    public enum Content {
        /// A simple boolean
        BOOLEAN,
        /// Either integer or double.
        NUMBER,
        /// A string which may be empty to indicate no value.
        STRING
    }
}
