package tennispredict.props;

/**
 * Factory for the default {@link FeatureSet}. Most extractors are written as
 * lambdas; one is written as an anonymous class that holds a small bit of config
 * (a weight), so the two forms can be contrasted directly. Every feature is an
 * A-minus-B difference, so a positive value favours player A.
 */
public final class Features {

    private Features() {
        // Static factory — no instances.
    }

    public static FeatureSet defaultSet() {
        FeatureSet set = new FeatureSet();

        // --- Lambdas: the common case --------------------------------------
        // Rank: lower number is better, so (b - a) makes "A better" positive.
        set.add("rankDiff", (a, b) -> b.getRank() - a.getRank());
        set.add("ageDiff", (a, b) -> a.getAge() - b.getAge());
        set.add("heightDiff", (a, b) -> a.getHeightCm() - b.getHeightCm());
        set.add("formDiff", (a, b) -> a.getRecentWinRate() - b.getRecentWinRate());
        set.add("surfaceWinRateDiff", (a, b) -> a.getSurfaceWinRate() - b.getSurfaceWinRate());
        set.add("h2hDiff", (a, b) -> a.getH2hWins() - b.getH2hWins());
        set.add("experienceDiff", (a, b) -> a.getMatchesPlayed() - b.getMatchesPlayed());
        set.add("servePointsWonDiff", (a, b) -> a.serveWinProbability() - b.serveWinProbability());
        set.add("firstServeInDiff", (a, b) -> a.getFirstServeInPct() - b.getFirstServeInPct());
        set.add("aceRateDiff", (a, b) -> a.getAceRate() - b.getAceRate());

        // --- Anonymous class: holds config (a weight) ----------------------
        // The serve-vs-return matchup: A's serve edge over B's return, minus
        // B's serve edge over A's return. Scaled by a configurable weight to
        // show an anonymous class carrying state a plain lambda wouldn't.
        final double edgeWeight = 1.0;
        set.add("serveVsReturnEdge", new FeatureExtractor() {
            private final double weight = edgeWeight;

            @Override
            public double extract(MatchContext a, MatchContext b) {
                double aEdge = a.serveWinProbability() - b.getReturnWonPct();
                double bEdge = b.serveWinProbability() - a.getReturnWonPct();
                return weight * (aEdge - bEdge);
            }
        });

        return set;
    }
}
