package tennispredict.props;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An ordered collection of {@link FeatureExtractor}s that together turn a pair of
 * {@link MatchContext}s into a {@code double[]} feature vector. Adding a feature
 * is one {@link #add} call — the Strategy pattern: each extractor is an
 * interchangeable strategy, and the set composes them.
 */
public class FeatureSet {

    private final List<String> names = new ArrayList<>();
    private final List<FeatureExtractor> extractors = new ArrayList<>();

    /** Register a named feature. Returns {@code this} for chaining. */
    public FeatureSet add(String name, FeatureExtractor extractor) {
        names.add(name);
        extractors.add(extractor);
        return this;
    }

    /** Compute the full feature vector for one (A, B) pairing. */
    public double[] extract(MatchContext a, MatchContext b) {
        double[] vector = new double[extractors.size()];
        for (int i = 0; i < extractors.size(); i++) {
            vector[i] = extractors.get(i).extract(a, b);
        }
        return vector;
    }

    public int size() {
        return extractors.size();
    }

    /** Feature names, in vector order (unmodifiable). */
    public List<String> names() {
        return Collections.unmodifiableList(names);
    }
}
