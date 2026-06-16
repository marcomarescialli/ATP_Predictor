package tennispredict.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A generic CSV reading engine. It knows how to open a file, read the header,
 * address columns by name, split rows, and apply the skip-on-error policy — but
 * it knows nothing about tennis. The caller supplies a row-mapping function
 * ({@code Function<CsvRow, T>}) that turns one row into a {@code T}. The same
 * engine therefore parses {@link RawMatch} (and could parse {@link Player}) with
 * no duplication. This is the natural generics + lambda demonstration.
 *
 * <p>Policy: a row whose mapping throws (a parse error, or a validation failure
 * in the target's constructor) is <em>skipped</em> and counted, never fatal.
 *
 * @param <T> the object type each row becomes
 */
public class CsvParser<T> {

    private final Function<CsvRow, T> rowMapper;
    private int skippedCount = 0;

    public CsvParser(Function<CsvRow, T> rowMapper) {
        this.rowMapper = rowMapper;
    }

    /** Parse one file into a list of {@code T}, skipping the header and any bad rows. */
    public List<T> parse(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file);
        List<T> result = new ArrayList<>();
        if (lines.isEmpty()) {
            return result;
        }

        Map<String, Integer> columnIndex = buildColumnIndex(lines.get(0));

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            // -1 keeps trailing empty cells so column indices never shift.
            String[] fields = line.split(",", -1);
            try {
                result.add(rowMapper.apply(new CsvRow(fields, columnIndex)));
            } catch (RuntimeException e) {
                // Documented skip policy: drop malformed/invalid rows, keep a count.
                skippedCount++;
            }
        }
        return result;
    }

    /** Number of rows skipped across all {@link #parse} calls on this parser. */
    public int getSkippedCount() {
        return this.skippedCount;
    }

    private static Map<String, Integer> buildColumnIndex(String headerLine) {
        String[] headers = headerLine.split(",", -1);
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            index.put(headers[i].strip(), i);
        }
        return index;
    }
}
