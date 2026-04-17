package org.example.repository;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Manages a HikariCP connection pool configured via {@link AppProperties}.
 * Replaces the former singleton raw-connection approach with a proper pool.
 */
public class DatabaseConnectionManager {

    private static DatabaseConnectionManager INSTANCE;
    private final HikariDataSource dataSource;

    /**
     * Creates the HikariCP data source from the supplied properties.
     *
     * @param props application properties containing JDBC URL, credentials,
     *              and pool tuning parameters
     */
    private DatabaseConnectionManager(AppProperties props) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.jdbcUrl());
        config.setUsername(props.dbUser());
        config.setPassword(props.dbPassword());

        config.setMaximumPoolSize(props.hikariMaxPoolSize());
        config.setConnectionTimeout(props.hikariConnectionTimeout());
        config.setIdleTimeout(props.hikariIdleTimeout());
        config.setMinimumIdle(props.hikariMinIdle());

        dataSource = new HikariDataSource(config);
    }

    /**
     * Initializes the singleton with the given properties.
     * Must be called once at application startup before {@link #getInstance()}.
     *
     * @param props application properties containing DB and pool configuration
     * @return the initialized manager instance
     */
    public static synchronized DatabaseConnectionManager init(AppProperties props) {
        if (INSTANCE == null) {
            INSTANCE = new DatabaseConnectionManager(props);
        }
        return INSTANCE;
    }

    /**
     * @return the sole manager instance
     * @throws IllegalStateException if {@link #init(AppProperties)} has not been called
     */
    public static synchronized DatabaseConnectionManager getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("DatabaseConnectionManager not initialized. Call init(AppProperties) first.");
        }
        return INSTANCE;
    }

    /**
     * Returns a connection from the pool.
     * Callers <b>must</b> close it (which returns it to the pool, not
     * a physical close).
     *
     * @return a pooled JDBC connection
     * @throws SQLException if the pool cannot supply a connection
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /** Shuts down the connection pool. Call once at application exit. */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    /**
     * Closes the pool and clears the singleton so it can be re-initialized.
     * Intended for test teardown only.
     */
    public static synchronized void reset() {
        if (INSTANCE != null) {
            INSTANCE.close();
            INSTANCE = null;
        }
    }
}
