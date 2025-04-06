package fr.domotique.data;

import java.util.*;

public enum ActionLogFlags {
    POWER_ON,
    POWER_OFF,
    DELETE_REQUESTED,
    DELETE_REQUEST_DELETED,
    PASSWORD_CHANGED,
    EMAIL_CONFIRMED;

    public static EnumSet<ActionLogFlags> fromLong(long flags) {
        EnumSet<ActionLogFlags> result = EnumSet.noneOf(ActionLogFlags.class);
        for (ActionLogFlags flag : ActionLogFlags.values()) {
            if ((flags & (1 << flag.ordinal())) != 0) {
                result.add(flag);
            }
        }
        return result;
    }

    public static long toLong(EnumSet<ActionLogFlags> flags) {
        long result = 0;
        for (ActionLogFlags flag : flags) {
            result |= (1 << flag.ordinal());
        }
        return result;
    }
}
