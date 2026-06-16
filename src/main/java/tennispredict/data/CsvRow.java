package tennispredict.data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * One CSV data row, addressable by column name. Wraps the raw split fields plus
 * the header's name-to-index map, and centralizes all the messy string-to-type
 * conversion — including the missing-data policy (blank/unparseable int cells
 * become {@link RawMatch#MISSING}). Keeping this logic in one place means the
 * row-mapping functions stay short and readable.
 */
public class CsvRow {

    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final String[] fields;
    private final Map<String, Integer> columnIndex;

    public CsvRow(String[] fields, Map<String, Integer> columnIndex) {
        this.fields = fields;
        this.columnIndex = columnIndex;
    }

    /** Raw cell text for a column, or "" if the column/cell is absent. */
    public String getString(String column) {
        Integer i = columnIndex.get(column);
        if (i == null || i >= fields.length) {
            return "";
        }
        return fields[i].strip();
    }

    /** Integer value of a column, or {@link RawMatch#MISSING} if blank/unparseable. */
    public int getIntOrMissing(String column) {
        String s = getString(column);
        if (s.isEmpty()) {
            return RawMatch.MISSING;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return RawMatch.MISSING;
        }
    }

    /**
     * Some columns (e.g. age) are decimals in the CSV but we store them as ints.
     * Parse as a double and round, or {@link RawMatch#MISSING} if blank/bad.
     */
    public int getRoundedIntOrMissing(String column) {
        String s = getString(column);
        if (s.isEmpty()) {
            return RawMatch.MISSING;
        }
        try {
            return (int) Math.round(Double.parseDouble(s));
        } catch (NumberFormatException e) {
            return RawMatch.MISSING;
        }
    }

    /** First character, upper-cased; {@code fallback} if the cell is blank. */
    public char getChar(String column, char fallback) {
        String s = getString(column);
        if (s.isEmpty()) {
            return fallback;
        }
        return Character.toUpperCase(s.charAt(0));
    }

    /** Parse a {@code yyyyMMdd} date; throws if absent or malformed (the row is then skipped). */
    public LocalDate getDate(String column) {
        String s = getString(column);
        return LocalDate.parse(s, YYYYMMDD);
    }
}
