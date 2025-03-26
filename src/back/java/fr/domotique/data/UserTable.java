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
    public Future<List<UserProfile>> getProfilesByFullName(String fullName) {
        return queryMany(UserProfile.MAP, PROFILE_FN_SQL, fullName, fullName);
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
}
