package tennispredict;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * A trivial test that proves the JUnit 5 wiring works. Real tests
 * (CSV parsing, symmetrization balance, a hand-checked gradient step,
 * tree splits, a concurrency smoke test) arrive with their phases.
 */
class SmokeTest {

    @Test
    void junitIsWired() {
        assertEquals(4, 2 + 2);
    }
}
