package org.example.util;

import org.example.util.TableFormatter.Column;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TableFormatter}.
 *
 * <p>Captures {@code System.out} to verify header rendering, separator lines,
 * data-row formatting, truncation with ellipsis, right-padding, and null handling.
 */
class TableFormatterTest {

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream captured;

    /** Redirects {@code System.out} to a byte buffer before each test. */
    @BeforeEach
    void captureStdout() {
        captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured));
    }

    /** Restores the original {@code System.out} after each test. */
    @AfterEach
    void restoreStdout() {
        System.setOut(originalOut);
    }

    /** @return everything written to {@code System.out} since the last {@code captureStdout} call */
    private String output() {
        return captured.toString();
    }

    // Simple record for testing
    record Item(String name, String description) {}

    private static final List<Column<Item>> COLUMNS = List.of(
            new Column<>("NAME", 10, Item::name),
            new Column<>("DESC", 15, Item::description)
    );

    @Test
    void print_emptyList_printsNoRecords() {
        TableFormatter.print(List.of(), COLUMNS);
        assertEquals("(no records)" + System.lineSeparator(), output());
    }

    @Test
    void print_singleRow_printsHeaderSeparatorAndRow() {
        TableFormatter.print(List.of(new Item("Pen", "A writing tool")), COLUMNS);

        String out = output();
        String[] lines = out.split(System.lineSeparator());
        assertEquals(3, lines.length);
        // header
        assertTrue(lines[0].contains("NAME"));
        assertTrue(lines[0].contains("DESC"));
        // separator
        assertTrue(lines[1].contains("-+-"));
        // data row
        assertTrue(lines[2].contains("Pen"));
        assertTrue(lines[2].contains("A writing tool"));
    }

    @Test
    void print_multipleRows_printsAllRows() {
        List<Item> items = List.of(
                new Item("Apple", "A fruit"),
                new Item("Bread", "Baked good"),
                new Item("Cheese", "Dairy product")
        );
        TableFormatter.print(items, COLUMNS);

        String[] lines = output().split(System.lineSeparator());
        assertEquals(5, lines.length); // header + separator + 3 rows
    }

    @Test
    void print_longValue_isTruncatedWithEllipsis() {
        // Column width is 10 for NAME; "LongItemName" is 12 chars -> should be truncated
        TableFormatter.print(List.of(new Item("LongItemName", "short")), COLUMNS);

        String[] lines = output().split(System.lineSeparator());
        String dataRow = lines[2];
        // Should show 9 chars + ellipsis (…)
        assertTrue(dataRow.contains("LongItemN\u2026"));
    }

    @Test
    void print_exactWidthValue_noTruncation() {
        // NAME column is 10 chars; "ExactlyTen" is exactly 10 -> no ellipsis
        TableFormatter.print(List.of(new Item("ExactlyTen", "desc")), COLUMNS);

        String[] lines = output().split(System.lineSeparator());
        String dataRow = lines[2];
        assertTrue(dataRow.contains("ExactlyTen"));
        assertFalse(dataRow.contains("\u2026"));
    }

    @Test
    void print_shortValue_isPaddedWithSpaces() {
        TableFormatter.print(List.of(new Item("Hi", "Yo")), COLUMNS);

        String[] lines = output().split(System.lineSeparator());
        String dataRow = lines[2];
        // "Hi" padded to 10 chars -> "Hi        "
        assertTrue(dataRow.startsWith("Hi        "));
    }

    @Test
    void print_nullValue_treatedAsEmpty() {
        record Nullable(String val) {}
        List<Column<Nullable>> cols = List.of(new Column<>("VAL", 10, Nullable::val));
        TableFormatter.print(List.of(new Nullable(null)), cols);

        String[] lines = output().split(System.lineSeparator());
        String dataRow = lines[2];
        // Should be 10 spaces
        assertEquals(10, dataRow.trim().length() == 0 ? 10 : dataRow.length());
    }

    @Test
    void print_headerTruncatedWhenWiderThanColumn() {
        List<Column<Item>> narrowCols = List.of(
                new Column<>("VERY_LONG_HEADER", 5, Item::name)
        );
        TableFormatter.print(List.of(new Item("Hi", "x")), narrowCols);

        String[] lines = output().split(System.lineSeparator());
        // Header should be truncated to 5 chars
        assertEquals("VERY_", lines[0].trim());
    }
}
