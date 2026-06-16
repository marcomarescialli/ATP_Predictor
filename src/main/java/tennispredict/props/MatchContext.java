package tennispredict.props;

/**
 * An immutable snapshot of one player's situation going <em>into</em> a match —
 * everything a feature extractor is allowed to see. It blends two sources:
 * per-match attributes from the row (rank, age, height) and pre-match rolling
 * aggregates read from that player's {@link PlayerHistory} (form, surface win
 * rate, head-to-head, serve/return rates) <em>before</em> the match is folded in.
 *
 * <p>All fields are doubles so extractors can compute simple differences. Missing
 * rank/age/height have already been imputed by the builder.
 */
public class MatchContext {

    private final double rank;
    private final double age;
    private final double heightCm;
    private final double recentWinRate;
    private final double surfaceWinRate;
    private final double matchesPlayed;
    private final double h2hWins;

    private final double firstServeInPct;
    private final double firstServeWonPct;
    private final double secondServeWonPct;
    private final double aceRate;
    private final double dfRate;
    private final double bpSavedPct;
    private final double returnWonPct;

    public MatchContext(
            double rank, double age, double heightCm, double recentWinRate,
            double surfaceWinRate, double matchesPlayed, double h2hWins,
            double firstServeInPct, double firstServeWonPct, double secondServeWonPct,
            double aceRate, double dfRate, double bpSavedPct, double returnWonPct) {
        this.rank = rank;
        this.age = age;
        this.heightCm = heightCm;
        this.recentWinRate = recentWinRate;
        this.surfaceWinRate = surfaceWinRate;
        this.matchesPlayed = matchesPlayed;
        this.h2hWins = h2hWins;
        this.firstServeInPct = firstServeInPct;
        this.firstServeWonPct = firstServeWonPct;
        this.secondServeWonPct = secondServeWonPct;
        this.aceRate = aceRate;
        this.dfRate = dfRate;
        this.bpSavedPct = bpSavedPct;
        this.returnWonPct = returnWonPct;
    }

    public double getRank() { return this.rank; }
    public double getAge() { return this.age; }
    public double getHeightCm() { return this.heightCm; }
    public double getRecentWinRate() { return this.recentWinRate; }
    public double getSurfaceWinRate() { return this.surfaceWinRate; }
    public double getMatchesPlayed() { return this.matchesPlayed; }
    public double getH2hWins() { return this.h2hWins; }
    public double getFirstServeInPct() { return this.firstServeInPct; }
    public double getFirstServeWonPct() { return this.firstServeWonPct; }
    public double getSecondServeWonPct() { return this.secondServeWonPct; }
    public double getAceRate() { return this.aceRate; }
    public double getDfRate() { return this.dfRate; }
    public double getBpSavedPct() { return this.bpSavedPct; }
    public double getReturnWonPct() { return this.returnWonPct; }

    /**
     * Probability of winning a single point on serve, derived from the rolling
     * serve rates: P(1st in)·P(win|1st) + P(1st out)·P(win|2nd).
     */
    public double serveWinProbability() {
        return firstServeInPct * firstServeWonPct
                + (1.0 - firstServeInPct) * secondServeWonPct;
    }
}
