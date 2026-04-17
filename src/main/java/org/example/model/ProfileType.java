package org.example.model;

/**
 * High-level user roles supported by registration and persistence.
 */
public enum ProfileType {
    /** End-user / taxpayer-facing profile. */
    INDIVIDUAL,
    /** Professional preparer role. */
    TAX_PREPARER,
    /** Administrative access. */
    ADMIN;

    /**
     * Parses console input such as {@code 1}, {@code 2}, {@code 3}, or enum names.
     *
     * @param line raw user input (trimmed internally)
     * @return matching enum constant
     * @throws IllegalArgumentException if blank or unrecognized
     */
    public static ProfileType parseConsoleChoice(String line) {
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("Profile type is required");
        }
        String normalized = line.trim().toUpperCase().replace('-', '_').replace(' ', '_');
        return switch (normalized) {
            case "1", "INDIVIDUAL" -> INDIVIDUAL;
            case "2", "TAX_PREPARER", "TAXPREPARER" -> TAX_PREPARER;
            case "3", "ADMIN" -> ADMIN;
            default -> throw new IllegalArgumentException("Unknown profile type: " + line);
        };
    }
}
