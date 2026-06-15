# Build Plan: Tennis Match Outcome Predictor (Java, build it yourself)

A phased roadmap. Each phase ends in a **runnable milestone** so you always have working
code, and each phase tells you **which course topics it lets you demonstrate** so you know why
you're doing it. Build single-threaded and *correct* first; concurrency comes near the end.

---

## The task, stated precisely

> Given two players and the **pre-match** context of a match (their rankings, ages, heights,
> recent form, surface, head-to-head history), output the probability that player A beats player B.

Pre-match only. No in-match stats (aces, double faults, break points) as inputs — that's leakage.

**Realistic expectations (so you don't think you failed):** a "higher-ranked player wins"
baseline already gets ~**65%** accuracy on ATP matches. A good engineered model lands roughly
**66–70%**. Beating the baseline by even 2–4 points with honest features is a solid, defensible
result. Tennis is genuinely hard to predict; the *engineering and code quality* are what's graded,
not chasing 90%.

---

## Target architecture (your goal to grow into — don't build it all at once)

```
tennispredict/
  data/        RawMatch, Player, Surface, CsvParser           ← parse the CSVs
  features/    FeatureExtractor, extractors, Example, Dataset  ← turn matches into vectors
  model/       Classifier, AbstractClassifier, LogisticReg,    ← the learners
               DecisionTree, NaiveBayes, RandomForest, Baseline
  eval/        Evaluator, Metrics, ConfusionMatrix, CrossValidator
  concurrency/ FeatureBuffer (producer/consumer), MetricsAccumulator
  app/         Main, ModelRegistry (reflection)
```

Strict dependency direction: `app → eval/concurrency → model → features → data`. Lower layers
never import higher ones. This one-way flow is itself the cleanest demonstration of the
**packages / access-modifiers** topic.

---

## Phase 0 — Setup (½ day)

1. Install JDK 17+, pick a build tool (**Maven** is fine), create the project skeleton.
2. `git init`. Commit at the end of every phase — you'll want to roll back when an experiment
   ruins your accuracy.
3. Add JUnit 5. Add nothing else yet.
4. Download a few seasons from `github.com/JeffSackmann/tennis_atp` (e.g. `atp_matches_2015.csv`
   … `atp_matches_2024.csv`) into a `data/` resources folder. Open `matches_data_dictionary.txt`
   and skim the column meanings — you'll refer to it constantly.

**Milestone:** `mvn package` runs; an empty `Main` prints "hello".

---

## Phase 1 — Data layer: get matches into memory

**Goal:** parse the CSVs into clean, immutable Java objects.

Build:
- `enum Surface { HARD, CLAY, GRASS, CARPET }` (+ a `fromString` factory that tolerates messy
  values).
- `record Player(int id, String name, char hand, int heightCm, ...)` — immutable.
- `record RawMatch(...)` holding the fields you actually need: `tourneyDate`, `surface`,
  `winnerId`, `winnerRank`, `winnerAge`, `winnerHt`, `loserId`, `loserRank`, … Keep it lean; don't
  model all 49 columns, just the pre-match ones plus IDs and date.
- `CsvParser` that reads a file into `List<RawMatch>`. Handle the real-world mess: missing ranks
  (blank cells), unranked players, malformed rows — decide a policy (skip or impute) and document
  it. Parse `tourneyDate` (format `yyyyMMdd`) into a `LocalDate`.

Design notes:
- Make a **generic** `CsvParser<T>` that takes a row-mapping function `Function<String[], T>` —
  then `RawMatch` and `Player` parsing reuse the same parser. (Early, natural generics + lambda.)
- Private fields, validation in constructors/factories, no setters.

**Milestone:** load all seasons, print total match count and a histogram of matches per surface.

**Topics lit up:** ADTs / classes / encapsulation (1), packages (4), generics + lambda for the
parser (5, 6).

---

## Phase 2 — Feature engineering (the most important phase)

**Goal:** turn the chronological stream of matches into a `Dataset` of `(featureVector, label)`
examples — using only pre-match knowledge.

### 2a. Maintain rolling player state (process matches in DATE ORDER)
Sort all matches by `tourneyDate`. Walk them in order, keeping a mutable `PlayerHistory` per
player that you **read before** recording the example, then **update after**:
- recent form: win rate over last *N* matches (a fixed-size deque per player)
- surface-specific win rate
- head-to-head: wins of A vs B so far
- matches played (experience), maybe days since last match (rust/fatigue)
- **serve & return ability** (rolling aggregates of past serve stats — see below)

**The leakage rule is about TIME, not about which column.** A feature for predicting match *M*
may use only information knowable *strictly before M is played*. So this match's `w_ace` count
is off-limits as an input (it's part of the outcome), but a player's serve quality *aggregated
from their earlier matches* is completely legitimate — and it's one of the most predictive feature
families in tennis. "Read-then-update as you sweep through time" is what enforces this: read each
player's pre-match aggregates to build the feature, *then* fold the current match's stats into
their running totals. This is the subtle heart of the project — get it right and document it.

#### Serve & return ability (the highest-value upgrade)
The per-match serve columns (`w_ace`, `w_df`, `w_svpt`, `w_1stIn`, `w_1stWon`, `w_2ndWon`,
`w_bpSaved`, `w_bpFaced`, and the `l_` equivalents) are *outcomes* of each match, so never feed
the current match's values in. Instead, as you sweep in date order, maintain per player a rolling
average over their **last N matches** of derived **rates** (rates, not raw counts — players face
different numbers of points per match):
- first-serve-in %  = `1stIn / svpt`
- first-serve points won %  = `1stWon / 1stIn`
- second-serve points won %  = `2ndWon / (svpt − 1stIn)`
- ace rate  = `ace / svpt`, double-fault rate  = `df / svpt`
- break-points-saved %  = `bpSaved / bpFaced`

Prefer a rolling window (recent form) over career-to-date — serving changes a lot across a career.
**Bonus — return ability for free:** in any match, the *opponent's* serve columns describe what
this player had to return against, so you can aggregate a symmetric "return points won %" per
player from the other side of their past matches. That gives you the natural **serve-vs-return
matchup** (A's serve quality vs B's return quality, and vice versa), which is how real tennis
models think.

**Three caveats to handle (and to mention in the oral exam — they show maturity):**
1. *Coverage.* The serve columns only exist from ~1991 for tour-level matches and are blank
   before that, so restrict to ~2000+ seasons or handle the missing early data explicitly.
2. *Cold start.* A player's first matches have no history to aggregate. Use a fallback — the
   tour-average serve profile as a prior, or require a minimum number of prior matches before an
   example is emitted. (Same issue you already have with form and H2H.)
3. *Confounding (state this as a known limitation, don't necessarily fix it).* Raw rolling serve
   rates are influenced by *who* a player faced and on *what surface* (faster courts inflate ace
   rates). Plain averages are perfectly fine and defensible for a course project; saying out loud
   "I didn't opponent-adjust or surface-split these — that's the next refinement" is exactly the
   kind of self-aware remark that scores well in a defense.

### 2b. Feature extractors as composable functions
- `interface FeatureExtractor { double extract(MatchContext a, MatchContext b); }` —
  a **functional interface**.
- Implement several: `RankDiff`, `AgeDiff`, `HeightDiff`, `FormDiff`, `SurfaceWinRateDiff`,
  `H2HDiff`, plus the serve/return family from 2a — `ServePointsWonDiff`, `FirstServeInDiff`,
  `AceRateDiff`, and a `ServeVsReturnEdge` (A's serve quality vs B's return quality). Write some as
  **lambdas**, and write at least one as an **anonymous class** (e.g. one that holds a small bit of
  config) so you can contrast the two forms in the exam.
- A `FeatureSet` holds an ordered `List<FeatureExtractor>` and produces a `double[]` — adding a
  feature is then one line. (Strategy pattern, for bonus credit.)

### 2c. Symmetrize and label
For each historical match, randomly assign the actual winner/loser to slots A/B (seeded `Random`
for reproducibility). Feature vector = extractor outputs computed as (A's stat − B's stat).
Label = `true` if A is the real winner. This balances the classes ~50/50 and removes the
"slot 1 always wins" artifact. **Do this — skipping it silently breaks everything.**

### 2d. Containers and split
- `record Example<F, L>(F features, L label)` — generic.
- `class Dataset<F, L>` wrapping `List<Example<F, L>>` with `size()`, `shuffle(Random)`,
  and a **chronological** `split(double trainFraction)` (train on earlier matches, test on later
  — NOT random, to mimic real forecasting and avoid leaking future form into the test set).

**Milestone:** build the dataset; print number of examples, feature dimension, class balance
(should be ~50/50), and one example vector with its label.

**Topics lit up:** functional interface + lambdas + anonymous class (5), generics & generic
containers (6), encapsulation & ADTs (1), Strategy pattern (bonus).

---

## Phase 3 — First model + evaluation loop (close the loop EARLY)

**Goal:** get one real model scoring on held-out data, so you can measure every later change.

### 3a. The abstraction
- `interface Classifier<F, L>` with `void fit(Dataset<F, L> train)`, `L predict(F x)`, and for
  probabilistic models `double predictProba(F x)`.
- `abstract class AbstractClassifier<F, L> implements Classifier<F, L>` holding shared scaffolding
  (e.g. whether it's been trained, feature dimension, a `protected` hook). Use a **template
  method**: e.g. `fit` does common bookkeeping then calls an abstract `train(...)` the subclass
  implements. This single design choice carries inheritance + abstract-class-vs-interface.

### 3b. Two first models
- `BaselineClassifier` — predicts the higher-ranked (lower rank number) player wins. No learning.
  This is your bar to beat; print it first, every run.
- `LogisticRegression extends AbstractClassifier<double[], Boolean>` — weights + bias, sigmoid,
  trained by batch (or mini-batch) gradient descent over the training set. ~60 lines. You'll be
  able to explain every line in the exam, which is exactly why we're not using a library.
  Remember to **standardize features** (subtract mean, divide by std, computed on train only) or
  gradient descent will crawl.

### 3c. Evaluation
- `ConfusionMatrix`, and a `Metrics` value object: accuracy, precision, recall, F1, and
  **log-loss** (rewards good probabilities, not just hard calls).
- `Evaluator` that runs a trained classifier over the test set and returns `Metrics`.

**Milestone:** print a table — Baseline vs LogisticRegression — accuracy and log-loss on the test
set. Logistic should edge past the baseline. If it doesn't, suspect (in order): no symmetrization,
no feature standardization, learning rate too high/low, or leakage in form features.

**Topics lit up:** interfaces & abstract classes (3), inheritance + template method (2), generics
& generic type hierarchy (6), encapsulation (1).

---

## Phase 4 — More models (make the hierarchy real)

**Goal:** add genuinely different algorithms so the inheritance hierarchy isn't a one-off.

- `DecisionTree extends AbstractClassifier<double[], Boolean>` — recursive binary splits on a
  feature threshold chosen by Gini impurity or information gain, with a max-depth stop. The tree
  **node** is a perfect **nested class** (a `private static final class Node` holding feature
  index, threshold, children, or a leaf prediction). A recursive structure is also just satisfying
  to build.
- `NaiveBayes extends AbstractClassifier<double[], Boolean>` — Gaussian NB: per-class feature
  means/variances, predict by comparing class likelihoods. A totally different paradigm from the
  other two, which is what makes the abstraction worth having.

**Milestone:** one comparison table across Baseline, Logistic, Tree, NaiveBayes on the same test
split.

**Topics lit up:** inheritance with varied subclasses (2), abstract/interface (3), **nested
classes** for tree nodes (5), generics (6).

---

## Phase 5 — Ensemble (random forest) + sets up threading

**Goal:** an ensemble that beats the single tree — and is naturally parallelizable.

- `RandomForest extends AbstractClassifier<double[], Boolean>` holding a `List<DecisionTree>`.
  Train each tree on a bootstrap sample (bagging) with a random feature subset; predict by
  majority vote / averaged probability.
- Note for the exam: a forest treating many trees behind the single `Classifier` interface **is
  the Composite pattern**. Each tree is independently trainable → the obvious thing to parallelize
  next.

**Milestone:** forest beats the single tree on the test set (usually it will).

**Topics lit up:** inheritance + composition, Composite pattern (bonus), and it tees up topic 7.

---

## Phase 6 — Concurrency (the exam-critical layer — make it organic, not bolted on)

Your professor leans hard on threads, so do this carefully and be ready to *explain* it. Three
places where concurrency arises naturally:

### 6a. Parallel model training (easy win — threads)
Train the forest's trees in parallel with an `ExecutorService` fixed thread pool; each tree is a
`Callable<DecisionTree>`. Independent tasks, so no shared mutable state here — note that in your
docs (it's "embarrassingly parallel"). Do the same for **cross-validation folds**: a
`CrossValidator` that runs K folds concurrently.

### 6b. Producer/consumer feature pipeline (synchronization + the real shared state)
This is where you demonstrate synchronization properly. Build a **bounded** `FeatureBuffer<E>`
**by hand** — `ReentrantLock` + two `Condition`s (`notFull`, `notEmpty`), `put`/`take` blocking
correctly (always `await` inside a `while`, never `if` — explain "lost wakeup" / spurious wakeup):
- **Producer threads**: read/parse CSV rows and push `RawMatch` into the buffer.
- **Consumer threads**: take matches, run feature extraction, and write `Example`s into a shared
  `Dataset` (guarded, or use a concurrent collection — discuss the trade-off).
- Shut down cleanly with a **poison-pill** sentinel; no `Thread.stop`, no busy-wait spin.

### 6c. Shared metrics accumulator (write contention → starvation story)
When folds run in parallel (6a), have them write into one `MetricsAccumulator` guarded by a
`ReentrantReadWriteLock` (many readers of running totals, exclusive writers). Make the lock's
**fairness** a constructor flag wired to a `--fair-locks` CLI option, and be ready to explain:
under an *unfair* lock with heavy reads, the **writer can starve**; a *fair* lock grants in FIFO
order and prevents it, at some throughput cost. That's your concrete starvation talking point.

### 6d. Deadlock — reason about it explicitly
With two locks in play (the buffer's lock and the accumulator's/Dataset's lock) a deadlock is
possible if a thread holds one and grabs the other while another thread does the reverse.
**Rule to enforce and document: never hold two of these locks at once** (consistent lock ordering
/ no nested locks). State that argument plainly — it's exactly what the examiner wants to hear.
*Optional bonus:* a standalone `DeadlockDemo` `main` that intentionally deadlocks two threads by
acquiring locks in opposite order, with a watchdog using `ThreadMXBean.findDeadlockedThreads()` to
detect and report it. Keep it out of the main path; it's a teaching toy.

**Milestone:** training/CV runs multithreaded, finishes faster on a fixed seed, produces the same
metrics as single-threaded, and shuts down with no thread left blocked.

**Topics lit up:** threads, synchronization, deadlock, starvation (7) — the whole topic, arising
from the actual problem.

---

## Phase 7 — Reflection bonus + polish

Your professor's oral questions hit reflection hard even though it's off the official list — so a
small, natural use is worth a lot.

- `ModelRegistry`: read a model name from a config file / CLI arg and instantiate the classifier
  via `Class.forName(name).getDeclaredConstructor().newInstance()`. Now `--model RandomForest`
  picks the model with no `switch` statement. Be ready to explain `getDeclaredConstructor` vs
  `newInstance`, and `getMethod` vs `getDeclaredMethod`.
- `Main`: wire it all, parse flags (`--seasons`, `--model`, `--threads`, `--fair-locks`,
  `--train-fraction`, `--seed`), print the comparison table and final metrics.
- Output a CSV of results; optionally an ASCII bar chart of model accuracies to stdout.

**Topics lit up:** reflection (exam bonus), packages/access modifiers (4).

---

## Cross-cutting: tests and docs (do a little every phase)

- **JUnit** as you go: CSV parser on a tiny fixed sample; symmetrization gives ~50/50 labels;
  logistic gradient step on a hand-checked 2-point example; tree split picks the obviously-correct
  feature on a toy set; a concurrency smoke test (multithreaded build on a fixed seed finishes and
  matches the single-threaded dataset size).
- **`README.md`**: how to build/run, the flags, where the data comes from + the CC BY-NC-SA
  citation, the realistic-accuracy note.
- **`LEARNING_OBJECTIVES.md`**: map each course topic to the exact class/file that shows it, in
  exam-ready prose (one short paragraph each). Build this as you finish each phase — don't leave it
  to the end.

---

## Suggested order & pacing

1. Phases 0–1: data in memory.
2. Phase 2: features + dataset (budget the most time here; it's fiddly and decides your accuracy).
3. Phase 3: **stop and verify a model beats baseline before going further.**
4. Phases 4–5: more models, ensemble.
5. Phase 6: concurrency — only after the single-threaded pipeline is correct.
6. Phase 7: reflection, CLI, docs.

The single rule that saves you the most pain: **never debug machine learning and threads at the
same time.** Get the whole pipeline correct and accurate single-threaded; only then parallelize.

---

## Watch-outs checklist

- [ ] No in-match stats used as features (leakage).
- [ ] Examples symmetrized (~50/50 labels), seeded.
- [ ] Features standardized using **train-set** statistics only (don't peek at test).
- [ ] Chronological train/test split, not random.
- [ ] Rolling player state read *before* update while sweeping in date order.
- [ ] `await` always inside `while`, clean shutdown, no nested locks.
- [ ] Every value object immutable; private fields; one-way package dependencies.
