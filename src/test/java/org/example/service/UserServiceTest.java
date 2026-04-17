package org.example.service;

import org.example.model.ProfileType;
import org.example.model.User;
import org.example.repository.AppProperties;
import org.example.repository.DatabaseConnectionManager;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link UserService} in-memory sorting against an H2 database.
 *
 * <p>Verifies that {@code getAllUsersSortedByDateDesc} returns newest-first order
 * and that {@code getAllUsersSortedByName} returns case-insensitive alphabetical order.
 */
class UserServiceTest {

    private static UserRepository repo;
    private static UserService service;

    /** Initialises pool, schema, repository, and service. */
    @BeforeAll
    static void initPool() throws Exception {
        AppProperties props = AppProperties.load();
        DatabaseConnectionManager.init(props);
        runSchema();
        repo = new UserRepository();
        service = new UserService(repo);
    }

    /** Shuts down the connection pool after all tests. */
    @AfterAll
    static void tearDown() {
        DatabaseConnectionManager.reset();
    }

    /** Clears the users table before each test. */
    @BeforeEach
    void clearTable() throws SQLException {
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM userinfo.users");
        }
    }

    /** Executes the H2 DDL from the test classpath. */
    private static void runSchema() throws SQLException, IOException {
        String sql;
        try (InputStream is = UserServiceTest.class.getClassLoader()
                .getResourceAsStream("h2-schema.sql")) {
            assertNotNull(is);
            sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    /** Factory helper that creates a {@link User} with a random UUID. */
    private User newUser(String name, String email, ProfileType type, LocalDateTime createdAt) {
        return new User(UUID.randomUUID().toString(), name, email, type,
                "hashed_pw", createdAt);
    }

    // ---- sortByDateDesc ----

    @Test
    void getAllUsersSortedByDateDesc_emptyTable_returnsEmptyList() throws SQLException {
        List<User> result = service.getAllUsersSortedByDateDesc();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllUsersSortedByDateDesc_returnsNewestFirst() throws SQLException {
        User oldest = newUser("Alice", "alice@example.com", ProfileType.INDIVIDUAL,
                LocalDateTime.of(2025, 1, 1, 0, 0));
        User middle = newUser("Bob", "bob@example.com", ProfileType.ADMIN,
                LocalDateTime.of(2025, 6, 15, 12, 0));
        User newest = newUser("Carol", "carol@example.com", ProfileType.TAX_PREPARER,
                LocalDateTime.of(2026, 3, 10, 8, 30));

        repo.insertUser(oldest);
        repo.insertUser(middle);
        repo.insertUser(newest);

        List<User> result = service.getAllUsersSortedByDateDesc();

        assertEquals(3, result.size());
        assertEquals("Carol", result.get(0).getName());
        assertEquals("Bob", result.get(1).getName());
        assertEquals("Alice", result.get(2).getName());
    }

    @Test
    void getAllUsersSortedByDateDesc_sameDate_returnsAll() throws SQLException {
        LocalDateTime same = LocalDateTime.of(2026, 4, 1, 12, 0);
        repo.insertUser(newUser("Dan", "dan@example.com", ProfileType.INDIVIDUAL, same));
        repo.insertUser(newUser("Eve", "eve@example.com", ProfileType.ADMIN, same));

        List<User> result = service.getAllUsersSortedByDateDesc();
        assertEquals(2, result.size());
    }

    // ---- sortByName ----

    @Test
    void getAllUsersSortedByName_emptyTable_returnsEmptyList() throws SQLException {
        List<User> result = service.getAllUsersSortedByName();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllUsersSortedByName_returnsAlphabeticalOrder() throws SQLException {
        repo.insertUser(newUser("Charlie", "charlie@example.com", ProfileType.ADMIN,
                LocalDateTime.now()));
        repo.insertUser(newUser("Alice", "alice@example.com", ProfileType.INDIVIDUAL,
                LocalDateTime.now()));
        repo.insertUser(newUser("Bob", "bob@example.com", ProfileType.TAX_PREPARER,
                LocalDateTime.now()));

        List<User> result = service.getAllUsersSortedByName();

        assertEquals(3, result.size());
        assertEquals("Alice", result.get(0).getName());
        assertEquals("Bob", result.get(1).getName());
        assertEquals("Charlie", result.get(2).getName());
    }

    @Test
    void getAllUsersSortedByName_caseInsensitive() throws SQLException {
        repo.insertUser(newUser("bob", "bob@example.com", ProfileType.INDIVIDUAL,
                LocalDateTime.now()));
        repo.insertUser(newUser("Alice", "alice@example.com", ProfileType.ADMIN,
                LocalDateTime.now()));
        repo.insertUser(newUser("carol", "carol@example.com", ProfileType.TAX_PREPARER,
                LocalDateTime.now()));

        List<User> result = service.getAllUsersSortedByName();

        assertEquals(3, result.size());
        assertEquals("Alice", result.get(0).getName());
        assertEquals("bob", result.get(1).getName());
        assertEquals("carol", result.get(2).getName());
    }

    @Test
    void getAllUsersSortedByName_singleUser_returnsList() throws SQLException {
        repo.insertUser(newUser("Zara", "zara@example.com", ProfileType.INDIVIDUAL,
                LocalDateTime.now()));

        List<User> result = service.getAllUsersSortedByName();
        assertEquals(1, result.size());
        assertEquals("Zara", result.get(0).getName());
    }
}
