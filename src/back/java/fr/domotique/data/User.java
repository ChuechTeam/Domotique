package fr.domotique.data;

import fr.domotique.base.data.*;
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

    /// The email address of the user. Must be unique. Is used to log in.
    private String email;

    /// The confirmation token given by mail to confirm a user's email.
    /// Should be generated randomly by using PRNGs.
    private long emailConfirmationToken;

    /// True when the user has confirmed their email address.
    private boolean emailConfirmed;

    /**
     * The hashed password of the user. Uses PBKDF2 algorithm.
     *
     * @see io.vertx.ext.auth.hashing.HashingStrategy
     */
    private String passHash;

    /// The first name of the user. Not null!
    private String firstName;
    /// The last name of the user. Not null!
    private String lastName;

    /**
     * The gender of the user.
     */
    private Gender gender;

    /// The role of the user in the EHPAD
    private Role role;

    /// The current level of the user.
    private Level level;

    /// The number of points this user has accumulated.
    private int points;

    public User(int id,
                String email,
                long emailConfirmationToken,
                boolean emailConfirmed,
                String passHash, String firstName,
                String lastName,
                Gender gender,
                Role role,
                Level level,
                int points) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.emailConfirmationToken = emailConfirmationToken;
        this.emailConfirmed = emailConfirmed;
        this.passHash = passHash;
        this.gender = gender;
        this.role = role;
        this.level = level;
        this.points = points;
    }

    /**
     * Entity information for the User class, used for database operations.
     */
    public static final EntityInfo<User> ENTITY = new EntityInfo<>(
        User.class,
        (r, s) -> new User(
            r.getInteger(s.next()),
            r.getString(s.next()),
            r.getLong(s.next()),
            r.getBoolean(s.next()),
            r.getString(s.next()),
            r.getString(s.next()),
            r.getString(s.next()),
            Gender.fromByte(r.get(Byte.class, s.next())),
            Role.fromByte(r.get(Byte.class, s.next())),
            Level.fromByte(r.get(Byte.class, s.next())),
            r.getInteger(s.next())
        ),
        new EntityColumn<>("id", User::getId, ColumnType.GENERATED_KEY),
        new EntityColumn<>("email", User::getEmail),
        new EntityColumn<>("emailConfirmationToken", User::getEmailConfirmationToken),
        new EntityColumn<>("emailConfirmed", User::isEmailConfirmed),
        new EntityColumn<>("passHash", User::getPassHash),
        new EntityColumn<>("firstName", User::getFirstName),
        new EntityColumn<>("lastName", User::getLastName),
        new EntityColumn<>("gender", User::getGender),
        new EntityColumn<>("role", User::getRole),
        new EntityColumn<>("level", User::getLevel),
        new EntityColumn<>("points", User::getPoints)
    );

    // Boring getters and setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getEmailConfirmationToken() {
        return emailConfirmationToken;
    }

    public void setEmailConfirmationToken(long emailConfirmationToken) {
        this.emailConfirmationToken = emailConfirmationToken;
    }

    public boolean isEmailConfirmed() {
        return emailConfirmed;
    }

    public void setEmailConfirmed(boolean emailConfirmed) {
        this.emailConfirmed = emailConfirmed;
    }

    public String getPassHash() {
        return passHash;
    }

    public void setPassHash(String passHash) {
        this.passHash = passHash;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}
