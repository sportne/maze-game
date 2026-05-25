package io.github.sportne.mazegame.model;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

/** Immutable wall layout for a level. */
public record MazeState(LevelDefinition levelDefinition, Set<GridPosition> walls) {
  public MazeState {
    Objects.requireNonNull(levelDefinition, "levelDefinition");
    Objects.requireNonNull(walls, "walls");
    walls = Set.copyOf(walls);
    for (GridPosition wall : walls) {
      validateWallPosition(levelDefinition, wall);
    }
    if (!hasPathFromStartToCheese(levelDefinition, walls)) {
      throw new IllegalArgumentException("maze must keep a path from mouse start to cheese");
    }
  }

  /** Creates an empty maze for the given level. */
  public static MazeState empty(LevelDefinition levelDefinition) {
    return new MazeState(levelDefinition, Set.of());
  }

  /** Returns a new maze with a normal wall at the given position. */
  public MazeState withWall(GridPosition position) {
    WallPlacementResult result = placeWall(position);
    if (!result.accepted()) {
      throw new IllegalArgumentException("wall placement rejected: " + result.status());
    }
    return result.mazeState();
  }

  /** Returns the result of trying to place a normal wall at the given position. */
  public WallPlacementResult placeWall(GridPosition position) {
    Objects.requireNonNull(position, "position");
    if (!position.isWithin(levelDefinition.gridSize())) {
      return WallPlacementResult.rejected(this, WallPlacementStatus.REJECTED_OUTSIDE_GRID);
    }
    if (isProtected(position)) {
      return WallPlacementResult.rejected(this, WallPlacementStatus.REJECTED_PROTECTED_CELL);
    }
    if (walls.contains(position)) {
      return WallPlacementResult.accepted(this, WallPlacementStatus.ALREADY_PRESENT);
    }
    Set<GridPosition> updatedWalls = new HashSet<>(walls);
    updatedWalls.add(position);
    if (!hasPathFromStartToCheese(levelDefinition, updatedWalls)) {
      return WallPlacementResult.rejected(this, WallPlacementStatus.REJECTED_BLOCKS_PATH);
    }
    return WallPlacementResult.accepted(
        new MazeState(levelDefinition, updatedWalls), WallPlacementStatus.PLACED);
  }

  /** Returns a new maze without a wall at the given position. */
  public MazeState withoutWall(GridPosition position) {
    requireInsideGrid(position);
    Set<GridPosition> updatedWalls = new HashSet<>(walls);
    updatedWalls.remove(position);
    return new MazeState(levelDefinition, updatedWalls);
  }

  /** Returns whether a normal wall occupies the given position. */
  public boolean hasWallAt(GridPosition position) {
    requireInsideGrid(position);
    return walls.contains(position);
  }

  /** Returns whether the given position is reserved for start or cheese content. */
  public boolean isProtected(GridPosition position) {
    requireInsideGrid(position);
    return position.equals(levelDefinition.mouseStart())
        || position.equals(levelDefinition.cheese());
  }

  /** Returns the content rendered for the given position. */
  public CellContent cellContentAt(GridPosition position) {
    requireInsideGrid(position);
    if (position.equals(levelDefinition.mouseStart())) {
      return CellContent.MOUSE_START;
    }
    if (position.equals(levelDefinition.cheese())) {
      return CellContent.CHEESE;
    }
    if (walls.contains(position)) {
      return CellContent.NORMAL_WALL;
    }
    return CellContent.EMPTY;
  }

  /** Returns whether the mouse start can reach the cheese through open cells. */
  public boolean hasPathFromStartToCheese() {
    return hasPathFromStartToCheese(levelDefinition, walls);
  }

  private void requireInsideGrid(GridPosition position) {
    Objects.requireNonNull(position, "position");
    if (!position.isWithin(levelDefinition.gridSize())) {
      throw new IllegalArgumentException("position must be inside the grid");
    }
  }

  private static void validateWallPosition(LevelDefinition levelDefinition, GridPosition position) {
    Objects.requireNonNull(position, "position");
    if (!position.isWithin(levelDefinition.gridSize())) {
      throw new IllegalArgumentException("wall position must be inside the grid");
    }
    if (position.equals(levelDefinition.mouseStart())
        || position.equals(levelDefinition.cheese())) {
      throw new IllegalArgumentException("wall position must not be protected");
    }
  }

  private static boolean hasPathFromStartToCheese(
      LevelDefinition levelDefinition, Set<GridPosition> walls) {
    Queue<GridPosition> frontier = new ArrayDeque<>();
    Set<GridPosition> visited = new HashSet<>();
    frontier.add(levelDefinition.mouseStart());
    visited.add(levelDefinition.mouseStart());

    while (!frontier.isEmpty()) {
      GridPosition current = frontier.remove();
      if (current.equals(levelDefinition.cheese())) {
        return true;
      }
      for (GridPosition neighbor : neighbors(current)) {
        if (isOpen(levelDefinition, walls, neighbor) && visited.add(neighbor)) {
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

  private static boolean isOpen(
      LevelDefinition levelDefinition, Set<GridPosition> walls, GridPosition position) {
    return position.isWithin(levelDefinition.gridSize())
        && (position.equals(levelDefinition.mouseStart())
            || position.equals(levelDefinition.cheese())
            || !walls.contains(position));
  }
}
