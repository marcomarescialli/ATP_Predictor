package tennispredict.data;

import java.time.LocalDate;
import java.util.Objects;

public class RawMatch {

    public static final int MISSING = -1;

    private final LocalDate date;
    private final Surface surface;
    private final int bestOf;
    private final int w_id;
    private final int l_id;
    private final int w_rank;
    private final int l_rank;
    private final String w_name;
    private final String l_name;
    private final char w_hand;
    private final char l_hand;
    private final int w_height;
    private final int l_height;
    private final int w_age;
    private final int l_age;
    private final int w_ace;
    private final int w_df;
    private final int w_svpt;
    private final int w_1stin;
    private final int w_1stwon;
    private final int w_2ndwon;
    private final int w_svGms;
    private final int w_bpS;
    private final int w_bpF;
    private final int l_ace;
    private final int l_df;
    private final int l_svpt;
    private final int l_1stin;
    private final int l_1stwon;
    private final int l_2ndwon;
    private final int l_svGms;
    private final int l_bpS;
    private final int l_bpF;

    public RawMatch(
            LocalDate date, Surface surface, int bestOf, int w_id, int l_id, int w_rank, int l_rank,
            String w_name, String l_name, char w_hand, char l_hand, int w_height, int l_height,
            int w_age, int l_age, int w_ace, int w_df, int w_svpt, int w_1stin, int w_1stwon,
            int w_2ndwon, int w_svGms, int w_bpS, int w_bpF, int l_ace, int l_df, int l_svpt,
            int l_1stin, int l_1stwon, int l_2ndwon, int l_svGms, int l_bpS, int l_bpF
    ) {
        this.date = Objects.requireNonNull(date, "date");
        this.surface = Objects.requireNonNull(surface, "surface");
        this.bestOf = requirePositive(bestOf, "bestOf");

        this.w_id = requirePositive(w_id, "w_id");
        this.l_id = requirePositive(l_id, "l_id");
        this.w_name = Objects.requireNonNull(w_name, "w_name");
        this.l_name = Objects.requireNonNull(l_name, "l_name");
        this.w_hand = validHand(w_hand);
        this.l_hand = validHand(l_hand);

        this.w_rank = positiveOrMissing(w_rank, "w_rank");
        this.l_rank = positiveOrMissing(l_rank, "l_rank");
        this.w_height = positiveOrMissing(w_height, "w_height");
        this.l_height = positiveOrMissing(l_height, "l_height");
        this.w_age = positiveOrMissing(w_age, "w_age");
        this.l_age = positiveOrMissing(l_age, "l_age");

        this.w_ace = countOrMissing(w_ace, "w_ace");
        this.w_df = countOrMissing(w_df, "w_df");
        this.w_svpt = countOrMissing(w_svpt, "w_svpt");
        this.w_1stin = countOrMissing(w_1stin, "w_1stin");
        this.w_1stwon = countOrMissing(w_1stwon, "w_1stwon");
        this.w_2ndwon = countOrMissing(w_2ndwon, "w_2ndwon");
        this.w_svGms = countOrMissing(w_svGms, "w_svGms");
        this.w_bpS = countOrMissing(w_bpS, "w_bpS");
        this.w_bpF = countOrMissing(w_bpF, "w_bpF");
        this.l_ace = countOrMissing(l_ace, "l_ace");
        this.l_df = countOrMissing(l_df, "l_df");
        this.l_svpt = countOrMissing(l_svpt, "l_svpt");
        this.l_1stin = countOrMissing(l_1stin, "l_1stin");
        this.l_1stwon = countOrMissing(l_1stwon, "l_1stwon");
        this.l_2ndwon = countOrMissing(l_2ndwon, "l_2ndwon");
        this.l_svGms = countOrMissing(l_svGms, "l_svGms");
        this.l_bpS = countOrMissing(l_bpS, "l_bpS");
        this.l_bpF = countOrMissing(l_bpF, "l_bpF");
    }

    private static int requirePositive(int v, String field) {
        if (v <= 0) {
            throw new IllegalArgumentException(field + " must be positive, got " + v);
        }
        return v;
    }

    private static int positiveOrMissing(int v, String field) {
        if (v != MISSING && v <= 0) {
            throw new IllegalArgumentException(
                    field + " must be positive or " + MISSING + " (missing), got " + v);
        }
        return v;
    }

    private static int countOrMissing(int v, String field) {
        if (v != MISSING && v < 0) {
            throw new IllegalArgumentException(
                    field + " must be >= 0 or " + MISSING + " (missing), got " + v);
        }
        return v;
    }

    private static char validHand(char h) {
        if (h == 'L' || h == 'R' || h == 'U') {
            return h;
        }
        throw new IllegalArgumentException("hand must be L, R or U, got '" + h + "'");
    }


    public LocalDate getDate() { return this.date; }
    public Surface getSurface() { return this.surface; }
    public int getBestOf() { return this.bestOf; }

    public int getW_id() { return this.w_id; }
    public int getL_id() { return this.l_id; }
    public int getW_rank() { return this.w_rank; }
    public int getL_rank() { return this.l_rank; }
    public String getW_name() { return this.w_name; }
    public String getL_name() { return this.l_name; }
    public char getW_hand() { return this.w_hand; }
    public char getL_hand() { return this.l_hand; }
    public int getW_height() { return this.w_height; }
    public int getL_height() { return this.l_height; }
    public int getW_age() { return this.w_age; }
    public int getL_age() { return this.l_age; }

    public int getW_ace() { return this.w_ace; }
    public int getW_df() { return this.w_df; }
    public int getW_svpt() { return this.w_svpt; }
    public int getW_1stin() { return this.w_1stin; }
    public int getW_1stwon() { return this.w_1stwon; }
    public int getW_2ndwon() { return this.w_2ndwon; }
    public int getW_svGms() { return this.w_svGms; }
    public int getW_bpS() { return this.w_bpS; }
    public int getW_bpF() { return this.w_bpF; }
    public int getL_ace() { return this.l_ace; }
    public int getL_df() { return this.l_df; }
    public int getL_svpt() { return this.l_svpt; }
    public int getL_1stin() { return this.l_1stin; }
    public int getL_1stwon() { return this.l_1stwon; }
    public int getL_2ndwon() { return this.l_2ndwon; }
    public int getL_svGms() { return this.l_svGms; }
    public int getL_bpS() { return this.l_bpS; }
    public int getL_bpF() { return this.l_bpF; }

    public String toString() {
        return date + ": " + w_name + " def. " + l_name + " (best of " + bestOf + ")";
    }
}
