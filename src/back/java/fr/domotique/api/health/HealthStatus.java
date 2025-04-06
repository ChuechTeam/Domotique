package fr.domotique.api.health;

import java.util.function.*;

public enum HealthStatus {
    HEALTHY,
    WARNING,
    MISSING_DATA,
    IGNORED;

    public static HealthStatus healthyWhen(Object v, Function<Double, Boolean> predicate) {
        if (v == null) {
            return MISSING_DATA;
        }
        if (v instanceof Number) {
            double numVal = ((Number) v).doubleValue();
            return predicate.apply(numVal) ? HEALTHY : WARNING;
        } else {
            throw new IllegalArgumentException("Invalid value type: " + v.getClass());
        }
    }
}
