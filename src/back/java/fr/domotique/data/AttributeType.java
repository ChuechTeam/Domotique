package fr.domotique.data;

import fr.domotique.base.apidocs.*;
import lombok.*;

import java.util.function.*;

/// Type of device attribute
///
/// **WARNING:** Don't remove enum elements, mark them as deprecated instead!!
@Getter
@ApiDoc("A type of attribute for devices.")
public enum AttributeType {
    @ApiDoc("Calories burned during the entire lifetime of the device.")
    CALORIES_BURNED("Calories brûlées", Content.NUMBER, (Double x) -> x >= 0),

    @ApiDoc("The total amount of physical activity recorded by the device, during its entire lifetime, in minutes.")
    ACTIVITY_DURATION("Durée d'activité physique (minutes)", Content.NUMBER, (Double x) -> x >= 0),

    @ApiDoc("The total distance traveled by the device, during its entire lifetime, in meters.")
    TEMPERATURE("Température (°C)", Content.NUMBER),

    @ApiDoc("The percentage of humidity at this current point in time.")
    HUMIDITY("Humidité (%)", Content.NUMBER, (Double x) -> x >= 0 && x <= 100),

    @ApiDoc("The current beating rate of the heart, in beats per minute.")
    HEART_RATE("Fréquence cardiaque (bpm)", Content.NUMBER, true, (Double x) -> x >= 0),

    @ApiDoc("The current blood pressure of the user, in mmHg.")
    BLOOD_PRESSURE("Tension artérielle (mmHg)", Content.NUMBER, true, (Double x) -> x >= 0),

    @ApiDoc("The current blood oxygen level of the user, in percentage.")
    BLOOD_OXYGEN("Taux d'oxygène dans le sang (%)", Content.NUMBER, true, (Double x) -> x >= 0 && x <= 100),

    @ApiDoc("The current blood glucose level of the user, in mg/dL.")
    BLOOD_GLUCOSE("Taux de sucre dans le sang (mg/dL)", Content.NUMBER, true, (Double x) -> x >= 0),

    @ApiDoc("The amount of fat in the body, in percentage.")
    FAT_PERCENTAGE("Taux de graisse corporelle (%)", Content.NUMBER, true, (Double x) -> x >= 0 && x <= 100),

    @ApiDoc("The amount of steps done since the device was created.")
    STEPS("Nombre de pas", Content.NUMBER, (Double x) -> x >= 0),

    @ApiDoc("The amount of sleep done since the device was created, in minutes.")
    LAST_SLEEP_DURATION("Durée du dernier sommeil (minutes)", Content.NUMBER, true, (Double x) -> x >= 0),

    @ApiDoc("The maximum VO2 of the user, in mL/kg/min.")
    MAX_VO2("VO2 max (mL/kg/min)", Content.NUMBER, true, (Double x) -> x >= 0),

    @ApiDoc("The current respiratory rate of the user, in breaths per minute.")
    RESPIRATORY_RATE("Fréquence respiratoire (par minute)", Content.NUMBER, true, (Double x) -> x >= 0),

    @ApiDoc("The current body temperature of the user, in °C.")
    BODY_TEMPERATURE("Température corporelle (°C)", Content.NUMBER, true, (Double x) -> x >= 0),

    @ApiDoc("The current weight of the user, in kg.")
    BODY_WEIGHT("Poids (kg)", Content.NUMBER, true, (Double x) -> x >= 0),

    @ApiDoc("The amount of battery left in percent.")
    BATTERY_LEVEL("Batterie restante (%)", Content.NUMBER, (Double x) -> x >= 0 && x <= 100),

    @ApiDoc("The height of your body in centimeters.")
    BODY_HEIGHT("Taille (cm)", Content.NUMBER, true, (Double x) -> x >= 0);

    /// The displayed name of the attribute.
    public final String name;

    /// The value type of the attribute.
    public final Content content;

    /// True when this is personal health data that can only be seen by:
    /// - device's owner;
    /// - people with the [Role#CAREGIVER] role
    /// - people with a level of [Level#ADVANCED]
    ///
    /// If a device has no owner this has no effect.
    public final boolean personal;

    /// A function that validates the incoming values for this attribute.
    private final Predicate<?> validator;

    AttributeType(String name, Content content) {
        this.name = name;
        this.content = content;
        this.personal = false;
        this.validator = _ -> true;
    }

    AttributeType(String name, Content content, Predicate<?> validator) {
        this.name = name;
        this.content = content;
        this.personal = false;
        this.validator = validator;
    }

    AttributeType(String name, Content content, boolean personal) {
        this.name = name;
        this.content = content;
        this.personal = personal;
        this.validator = _ -> true;
    }

    AttributeType(String name, Content content, boolean personal, Predicate<?> validator) {
        this.name = name;
        this.content = content;
        this.personal = personal;
        this.validator = validator;
    }

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

        // Convert numbers to double for checking.
        Object v = value;
        if (content == Content.NUMBER) {
            v = ((Number) value).doubleValue();
        }
        return ((Predicate<Object>) validator).test(v);
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
