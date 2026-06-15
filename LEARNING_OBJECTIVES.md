# Learning objectives → where they live

Each course topic mapped to the exact class/file that demonstrates it, in
exam-ready prose. **Filled in as each phase lands** — don't leave it to the end.

> Status: Phase 0 complete. The entries below are placeholders describing where
> each topic *will* be demonstrated; update them to point at concrete files as
> you build.

### 1. ADTs / classes / encapsulation
`data.Player`, `data.RawMatch` (immutable records, private fields, validation in
factories, no setters); `features.Dataset`. _To be implemented (Phases 1–2)._

### 2. Inheritance + template method
`model.AbstractClassifier.fit` does shared bookkeeping then calls the abstract
`train(...)` each subclass implements. _Phase 3._

### 3. Interfaces vs abstract classes
`model.Classifier` (interface) vs `model.AbstractClassifier` (abstract base).
_Phase 3._

### 4. Packages / access modifiers
The strict one-way flow `app -> eval/concurrency -> model -> features -> data`,
documented in each `package-info.java`. _Demonstrated from Phase 0._

### 5. Functional interfaces, lambdas, anonymous & nested classes
`features.FeatureExtractor` (functional interface) with lambda and anonymous-class
implementations; `model.DecisionTree.Node` (private static nested class).
_Phases 2 & 4._

### 6. Generics & generic containers
`data.CsvParser<T>` with a `Function<String[], T>` mapper; generic
`features.Example<F, L>`, `features.Dataset<F, L>`, `model.Classifier<F, L>`.
_Phases 1–3._

### 7. Threads, synchronization, deadlock, starvation
`concurrency.FeatureBuffer` (hand-built bounded buffer, producer/consumer);
parallel forest training and CV folds via `ExecutorService`;
`concurrency.MetricsAccumulator` with a fair/unfair `ReentrantReadWriteLock`
(starvation); documented consistent lock ordering (no deadlock). _Phase 6._

### Bonus — reflection
`app.ModelRegistry` instantiates a classifier from a `--model` name via
`Class.forName(...).getDeclaredConstructor().newInstance()`. _Phase 7._

### Bonus — design patterns
Strategy (`features.FeatureSet`), Composite (`model.RandomForest` of trees behind
one `Classifier`), Template Method (`AbstractClassifier`).
