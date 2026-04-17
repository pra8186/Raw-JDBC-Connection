package org.example.repository;

import org.example.model.ProfileType;
import org.example.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO for {@code userinfo.users}. All methods use try-with-resources
 * for connection, statement, and result-set lifecycle.
 */
public class UserRepository {

    /**
     * Constructs a repository. Connections are obtained from the HikariCP
     * pool via {@link DatabaseConnectionManager#getConnection()}.
     */
    public UserRepository() {
    }

    /**
     * Inserts a user row.
     *
     * @param user entity to persist
     * @return true if the row was created
     * @throws SQLException if the insert fails
     */
    public boolean insertUser(User user) throws SQLException {
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(UserSQL.INSERT)) {
            stmt.setString(1, user.getId());
            stmt.setString(2, user.getName());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPassword());
            stmt.setString(5, user.getProfileType().toString());
            stmt.setTimestamp(6, Timestamp.valueOf(user.getCreatedAt()));
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Finds a user by primary key.
     *
     * @param id user UUID
     * @return the user, or empty if not found
     * @throws SQLException if the query fails
     */
    public Optional<User> findById(String id) throws SQLException {
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(UserSQL.FIND_BY_ID)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        }
    }

    /**
     * Finds all users matching the given name.
     *
     * @param name display name to search for
     * @return list of matching users (may be empty)
     * @throws SQLException if the query fails
     */
    public List<User> findAllByName(String name) throws SQLException {
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(UserSQL.FIND_ALL_BY_NAME)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRow(rs));
                }
            }
        }
        return users;
    }

    /**
     * Fetches all users (unsorted).
     *
     * @return list of all users (may be empty)
     * @throws SQLException if the query fails
     */
    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(UserSQL.FIND_ALL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        }
        return users;
    }

    /**
     * Updates the email for the user with the given ID.
     *
     * @param id       user UUID
     * @param newEmail new email address
     * @return true if a row was updated
     * @throws SQLException if the update fails
     */
    public boolean updateEmail(String id, String newEmail) throws SQLException {
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(UserSQL.UPDATE_EMAIL)) {
            stmt.setString(1, newEmail);
            stmt.setString(2, id);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Maps the current row of a {@link ResultSet} to a {@link User} instance.
     *
     * @param rs result set positioned on a valid row
     * @return a fully populated {@link User}
     * @throws SQLException if a column cannot be read
     */
    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("email"),
                ProfileType.valueOf(rs.getString("profile_type")),
                rs.getString("password"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
