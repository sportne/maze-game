package io.github.sportne.mazegame.model.result;

import io.github.sportne.mazegame.model.solver.SolverRunResult;
import java.time.Duration;
import java.util.Objects;

/**
 * Best saved player result for one level.
 *
 * <p>Maze Game rewards making the solver take longer, so a larger elapsed time is better. Move
 * count breaks ties in the same direction.
 *
 * @param elapsedTime completed solver run time
 * @param moveCount completed solver move count
 */
public record BestResult(Duration elapsedTime, int moveCount) {
  /**
   * Creates a validated best result.
   *
   * @throws IllegalArgumentException when elapsed time or move count is negative
   */
  public BestResult {
    Objects.requireNonNull(elapsedTime, "elapsedTime");
    if (elapsedTime.isNegative()) {
      throw new IllegalArgumentException("elapsedTime must not be negative");
    }
    if (moveCount < 0) {
      throw new IllegalArgumentException("moveCount must not be negative");
    }
  }

  /**
   * Creates a best result candidate from a completed solver run.
   *
   * @param solverRunResult completed run result
   * @return best result candidate with the same time and move count
   */
  public static BestResult from(SolverRunResult solverRunResult) {
    Objects.requireNonNull(solverRunResult, "solverRunResult");
    return new BestResult(solverRunResult.elapsedTime(), solverRunResult.moveCount());
  }

  /**
   * Returns whether this result should replace an existing best result.
   *
   * @param currentBest existing best result, or null when none has been saved
   * @return true when this result is better than the existing result
   */
  public boolean beats(BestResult currentBest) {
    if (currentBest == null) {
      return true;
    }
    int timeComparison = elapsedTime.compareTo(currentBest.elapsedTime);
    if (timeComparison != 0) {
      return timeComparison > 0;
    }
    return moveCount > currentBest.moveCount;
  }
}
