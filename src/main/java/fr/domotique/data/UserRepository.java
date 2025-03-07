package fr.domotique.data;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * The repository to interact with the {@link User} table in the database.
 * <p>
 * This interface is magically implemented by Spring Data JPA, so no need to create a class
 * or anything, just put it straight in the controller and it will work!
 *
 * @see User
 * @author Dynamic
 */
public interface UserRepository extends JpaRepository<User, Long> {
    // Let me explain the magic behind those functions:
    //
    // Spring has a system that automatically creates SQL QUERIES depending on the FUNCTION NAME.
    // With "findByEmail", it saw an attribute named "email", so it creates a SELECT query automatically!
    //
    // See https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html for all available functions.
    //
    // You can still make queries by hand, using the @Query annotation.

    /**
     * Find a user by their email address.
     *
     * @param email the email address of the user
     * @return the user with that email address, or null if none exists
     */
    User findByEmail(String email);

    /**
     * Finds a user by BOTH their email address AND a valid password hash.
     * <p>
     *
     * To hash a password, see this <a href="https://www.baeldung.com/spring-security-registration-password-encoding-bcrypt">tutorial</a>!
     *
     * @param email the email address of the user
     * @param passHash the hashed password of the user
     * @return the user found with those credentials
     */
    User findByEmailAndPassHash(String email, String passHash);
}
