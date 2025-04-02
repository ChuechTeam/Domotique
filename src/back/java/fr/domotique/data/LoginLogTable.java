package fr.domotique.data;

import fr.domotique.base.data.*;
import io.vertx.core.*;
import io.vertx.sqlclient.*;
import org.jetbrains.annotations.*;

import java.util.*;

import static fr.domotique.data.LoginLog.*;

public class LoginLogTable extends Table {
    public LoginLogTable(SqlClient client) {
        super(client);
    }

    public Future<List<LoginLog>> getAllSorted(@Nullable Integer userId) {
        if (userId != null) {
            return queryMany(ENTITY.mapper(), "SELECT * FROM LoginLog WHERE userId = ? ORDER BY time DESC", userId);
        } else {
            return queryMany(ENTITY.mapper(), "SELECT * FROM LoginLog ORDER BY time DESC");
        }
    }

    public Future<LoginLog> get(int id) {
        return querySingle(ENTITY.mapper(), "SELECT * FROM LoginLog WHERE id = ?", id);
    }

    public Future<LoginLog> insert(LoginLog deviceType) {
        return insert(ENTITY, deviceType, LoginLog::getId, LoginLog::setId);
    }

    public Future<LoginLog> update(LoginLog deviceType) {
        return update(ENTITY, deviceType);
    }

    public Future<Boolean> delete(int id) {
        return delete(ENTITY, id);
    }
}
