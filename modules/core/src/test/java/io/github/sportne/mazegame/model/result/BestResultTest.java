package io.github.sportne.mazegame.model.result;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.solver.SolverRunResult;
import io.github.sportne.mazegame.model.solver.SolverRunStatus;
import java.time.Duration;
import org.junit.jupiter.api.Test;

final class BestResultTest {
  @Test
  void longerElapsedTimeBeatsCurrentBest() {
    BestResult candidate = new BestResult(Duration.ofSeconds(10), 40);
    BestResult currentBest = new BestResult(Duration.ofSeconds(9), 50);

    assertTrue(candidate.beats(currentBest));
  }

  @Test
  void higherMoveCountBreaksElapsedTimeTies() {
    BestResult candidate = new BestResult(Duration.ofSeconds(10), 41);
    BestResult currentBest = new BestResult(Duration.ofSeconds(10), 40);

    assertTrue(candidate.beats(currentBest));
    assertFalse(currentBest.beats(candidate));
  }

  @Test
  void anyCandidateBeatsMissingBest() {
    assertTrue(new BestResult(Duration.ZERO, 0).beats(null));
  }

  @Test
  void createsCandidateFromSolverRunResult() {
    SolverRunResult runResult =
        new SolverRunResult(
            new GridPosition(0, 2), Duration.ofMillis(1250L), 5, SolverRunStatus.REACHED_GOAL);

    assertEquals(new BestResult(Duration.ofMillis(1250L), 5), BestResult.from(runResult));
  }

  @Test
  void rejectsInvalidValues() {
    assertThrows(NullPointerException.class, () -> new BestResult(null, 0));
    assertThrows(IllegalArgumentException.class, () -> new BestResult(Duration.ofMillis(-1L), 0));
    assertThrows(IllegalArgumentException.class, () -> new BestResult(Duration.ZERO, -1));
  }
}
