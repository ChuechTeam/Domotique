package fr.domotique.api.devices;

import fr.domotique.base.apidocs.*;
import fr.domotique.data.*;

public record DeviceStatsQuery(
    Grouping grouping,
    Function function,
    @ApiDoc("The attribute to apply the function to. Devices without that attribute are not taken into account.")
    AttributeType attribute,
    @ApiDoc("True when the results should be sorted in ascending order.")
    boolean ascendingOrder
) {
    @DocName("DeviceStatsQueryGrouping")
    @ApiDoc("The entity to group the results by.")
    public enum Grouping {
        @ApiDoc("Group by individual devices. Outputs the device ID.")
        DEVICE,
        @ApiDoc("Group by the type of the device. Outputs the device type.")
        DEVICE_TYPE,
        @ApiDoc("Group by the room the device is in. Outputs the room ID.")
        ROOM,
        @ApiDoc("Group by the user who owns the device. Outputs the user ID.")
        USER,
        @ApiDoc("Group by the category of the device. Outputs the category name.")
        CATEGORY
    }

    @DocName("DeviceStatsQueryFunction")
    @ApiDoc("The function to apply to the attribute values.")
    public enum Function {
        @ApiDoc("Counts the number of devices: `COUNT(*)` in SQL")
        COUNT,
        @ApiDoc("Sums the values of the attribute: `SUM(attribute)` in SQL")
        SUM,
        @ApiDoc("Averages the values of the attribute: `AVG(attribute)` in SQL")
        AVG,
        @ApiDoc("Finds the minimum value of the attribute: `MIN(attribute)` in SQL")
        MIN,
        @ApiDoc("Finds the maximum value of the attribute: `MAX(attribute)` in SQL")
        MAX
    }

    public boolean validQuery() {
        return switch (attribute.getContent()) {
            case BOOLEAN -> function == Function.COUNT || function == Function.SUM;
            case STRING -> function == Function.COUNT;
            case NUMBER -> true;
        };
    }

    static final DeviceStatsQuery EXAMPLE = new DeviceStatsQuery(
        Grouping.USER,
        Function.SUM,
        AttributeType.CALORIES_BURNED,
        false
    );
}
