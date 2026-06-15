/**
 * Application layer (highest). Wiring only: {@code Main} (CLI flag parsing,
 * runs the comparison) and {@code ModelRegistry} (reflection — picks a model by
 * name with no {@code switch}).
 *
 * <p>Dependency rule: the top of the one-way flow
 * {@code app -> eval/concurrency -> model -> features -> data}. Lower layers
 * never import this one. Phase 7.
 */
package tennispredict.app;
