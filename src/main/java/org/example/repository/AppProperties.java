package org.example.repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * Immutable holder for database connection and HikariCP pool configuration.
 *
 * <p>Properties are loaded from {@code application.properties} on the classpath,
 * with environment-variable overrides for JDBC URL, user, and password.
 *
 * @see DatabaseConnectionManager
 */
public final class AppProperties {

    private final String jdbcUrl;
    private final String dbUser;
    private final String dbPassword;

    // HikariCP pool settings
    private final int hikariMaxPoolSize;
    private final long hikariConnectionTimeout;
    private final long hikariIdleTimeout;
    private final int hikariMinIdle;

    private AppProperties(String jdbcUrl, String dbUser, String dbPassword,
                          int hikariMaxPoolSize, long hikariConnectionTimeout,
                          long hikariIdleTimeout, int hikariMinIdle) {
        this.jdbcUrl = jdbcUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.hikariMaxPoolSize = hikariMaxPoolSize;
        this.hikariConnectionTimeout = hikariConnectionTimeout;
        this.hikariIdleTimeout = hikariIdleTimeout;
        this.hikariMinIdle = hikariMinIdle;
    }

    /**
     * Loads configuration from {@code application.properties} on the classpath.
     * Environment variables {@code JDBC}, {@code JDBC_DB_USER}, and
     * {@code JDBC_DB_PASSWORD} override file-based values when set.
     *
     * @return a fully populated {@code AppProperties} instance
     * @throws IOException if the properties file is missing or required values are blank
     */
    public static AppProperties load() throws IOException {
        Properties p = new Properties();
        try (InputStream in = AppProperties.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (in == null) {
                throw new IOException("application.properties not found on classpath.");
            }
            p.load(in);
        }
        String url = firstNonBlank(System.getenv("JDBC"), p.getProperty("db.url"));
        String user = firstNonBlank(System.getenv("JDBC_DB_USER"), p.getProperty("db.user"));
        String pass = Objects.requireNonNullElse(
                System.getenv("JDBC_DB_PASSWORD"),
                p.getProperty("db.password", "")
        );
        if (url == null || url.isBlank()) {
            throw new IOException("db.url is not set.");
        }
        if (user == null || user.isBlank()) {
            throw new IOException("db.user is not set.");
        }
        int maxPool = Integer.parseInt(p.getProperty("hikari.maximumPoolSize", "5"));
        long connTimeout = Long.parseLong(p.getProperty("hikari.connectionTimeout", "30000"));
        long idleTimeout = Long.parseLong(p.getProperty("hikari.idleTimeout", "180000"));
        int minIdle = Integer.parseInt(p.getProperty("hikari.minimumIdle", "2"));

        return new AppProperties(url.trim(), user.trim(), pass == null ? "" : pass,
                maxPool, connTimeout, idleTimeout, minIdle);
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        return b;
    }


    /** @return the JDBC connection URL */
    public String jdbcUrl() {
        return jdbcUrl;
    }


    /** @return the database username */
    public String dbUser() {
        return dbUser;
    }


    /** @return the database password (may be empty) */
    public String dbPassword() {
        return dbPassword;
    }

    /** @return maximum number of connections in the HikariCP pool */
    public int hikariMaxPoolSize() {
        return hikariMaxPoolSize;
    }

    /** @return maximum wait time in milliseconds for a connection from the pool */
    public long hikariConnectionTimeout() {
        return hikariConnectionTimeout;
    }

    /** @return time in milliseconds an idle connection may sit in the pool before being retired */
    public long hikariIdleTimeout() {
        return hikariIdleTimeout;
    }

    /** @return minimum number of idle connections the pool maintains */
    public int hikariMinIdle() {
        return hikariMinIdle;
    }
}
