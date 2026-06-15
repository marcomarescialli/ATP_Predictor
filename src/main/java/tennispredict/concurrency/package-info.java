/**
 * Concurrency layer. The threaded machinery, kept organic rather than bolted
 * on: a hand-built bounded {@code FeatureBuffer} (producer/consumer with a
 * {@code ReentrantLock} + {@code notFull}/{@code notEmpty} conditions, poison-pill
 * shutdown), and a {@code MetricsAccumulator} guarded by a
 * {@code ReentrantReadWriteLock} (fairness as a flag — the starvation story).
 *
 * <p>Built last, only once the single-threaded pipeline is correct. Phase 6.
 *
 * <p>Dependency rule: may depend on {@code model}/{@code features}/{@code data}.
 */
package tennispredict.concurrency;
