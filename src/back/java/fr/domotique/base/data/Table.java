package fr.domotique.base.data;

import io.vertx.core.*;
import io.vertx.mysqlclient.*;
import io.vertx.sqlclient.*;

import java.util.*;
import java.util.function.*;

/// Contains all SQL queries to access a SQL table. Provides useful functions for using SQL.
public abstract class Table {
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
    /// ```
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

    /// Upon encountering a MySQL error due to an UNIQUE constraint preventing an `INSERT` operation,
    /// throws a [DuplicateException] with the given error message.
    ///
    /// Must be used inside the `recover` function of a [Future].
    ///
    /// ## Example
    /// ```java
    /// // Insert a user and handle duplicates
    /// Future<User> future = client.preparedQuery("INSERT INTO user (name) VALUES (?)")
    ///   .execute(Tuple.of("John"))
    ///   .map(attachId(user, user::setId))
    ///   .recover(Table.handleDuplicates("The user already exists."));
    /// ```
    public static <T> Function<Throwable, Future<T>> handleDuplicates(String errMsg) {
        return ex -> {
            if (ex instanceof DatabaseException my && my.getErrorCode() == 1062) {
                return Future.failedFuture(new DuplicateException(errMsg, ex));
            }
            else {
                return Future.failedFuture(ex);
            }
        };
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
