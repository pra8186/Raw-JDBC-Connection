package org.example.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link UserFactory}.
 *
 * <p>Confirms that {@code createUser} generates a UUID, hashes the password
 * with BCrypt, and produces unique IDs across calls.
 */
class UserFactoryTest {

    @Test
    void createUser_generatesUuidAndHashesPassword() {
        User user = UserFactory.createUser("Alice", "alice@example.com",
                ProfileType.INDIVIDUAL, "password123");

        assertNotNull(user.getId());
        assertFalse(user.getId().isBlank());
        assertEquals("Alice", user.getName());
        assertEquals("alice@example.com", user.getEmail());
        assertEquals(ProfileType.INDIVIDUAL, user.getProfileType());
        // password should be BCrypt hashed, not the raw value
        assertNotEquals("password123", user.getPassword());
        assertTrue(user.getPassword().startsWith("$2a$"));
        assertNotNull(user.getCreatedAt());
    }

    @Test
    void createUser_eachCallGeneratesUniqueId() {
        User u1 = UserFactory.createUser("A", "a@example.com", ProfileType.ADMIN, "password1");
        User u2 = UserFactory.createUser("B", "b@example.com", ProfileType.ADMIN, "password2");
        assertNotEquals(u1.getId(), u2.getId());
    }
}
