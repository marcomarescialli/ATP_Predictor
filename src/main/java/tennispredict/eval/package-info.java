/**
 * Evaluation layer. Scores trained classifiers on held-out data:
 * {@code ConfusionMatrix}, the {@code Metrics} value object (accuracy,
 * precision, recall, F1, log-loss), {@code Evaluator}, and
 * {@code CrossValidator}.
 *
 * <p>Dependency rule: may depend on {@code model}, {@code features},
 * {@code data}. Phases 3–4 (and CV in Phase 6).
 */
package tennispredict.eval;
