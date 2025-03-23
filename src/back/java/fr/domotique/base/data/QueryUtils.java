package fr.domotique.base.data;

import org.jetbrains.annotations.*;

import java.util.*;

/// Random utility functions for SQL queries.
public final class QueryUtils {
    private QueryUtils() {}

    public static String columnList(@Nullable String tableName, String... columns) {
        var sj = new StringJoiner(", ");
        if (tableName == null) {
            for (var column : columns) {
                sj.add(column);
            }
        }
        else {
            for (var column : columns) {
                sj.add(tableName + "." + column);
            }
        }
        return sj.toString();
    }
}
