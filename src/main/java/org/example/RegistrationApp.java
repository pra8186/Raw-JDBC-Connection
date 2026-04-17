package org.example;

import org.example.model.ProfileType;
import org.example.model.User;
import org.example.model.UserFactory;
import org.example.repository.AppProperties;
import org.example.repository.DatabaseConnectionManager;
import org.example.repository.UserRepository;
import org.example.service.UserService;
import org.example.util.TableFormatter;
import org.example.util.TableFormatter.Column;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Menu-driven console application for managing users in the {@code userinfo.users} table.
 *
 * <p>Presents an interactive text menu that supports user registration,
 * lookup by ID or name, email updates, and sorted listing. Output is
 * formatted as fixed-width tables via {@link TableFormatter}.
 *
 * <p>The application initialises a HikariCP connection pool on startup
 * through {@link DatabaseConnectionManager} and shuts it down on exit.
 *
 * @see UserRepository
 * @see UserService
 * @see TableFormatter
 */
public final class RegistrationApp {

    /** Regex used to validate email addresses ({@code .com} and {@code .in} TLDs only). */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^.+@[a-zA-Z0-9.-]+\\.(com|in)$");

    /** Column layout for user tables without the {@code created_at} timestamp. */
    private static final List<Column<User>> USER_COLUMNS = List.of(
            new Column<>("ID", 36, User::getId),
            new Column<>("NAME", 20, User::getName),
            new Column<>("EMAIL", 30, User::getEmail),
            new Column<>("PROFILE TYPE", 14, User::getProfileType)
    );

    /** Column layout for user tables including the {@code created_at} timestamp. */
    private static final List<Column<User>> USER_COLUMNS_WITH_DATE = List.of(
            new Column<>("ID", 36, User::getId),
            new Column<>("NAME", 20, User::getName),
            new Column<>("EMAIL", 30, User::getEmail),
            new Column<>("PROFILE TYPE", 14, User::getProfileType),
            new Column<>("CREATED AT", 19, User::getCreatedAt)
    );

    /** Non-instantiable utility class. */
    private RegistrationApp() {}

    /**
     * Application entry point. Loads configuration, initialises the connection
     * pool, and enters the interactive menu loop.
     *
     * @param args command-line arguments (currently unused)
     */
    public static void main(String[] args) {
        AppProperties props;
        try {
            props = AppProperties.load();
        } catch (Exception e) {
            System.err.println("Error loading properties: " + e.getMessage());
            System.exit(1);
            return;
        }

        DatabaseConnectionManager dbManager = DatabaseConnectionManager.init(props);
        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            UserRepository repo = new UserRepository();
            UserService service = new UserService(repo);
            runLoop(scanner, repo, service);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } finally {
            dbManager.close();
        }
    }

    /**
     * Runs the interactive menu loop until the user selects exit.
     *
     * @param scanner console input source
     * @param repo    data-access object for user operations
     * @param service service layer providing sorted queries
     * @throws SQLException if an unrecoverable database error occurs
     */
    private static void runLoop(Scanner scanner, UserRepository repo, UserService service)
            throws SQLException {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            try {
                running = handleChoice(choice, scanner, repo, service);
            } catch (IllegalArgumentException ex) {
                System.out.println("Invalid input: " + ex.getMessage());
            } catch (SQLException ex) {
                printSqlException(ex);
            }
        }
    }

    /** Prints the numbered menu options to standard output. */
    private static void printMenu() {
        System.out.println();
        System.out.println("--- User Console ---");
        System.out.println("1  Register user");
        System.out.println("2  Find user by ID");
        System.out.println("3  Find users by name");
        System.out.println("4  Update user email");
        System.out.println("5  List all users (sorted)");
        System.out.println("0  Exit");
        System.out.print("Choice: ");
    }

    /**
     * Dispatches a menu selection to the appropriate handler.
     *
     * @param choice  the user's menu input (e.g. {@code "1"}, {@code "0"})
     * @param scanner console input source
     * @param repo    data-access object
     * @param service service layer
     * @return {@code true} to continue the loop, {@code false} to exit
     * @throws SQLException if a database operation fails
     */
    private static boolean handleChoice(String choice, Scanner scanner, UserRepository repo,
                                        UserService service) throws SQLException {
        return switch (choice) {
            case "1" -> {
                registerUser(scanner, repo);
                yield true;
            }
            case "2" -> {
                findById(scanner, repo);
                yield true;
            }
            case "3" -> {
                findByName(scanner, repo);
                yield true;
            }
            case "4" -> {
                updateEmail(scanner, repo);
                yield true;
            }
            case "5" -> {
                listUsersSorted(scanner, service);
                yield true;
            }
            case "0" -> {
                System.out.println("Goodbye.");
                yield false;
            }
            default -> {
                System.out.println("Unknown option.");
                yield true;
            }
        };
    }

    /**
     * Collects name, email, password, and profile type from the console,
     * then inserts a new user via the repository.
     *
     * @param scanner console input source
     * @param repo    data-access object
     * @throws SQLException if the insert fails
     */
    private static void registerUser(Scanner scanner, UserRepository repo) throws SQLException {
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();

        String email = readValidEmail(scanner);
        String password = readValidPassword(scanner);
        ProfileType profileType = readProfileType(scanner);

        User user = UserFactory.createUser(name, email, profileType, password);
        boolean created = repo.insertUser(user);
        if (created) {
            System.out.println(user);
        } else {
            System.out.println("User was not created.");
        }
    }

    /**
     * Prompts for a user ID and displays the matching record in a table.
     *
     * @param scanner console input source
     * @param repo    data-access object
     * @throws SQLException if the query fails
     */
    private static void findById(Scanner scanner, UserRepository repo) throws SQLException {
        System.out.print("User ID: ");
        String id = scanner.nextLine().trim();
        Optional<User> result = repo.findById(id);
        if (result.isPresent()) {
            TableFormatter.print(List.of(result.get()), USER_COLUMNS);
        } else {
            System.err.println("No user found with ID: " + id);
        }
    }

    /**
     * Prompts for a name and displays all matching users in a table.
     *
     * @param scanner console input source
     * @param repo    data-access object
     * @throws SQLException if the query fails
     */
    private static void findByName(Scanner scanner, UserRepository repo) throws SQLException {
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        List<User> users = repo.findAllByName(name);
        if (users.isEmpty()) {
            System.out.println("No users found with name: " + name);
            return;
        }
        TableFormatter.print(users, USER_COLUMNS);
    }

    /**
     * Prompts for a user ID and new email, then updates the record.
     *
     * @param scanner console input source
     * @param repo    data-access object
     * @throws SQLException if the update fails
     */
    private static void updateEmail(Scanner scanner, UserRepository repo) throws SQLException {
        System.out.print("User ID: ");
        String id = scanner.nextLine().trim();
        String newEmail = readValidEmail(scanner);
        boolean updated = repo.updateEmail(id, newEmail);
        System.out.println(updated ? "Email updated." : "No row updated (check user ID).");
    }

    /**
     * Prompts for a sort order (date or name), fetches all users via the
     * service layer, and displays the results in a table with timestamps.
     *
     * @param scanner console input source
     * @param service service layer for sorted queries
     * @throws SQLException if the query fails
     */
    private static void listUsersSorted(Scanner scanner, UserService service) throws SQLException {
        System.out.println("Sort by:");
        System.out.println("  1 / Date created (newest first)");
        System.out.println("  2 / Name (A-Z)");
        System.out.print("Choice: ");
        String sortChoice = scanner.nextLine().trim();

        List<User> users = switch (sortChoice) {
            case "1" -> service.getAllUsersSortedByDateDesc();
            case "2" -> service.getAllUsersSortedByName();
            default -> {
                System.out.println("Invalid sort option, using default (date descending).");
                yield service.getAllUsersSortedByDateDesc();
            }
        };
        TableFormatter.print(users, USER_COLUMNS_WITH_DATE);
    }

    /**
     * Prints a user-friendly message for common SQL state codes.
     *
     * @param ex the SQL exception to inspect
     */
    private static void printSqlException(SQLException ex) {
        String state = ex.getSQLState();
        if ("23505".equals(state)) {
            System.out.println("Constraint: duplicate value (unique key).");
        } else if ("23503".equals(state)) {
            System.out.println("Constraint: referenced row missing (foreign key).");
        } else if ("23514".equals(state)) {
            System.out.println("Constraint: check violation.");
        } else {
            System.out.println("Database error: " + ex.getMessage());
        }
    }

    /**
     * Prompts repeatedly until the user enters a valid email address.
     *
     * @param scanner console input source
     * @return trimmed, validated email string
     */
    private static String readValidEmail(Scanner scanner) {
        while (true) {
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();
            if (EMAIL_PATTERN.matcher(email).matches()) {
                return email;
            }
            System.out.println("Invalid email. Use a valid address (e.g. name@example.com).");
        }
    }

    /**
     * Prompts repeatedly until the user enters a password of at least 8 characters.
     *
     * @param scanner console input source
     * @return the raw password (hashing is handled by {@link UserFactory})
     */
    private static String readValidPassword(Scanner scanner) {
        while (true) {
            System.out.print("Password: ");
            String password = scanner.nextLine();
            if (password.length() >= 8) {
                return password;
            }
            System.out.println("Password must be at least 8 characters.");
        }
    }

    /**
     * Prompts repeatedly until the user selects a valid {@link ProfileType}.
     *
     * @param scanner console input source
     * @return the selected profile type
     */
    private static ProfileType readProfileType(Scanner scanner) {
        while (true) {
            System.out.println("Profile type — enter 1, 2, 3 or name:");
            System.out.println("  1 / INDIVIDUAL");
            System.out.println("  2 / TAX_PREPARER");
            System.out.println("  3 / ADMIN");
            System.out.print("Choice: ");
            String line = scanner.nextLine().trim();
            try {
                return ProfileType.parseConsoleChoice(line);
            } catch (IllegalArgumentException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
}
