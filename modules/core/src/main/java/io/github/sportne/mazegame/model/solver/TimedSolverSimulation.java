package io.github.sportne.mazegame.model.solver;

import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.level.LevelSolver;
import io.github.sportne.mazegame.model.maze.MazeState;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Shared fixed-step timing and terminal-state handling for solver simulations. */
abstract class TimedSolverSimulation implements SolverSimulation {
  private final MazeState mazeState;
  private final LevelSolver solver;
  private GridPosition position;
  private Duration elapsedTime = Duration.ZERO;
  private Duration timeUntilDecision;
  private boolean delayedDecision;
  private int moveCount;
  private CardinalDirection lastDirection;
  private SolverRunStatus status = SolverRunStatus.RUNNING;

  TimedSolverSimulation(MazeState mazeState) {
    this(
        mazeState,
        Objects.requireNonNull(mazeState, "mazeState").levelDefinition().primarySolver());
  }

  TimedSolverSimulation(MazeState mazeState, LevelSolver solver) {
    this.mazeState = Objects.requireNonNull(mazeState, "mazeState");
    this.solver = Objects.requireNonNull(solver, "solver");
    position = solver.start();
    timeUntilDecision = mazeState.levelDefinition().solverMoveInterval();
  }

  @Override
  public final SolverRunResult update(Duration deltaTime) {
    Objects.requireNonNull(deltaTime, "deltaTime");
    if (deltaTime.isNegative()) {
      throw new IllegalArgumentException("deltaTime must not be negative");
    }
    if (status != SolverRunStatus.RUNNING) {
      return result();
    }

    Duration remainingDelta = deltaTime;
    while (status == SolverRunStatus.RUNNING && !remainingDelta.isZero()) {
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
        if (status == SolverRunStatus.RUNNING) {
          delayedDecision = mazeState.delaysNextDecisionAt(position);
          timeUntilDecision =
              delayedDecision
                  ? mazeState.levelDefinition().solverMoveInterval().multipliedBy(2)
                  : mazeState.levelDefinition().solverMoveInterval();
        }
      } else {
        updateStatus();
      }
    }
    return result();
  }

  @Override
  public final SolverRunResult result() {
    return new SolverRunResult(position, elapsedTime, moveCount, status);
  }

  @Override
  public final Optional<CardinalDirection> lastDirection() {
    return Optional.ofNullable(lastDirection);
  }

  /** Makes one behavior-specific movement decision. */
  abstract void moveOnce();

  final GridPosition position() {
    return position;
  }

  final void moveTo(GridPosition destination) {
    GridPosition nextPosition = Objects.requireNonNull(destination, "destination");
    lastDirection = CardinalDirection.between(position, nextPosition);
    position = nextPosition;
  }

  final boolean isOpen(GridPosition candidate) {
    return mazeState.isTraversableAt(candidate, elapsedTime);
  }

  /** Returns legal neighbors in the exact supplied behavior-specific decision order. */
  final List<GridPosition> openNeighbors(List<CardinalDirection> directionOrder) {
    List<GridPosition> neighbors = new ArrayList<>();
    for (CardinalDirection direction : directionOrder) {
      GridPosition candidate = direction.move(position);
      if (isOpen(candidate)) {
        neighbors.add(candidate);
      }
    }
    return List.copyOf(neighbors);
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
    if (position.equals(solver.goal())) {
      status = SolverRunStatus.REACHED_GOAL;
    } else if (elapsedTime.compareTo(mazeState.levelDefinition().maximumSolveTime()) >= 0) {
      status = SolverRunStatus.TIMED_OUT;
    }
  }
}
