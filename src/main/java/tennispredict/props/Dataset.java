package tennispredict.props;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A generic, ordered collection of {@link Example}s. The builder adds examples in
 * chronological order, which is what makes {@link #split(double)} meaningful: it
 * trains on earlier matches and tests on later ones, mimicking real forecasting
 * and avoiding leakage of future form into the test set.
 *
 * @param <F> feature type
 * @param <L> label type
 */
public class Dataset<F, L> {

    private final List<Example<F, L>> examples = new ArrayList<>();

    public void add(Example<F, L> example) {
        examples.add(example);
    }

    public int size() {
        return examples.size();
    }

    public Example<F, L> get(int i) {
        return examples.get(i);
    }

    /** Examples in order (unmodifiable view). */
    public List<Example<F, L>> examples() {
        return Collections.unmodifiableList(examples);
    }

    /** Shuffle in place with a seeded RNG (for reproducibility). Not used by the chronological split. */
    public void shuffle(Random rng) {
        Collections.shuffle(examples, rng);
    }

    /**
     * Chronological split: the first {@code trainFraction} of examples (the
     * earlier matches) become the training set, the remainder the test set.
     * NOT random — this preserves the time order on purpose.
     */
    public Split<F, L> split(double trainFraction) {
        if (trainFraction < 0.0 || trainFraction > 1.0) {
            throw new IllegalArgumentException("trainFraction must be in [0,1], got " + trainFraction);
        }
        int cut = (int) Math.round(examples.size() * trainFraction);

        Dataset<F, L> train = new Dataset<>();
        Dataset<F, L> test = new Dataset<>();
        for (int i = 0; i < examples.size(); i++) {
            if (i < cut) {
                train.add(examples.get(i));
            } else {
                test.add(examples.get(i));
            }
        }
        return new Split<>(train, test);
    }

    /** A train/test pair returned by {@link #split(double)}. */
    public static final class Split<F, L> {
        private final Dataset<F, L> train;
        private final Dataset<F, L> test;

        Split(Dataset<F, L> train, Dataset<F, L> test) {
            this.train = train;
            this.test = test;
        }

        public Dataset<F, L> getTrain() { return this.train; }
        public Dataset<F, L> getTest() { return this.test; }
    }
}
