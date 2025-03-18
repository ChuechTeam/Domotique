package fr.domotique.data;

import io.vertx.core.*;
import io.vertx.mysqlclient.*;
import io.vertx.sqlclient.*;
import org.jetbrains.annotations.*;

/// Contains many functions to interact with the [User] table in the database.
///
/// This class doesn't need any particular lifetime considerations; you can just instantiate it
/// however you like!
///
/// ## Example
///
/// ```java
/// // Get the user with ID 5
/// userTable.get(5).onSuccess(user -> { ... });
/// ```
public class UserTable {
    /// The SQL client allowing us to run SQL queries on the MySQL database.
    private final SqlClient client;

    /// Creates a new user table using the SQL client.
    ///
    /// @param client the SQL client to use
    public UserTable(SqlClient client) {
        this.client = client;
    }

    /// Gets the user with the given ID. Can return `null`.
    public Future<@Nullable User> get(int id) {
        // What we're doing:
        // 1. prepareQuery : make a SQL statement taking parameters (in question marks)
        //
        // 2. mapping      : converts the row into a User object;
        //                   a row is just a simple array of values like [1, "hello", 45]
        //
        // 3. execute      : executes the query with the given parameters (in this case, the id)
        //
        // 4. map          : take only the first row of the list of rows
        return client.preparedQuery("SELECT * FROM user WHERE id = ?")
                .mapping(User::map)
                .execute(Tuple.of(id))
                .map(UserTable::firstRow);
    }

    /// Gets the user with the given email
    ///
    /// @param email the email of the user
    /// @return the user, or `null` if not found
    public Future<@Nullable User> getByEmail(String email) {
        // What we're doing:
        // 1. prepareQuery : make a SQL statement taking parameters (in question marks)
        //
        // 2. mapping      : converts the row into a User object;
        //                   a row is just a simple array of values like [1, "hello", 45]
        //
        // 3. execute      : executes the query with the given parameters (in this case, the email)
        //
        // 4. map          : take only the first row of the list of rows
        return client.preparedQuery("SELECT * FROM user WHERE email = ?")
                .mapping(User::map)
                .execute(Tuple.of(email))
                .map(UserTable::firstRow);
    }

    /// Creates a user in the database. Doesn't validate anything!
    ///
    /// @param user the user to create, must have an id of 0
    /// @throws IllegalArgumentException if the user has a non-zero id
    /// @return the same user, with its new ID (note: this is just `user` but mutated)
    public Future<User> create(User user) {
        // Make sure the user has a zero id (0 means it's a new user)
        if (user.getId() != 0) {
            return Future.failedFuture(new IllegalArgumentException("User has a non-zero id!"));
        }

        // What we're doing:
        // 1. prepareQuery : make a SQL INSERT statement taking parameters (in question marks)
        //
        // 2. execute      : executes the query with the given parameters (?, ?, ?) = (email, gender, password hash)
        //
        // 3. map          : the INSERT statement returns zero rows, BUT it gives us the ID it has generated;
        //                   use it to give the ID of the user we've just created!
        return client.preparedQuery("INSERT INTO user (email, gender, pass_hash) VALUES (?, ?, ?)")
                .execute(Tuple.of(user.getEmail(), user.getGender().ordinal(), user.getPassHash()))
                .map(rs -> {
                    // MySQL gives us the id of the INSERT SQL statement. So let's get it!
                    int id = rs.property(MySQLClient.LAST_INSERTED_ID).intValue();
                    user.setId(id);
                    return user;
                });
    }

    /// Given a list of rows, gives the first one, or null if there's nothing.
    private static <T> T firstRow(RowSet<T> rs) {
        return rs.iterator().hasNext() ? rs.iterator().next() : null;
    }
}
