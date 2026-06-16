package tennispredict.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsvParserTest {

    private static final String HEADER =
            "tourney_date,surface,best_of,winner_id,loser_id,winner_name,loser_name,"
            + "winner_hand,loser_hand,winner_rank,loser_rank";

    @Test
    void parsesRowsAddressesColumnsByNameAndAppliesMissingPolicy(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("atp_matches_test.csv");
        Files.writeString(file, String.join("\n",
                HEADER,
                "20150104,Hard,3,100,200,Alice A,Bob B,R,L,5,10",
                "20150105,Clay,3,101,201,Carl C,Dan D,R,R,,12",   // blank winner_rank -> MISSING
                "BADDATE,Hard,3,102,202,Eve E,Foe F,R,R,3,4"      // malformed date -> skipped
        ));

        CsvParser<RawMatch> parser = new CsvParser<>(RawMatchMapper::map);
        List<RawMatch> matches = parser.parse(file);

        assertEquals(2, matches.size(), "the malformed-date row should be skipped");
        assertEquals(1, parser.getSkippedCount());

        RawMatch first = matches.get(0);
        assertEquals(100, first.getW_id());
        assertEquals(5, first.getW_rank());
        assertEquals(Surface.HARD, first.getSurface());
        assertEquals('L', first.getL_hand());

        RawMatch second = matches.get(1);
        assertEquals(RawMatch.MISSING, second.getW_rank(), "blank rank cell becomes MISSING");
        assertEquals(Surface.CLAY, second.getSurface());
    }

    @Test
    void surfaceFromStringHandlesMessyAndUnknownValues() {
        assertEquals(Surface.CLAY, Surface.fromString("  clay "));
        assertEquals(Surface.GRASS, Surface.fromString("Grass"));
        assertEquals(Surface.CARPET, Surface.fromString("CARPET"));
        assertEquals(Surface.HARD, Surface.fromString(""));      // blank -> default HARD
        assertEquals(Surface.HARD, Surface.fromString("banana")); // unknown -> default HARD
    }
}
