package fr.domotique.data;

import fr.domotique.base.data.*;
import io.vertx.core.*;
import io.vertx.sqlclient.*;

import java.util.*;

import static fr.domotique.data.LoginLog.*;

public class PowerLogTable extends Table {

    public PowerLogTable(SqlClient client) {
        super(client);
    }
    public Future<List<PowerLog>> getAllSorted() {
        return queryMany(ENTITY.mapper(), "SELECT * FROM PowerLog ORDER BY time DESC");}
    public Future<PowerLog> get(int id) {
        return querySingle(ENTITY.mapper(), "SELECT * FROM PowerLog WHERE id = ?", id);}

    public Future<PowerLog> insert(PowerLog log) {
        return insert(ENTITY, log, PowerLog::getId, PowerLog::setId);
    }

    public Future<PowerLog> update(PowerLog log) {return update(ENTITY, log);}

    public Future<Boolean> delete(int id) {return delete(ENTITY, id);
    }
}