package io.github.sportne.mazegame.model;

import java.time.Duration;
import java.util.Objects;

/**
 * Immutable snapshot of a mouse run.
 *
 * @param position current mouse position
 * @param elapsedTime elapsed solve time since the mouse started moving
 * @param moveCount number of movement decisions made so far
 * @param status current terminal/running status
 */
public record MouseRunResult(
    GridPosition position, Duration elapsedTime, int moveCount, MouseRunStatus status) {
  /**
   * Creates a validated run snapshot.
   *
   * @throws IllegalArgumentException when elapsed time or move count is negative
   */
  public MouseRunResult {
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
