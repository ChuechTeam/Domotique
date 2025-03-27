package fr.domotique.data;

/// The category of a device.
public enum DeviceCategory {
    /// Anything else.
    OTHER,
    /// Lamps, bulbs, etc.
    LIGHTING,
    /// Fridges, ovens, microwaves, etc.
    KITCHEN,
    /// Cameras, alarms, locks, etc.
    SECURITY,
    /// Air conditioners, heaters, fans, thermometers, etc.
    TEMPERATURE_REGULATION,
    /// Exercise bikes, treadmills, etc.
    SPORT,
    /// Smartwatches, fitness trackers, etc.
    HEALTH,
    /// Automatic sprinklers, irrigation systems, etc.
    GARDENING;

    public static DeviceCategory fromByte(byte b) {
        return values()[b];
    }
}
