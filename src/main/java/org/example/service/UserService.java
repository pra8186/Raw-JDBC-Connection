package org.example.service;

import org.example.model.User;
import org.example.repository.UserRepository;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

/**
 * Service layer for {@link User} operations.
 * Delegates data access to {@link UserRepository} and applies
 * in-memory sorting using {@link Comparator}.
 */
public class UserService {

    private final UserRepository repository;

    /**
     * Creates a service backed by the given repository.
     *
     * @param repository the data-access object to delegate queries to
     */
    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    /**
     * Returns all users sorted by {@code created_at} descending (newest first).
     *
     * @return sorted list of users
     * @throws SQLException if the query fails
     */
    public List<User> getAllUsersSortedByDateDesc() throws SQLException {
        List<User> users = repository.findAll();
        users.sort(Comparator.comparing(User::getCreatedAt).reversed());
        return users;
    }

    /**
     * Returns all users sorted by {@code name} alphabetically (A–Z, case-insensitive).
     *
     * @return sorted list of users
     * @throws SQLException if the query fails
     */
    public List<User> getAllUsersSortedByName() throws SQLException {
        List<User> users = repository.findAll();
        users.sort(Comparator.comparing(User::getName, String.CASE_INSENSITIVE_ORDER));
        return users;
    }
}
