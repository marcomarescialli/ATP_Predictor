package tennispredict.app;

/**
 * Entry point for the ATP match-outcome predictor.
 *
 * <p>Phase 0 milestone: this just prints a greeting so the build pipeline
 * ({@code mvn package}) is verified end-to-end before any real logic exists.
 * Later phases grow this into the CLI that parses flags
 * ({@code --seasons}, {@code --model}, {@code --threads}, {@code --fair-locks},
 * {@code --train-fraction}, {@code --seed}), builds the dataset, trains the
 * configured models, and prints the comparison table.
 */
public final class Main {

    private Main() {
        // No instances — this is just an entry point.
    }

    public static void main(String[] args) {
        System.out.println("ATP Predictor — hello. Setup is working.");
    }
}
