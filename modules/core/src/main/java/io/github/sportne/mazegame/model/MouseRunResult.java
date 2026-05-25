package io.github.sportne.mazegame.model;

import java.time.Duration;
import java.util.Objects;

/** Immutable snapshot of a mouse run. */
public record MouseRunResult(
    GridPosition position, Duration elapsedTime, int moveCount, MouseRunStatus status) {
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
