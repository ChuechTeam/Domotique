package fr.domotique.data;

import jakarta.persistence.*;

/**
 * A user of the domotique app. Can log in using a combination of email and hashed password.
 *
 * @author Dynamic
 */
@Entity
@Table(name = "User",
        indexes = {
                // Add an index to quickly find a user by email and password
                @Index(columnList = "email, passHash")
        })
public class User {
    /**
     * The unique identifier of the user.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Enable AUTO INCREMENT to get automatic IDs
    private long id;

    /**
     * The email address of the user. Must be unique. Is used to log in.
     */
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * The gender of the user.
     */
    @Column(nullable = false)
    private Gender gender;

    /**
     * The hashed password of the user.
     * <p>
     * Try a <a href="https://www.baeldung.com/spring-security-registration-password-encoding-bcrypt">hashing tutorial</a>
     */
    @Column(nullable = false)
    private String passHash;

    /**
     * Used only by JPA (the SQL magic thing) so it can initialize every attribute of the User.
     */
    protected User() {}

    /**
     * Make a new user with all their necessary information
     *
     * @param email the email address of the user
     * @param gender the gender of the user
     * @param passHash the HASHED password of the user
     *                 (<a href="https://www.baeldung.com/spring-security-registration-password-encoding-bcrypt">hashing tutorial</a>)
     */
    public User(String email, Gender gender, String passHash) {
        this.email = email;
        this.gender = gender;
        this.passHash = passHash;
    }

    // Boring getters and setters

    public long getId() {
        return id;
    }

    public void setId(long id) {
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
