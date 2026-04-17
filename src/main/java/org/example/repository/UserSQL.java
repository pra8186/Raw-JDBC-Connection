package org.example.repository;

/**
 * Centralised SQL constants for the {@code userinfo.users} table.
 *
 * <p>All queries use explicit column names (not {@code SELECT *}) so that
 * the Java layer stays decoupled from column-ordering changes in the schema.
 *
 * @see UserRepository
 */
public final class UserSQL {

    /** Non-instantiable constants class. */
    private UserSQL() {}

    /** Inserts a new user row including the {@code created_at} timestamp. */
    public static final String INSERT =
            "INSERT INTO userinfo.users (id, name, email, password, profile_type, created_at) VALUES (?, ?, ?, ?, ?, ?)";

    /** Selects a single user by primary key ({@code id}). */
    public static final String FIND_BY_ID =
            "SELECT id, name, email, password, profile_type, created_at FROM userinfo.users WHERE id = ?";

    /** Selects all users whose {@code name} matches the bind parameter exactly. */
    public static final String FIND_ALL_BY_NAME =
            "SELECT id, name, email, password, profile_type, created_at FROM userinfo.users WHERE name = ?";

    /** Selects every user row (unsorted). */
    public static final String FIND_ALL =
            "SELECT id, name, email, password, profile_type, created_at FROM userinfo.users";

    /** Updates the {@code email} column for the user identified by {@code id}. */
    public static final String UPDATE_EMAIL =
            "UPDATE userinfo.users SET email = ? WHERE id = ?";
}
