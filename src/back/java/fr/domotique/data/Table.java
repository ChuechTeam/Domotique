package fr.domotique.data;

import io.vertx.core.*;
import io.vertx.mysqlclient.*;
import io.vertx.sqlclient.*;

import java.util.function.*;

public abstract class Table {
    public static <T> Function<RowSet<Row>, T> attachId(T entity, Consumer<Integer> idSetter) {
        return rs -> {
            // MySQL gives us the id of the INSERT SQL statement. So let's get it!
            int id = rs.property(MySQLClient.LAST_INSERTED_ID).intValue();
            idSetter.accept(id);
            return entity;
        };
    }

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
}
