package tennispredict.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Loads every {@code atp_matches_*.csv} season file in a directory into one
 * chronologically-sorted list of {@link RawMatch}. Sorting by date is essential:
 * Phase 2 sweeps the matches in time order so that each example only ever uses
 * information knowable before its match was played.
 */
public final class DataLoader {

    private DataLoader() {
        // Static utility — no instances.
    }

    /** The matches loaded, plus how many rows were skipped as malformed/invalid. */
    public static final class Loaded {
        private final List<RawMatch> matches;
        private final int skipped;

        Loaded(List<RawMatch> matches, int skipped) {
            this.matches = matches;
            this.skipped = skipped;
        }

        public List<RawMatch> getMatches() { return this.matches; }
        public int getSkipped() { return this.skipped; }
    }

    /** Parse all season files in {@code dir}, sorted by date ascending. */
    public static Loaded loadDir(Path dir) throws IOException {
        CsvParser<RawMatch> parser = new CsvParser<>(RawMatchMapper::map);
        List<RawMatch> all = new ArrayList<>();

        List<Path> files = new ArrayList<>();
        try (Stream<Path> entries = Files.list(dir)) {
            entries.filter(DataLoader::isSeasonFile)
                   .sorted()
                   .forEach(files::add);
        }
        for (Path file : files) {
            all.addAll(parser.parse(file));
        }

        // Sort by date; ties (same day) keep read order, which is fine for the sweep.
        all.sort(Comparator.comparing(RawMatch::getDate));
        return new Loaded(all, parser.getSkippedCount());
    }

    /** Count matches per surface, in a stable insertion order, for the milestone histogram. */
    public static Map<Surface, Integer> surfaceHistogram(List<RawMatch> matches) {
        Map<Surface, Integer> counts = new LinkedHashMap<>();
        counts.put(Surface.HARD, 0);
        counts.put(Surface.CLAY, 0);
        counts.put(Surface.GRASS, 0);
        counts.put(Surface.CARPET, 0);
        for (RawMatch m : matches) {
            counts.merge(m.getSurface(), 1, Integer::sum);
        }
        return counts;
    }

    private static boolean isSeasonFile(Path p) {
        String name = p.getFileName().toString();
        return name.startsWith("atp_matches_") && name.endsWith(".csv");
    }
}
