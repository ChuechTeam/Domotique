package fr.domotique.base.data;

import io.vertx.core.*;
import io.vertx.mysqlclient.*;
import io.vertx.sqlclient.*;

import java.util.*;
import java.util.function.*;

/// Contains all SQL queries to access a SQL table. Provides useful functions for using SQL.
@SuppressWarnings("SqlSourceToSinkFlow")
public abstract class Table {
    protected final SqlClient client;

    protected Table(SqlClient client) {
        this.client = client;
    }

    /// Replaces each '`%s`' in the SQL with the given parameters.
    ///
    /// This allows us, for example, to reuse repeated lists of columns in `SELECT` queries.
    ///
    /// **WARNING**: this function should NOT BE USED FOR SQL PARAMETERS; rather, it's for reusing parts
    /// of SQL queries we trust.
    ///
    /// ## Example
    protected static String makeModularSQL(String sql, Object... params) {
        return String.format(sql, params);
    }

    /// Executes an SQL query with the given parameters. Returns a [RowSet].
    protected Future<RowSet<Row>> query(String sql, Object... params) {
        return client.preparedQuery(sql)
            .execute(Tuple.wrap(params))
            .recover(Table::handleSqlErrors);
    }

    /// Executes an SQL query and returns the first row, or `null` if there's no row.
    ///
    /// The row is transformed into a Java object by using the `mapper` function.
    protected <T> Future<T> querySingle(Function<Row, T> mapper, String sql, Object... params) {
        return client.preparedQuery(sql)
            .execute(Tuple.wrap(params))
            .map(x -> {
                Row row = firstRow(x);
                if (row != null) {
                    return mapper.apply(row);
                } else {
                    return null;
                }
            })
            .recover(Table::handleSqlErrors);
    }

    /// Executes an SQL query and returns all rows.
    ///
    /// The rows are transformed into Java objects by using the `mapper` function.
    protected <T> Future<List<T>> queryMany(Function<Row, T> mapper, String sql, Object... params) {
        return client.preparedQuery(sql)
            .execute(Tuple.wrap(params))
            .map(x -> toList(x).stream().map(mapper).toList())
            .recover(Table::handleSqlErrors);
    }

    /// Executes an SQL INSERT query with the given parameters. Sets `value`'s id to the last inserted id, by using
    /// the `idSetter` function.
    protected <T> Future<T> insert(T value, Consumer<Integer> idSetter, String sql, Object... params) {
        return client.preparedQuery(sql)
            .execute(Tuple.wrap(params))
            .map(rs -> {
                int id = rs.property(MySQLClient.LAST_INSERTED_ID).intValue();
                idSetter.accept(id);
                return value;
            })
            .recover(Table::handleSqlErrors);
    }

    protected <T> Future<T> insert(EntityInfo<T> info, T value,
                                   Function<T, Integer> idGetter,
                                   BiConsumer<T, Integer> idSetter) {
        if (idGetter.apply(value) != 0) {
            throw new IllegalArgumentException("The entity must have a zero id");
        }

        return client.preparedQuery(info.insertSQL())
            .execute(Tuple.wrap(info.genInsertArguments(value)))
            .map(rs -> {
                idSetter.accept(value, rs.property(MySQLClient.LAST_INSERTED_ID).intValue());
                return value;
            })
            .recover(Table::handleSqlErrors);
    }

    protected <T> Future<T> update(EntityInfo<T> info, T value) {
        if (info.keyColumns().length == 0) {
            throw new IllegalArgumentException("The entity must have at least one key column");
        }

        return client.preparedQuery(info.updateSQL())
            .execute(Tuple.wrap(info.genUpdateArguments(value)))
            .map(value)
            .recover(Table::handleSqlErrors);
    }

    protected <T> Future<Boolean> delete(EntityInfo<T> info, Object... idColumns) {
        if (info.keyColumns().length == 0) {
            throw new IllegalArgumentException("The entity must have at least one key column");
        }

        return client.preparedQuery(info.deleteSQL())
            .execute(Tuple.wrap(idColumns))
            .map(rs -> rs.rowCount() > 0)
            .recover(Table::handleSqlErrors);
    }

    /// After the request succeeded, attaches the inserted id of the `INSERT` SQL statement to the `entity`.
    ///
    /// The `idSetter` must set `entity`'s id to the id it receives.
    ///
    /// Must be used in the `map` function of the [Future].
    ///
    /// ## Example
    /// ```java
    /// // Insert a user and attach the id to it
    /// User user = new User();
    /// Future<User> future = client.preparedQuery("INSERT INTO user (name) VALUES (?)")
    ///    .execute(Tuple.of("John"))
    ///    .map(attachId(user, user::setId));
    ///```
    ///
    /// @param entity the entity to attach the id to
    /// @param idSetter the function that sets the id of the entity
    public static <T> Function<RowSet<Row>, T> attachId(T entity, Consumer<Integer> idSetter) {
        return rs -> {
            // MySQL gives us the id of the INSERT SQL statement. So let's get it!
            int id = rs.property(MySQLClient.LAST_INSERTED_ID).intValue();
            idSetter.accept(id);
            return entity;
        };
    }

    /// Upon encountering a MySQL error due to varius constraints preventing an `INSERT` operation,
    /// throws a [DuplicateException] or a [ForeignException].
    ///
    /// Must be used inside the `recover` function of a [Future].
    ///
    /// ## Example
    /// ```java
    /// // Insert a user and handle duplicates
    /// Future<User> future = client.preparedQuery("INSERT INTO user (name) VALUES (?)")
    ///   .execute(Tuple.of("John"))
    ///   .map(attachId(user, user::setId))
    ///   .recover(Table.handleDuplicates());
    ///```
    public static <T> Future<T> handleSqlErrors(Throwable ex) {
        if (ex instanceof DatabaseException my) {
            return switch (my.getErrorCode()) {
                case 1062 -> Future.failedFuture(new DuplicateException(my.getMessage(), my));
                case 1452 -> Future.failedFuture(new ForeignException(my.getMessage(), my));
                default -> Future.failedFuture(my);
            };
        } else {
            return Future.failedFuture(ex);
        }
    }

    /// Given a list of rows, gives the first one, or null if there's nothing.
    public static <T> T firstRow(RowSet<T> rs) {
        return rs.iterator().hasNext() ? rs.iterator().next() : null;
    }

    /// Converts a row set into a list of elements.
    public static <T> List<T> toList(RowSet<T> rs) {
        List<T> list = new ArrayList<>();
        rs.forEach(list::add);
        return list;
    }
}
