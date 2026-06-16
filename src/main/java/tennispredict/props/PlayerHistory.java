package tennispredict.props;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import tennispredict.data.Surface;

/**
 * Mutable rolling state for a single player, accumulated as matches are swept in
 * date order. The contract is strict: <b>read the snapshot methods before
 * calling {@link #recordMatch}</b>, never after — that ordering is exactly what
 * keeps a feature for match M from seeing M's own outcome (no leakage).
 *
 * <p>Form and serve/return rates use a rolling window of recent matches; surface
 * win rate and head-to-head are career-to-date. Where a player has no history
 * yet (cold start), the snapshot falls back to neutral defaults / tour-average
 * serve priors.
 */
public class PlayerHistory {

    private static final int FORM_WINDOW = 20;   // recent matches for win-rate form
    private static final int SERVE_WINDOW = 30;  // recent matches for serve/return rates

    // Tour-average serve/return priors, used before any serve data exists.
    // Order matches the rate vector in recordMatch / averaged snapshot.
    private static final double[] TOUR_AVG = {
            0.62, // first-serve in %
            0.73, // first-serve points won %
            0.52, // second-serve points won %
            0.07, // ace rate
            0.04, // double-fault rate
            0.62, // break-points saved %
            0.36  // return points won %
    };

    private final int playerId;

    private final Deque<Boolean> recentResults = new ArrayDeque<>();
    private final Map<Surface, int[]> surfaceRecord = new HashMap<>(); // surface -> {wins, total}
    private final Map<Integer, Integer> h2hWins = new HashMap<>();      // opponentId -> wins over them
    private final Deque<double[]> serveWindow = new ArrayDeque<>();     // recent rate vectors
    private int matchesPlayed = 0;

    public PlayerHistory(int playerId) {
        this.playerId = playerId;
    }

    public int getPlayerId() { return this.playerId; }

    // --- Snapshot reads (call BEFORE recordMatch) ---------------------------

    public int matchesPlayed() {
        return this.matchesPlayed;
    }

    /** Win rate over the last {@code FORM_WINDOW} matches; 0.5 if none yet. */
    public double recentWinRate() {
        if (recentResults.isEmpty()) {
            return 0.5;
        }
        int wins = 0;
        for (Boolean won : recentResults) {
            if (won) {
                wins++;
            }
        }
        return (double) wins / recentResults.size();
    }

    /** Career-to-date win rate on the given surface; 0.5 if none yet. */
    public double surfaceWinRate(Surface surface) {
        int[] rec = surfaceRecord.get(surface);
        if (rec == null || rec[1] == 0) {
            return 0.5;
        }
        return (double) rec[0] / rec[1];
    }

    /** Number of wins this player has over {@code opponentId} so far. */
    public int h2hWins(int opponentId) {
        return h2hWins.getOrDefault(opponentId, 0);
    }

    public double firstServeInPct()   { return avgRate(0); }
    public double firstServeWonPct()  { return avgRate(1); }
    public double secondServeWonPct() { return avgRate(2); }
    public double aceRate()           { return avgRate(3); }
    public double dfRate()            { return avgRate(4); }
    public double bpSavedPct()        { return avgRate(5); }
    public double returnWonPct()      { return avgRate(6); }

    /** Average of one rate column over the serve window, or the tour-average prior if empty. */
    private double avgRate(int col) {
        if (serveWindow.isEmpty()) {
            return TOUR_AVG[col];
        }
        double sum = 0.0;
        for (double[] rates : serveWindow) {
            sum += rates[col];
        }
        return sum / serveWindow.size();
    }

    // --- Update (call AFTER reading the snapshot) ---------------------------

    /**
     * Fold one completed match into this player's history.
     *
     * @param won          did this player win the match
     * @param surface      the surface played on
     * @param opponentId   the other player's id (for head-to-head)
     * @param derivedRates the 7-element serve/return rate vector for this match,
     *                     or {@code null} if the match had no usable serve stats
     */
    public void recordMatch(boolean won, Surface surface, int opponentId, double[] derivedRates) {
        // Recent form (rolling window of W/L).
        recentResults.addLast(won);
        if (recentResults.size() > FORM_WINDOW) {
            recentResults.removeFirst();
        }

        // Surface record (career-to-date).
        int[] rec = surfaceRecord.computeIfAbsent(surface, s -> new int[2]);
        if (won) {
            rec[0]++;
        }
        rec[1]++;

        // Head-to-head: count only wins over this opponent.
        if (won) {
            h2hWins.merge(opponentId, 1, Integer::sum);
        }

        matchesPlayed++;

        // Serve/return rolling window (only if this match had usable stats).
        if (derivedRates != null) {
            serveWindow.addLast(derivedRates);
            if (serveWindow.size() > SERVE_WINDOW) {
                serveWindow.removeFirst();
            }
        }
    }
}
