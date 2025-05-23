package fr.domotique.data;

import fr.domotique.base.data.*;
import io.vertx.sqlclient.*;
import io.vertx.core.*;

import java.util.*;

import static fr.domotique.data.DeviceType.ENTITY;

public class DeviceTypeTable extends Table {
    public DeviceTypeTable(SqlClient client) {
        super(client);
    }

    public Future<List<DeviceType>> getAll() {
        return queryMany(ENTITY.mapper(), "SELECT * FROM DeviceType");
    }

    public Future<List<DeviceType>> getAllByName(String name) {
        return queryMany(ENTITY.mapper(), "SELECT * FROM DeviceType WHERE INSTR(name, ?) > 0", name);
    }

    public Future<List<DeviceType>> getAll(Collection<Integer> ids) {
        if (ids.isEmpty()) {
            return Future.succeededFuture(Collections.emptyList());
        }

        return queryMany(ENTITY.mapper(), "SELECT * FROM DeviceType WHERE id IN " + paramList(ids.size()), ids);
    }

    public Future<DeviceType> get(int id) {
        return querySingle(ENTITY.mapper(), "SELECT * FROM DeviceType WHERE id = ?", id);
    }

    public Future<DeviceType> insert(DeviceType deviceType) {
        return insert(ENTITY, deviceType, DeviceType::getId, DeviceType::setId);
    }

    public Future<DeviceType> update(DeviceType deviceType) {
        return update(ENTITY, deviceType);
    }

    public Future<Boolean> delete(int id) {
        return delete(ENTITY, id);
    }
}
