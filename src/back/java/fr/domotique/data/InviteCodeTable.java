package fr.domotique.data;

import fr.domotique.base.data.*;
import io.vertx.core.*;
import io.vertx.sqlclient.*;
import org.jetbrains.annotations.*;

import java.util.*;

import static fr.domotique.data.InviteCode.ENTITY;

public class InviteCodeTable extends Table {
    public InviteCodeTable(SqlClient client) {
        super(client);
    }

    public Future<List<InviteCode>> getAll() {
        return queryMany(ENTITY.mapper(), "SELECT * FROM InviteCode ORDER BY createdAt DESC");
    }

    public Future<InviteCode> get(String id) {
        return querySingle(ENTITY.mapper(), "SELECT * FROM InviteCode WHERE id = ?", id);
    }

    public Future<InviteCode> insert(InviteCode inviteCode) {
        return query(ENTITY.insertSQL(), ENTITY.genInsertArguments(inviteCode)).map(inviteCode);
    }

    public Future<InviteCode> update(InviteCode inviteCode) {
        return update(ENTITY, inviteCode);
    }

    public Future<Boolean> delete(String id) {
        return delete(ENTITY, id);
    }
}
