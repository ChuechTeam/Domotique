package fr.domotique.base.data;

import io.vertx.sqlclient.*;

import java.util.*;
import java.util.function.*;

/// Contains information about a SQL entity: a list of columns, and auto-generated SQL.
///
/// This class allows us to automatically create boring `INSERT`, `UPDATE` and `DELETE` SQL queries, by
/// **associating SQL columns with Java attributes**.
///
/// Its usage is really simple, and you can opt out of it if you don't like it.
///
/// ## Example
/// ```java
/// public class Article {
///     private int id;
///     private String title;
///     private String content;
///     private LocalDateTime publishedAt;
///
///     // Constructor, getters, setters...
///
///     public static final EntityInfo<Article> ENTITY = new EntityInfo<>(
///         Article.class,
///         // Mapping function
///         (r, s) -> new Article(
///             r.getInteger(s.next()), // id
///             r.getString(s.next()),  // title
///             r.getString(s.next()),  // content
///             r.getLocalDateTime(s.next()) // publishedAt
///         ),
///         new EntityColumn<>("id", Article::getId, ColumnType.GENERATED_KEY),
///         new EntityColumn<>("title", Article::getTitle),
///         new EntityColumn<>("content", Article::getContent),
///         new EntityColumn<>("published_at", Article::getPublishedAt)
///     );
/// }
/// ```
///
/// @param clazz the Java class that represents the entity
/// @param columns the list of columns in the entity
/// @param keyColumns the list of columns that are keys
/// @param insertColumns the list of columns that are used in INSERT queries
/// @param updateColumns the list of columns that are used in UPDATE queries
/// @param tableName the name of the SQL table
/// @param mapper the function that converts a SQL row to a Java entity
/// @param insertSQL the SQL query to insert a new entity
/// @param updateSQL the SQL query to update an entity
/// @param deleteSQL the SQL query to delete an entity
/// @param <T> the type of the entity
public record EntityInfo<T>(Class<T> clazz,
                            EntityColumn<T, ?>[] columns,
                            EntityColumn<T, ?>[] keyColumns,
                            EntityColumn<T, ?>[] insertColumns,
                            EntityColumn<T, ?>[] updateColumns,
                            String tableName,
                            Mapper<T> mapper,
                            String insertSQL,
                            String updateSQL,
                            String deleteSQL) {

    @SafeVarargs
    public EntityInfo(Class<T> clazz, BiFunction<Row, Mapper.Session, T> mapper, EntityColumn<T, ?>... columns) {
        this(clazz, columns, mapper);
    }

    @SuppressWarnings("unchecked")
    public EntityInfo(Class<T> clazz, EntityColumn<T, ?>[] columns, BiFunction<Row, Mapper.Session, T> mapper) {
        this(clazz, columns,
            filterBy(columns, x -> x.type().key),
            filterBy(columns, x -> x.type().insertArg),
            filterBy(columns, x -> x.type().updateArg),
            clazz.getSimpleName(),
            Mapper.of(columns.length, mapper),
            generateInsertSQL(columns, clazz.getSimpleName()),
            generateUpdateSQL(columns, clazz.getSimpleName()),
            generateDeleteSQL(columns, clazz.getSimpleName()));
    }

    static <T> String generateInsertSQL(EntityColumn<T, ?>[] columns, String tableName) {
        StringJoiner names = new StringJoiner(", ", "(", ")");
        StringJoiner values = new StringJoiner(", ", "(", ")");
        for (EntityColumn<T, ?> column : columns) {
            if (column.type().insertArg) {
                names.add(column.name());
                values.add("?");
            }
        }
        return "INSERT INTO " + tableName + names + " VALUES " + values;
    }

    static <T> String generateUpdateSQL(EntityColumn<T, ?>[] columns, String tableName) {
        StringJoiner set = new StringJoiner(", ", "SET ", "");
        StringJoiner where = new StringJoiner(" AND ", "WHERE ", "");
        for (EntityColumn<T, ?> column : columns) {
            if (column.type().updateArg) {
                set.add(column.name() + " = ?");
            }
            if (column.type().key) {
                where.add(column.name() + " = ?");
            }
        }
        return "UPDATE " + tableName + " " + set + " " + where;
    }

    static <T> String generateDeleteSQL(EntityColumn<T, ?>[] columns, String tableName) {
        StringJoiner where = new StringJoiner(" AND ", "WHERE ", "");
        for (EntityColumn<T, ?> column : columns) {
            if (column.type().key) {
                where.add(column.name() + " = ?");
            }
        }
        return "DELETE FROM " + tableName + " " + where;
    }

    @SuppressWarnings("unchecked")
    static <T> EntityColumn<T, ?>[] filterBy(EntityColumn<T, ?>[] columns, Predicate<EntityColumn<T, ?>> pred) {
        return Arrays.stream(columns)
            .filter(pred)
            .toArray(EntityColumn[]::new);
    }

    /// Generates the SQL arguments for an INSERT query
    public Object[] genInsertArguments(T entity) {
        // TODO: cool MethodHandle optimization
        Object[] args = new Object[insertColumns.length];
        for (int i = 0; i < insertColumns.length; i++) {
            Object val = toDatabase(insertColumns[i].getter().apply(entity));
            args[i] = val;
        }
        return args;
    }

    /// Generates the SQL arguments for an UPDATE query
    public Object[] genUpdateArguments(T entity) {
        // TODO: cool MethodHandle optimization
        Object[] args = new Object[updateColumns.length + keyColumns.length];
        for (int i = 0; i < updateColumns.length; i++) {
            args[i] = toDatabase(updateColumns[i].getter().apply(entity));
        }
        for (int i = 0; i < keyColumns.length; i++) {
            args[updateColumns.length + i] = toDatabase(keyColumns[i].getter().apply(entity));
        }
        return args;
    }

    private Object toDatabase(Object val) {
        if (val instanceof Enum) {
            return ((Enum<?>) val).ordinal();
        }
        return val;
    }

    /// Generates a list of columns for SELECT queries
    ///
    /// ## Example
    /// ```
    /// var columns = new EntityColumn[] {
    ///    new EntityColumn<>("id", x -> x.id()),
    ///    new EntityColumn<>("name", x -> x.name())
    /// };
    /// var info = new EntityInfo<>(Entity.class, columns);
    /// var list = info.genColumnList("e");
    /// // list == "e.id, e.name"
    /// ```
    public String genColumnList(String tableName) {
        var sj = new StringJoiner(", ");
        if (tableName == null) {
            for (var column : columns) {
                sj.add(column.name());
            }
        } else {
            for (var column : columns) {
                sj.add(tableName + "." + column.name());
            }
        }
        return sj.toString();
    }
}
