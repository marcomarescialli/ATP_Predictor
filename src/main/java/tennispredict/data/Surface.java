package tennispredict.data;

/**
 * The court surface — a closed set of values, modelled as the "type-safe
 * constant" pattern (a class with a private constructor and a fixed set of
 * public static instances). This is exactly what a Java {@code enum} generates
 * under the hood; we write it by hand to stay within features the course has
 * covered.
 */
public class Surface {

    public static final Surface HARD = new Surface("Hard");
    public static final Surface CLAY = new Surface("Clay");
    public static final Surface GRASS = new Surface("Grass");
    public static final Surface CARPET = new Surface("Carpet");

    private final String name;

    private Surface(String name) {
        this.name = name;
    }

    /**
     * Maps a messy CSV value to one of the constants. Policy for blank/unknown
     * values: default to HARD (the most common surface). Deliberate and
     * documented — an unknown surface is rare and HARD is the safest guess.
     */
    public static Surface fromString(String s) {
        if (s == null) {
            return HARD;
        }
        String cleanS = s.toUpperCase().strip();
        if (cleanS.equals("CLAY")) {
            return CLAY;
        }
        if (cleanS.equals("GRASS")) {
            return GRASS;
        }
        if (cleanS.equals("CARPET")) {
            return CARPET;
        }
        return HARD;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
