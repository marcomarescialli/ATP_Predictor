package tennispredict.main;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import tennispredict.data.DataLoader;
import tennispredict.data.RawMatch;
import tennispredict.data.Surface;
import tennispredict.props.Dataset;
import tennispredict.props.DatasetBuilder;
import tennispredict.props.Example;
import tennispredict.props.FeatureSet;
import tennispredict.props.Features;

/**
 * Wires the pipeline and prints the Phase 1 and Phase 2 milestones. A real CLI
 * (flags for seasons, model, threads, seed, ...) arrives in later phases.
 */
public final class Main {

    private static final Path DATA_DIR = Path.of("src/main/resources/data");

    private static final long SEED = 42L;
    private static final int MIN_PRIOR_MATCHES = 10;
    private static final double TRAIN_FRACTION = 0.8;

    public static void main(String[] args) throws Exception {
        Path dataDir = (args.length > 0) ? Path.of(args[0]) : DATA_DIR;

        // ---- Phase 1 milestone: data into memory --------------------------
        DataLoader.Loaded loaded = DataLoader.loadDir(dataDir);
        List<RawMatch> matches = loaded.getMatches();

        System.out.println("=== Phase 1: data layer ===");
        System.out.println("Loaded matches : " + matches.size());
        System.out.println("Skipped rows   : " + loaded.getSkipped());
        if (!matches.isEmpty()) {
            System.out.println("Date range     : "
                    + matches.get(0).getDate() + " -> " + matches.get(matches.size() - 1).getDate());
        }
        System.out.println("Matches per surface:");
        Map<Surface, Integer> hist = DataLoader.surfaceHistogram(matches);
        for (Map.Entry<Surface, Integer> e : hist.entrySet()) {
            System.out.printf("  %-7s %6d  %s%n",
                    e.getKey().getName(), e.getValue(), bar(e.getValue(), matches.size()));
        }

        // ---- Phase 2 milestone: features + dataset ------------------------
        FeatureSet featureSet = Features.defaultSet();
        Dataset<double[], Boolean> dataset =
                DatasetBuilder.build(matches, featureSet, SEED, MIN_PRIOR_MATCHES);

        System.out.println();
        System.out.println("=== Phase 2: feature engineering ===");
        System.out.println("Examples         : " + dataset.size());
        System.out.println("Feature dimension: " + featureSet.size());

        int positives = 0;
        for (Example<double[], Boolean> ex : dataset.examples()) {
            if (ex.getLabel()) {
                positives++;
            }
        }
        double posRate = dataset.size() == 0 ? 0.0 : (double) positives / dataset.size();
        System.out.printf("Class balance    : %.1f%% A-wins (should be ~50%%)%n", posRate * 100);

        Dataset.Split<double[], Boolean> split = dataset.split(TRAIN_FRACTION);
        int trainPct = (int) Math.round(TRAIN_FRACTION * 100);
        System.out.println("Chronological split (" + trainPct + "/" + (100 - trainPct) + "): "
                + split.getTrain().size() + " train / " + split.getTest().size() + " test");

        if (dataset.size() > 0) {
            System.out.println();
            System.out.println("Sample example (feature : value):");
            Example<double[], Boolean> sample = dataset.get(0);
            List<String> names = featureSet.names();
            double[] vec = sample.getFeatures();
            for (int i = 0; i < vec.length; i++) {
                System.out.printf("  %-20s %+.4f%n", names.get(i), vec[i]);
            }
            System.out.println("  label (A wins?)     : " + sample.getLabel());
        }
    }

    /** A little ASCII bar scaled to the largest possible count, for the histogram. */
    private static String bar(int value, int total) {
        if (total == 0) {
            return "";
        }
        int width = (int) Math.round(40.0 * value / total);
        return "#".repeat(width);
    }
}
