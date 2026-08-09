package io.github.sportne.mazegame.model.mouse;

import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.maze.MazeState;
import java.time.Duration;
import java.util.Objects;

/** Shared fixed-step timing and terminal-state handling for mouse simulations. */
abstract class TimedMouseSimulation implements MouseSimulation {
  private final MazeState mazeState;
  private GridPosition position;
  private Duration elapsedTime = Duration.ZERO;
  private Duration timeUntilDecision;
  private boolean delayedDecision;
  private int moveCount;
  private MouseRunStatus status = MouseRunStatus.RUNNING;

  TimedMouseSimulation(MazeState mazeState) {
    this.mazeState = Objects.requireNonNull(mazeState, "mazeState");
    position = mazeState.levelDefinition().mouseStart();
    timeUntilDecision = mazeState.levelDefinition().mouseMoveInterval();
  }

  @Override
  public final MouseRunResult update(Duration deltaTime) {
    Objects.requireNonNull(deltaTime, "deltaTime");
    if (deltaTime.isNegative()) {
      throw new IllegalArgumentException("deltaTime must not be negative");
    }
    if (status != MouseRunStatus.RUNNING) {
      return result();
    }

    Duration remainingDelta = deltaTime;
    while (status == MouseRunStatus.RUNNING && !remainingDelta.isZero()) {
      Duration step = nextStep(remainingDelta);
      elapsedTime = elapsedTime.plus(step);
      timeUntilDecision = timeUntilDecision.minus(step);
      remainingDelta = remainingDelta.minus(step);
      boolean reachedTimeout =
          elapsedTime.compareTo(mazeState.levelDefinition().maximumSolveTime()) >= 0;
      if (timeUntilDecision.isZero() && !(delayedDecision && reachedTimeout)) {
        moveOnce();
        moveCount++;
        updateStatus();
        if (status == MouseRunStatus.RUNNING) {
          delayedDecision = mazeState.delaysNextDecisionAt(position);
          timeUntilDecision =
              delayedDecision
                  ? mazeState.levelDefinition().mouseMoveInterval().multipliedBy(2)
                  : mazeState.levelDefinition().mouseMoveInterval();
        }
      } else {
        updateStatus();
      }
    }
    return result();
  }

  @Override
  public final MouseRunResult result() {
    return new MouseRunResult(position, elapsedTime, moveCount, status);
  }

  /** Makes one behavior-specific movement decision. */
  abstract void moveOnce();

  final GridPosition position() {
    return position;
  }

  final void moveTo(GridPosition destination) {
    position = Objects.requireNonNull(destination, "destination");
  }

  final boolean isOpen(GridPosition candidate) {
    return mazeState.isTraversable(candidate);
  }

  private Duration nextStep(Duration remainingDelta) {
    return min(remainingDelta, min(timeUntilNextMove(), timeUntilTimeout()));
  }

  private Duration timeUntilNextMove() {
    return timeUntilDecision;
  }

  private Duration timeUntilTimeout() {
    return mazeState.levelDefinition().maximumSolveTime().minus(elapsedTime);
  }

  private static Duration min(Duration first, Duration second) {
    if (first.compareTo(second) <= 0) {
      return first;
    }
    return second;
  }

  private void updateStatus() {
    if (position.equals(mazeState.levelDefinition().cheese())) {
      status = MouseRunStatus.REACHED_CHEESE;
    } else if (elapsedTime.compareTo(mazeState.levelDefinition().maximumSolveTime()) >= 0) {
      status = MouseRunStatus.TIMED_OUT;
    }
  }
}
