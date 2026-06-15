/**
 * Model layer. The learners, behind a single {@code Classifier} abstraction:
 * {@code BaselineClassifier}, {@code LogisticRegression}, {@code DecisionTree},
 * {@code NaiveBayes}, {@code RandomForest}.
 *
 * <p>{@code AbstractClassifier} carries the shared scaffolding via a template
 * method ({@code fit} does common bookkeeping, then calls an abstract
 * {@code train}). Phases 3–5.
 *
 * <p>Dependency rule: may depend on {@code features} and {@code data}; nothing
 * higher.
 */
package tennispredict.model;
