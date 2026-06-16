package tennispredict.props;

/**
 * A single feature: given the two players' pre-match snapshots, produce one
 * number. By convention each extractor returns an A-minus-B difference, so a
 * positive value means the feature favours player A.
 *
 * <p>A {@code @FunctionalInterface}, so implementations can be written as concise
 * lambdas (and at least one as an anonymous class — see {@link Features}).
 */
@FunctionalInterface
public interface FeatureExtractor {
    double extract(MatchContext a, MatchContext b);
}
