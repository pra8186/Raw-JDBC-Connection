package org.example.repository;

import org.example.model.ProfileType;
import org.example.model.User;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link UserRepository} against an H2 in-memory database.
 *
 * <p>Covers all DAO methods: {@code insertUser}, {@code findById},
 * {@code findAllByName}, {@code findAll}, and {@code updateEmail},
 * including happy paths, not-found cases, duplicate-key violations,
 * and field-integrity checks after updates.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserRepositoryTest {

    private static UserRepository repo;

    /** Initialises the HikariCP pool, creates the H2 schema, and instantiates the repository. */
    @BeforeAll
    static void initPool() throws Exception {
        AppProperties props = AppProperties.load();
        DatabaseConnectionManager.init(props);
        runSchema();
        repo = new UserRepository();
    }

    /** Shuts down the connection pool after all tests in this class. */
    @AfterAll
    static void tearDown() {
        DatabaseConnectionManager.reset();
    }

    /** Deletes all rows from {@code userinfo.users} so each test starts with a clean slate. */
    @BeforeEach
    void clearTable() throws SQLException {
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM userinfo.users");
        }
    }

    /** Reads and executes the H2-compatible DDL from the test classpath. */
    private static void runSchema() throws SQLException, IOException {
        String sql;
        try (InputStream is = UserRepositoryTest.class.getClassLoader()
                .getResourceAsStream("h2-schema.sql")) {
            assertNotNull(is, "h2-schema.sql not found on test classpath");
            sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    /** Factory helper that creates a {@link User} with a random UUID. */
    private User newUser(String name, String email, ProfileType type, LocalDateTime createdAt) {
        String id = UUID.randomUUID().toString();
        return new User(id, name, email, type, "hashed_password_placeholder", createdAt);
    }

    // ---- insertUser ----

    @Test
    void insertUser_returnsTrue() throws SQLException {
        User user = newUser("Alice", "alice@example.com", ProfileType.INDIVIDUAL, LocalDateTime.now());
        assertTrue(repo.insertUser(user));
    }

    @Test
    void insertUser_duplicateId_throwsSQLException() throws SQLException {
        User user = newUser("Bob", "bob@example.com", ProfileType.ADMIN, LocalDateTime.now());
        repo.insertUser(user);
        User duplicate = new User(user.getId(), "Bob2", "bob2@example.com",
                ProfileType.ADMIN, "password", LocalDateTime.now());
        assertThrows(SQLException.class, () -> repo.insertUser(duplicate));
    }

    @Test
    void insertUser_duplicateEmail_throwsSQLException() throws SQLException {
        User user1 = newUser("Carol", "carol@example.com", ProfileType.TAX_PREPARER, LocalDateTime.now());
        repo.insertUser(user1);
        User user2 = newUser("Dave", "carol@example.com", ProfileType.INDIVIDUAL, LocalDateTime.now());
        assertThrows(SQLException.class, () -> repo.insertUser(user2));
    }

    // ---- findById ----

    @Test
    void findById_existingUser_returnsUser() throws SQLException {
        User user = newUser("Eve", "eve@example.com", ProfileType.INDIVIDUAL, LocalDateTime.of(2026, 1, 15, 10, 30));
        repo.insertUser(user);

        Optional<User> result = repo.findById(user.getId());

        assertTrue(result.isPresent());
        User found = result.get();
        assertEquals(user.getId(), found.getId());
        assertEquals("Eve", found.getName());
        assertEquals("eve@example.com", found.getEmail());
        assertEquals(ProfileType.INDIVIDUAL, found.getProfileType());
        assertEquals("hashed_password_placeholder", found.getPassword());
        assertEquals(LocalDateTime.of(2026, 1, 15, 10, 30), found.getCreatedAt());
    }

    @Test
    void findById_nonExistingId_returnsEmpty() throws SQLException {
        Optional<User> result = repo.findById("non-existent-id");
        assertTrue(result.isEmpty());
    }

    @Test
    void findById_afterMultipleInserts_returnsCorrectUser() throws SQLException {
        User user1 = newUser("Frank", "frank@example.com", ProfileType.ADMIN, LocalDateTime.now());
        User user2 = newUser("Grace", "grace@example.com", ProfileType.TAX_PREPARER, LocalDateTime.now());
        repo.insertUser(user1);
        repo.insertUser(user2);

        Optional<User> result = repo.findById(user2.getId());
        assertTrue(result.isPresent());
        assertEquals("Grace", result.get().getName());
    }

    // ---- findAllByName ----

    @Test
    void findAllByName_emptyTable_returnsEmptyList() throws SQLException {
        List<User> result = repo.findAllByName("Nobody");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findAllByName_noMatch_returnsEmptyList() throws SQLException {
        repo.insertUser(newUser("Hank", "hank@example.com", ProfileType.INDIVIDUAL, LocalDateTime.now()));
        List<User> result = repo.findAllByName("NoSuchName");
        assertTrue(result.isEmpty());
    }

    @Test
    void findAllByName_singleMatch_returnsOneUser() throws SQLException {
        repo.insertUser(newUser("Ivy", "ivy@example.com", ProfileType.ADMIN, LocalDateTime.now()));
        repo.insertUser(newUser("Jack", "jack@example.com", ProfileType.INDIVIDUAL, LocalDateTime.now()));

        List<User> result = repo.findAllByName("Ivy");
        assertEquals(1, result.size());
        assertEquals("Ivy", result.get(0).getName());
    }

    @Test
    void findAllByName_multipleMatches_returnsAll() throws SQLException {
        repo.insertUser(newUser("Sam", "sam1@example.com", ProfileType.INDIVIDUAL, LocalDateTime.now()));
        repo.insertUser(newUser("Sam", "sam2@example.com", ProfileType.TAX_PREPARER, LocalDateTime.now()));
        repo.insertUser(newUser("Sam", "sam3@example.com", ProfileType.ADMIN, LocalDateTime.now()));
        repo.insertUser(newUser("Other", "other@example.com", ProfileType.INDIVIDUAL, LocalDateTime.now()));

        List<User> result = repo.findAllByName("Sam");
        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(u -> "Sam".equals(u.getName())));
    }

    // ---- findAll ----

    @Test
    void findAll_emptyTable_returnsEmptyList() throws SQLException {
        List<User> result = repo.findAll();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_multipleUsers_returnsAll() throws SQLException {
        repo.insertUser(newUser("Kate", "kate@example.com", ProfileType.INDIVIDUAL, LocalDateTime.now()));
        repo.insertUser(newUser("Leo", "leo@example.com", ProfileType.ADMIN, LocalDateTime.now()));
        repo.insertUser(newUser("Mia", "mia@example.com", ProfileType.TAX_PREPARER, LocalDateTime.now()));

        List<User> result = repo.findAll();
        assertEquals(3, result.size());
    }

    // ---- updateEmail ----

    @Test
    void updateEmail_existingUser_returnsTrue() throws SQLException {
        User user = newUser("Nina", "nina@example.com", ProfileType.INDIVIDUAL, LocalDateTime.now());
        repo.insertUser(user);

        boolean updated = repo.updateEmail(user.getId(), "nina.new@example.com");
        assertTrue(updated);

        Optional<User> result = repo.findById(user.getId());
        assertTrue(result.isPresent());
        assertEquals("nina.new@example.com", result.get().getEmail());
    }

    @Test
    void updateEmail_nonExistingUser_returnsFalse() throws SQLException {
        boolean updated = repo.updateEmail("non-existent-id", "nobody@example.com");
        assertFalse(updated);
    }

    @Test
    void updateEmail_doesNotChangeOtherFields() throws SQLException {
        LocalDateTime created = LocalDateTime.of(2026, 3, 20, 14, 0);
        User user = newUser("Oscar", "oscar@example.com", ProfileType.TAX_PREPARER, created);
        repo.insertUser(user);

        repo.updateEmail(user.getId(), "oscar.updated@example.com");

        User found = repo.findById(user.getId()).orElseThrow();
        assertEquals("Oscar", found.getName());
        assertEquals(ProfileType.TAX_PREPARER, found.getProfileType());
        assertEquals("hashed_password_placeholder", found.getPassword());
        assertEquals(created, found.getCreatedAt());
    }

    @Test
    void updateEmail_toDuplicateEmail_throwsSQLException() throws SQLException {
        repo.insertUser(newUser("Pat", "pat@example.com", ProfileType.INDIVIDUAL, LocalDateTime.now()));
        User quinn = newUser("Quinn", "quinn@example.com", ProfileType.ADMIN, LocalDateTime.now());
        repo.insertUser(quinn);

        assertThrows(SQLException.class, () -> repo.updateEmail(quinn.getId(), "pat@example.com"));
    }
}
