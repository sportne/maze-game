package io.github.sportne.mazegame.model.solver;

import io.github.sportne.mazegame.model.grid.GridPosition;
import java.time.Duration;
import java.util.Objects;

/**
 * Immutable snapshot of a solver run.
 *
 * @param position current solver position
 * @param elapsedTime elapsed solve time since the solver started moving
 * @param moveCount number of movement decisions made so far
 * @param status current terminal/running status
 */
public record SolverRunResult(
    GridPosition position, Duration elapsedTime, int moveCount, SolverRunStatus status) {
  /**
   * Creates a validated run snapshot.
   *
   * @throws IllegalArgumentException when elapsed time or move count is negative
   */
  public SolverRunResult {
    Objects.requireNonNull(position, "position");
    Objects.requireNonNull(elapsedTime, "elapsedTime");
    Objects.requireNonNull(status, "status");
    if (elapsedTime.isNegative()) {
      throw new IllegalArgumentException("elapsedTime must not be negative");
    }
    if (moveCount < 0) {
      throw new IllegalArgumentException("moveCount must not be negative");
    }
  }
}
