package fr.domotique.data;

import fr.domotique.base.data.*;
import lombok.*;

/// A user of the domotique app. Can log in using a combination of email and hashed password.
///
/// @author Dynamic
@Getter
@Setter
@AllArgsConstructor
public class User {
    /// The unique identifier of the user. A value of 0 means that the user will be created soon.
    int id;

    /// The email address of the user. Must be unique. Is used to log in.
    String email;

    /// The confirmation token given by mail to confirm a user's email.
    /// Should be generated randomly by using PRNGs.
    long emailConfirmationToken;

    /// True when the user has confirmed their email address.
    boolean emailConfirmed;

    /// The hashed password of the user. Uses PBKDF2 algorithm.
    String passHash;

    /// The first name of the user. Not null!
    String firstName;

    /// The last name of the user. Not null!
    String lastName;

    /// The gender of the user.
    Gender gender;

    /// The role of the user in the EHPAD
    Role role;

    /// The current level of the user.
    Level level;

    /// The number of points this user has accumulated.
    int points;

    /// Entity information for the User class, used for database operations.
    public static final EntityInfo<User> ENTITY = new EntityInfo<>(
        User.class,
        // Database -> Java
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
        // Java -> Database
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
}
