package org.example.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link User} domain model.
 *
 * <p>Verifies that the database-reconstruction constructor preserves all fields
 * and that {@link User#toString()} includes the key identifiers.
 */
class UserTest {

    @Test
    void dbConstructor_preservesAllFields() {
        LocalDateTime ts = LocalDateTime.of(2026, 4, 10, 9, 0);
        User user = new User("id-123", "Alice", "alice@example.com",
                ProfileType.INDIVIDUAL, "hashed", ts);

        assertEquals("id-123", user.getId());
        assertEquals("Alice", user.getName());
        assertEquals("alice@example.com", user.getEmail());
        assertEquals(ProfileType.INDIVIDUAL, user.getProfileType());
        assertEquals("hashed", user.getPassword());
        assertEquals(ts, user.getCreatedAt());
    }

    @Test
    void toString_containsNameEmailAndId() {
        User user = new User("id-456", "Bob", "bob@example.com",
                ProfileType.ADMIN, "pw", LocalDateTime.now());

        String str = user.toString();
        assertTrue(str.contains("Bob"));
        assertTrue(str.contains("bob@example.com"));
        assertTrue(str.contains("id-456"));
        assertTrue(str.contains("ADMIN"));
    }
}
