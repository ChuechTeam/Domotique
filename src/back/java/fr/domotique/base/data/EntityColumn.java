package fr.domotique.base.data;

import java.util.function.*;

/// A SQL column in an [entity][EntityInfo].
///
/// @param name the name of the column in the SQL database
/// @param getter a function to get the value of the column from the entity, can change the value to put
///               special treatment for SQL
/// @param type the type of the column
/// @param <T> the type of the entity
/// @param <V> the type of the column
public record EntityColumn<T, V>(String name, Function<T, V> getter, ColumnType type) {
    public EntityColumn(String name, Function<T, V> getter) {
        this(name, getter, ColumnType.NORMAL);
    }
}
