package org.example.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ProfileType#parseConsoleChoice(String)}.
 *
 * <p>Uses parameterized tests to verify numeric shortcuts ({@code "1"},
 * {@code "2"}, {@code "3"}), case-insensitive enum names, and error
 * handling for {@code null}, blank, and unknown inputs.
 */
class ProfileTypeTest {

    @ParameterizedTest
    @CsvSource({
            "1, INDIVIDUAL",
            "2, TAX_PREPARER",
            "3, ADMIN",
            "INDIVIDUAL, INDIVIDUAL",
            "TAX_PREPARER, TAX_PREPARER",
            "ADMIN, ADMIN",
            "individual, INDIVIDUAL",
            "admin, ADMIN",
            "tax_preparer, TAX_PREPARER",
            "TAXPREPARER, TAX_PREPARER"
    })
    void parseConsoleChoice_validInputs(String input, ProfileType expected) {
        assertEquals(expected, ProfileType.parseConsoleChoice(input));
    }

    @Test
    void parseConsoleChoice_null_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> ProfileType.parseConsoleChoice(null));
    }

    @Test
    void parseConsoleChoice_blank_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> ProfileType.parseConsoleChoice("   "));
    }

    @Test
    void parseConsoleChoice_unknown_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ProfileType.parseConsoleChoice("UNKNOWN"));
        assertTrue(ex.getMessage().contains("UNKNOWN"));
    }
}
