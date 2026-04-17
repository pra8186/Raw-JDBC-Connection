package org.example.model;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Factory that encapsulates {@link User} creation.
 * Responsible for hashing the raw password and constructing an immutable User
 * instance — callers never touch the User constructor directly.
 */
public class UserFactory {

    private static final int BCRYPT_COST = 12;

    /**
     * Creates a fully initialised, immutable {@link User}.
     *
     * @param name        display name
     * @param email       validated email address
     * @param profileType role classification
     * @param rawPassword plain-text password (will be BCrypt-hashed before storage)
     * @return a new {@link User} with a generated UUID and hashed password
     */
    public static User createUser(String name, String email,
                                  ProfileType profileType, String rawPassword) {
        String hashedPassword = BCrypt.withDefaults()
                .hashToString(BCRYPT_COST, rawPassword.toCharArray());
        return new User(name, email, profileType, hashedPassword);
    }
}
