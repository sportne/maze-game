package io.github.sportne.mazegame.model;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Deterministic random mouse movement for a maze.
 *
 * <p>The simulation advances in fixed movement intervals from the level definition. At each move it
 * randomly chooses one currently open orthogonal neighbor, including the previous cell when that
 * cell is open, so backtracking is allowed. The random generator is seeded from the level so replay
 * can reproduce the same path from the same maze.
 */
public final class RandomMouseSimulation {
  /** Maze whose walls and level timing control this run. */
  private final MazeState mazeState;

  /** Seeded source of deterministic movement choices. */
  private final Random random;

  /** Current mouse cell. */
  private GridPosition position;

  /** Total solve time that has elapsed. */
  private Duration elapsedTime = Duration.ZERO;

  /** Time accumulated toward the next movement interval. */
  private Duration accumulatedTime = Duration.ZERO;

  /** Number of movement decisions already made. */
  private int moveCount;

  /** Current run status. */
  private MouseRunStatus status = MouseRunStatus.RUNNING;

  /**
   * Starts a deterministic run for the given maze.
   *
   * @param mazeState maze to solve
   */
  public RandomMouseSimulation(MazeState mazeState) {
    this.mazeState = Objects.requireNonNull(mazeState, "mazeState");
    random = new Random(mazeState.levelDefinition().randomSeed());
    position = mazeState.levelDefinition().mouseStart();
  }

  /**
   * Advances the simulation by the given time.
   *
   * @param deltaTime amount of time to add to the run
   * @return the updated run snapshot
   */
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

  /**
   * Returns the current simulation result.
   *
   * @return immutable snapshot of position, time, move count, and status
   */
  public MouseRunResult result() {
    return new MouseRunResult(position, elapsedTime, moveCount, status);
  }

  /** Makes one random legal movement decision and increments the move count. */
  private void moveOnce() {
    List<GridPosition> moves = availableMoves();
    if (!moves.isEmpty()) {
      position = moves.get(random.nextInt(moves.size()));
    }
    moveCount++;
  }

  /**
   * Returns the next time slice to process without crossing a movement or timeout boundary.
   *
   * @param remainingDelta unprocessed time from the current update call
   * @return the smallest relevant duration slice
   */
  private Duration nextStep(Duration remainingDelta) {
    return min(remainingDelta, min(timeUntilNextMove(), timeUntilTimeout()));
  }

  /**
   * Returns the time remaining before the mouse should move again.
   *
   * @return duration until the next movement interval completes
   */
  private Duration timeUntilNextMove() {
    return mazeState.levelDefinition().mouseMoveInterval().minus(accumulatedTime);
  }

  /**
   * Returns the time remaining before this run times out.
   *
   * @return duration until the level maximum solve time
   */
  private Duration timeUntilTimeout() {
    return mazeState.levelDefinition().maximumSolveTime().minus(elapsedTime);
  }

  /**
   * Returns the earlier of two durations.
   *
   * @param first first duration to compare
   * @param second second duration to compare
   * @return the lesser duration
   */
  private static Duration min(Duration first, Duration second) {
    if (first.compareTo(second) <= 0) {
      return first;
    }
    return second;
  }

  /**
   * Collects all currently legal orthogonal moves.
   *
   * @return open neighboring cells
   */
  private List<GridPosition> availableMoves() {
    List<GridPosition> moves = new ArrayList<>();
    addIfOpen(moves, new GridPosition(position.row() - 1, position.column()));
    addIfOpen(moves, new GridPosition(position.row() + 1, position.column()));
    addIfOpen(moves, new GridPosition(position.row(), position.column() - 1));
    addIfOpen(moves, new GridPosition(position.row(), position.column() + 1));
    return moves;
  }

  /**
   * Adds a candidate move when it is inside the grid and not blocked by a wall.
   *
   * @param moves mutable list of legal moves being built
   * @param candidate candidate neighboring cell
   */
  private void addIfOpen(List<GridPosition> moves, GridPosition candidate) {
    if (candidate.isWithin(mazeState.levelDefinition().gridSize())
        && !mazeState.hasWallAt(candidate)) {
      moves.add(candidate);
    }
  }

  /** Updates terminal status after time and movement changes. */
  private void updateStatus() {
    if (position.equals(mazeState.levelDefinition().cheese())) {
      status = MouseRunStatus.REACHED_CHEESE;
    } else if (elapsedTime.compareTo(mazeState.levelDefinition().maximumSolveTime()) >= 0) {
      status = MouseRunStatus.TIMED_OUT;
    }
  }
}
