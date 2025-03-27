package fr.domotique.api.devices;

import fr.domotique.base.apidocs.*;
import fr.domotique.data.*;

public record DeviceStatsQuery(
    Grouping grouping,
    Function function,
    AttributeType attribute,
    boolean ascendingOrder
) {
    @DocName("DeviceStatsQueryGrouping")
    public enum Grouping {
        DEVICE,
        DEVICE_TYPE,
        ROOM,
        USER,
        CATEGORY
    }

    @DocName("DeviceStatsQueryFunction")
    public enum Function {
        COUNT,
        SUM,
        AVG,
        MIN,
        MAX
    }

    public boolean validQuery() {
        return switch (attribute.getContent()) {
            case BOOLEAN -> function == Function.COUNT || function == Function.SUM;
            case STRING -> function == Function.COUNT;
            case NUMBER -> true;
        };
    }
}
