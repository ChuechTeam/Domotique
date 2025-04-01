package fr.domotique.data;

import fr.domotique.api.users.*;
import fr.domotique.base.data.*;
import io.vertx.core.*;
import io.vertx.sqlclient.*;
import org.jetbrains.annotations.*;

import java.util.*;

import static fr.domotique.data.User.ENTITY;

/// Contains many functions to interact with the [User] table in the database.
///
/// ## Example
///
/// ```java
/// // Get the user with ID 5
/// server.db().users().get(5).onSuccess(user -> { ... });
///```
public class UserTable extends Table {
    /// Creates a new user table using the SQL client.
    ///
    /// @param client the SQL client to use
    public UserTable(SqlClient client) {
        super(client);
    }

    /// Gets the user with the given ID. Can return `null`.
    public Future<@Nullable User> get(int id) {
        return querySingle(ENTITY.mapper(), "SELECT * FROM User WHERE id = ?", id);
    }

    /// Gets the user with the given email
    ///
    /// @param email the email of the user
    /// @return the user, or `null` if not found
    public Future<@Nullable User> getByEmail(String email) {
        return querySingle(ENTITY.mapper(), "SELECT * FROM User WHERE email = ?", email);
    }

    static final String PROFILE_FN_SQL = makeModularSQL("""
        SELECT %s FROM User
        WHERE INSTR(firstName, ?) > 0 OR INSTR(lastName, ?) > 0
        """, UserProfile.columnList(null)); // -> "id, firstName, lastName, role, level, etc."

    /// Searches all user profile matching the given full name.
    public Future<List<UserProfile>> getAllProfilesByFullName(String fullName) {
        return queryMany(UserProfile.MAP, PROFILE_FN_SQL, fullName, fullName);
    }

    static final String PROFILE_IDS_SQL = makeModularSQL("""
        SELECT %s FROM User
        WHERE id""", UserProfile.columnList(null)); // -> "id, firstName, lastName, role, level, etc."

    public Future<List<UserProfile>> getAllProfiles(Collection<Integer> ids) {
        if (ids.isEmpty()) {
            return Future.succeededFuture(Collections.emptyList());
        }

        var sb = new StringBuilder(PROFILE_IDS_SQL);
        sb.append(" IN ");
        paramList(sb, ids.size());

        final String sql = sb.toString();

        return queryMany(UserProfile.MAP, sql, ids.toArray());
    }

    /// Creates a user in the database. Doesn't validate anything!
    ///
    /// @param user the user to create, must have an id of 0
    /// @throws IllegalArgumentException if the user has a non-zero id
    /// @return the same user, with its new ID (note: this is just `user` but mutated)
    public Future<User> create(User user) {
        return insert(ENTITY, user, User::getId, User::setId);
    }

    public Future<User> update(User user) {
        return update(ENTITY, user);
    }

    public Future<Boolean> delete(int id) {
        return delete(ENTITY, id);
    }

    // -- Point requests

    public Future<List<PointInfo>> getManyPointInfo(Collection<Integer> userIds) {
        if (userIds.isEmpty()) {
            return Future.succeededFuture(Collections.emptyList());
        }

        var args = new StringJoiner(", ", "(", ")");
        for (int i = 0; i < userIds.size(); i++) args.add("?");

        final String sql = """
                               SELECT id, points, level
                               FROM User
                               WHERE User.id IN\s""" + args;

        return queryMany(PointInfo::fromRow, sql, userIds.toArray());
    }

    public Future<Void> updateManyPointInfo(Collection<PointInfo> pointInfos) {
        if (pointInfos.isEmpty()) {
            return Future.succeededFuture();
        }

        // TODO: Check success after executeBatch
        return client.preparedQuery("""
                    UPDATE User
                    SET points = ?, level = ?
                    WHERE id = ?
                """)
            .executeBatch(pointInfos.stream().map(PointInfo::toTuple).toList())
            .mapEmpty();
    }

    public record PointInfo(int id, int points, Level level) {
        Tuple toTuple() {
            return Tuple.of(points, level.ordinal(), id);
        }

        static PointInfo fromRow(Row t) {
            return new PointInfo(t.getInteger(0), t.getInteger(1), Level.values()[t.getInteger(2)]);
        }
    }
}
