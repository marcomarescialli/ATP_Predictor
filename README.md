# ATP Predictor

A from-scratch Java predictor for **pre-match** ATP tennis outcomes. Given two
players and the pre-match context of a match (rankings, ages, heights, recent
form, surface, head-to-head, rolling serve/return ability), it outputs the
probability that player A beats player B.

No ML libraries — every model (logistic regression, decision tree, naïve Bayes,
random forest) is implemented by hand so each line is explainable. The point is
the engineering and code quality, not chasing accuracy.

## Realistic accuracy

A "higher-ranked player wins" baseline already gets **~65%** on ATP matches. A
good engineered model lands roughly **66–70%**. Beating the baseline by 2–4
points with honest, leak-free features is a solid result — tennis is genuinely
hard to predict.

## Requirements

- JDK 17+ (developed against a newer JDK; `pom.xml` targets release 17)
- Maven 3.9+

## Build & run

```bash
mvn package            # compiles, runs tests, builds the jar
mvn test               # tests only
mvn exec:java          # runs tennispredict.app.Main
```

Phase 0 milestone: `mvn exec:java` prints a greeting, confirming the pipeline works.

Planned CLI flags (later phases): `--seasons`, `--model`, `--threads`,
`--fair-locks`, `--train-fraction`, `--seed`.

## Data

ATP match CSVs come from **Jeff Sackmann's tennis_atp**
(https://github.com/JeffSackmann/tennis_atp), licensed **CC BY-NC-SA 4.0**
(non-commercial, attribution, share-alike). They are not committed to this repo —
see [`src/main/resources/data/README.md`](src/main/resources/data/README.md) for
how to download them.

## Architecture

Strict one-way dependency flow — lower layers never import higher ones:

```
app  ->  eval / concurrency  ->  model  ->  features  ->  data
```

```
src/main/java/tennispredict/
  data/         RawMatch, Player, Surface, CsvParser            parse the CSVs
  features/     FeatureExtractor, extractors, Example, Dataset  matches -> vectors
  model/        Classifier, AbstractClassifier, LogisticReg,    the learners
                DecisionTree, NaiveBayes, RandomForest, Baseline
  eval/         Evaluator, Metrics, ConfusionMatrix, CrossValidator
  concurrency/  FeatureBuffer (producer/consumer), MetricsAccumulator
  app/          Main, ModelRegistry (reflection)
```

Build single-threaded and *correct* first; concurrency comes last. The single
rule that saves the most pain: **never debug machine learning and threads at the
same time.**

## Build phases

See [`BUILD_PLAN.md`](BUILD_PLAN.md) for the full phased roadmap and
[`LEARNING_OBJECTIVES.md`](LEARNING_OBJECTIVES.md) for the course-topic mapping
(filled in as each phase lands).

| Phase | What | Status |
|-------|------|--------|
| 0 | Project setup, skeleton, `mvn package` runs | ✅ done |
| 1 | Data layer — CSVs into immutable objects | ⬜ |
| 2 | Feature engineering — rolling state, dataset | ⬜ |
| 3 | First model (logistic) + evaluation loop | ⬜ |
| 4 | More models — tree, naïve Bayes | ⬜ |
| 5 | Random forest ensemble | ⬜ |
| 6 | Concurrency — threads, sync, deadlock, starvation | ⬜ |
| 7 | Reflection, CLI, polish | ⬜ |
