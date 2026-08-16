package io.github.sportne.mazegame.model.solver;

import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.level.LevelSolver;
import io.github.sportne.mazegame.model.maze.MazeState;
import java.util.Objects;
import java.util.Random;
import java.util.function.Predicate;

/** Seeded exploration that changes to direct movement when the goal has clear line of sight. */
final class LineOfSightSolverSimulation extends TimedSolverSimulation {
  private final GridPosition goal;
  private final Random random;

  LineOfSightSolverSimulation(MazeState mazeState) {
    this(mazeState, mazeState.levelDefinition().primarySolver());
  }

  LineOfSightSolverSimulation(MazeState mazeState, LevelSolver solver) {
    super(mazeState, solver);
    goal = solver.goal();
    random = new Random(solver.randomSeed().orElseThrow());
  }

  @Override
  void moveOnce() {
    CardinalDirection visibleDirection = visibleGoalDirection(position(), goal, this::isOpen);
    if (visibleDirection != null) {
      moveTo(visibleDirection.move(position()));
      return;
    }
    var moves = openNeighbors(RandomSolverSimulation.SEEDED_MOVE_ORDER);
    if (!moves.isEmpty()) {
      moveTo(moves.get(RandomSolverSimulation.nextIndex(random, moves.size())));
    }
  }

  static CardinalDirection visibleGoalDirection(
      GridPosition position, GridPosition goal, Predicate<GridPosition> isOpen) {
    Objects.requireNonNull(position, "position");
    Objects.requireNonNull(goal, "goal");
    Objects.requireNonNull(isOpen, "isOpen");
    CardinalDirection direction = alignedDirection(position, goal);
    if (direction == null) {
      return null;
    }
    GridPosition candidate = direction.move(position);
    while (isOpen.test(candidate)) {
      if (candidate.equals(goal)) {
        return direction;
      }
      candidate = direction.move(candidate);
    }
    return null;
  }

  private static CardinalDirection alignedDirection(GridPosition position, GridPosition goal) {
    if (position.row() == goal.row()) {
      if (position.column() < goal.column()) {
        return CardinalDirection.EAST;
      }
      if (position.column() > goal.column()) {
        return CardinalDirection.WEST;
      }
    } else if (position.column() == goal.column()) {
      return position.row() < goal.row() ? CardinalDirection.SOUTH : CardinalDirection.NORTH;
    }
    return null;
  }
}
