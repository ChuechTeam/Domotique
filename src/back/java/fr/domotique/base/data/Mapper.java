package fr.domotique.base.data;

import io.vertx.sqlclient.*;
import org.jetbrains.annotations.*;

import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;

/// Transforms an SQL row into a Java object, with support for nullable JOINs.
///
/// A mapper is composed of two elements:
/// - a function to map a row to a Java object
/// - the number of columns that the mapper consumes
///
/// This last part is important, since some `JOIN` sql queries might return `NULL`, and we need to skip rows.
///
/// ## Example
///
/// See [EntityInfo]'s example.
// TODO: Investigate to see if a record (mapper, columns) yields better performance with trusted static final fields,
//       instead of a seemingly costly anonymous class.
public abstract class Mapper<T> implements Function<Row, T> {
    protected final int columns;

    protected Mapper(int columns) {
        this.columns = columns;
    }

    /// Creates a new mapper with the expected number of `columns`, and the mapping function `map`.
    public static <T> Mapper<T> of(int columns, BiFunction<Row, Session, T> map) {
        return new Mapper<>(columns) {
            @Override
            protected T map(Row row, Session session) {
                return map.apply(row, session);
            }
        };
    }

    protected abstract T map(Row row, Session session);

    public final @Nullable T apply(Row row, Session session) {
        if (row.getValue(session.get()) != null) {
            int col = session.column;
            T val = map(row, session);
            int consumed = session.column - col;
            if (consumed != columns) {
                throw new IllegalStateException("Mapper did not consume the correct number of columns. " +
                    "Columns consumed: " + consumed + ", expected: " + columns);
            }

            return val;
        } else {
            session.column += columns;
            return null;
        }
    }

    @Override
    public T apply(Row row) {
        return apply(row, new Session());
    }

    public int getColumns() {
        return columns;
    }

    /// Basically an integer you can increment. Used to keep track of the current column.
    public final static class Session {
        int column;

        public int get() {
            return column;
        }

        public int next() {
            return column++;
        }
    }
}
