package fr.domotique.data;

import io.vertx.sqlclient.*;

/**
 * A user of the domotique app. Can log in using a combination of email and hashed password.
 *
 * @author Dynamic
 */
public class User {
    /**
     * The unique identifier of the user. A value of 0 means that the user will be created soon.
     */
    private int id;

    /**
     * The email address of the user. Must be unique. Is used to log in.
     */
    private String email;

    /**
     * The gender of the user.
     */
    private Gender gender;

    /**
     * The hashed password of the user. Uses PBKDF2 algorithm.
     *
     * @see io.vertx.ext.auth.hashing.HashingStrategy
     */
    private String passHash;

    /**
     * Make a new user with all their necessary information
     *
     * @param id the identifier of this user; 0 -> automatically generated
     * @param email the email address of the user
     * @param gender the gender of the user
     * @param passHash the HASHED password of the user
     */
    public User(int id, String email, Gender gender, String passHash) {
        this.id = id;
        this.email = email;
        this.gender = gender;
        this.passHash = passHash;
    }

    /// Maps an SQL [Row] to a [User], considering all of its fields are in order, starting from `startIdx`.
    public static User map(Row row, int startIdx) {
        return new User(
                row.getInteger(startIdx),
                row.getString(startIdx + 1),
                Gender.fromByte(row.get(Byte.class, startIdx + 2)),
                row.getString(startIdx + 3)
        );
    }

    /// Maps an SQL [Row] to a [User], considering all of its fields are in order.
    public static User map(Row row) {
        return map(row, 0);
    }

    // Boring getters and setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getPassHash() {
        return passHash;
    }

    public void setPassHash(String passHash) {
        this.passHash = passHash;
    }
}
