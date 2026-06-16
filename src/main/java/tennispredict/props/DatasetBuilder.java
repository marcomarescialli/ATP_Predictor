package tennispredict.props;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import tennispredict.data.RawMatch;
import tennispredict.data.Surface;

/**
 * Turns a date-ordered list of {@link RawMatch}es into a {@link Dataset} of
 * {@code (double[] features, Boolean label)} examples. This is the subtle heart
 * of the project: for each match it <b>reads</b> both players' pre-match state to
 * build the example, then <b>updates</b> that state with the match outcome — in
 * that order — so no example ever sees its own match's result.
 *
 * <p>Symmetrization: the actual winner/loser are randomly assigned to slots A/B
 * (seeded), and the label records whether slot A is the real winner. This
 * balances the classes ~50/50 and removes any "slot A always wins" artifact.
 */
public final class DatasetBuilder {

    // Imputation for missing pre-match attributes (paired with RawMatch.MISSING).
    private static final double IMPUTED_RANK = 2000.0;   // treat "unranked" as a poor rank
    private static final double IMPUTED_AGE = 26.0;      // tour-typical age
    private static final double IMPUTED_HEIGHT = 185.0;  // tour-typical height (cm)

    private DatasetBuilder() {
        // Static engine — no instances.
    }

    /**
     * @param matches          matches sorted ascending by date
     * @param featureSet       the features to compute
     * @param seed             RNG seed for reproducible symmetrization
     * @param minPriorMatches  both players must have at least this many prior
     *                         matches before an example is emitted (cold-start guard)
     */
    public static Dataset<double[], Boolean> build(
            List<RawMatch> matches, FeatureSet featureSet, long seed, int minPriorMatches) {

        Random rng = new Random(seed);
        Map<Integer, PlayerHistory> histories = new HashMap<>();
        Dataset<double[], Boolean> dataset = new Dataset<>();

        for (RawMatch m : matches) {
            int wId = m.getW_id();
            int lId = m.getL_id();
            Surface surface = m.getSurface();

            PlayerHistory wHist = histories.computeIfAbsent(wId, PlayerHistory::new);
            PlayerHistory lHist = histories.computeIfAbsent(lId, PlayerHistory::new);

            // --- READ: build pre-match snapshots (before any update) --------
            MatchContext winnerCtx = buildContext(
                    wHist, m.getW_rank(), m.getW_age(), m.getW_height(), surface, lId);
            MatchContext loserCtx = buildContext(
                    lHist, m.getL_rank(), m.getL_age(), m.getL_height(), surface, wId);

            // Emit an example only once both players have enough history.
            boolean emit = wHist.matchesPlayed() >= minPriorMatches
                    && lHist.matchesPlayed() >= minPriorMatches;

            if (emit) {
                boolean aIsWinner = rng.nextBoolean();
                MatchContext a = aIsWinner ? winnerCtx : loserCtx;
                MatchContext b = aIsWinner ? loserCtx : winnerCtx;
                double[] features = featureSet.extract(a, b);
                dataset.add(new Example<>(features, Boolean.valueOf(aIsWinner)));
            }

            // --- UPDATE: fold this match into both players' state -----------
            double[] winnerRates = derivedRates(
                    m.getW_svpt(), m.getW_1stin(), m.getW_1stwon(), m.getW_2ndwon(),
                    m.getW_ace(), m.getW_df(), m.getW_bpS(), m.getW_bpF(),
                    m.getL_svpt(), m.getL_1stwon(), m.getL_2ndwon());
            double[] loserRates = derivedRates(
                    m.getL_svpt(), m.getL_1stin(), m.getL_1stwon(), m.getL_2ndwon(),
                    m.getL_ace(), m.getL_df(), m.getL_bpS(), m.getL_bpF(),
                    m.getW_svpt(), m.getW_1stwon(), m.getW_2ndwon());

            wHist.recordMatch(true, surface, lId, winnerRates);
            lHist.recordMatch(false, surface, wId, loserRates);
        }

        return dataset;
    }

    /** Combine per-match attributes with the history snapshot into a MatchContext. */
    private static MatchContext buildContext(
            PlayerHistory h, int rank, int age, int height, Surface surface, int opponentId) {
        double rankD = (rank == RawMatch.MISSING) ? IMPUTED_RANK : rank;
        double ageD = (age == RawMatch.MISSING) ? IMPUTED_AGE : age;
        double heightD = (height == RawMatch.MISSING) ? IMPUTED_HEIGHT : height;

        return new MatchContext(
                rankD, ageD, heightD,
                h.recentWinRate(),
                h.surfaceWinRate(surface),
                h.matchesPlayed(),
                h.h2hWins(opponentId),
                h.firstServeInPct(),
                h.firstServeWonPct(),
                h.secondServeWonPct(),
                h.aceRate(),
                h.dfRate(),
                h.bpSavedPct(),
                h.returnWonPct());
    }

    /**
     * Derive the 7-element serve/return rate vector for one player in one match.
     * Returns {@code null} when the serve data is missing/unusable so the caller
     * skips updating the serve window for this match.
     *
     * <p>Serve rates use this player's own columns; the return rate uses the
     * opponent's serve columns (the points this player had to return).
     */
    private static double[] derivedRates(
            int svpt, int firstIn, int firstWon, int secondWon, int ace, int df,
            int bpSaved, int bpFaced, int oppSvpt, int oppFirstWon, int oppSecondWon) {

        if (svpt <= 0 || oppSvpt <= 0 || firstIn < 0) {
            return null; // no usable serve stats this match
        }

        double firstInPct = (double) firstIn / svpt;
        double firstWonPct = firstIn > 0 ? (double) firstWon / firstIn : 0.0;
        int secondPts = svpt - firstIn;
        double secondWonPct = secondPts > 0 ? (double) secondWon / secondPts : 0.0;
        double aceRate = (double) ace / svpt;
        double dfRate = (double) df / svpt;
        double bpSavedPct = bpFaced > 0 ? (double) bpSaved / bpFaced : 1.0;

        // Return points won = 1 - (opponent's points won on their serve) / (their serve points).
        double oppWonOnServe = oppFirstWon + oppSecondWon;
        double returnWonPct = 1.0 - oppWonOnServe / oppSvpt;

        return new double[] {
                firstInPct, firstWonPct, secondWonPct, aceRate, dfRate, bpSavedPct, returnWonPct
        };
    }
}
