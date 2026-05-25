package io.github.sportne.mazegame.model;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/** Deterministic random mouse movement for a maze. */
public final class RandomMouseSimulation {
  private final MazeState mazeState;
  private final Random random;
  private GridPosition position;
  private Duration elapsedTime = Duration.ZERO;
  private Duration accumulatedTime = Duration.ZERO;
  private int moveCount;
  private MouseRunStatus status = MouseRunStatus.RUNNING;

  public RandomMouseSimulation(MazeState mazeState) {
    this.mazeState = Objects.requireNonNull(mazeState, "mazeState");
    random = new Random(mazeState.levelDefinition().randomSeed());
    position = mazeState.levelDefinition().mouseStart();
  }

  /** Advances the simulation by the given time. */
  public MouseRunResult update(Duration deltaTime) {
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
      accumulatedTime = accumulatedTime.plus(step);
      remainingDelta = remainingDelta.minus(step);
      if (accumulatedTime.compareTo(mazeState.levelDefinition().mouseMoveInterval()) >= 0) {
        accumulatedTime = Duration.ZERO;
        moveOnce();
      }
      updateStatus();
    }
    return result();
  }

  /** Returns the current simulation result. */
  public MouseRunResult result() {
    return new MouseRunResult(position, elapsedTime, moveCount, status);
  }

  private void moveOnce() {
    List<GridPosition> moves = availableMoves();
    if (!moves.isEmpty()) {
      position = moves.get(random.nextInt(moves.size()));
    }
    moveCount++;
  }

  private Duration nextStep(Duration remainingDelta) {
    return min(remainingDelta, min(timeUntilNextMove(), timeUntilTimeout()));
  }

  private Duration timeUntilNextMove() {
    return mazeState.levelDefinition().mouseMoveInterval().minus(accumulatedTime);
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

  private List<GridPosition> availableMoves() {
    List<GridPosition> moves = new ArrayList<>();
    addIfOpen(moves, new GridPosition(position.row() - 1, position.column()));
    addIfOpen(moves, new GridPosition(position.row() + 1, position.column()));
    addIfOpen(moves, new GridPosition(position.row(), position.column() - 1));
    addIfOpen(moves, new GridPosition(position.row(), position.column() + 1));
    return moves;
  }

  private void addIfOpen(List<GridPosition> moves, GridPosition candidate) {
    if (candidate.isWithin(mazeState.levelDefinition().gridSize())
        && !mazeState.hasWallAt(candidate)) {
      moves.add(candidate);
    }
  }

  private void updateStatus() {
    if (position.equals(mazeState.levelDefinition().cheese())) {
      status = MouseRunStatus.REACHED_CHEESE;
    } else if (elapsedTime.compareTo(mazeState.levelDefinition().maximumSolveTime()) >= 0) {
      status = MouseRunStatus.TIMED_OUT;
    }
  }
}
