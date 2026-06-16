package tennispredict.props;

/**
 * One training example: a feature representation {@code F} paired with a label
 * {@code L}. Generic so the same container works for {@code double[]} feature
 * vectors with {@code Boolean} labels now, and anything else later. Immutable.
 *
 * @param <F> feature type (e.g. {@code double[]})
 * @param <L> label type (e.g. {@code Boolean})
 */
public class Example<F, L> {

    private final F features;
    private final L label;

    public Example(F features, L label) {
        this.features = features;
        this.label = label;
    }

    public F getFeatures() { return this.features; }
    public L getLabel() { return this.label; }
}
