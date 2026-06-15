/**
 * Feature layer. Turns the chronological stream of {@code RawMatch}es into a
 * {@code Dataset} of {@code (featureVector, label)} examples using only
 * pre-match knowledge — the leakage rule is about TIME, not columns.
 *
 * <p>Holds the rolling {@code PlayerHistory} state, the composable
 * {@code FeatureExtractor} functional interface and its implementations,
 * symmetrization, and the generic {@code Example} / {@code Dataset} containers.
 *
 * <p>Dependency rule: may depend on {@code data}; nothing higher. Phase 2.
 */
package tennispredict.features;
