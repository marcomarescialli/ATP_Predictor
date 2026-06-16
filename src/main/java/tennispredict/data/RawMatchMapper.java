package tennispredict.data;

/**
 * Knows how to turn one {@link CsvRow} of the ATP matches file into a
 * {@link RawMatch}. Lives here (not on {@code RawMatch}) so that {@code RawMatch}
 * stays free of any CSV knowledge. Use as a method reference:
 * {@code new CsvParser<>(RawMatchMapper::map)}.
 */
public final class RawMatchMapper {

    private RawMatchMapper() {
        // Static utility — no instances.
    }

    /**
     * Build a {@link RawMatch} from a row. Columns are addressed by header name,
     * so this is robust to column reordering. Throws (and the parser then skips
     * the row) if the date is malformed or the constructor rejects the values.
     */
    public static RawMatch map(CsvRow row) {
        return new RawMatch(
                row.getDate("tourney_date"),
                Surface.fromString(row.getString("surface")),
                row.getIntOrMissing("best_of"),

                row.getIntOrMissing("winner_id"),
                row.getIntOrMissing("loser_id"),
                row.getIntOrMissing("winner_rank"),
                row.getIntOrMissing("loser_rank"),
                row.getString("winner_name"),
                row.getString("loser_name"),
                row.getChar("winner_hand", 'U'),
                row.getChar("loser_hand", 'U'),
                row.getIntOrMissing("winner_ht"),
                row.getIntOrMissing("loser_ht"),
                row.getRoundedIntOrMissing("winner_age"),
                row.getRoundedIntOrMissing("loser_age"),

                row.getIntOrMissing("w_ace"),
                row.getIntOrMissing("w_df"),
                row.getIntOrMissing("w_svpt"),
                row.getIntOrMissing("w_1stIn"),
                row.getIntOrMissing("w_1stWon"),
                row.getIntOrMissing("w_2ndWon"),
                row.getIntOrMissing("w_SvGms"),
                row.getIntOrMissing("w_bpSaved"),
                row.getIntOrMissing("w_bpFaced"),

                row.getIntOrMissing("l_ace"),
                row.getIntOrMissing("l_df"),
                row.getIntOrMissing("l_svpt"),
                row.getIntOrMissing("l_1stIn"),
                row.getIntOrMissing("l_1stWon"),
                row.getIntOrMissing("l_2ndWon"),
                row.getIntOrMissing("l_SvGms"),
                row.getIntOrMissing("l_bpSaved"),
                row.getIntOrMissing("l_bpFaced")
        );
    }
}
