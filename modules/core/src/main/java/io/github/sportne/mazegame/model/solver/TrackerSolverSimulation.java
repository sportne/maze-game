package io.github.sportne.mazegame.model.solver;

import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.level.LevelSolver;
import io.github.sportne.mazegame.model.maze.MazeState;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Deterministic solver that prefers less-visited cells before goal distance and direction. */
final class TrackerSolverSimulation extends TimedSolverSimulation {
  private final GridPosition goal;
  private final Map<GridPosition, Integer> cellVisitCounts = new HashMap<>();

  TrackerSolverSimulation(MazeState mazeState) {
    this(mazeState, mazeState.levelDefinition().primarySolver());
  }

  TrackerSolverSimulation(MazeState mazeState, LevelSolver solver) {
    super(mazeState, solver);
    goal = solver.goal();
    cellVisitCounts.put(solver.start(), 1);
  }

  @Override
  void moveOnce() {
    Map<CardinalDirection, GridPosition> openNeighbors = new EnumMap<>(CardinalDirection.class);
    for (CardinalDirection direction : CardinalDirection.values()) {
      GridPosition candidate = direction.move(position());
      if (isOpen(candidate)) {
        openNeighbors.put(direction, candidate);
      }
    }
    CardinalDirection selected =
        chooseDirection(position(), goal, openNeighbors.keySet(), cellVisitCounts);
    if (selected != null) {
      GridPosition destination = openNeighbors.get(selected);
      moveTo(destination);
      cellVisitCounts.merge(destination, 1, Integer::sum);
    }
  }

  @Override
  public SolverDecisionState decisionState() {
    return new SolverDecisionState(cellVisitCounts);
  }

  static CardinalDirection chooseDirection(
      GridPosition position,
      GridPosition goal,
      Set<CardinalDirection> openDirections,
      Map<GridPosition, Integer> cellVisitCounts) {
    Objects.requireNonNull(position, "position");
    Objects.requireNonNull(goal, "goal");
    Objects.requireNonNull(openDirections, "openDirections");
    Objects.requireNonNull(cellVisitCounts, "cellVisitCounts");
    return Arrays.stream(CardinalDirection.values())
        .filter(openDirections::contains)
        .min(
            Comparator.<CardinalDirection>comparingInt(
                    direction -> cellVisitCounts.getOrDefault(direction.move(position), 0))
                .thenComparingInt(direction -> manhattanDistance(direction.move(position), goal)))
        .orElse(null);
  }

  private static int manhattanDistance(GridPosition first, GridPosition second) {
    return Math.abs(first.row() - second.row()) + Math.abs(first.column() - second.column());
  }
}
