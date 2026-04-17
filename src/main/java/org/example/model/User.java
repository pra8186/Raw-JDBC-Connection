package org.example.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable domain model for a registered user.
 * Once constructed, no field can be changed — the UUID, password hash,
 * and every other attribute are locked at creation time.
 */
public class User {

    private final String id;
    private final String name;
    private final String email;
    private final ProfileType profileType;
    private final String password;
    private final LocalDateTime createdAt;

    /**
     * Creates a user with a newly generated {@link UUID} as identifier.
     * This constructor is package-private so that only {@link UserFactory}
     * (in the same package) can instantiate users.
     *
     * @param name        display name
     * @param email       email address
     * @param profileType role classification
     * @param password    BCrypt-hashed password
     */
    User(String name, String email, ProfileType profileType, String password) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.email = email;
        this.profileType = profileType;
        this.password = password;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Reconstructs a user read from the database (existing ID).
     *
     * @param id          existing primary key
     * @param name        display name
     * @param email       email address
     * @param profileType role classification
     * @param password    BCrypt-hashed password
     * @param createdAt   timestamp when the user was created
     */
    public User(String id, String name, String email, ProfileType profileType,
                String password, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.profileType = profileType;
        this.password = password;
        this.createdAt = createdAt;
    }

    /** @return persistent user identifier (UUID string) */
    public String getId() {
        return id;
    }

    /** @return display name */
    public String getName() {
        return name;
    }

    /** @return email address */
    public String getEmail() {
        return email;
    }

    /** @return profile classification */
    public ProfileType getProfileType() {
        return profileType;
    }

    /** @return BCrypt-hashed password */
    public String getPassword() {
        return password;
    }

    /** @return timestamp when the user was created */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * @return short summary suitable for console confirmation (excludes raw password)
     */
    @Override
    public String toString() {
        return "User{name='" + name + "', email='" + email + "', profileType=" + profileType + "} " +
                "has successfully  been created with Id " + id;
    }
}
