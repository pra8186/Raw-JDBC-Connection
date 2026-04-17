package org.example.util;

import java.util.List;
import java.util.function.Function;

/**
 * Prints a list of records as a fixed-width console table with column headers.
 * Long values are truncated with an ellipsis ({@code …}).
 *
 * <p>Usage example:
 * <pre>{@code
 * List<TableFormatter.Column<User>> cols = List.of(
 *     new TableFormatter.Column<>("NAME", 20, User::getName),
 *     new TableFormatter.Column<>("EMAIL", 30, User::getEmail)
 * );
 * TableFormatter.print(users, cols);
 * }</pre>
 */
public final class TableFormatter {

    private static final String ELLIPSIS = "\u2026";
    private static final String COLUMN_SEPARATOR = " | ";
    private static final String DASH_SEPARATOR = "-+-";

    private TableFormatter() {}

    /**
     * Defines a single table column.
     *
     * @param <T>       row type
     * @param header    column header text
     * @param width     fixed character width
     * @param extractor function to extract the display value from a row
     */
    public record Column<T>(String header, int width, Function<T, Object> extractor) {}

    /**
     * Prints records as a fixed-width table to {@link System#out}.
     *
     * @param rows    list of records (prints "(no records)" if empty)
     * @param columns column definitions
     * @param <T>     row type
     */
    public static <T> void print(List<T> rows, List<Column<T>> columns) {
        if (rows.isEmpty()) {
            System.out.println("(no records)");
            return;
        }

        printHeader(columns);
        printSeparator(columns);
        for (T row : rows) {
            printRow(row, columns);
        }
    }

    private static <T> void printHeader(List<Column<T>> columns) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) sb.append(COLUMN_SEPARATOR);
            Column<T> col = columns.get(i);
            sb.append(pad(col.header(), col.width()));
        }
        System.out.println(sb);
    }

    private static <T> void printSeparator(List<Column<T>> columns) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) sb.append(DASH_SEPARATOR);
            sb.append("-".repeat(columns.get(i).width()));
        }
        System.out.println(sb);
    }

    private static <T> void printRow(T row, List<Column<T>> columns) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) sb.append(COLUMN_SEPARATOR);
            Column<T> col = columns.get(i);
            Object value = col.extractor().apply(row);
            String text = value == null ? "" : value.toString();
            sb.append(truncateAndPad(text, col.width()));
        }
        System.out.println(sb);
    }

    /**
     * Truncates the value to fit within {@code width}, appending an ellipsis
     * if truncated, then right-pads with spaces to fill the column.
     */
    private static String truncateAndPad(String value, int width) {
        if (value.length() > width) {
            return value.substring(0, width - 1) + ELLIPSIS;
        }
        return pad(value, width);
    }

    private static String pad(String value, int width) {
        if (value.length() >= width) {
            return value.substring(0, width);
        }
        return value + " ".repeat(width - value.length());
    }
}
