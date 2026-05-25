package io.github.sportne.mazegame.model;

import java.util.HashSet;
import java.util.Objects;
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
  }

  /** Creates an empty maze for the given level. */
  public static MazeState empty(LevelDefinition levelDefinition) {
    return new MazeState(levelDefinition, Set.of());
  }

  /** Returns a new maze with a normal wall at the given position. */
  public MazeState withWall(GridPosition position) {
    validateWallPosition(levelDefinition, position);
    Set<GridPosition> updatedWalls = new HashSet<>(walls);
    updatedWalls.add(position);
    return new MazeState(levelDefinition, updatedWalls);
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
}
