package io.github.sportne.mazegame.model.grid;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;

/** Shared breadth-first connectivity checks for authored and player-edited grids. */
public final class GridPathfinder {
  private GridPathfinder() {}

  /** Returns whether walkable grid positions connect the start to the goal. */
  public static boolean hasPath(
      GridSize gridSize, GridPosition start, GridPosition goal, Predicate<GridPosition> walkable) {
    Objects.requireNonNull(gridSize, "gridSize");
    Objects.requireNonNull(start, "start");
    Objects.requireNonNull(goal, "goal");
    Objects.requireNonNull(walkable, "walkable");
    if (!start.isWithin(gridSize) || !goal.isWithin(gridSize)) {
      throw new IllegalArgumentException("path endpoints must be inside the grid");
    }
    Queue<GridPosition> frontier = new ArrayDeque<>();
    Set<GridPosition> visited = new HashSet<>();
    frontier.add(start);
    visited.add(start);
    while (!frontier.isEmpty()) {
      GridPosition current = frontier.remove();
      if (current.equals(goal)) {
        return true;
      }
      for (GridPosition neighbor : neighbors(current)) {
        if (neighbor.isWithin(gridSize) && walkable.test(neighbor) && visited.add(neighbor)) {
          frontier.add(neighbor);
        }
      }
    }
    return false;
  }

  private static Set<GridPosition> neighbors(GridPosition position) {
    return Set.of(
        new GridPosition(position.row() - 1, position.column()),
        new GridPosition(position.row() + 1, position.column()),
        new GridPosition(position.row(), position.column() - 1),
        new GridPosition(position.row(), position.column() + 1));
  }
}
