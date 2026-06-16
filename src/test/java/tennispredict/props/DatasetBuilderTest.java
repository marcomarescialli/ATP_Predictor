package tennispredict.props;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import tennispredict.data.RawMatch;
import tennispredict.data.Surface;

class DatasetBuilderTest {

    /** Build a synthetic match with plausible, non-missing serve stats. */
    private static RawMatch match(LocalDate date, int winnerId, int loserId) {
        return new RawMatch(
                date, Surface.HARD, 3,
                winnerId, loserId, 50, 60,
                "W" + winnerId, "L" + loserId, 'R', 'R',
                185, 183, 25, 27,
                5, 2, 60, 36, 27, 12, 12, 3, 5,   // winner serve line
                3, 4, 65, 40, 25, 10, 12, 6, 9);  // loser serve line
    }

    /** A round-robin-ish stream of matches over a pool of players, in date order. */
    private static List<RawMatch> syntheticMatches(int count) {
        List<RawMatch> matches = new ArrayList<>();
        LocalDate date = LocalDate.of(2010, 1, 1);
        int[] players = {1, 2, 3, 4, 5, 6};
        for (int i = 0; i < count; i++) {
            int w = players[i % players.length];
            int l = players[(i + 1 + (i / players.length)) % players.length];
            if (w == l) {
                l = players[(i + 2) % players.length];
            }
            matches.add(match(date, w, l));
            date = date.plusDays(1);
        }
        return matches;
    }

    @Test
    void featureVectorWidthMatchesFeatureSet() {
        FeatureSet fs = Features.defaultSet();
        Dataset<double[], Boolean> ds =
                DatasetBuilder.build(syntheticMatches(300), fs, 1L, 0);
        assertTrue(ds.size() > 0);
        assertEquals(fs.size(), ds.get(0).getFeatures().length);
    }

    @Test
    void symmetrizationGivesRoughlyBalancedLabels() {
        Dataset<double[], Boolean> ds =
                DatasetBuilder.build(syntheticMatches(2000), Features.defaultSet(), 7L, 0);

        int positives = 0;
        for (Example<double[], Boolean> ex : ds.examples()) {
            if (ex.getLabel()) {
                positives++;
            }
        }
        double rate = (double) positives / ds.size();
        assertTrue(rate > 0.4 && rate < 0.6, "labels should be ~50/50, got " + rate);
    }

    @Test
    void sameSeedProducesIdenticalDataset() {
        List<RawMatch> matches = syntheticMatches(500);
        Dataset<double[], Boolean> a = DatasetBuilder.build(matches, Features.defaultSet(), 99L, 0);
        Dataset<double[], Boolean> b = DatasetBuilder.build(matches, Features.defaultSet(), 99L, 0);

        assertEquals(a.size(), b.size());
        for (int i = 0; i < a.size(); i++) {
            assertEquals(a.get(i).getLabel(), b.get(i).getLabel());
            assertArrayEquals(a.get(i).getFeatures(), b.get(i).getFeatures(), 0.0);
        }
    }
}
