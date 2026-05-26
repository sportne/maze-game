package io.github.sportne.mazegame.model.maze;

import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

/**
 * Immutable wall layout for a level.
 *
 * <p>The maze owns the player-created wall set and enforces the milestone-one rule that the mouse
 * start must remain connected to the cheese through four-directional movement. UI code should use
 * {@link #placeWall(GridPosition)} for friendly rejection reasons; tests and internal code can use
 * {@link #withWall(GridPosition)} when exceptions are more convenient.
 *
 * @param levelDefinition static level data this maze belongs to
 * @param walls immutable set of normal wall positions
 */
public record MazeState(LevelDefinition levelDefinition, Set<GridPosition> walls) {
  /**
   * Creates a validated immutable maze state.
   *
   * @throws IllegalArgumentException when a wall is out of bounds, protected, or disconnects the
   *     start from the cheese
   */
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

  /**
   * Creates an empty maze for the given level.
   *
   * @param levelDefinition level data to attach to the maze
   * @return a maze with no player-placed walls
   */
  public static MazeState empty(LevelDefinition levelDefinition) {
    return new MazeState(levelDefinition, Set.of());
  }

  /**
   * Returns a new maze with a normal wall at the given position.
   *
   * @param position cell where the wall should be placed
   * @return the updated maze when placement is accepted
   * @throws IllegalArgumentException when the wall placement is rejected
   */
  public MazeState withWall(GridPosition position) {
    WallPlacementResult result = placeWall(position);
    if (!result.accepted()) {
      throw new IllegalArgumentException("wall placement rejected: " + result.status());
    }
    return result.mazeState();
  }

  /**
   * Returns the result of trying to place a normal wall at the given position.
   *
   * @param position cell where the wall should be placed
   * @return accepted result with a new maze, or rejected result with this maze unchanged
   */
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

  /**
   * Returns a new maze without a wall at the given position.
   *
   * @param position cell to clear
   * @return updated maze without that wall
   */
  public MazeState withoutWall(GridPosition position) {
    requireInsideGrid(position);
    Set<GridPosition> updatedWalls = new HashSet<>(walls);
    updatedWalls.remove(position);
    return new MazeState(levelDefinition, updatedWalls);
  }

  /**
   * Returns whether a normal wall occupies the given position.
   *
   * @param position cell to inspect
   * @return true when the position contains a wall
   */
  public boolean hasWallAt(GridPosition position) {
    requireInsideGrid(position);
    return walls.contains(position);
  }

  /**
   * Returns whether the given position is reserved for start or cheese content.
   *
   * @param position cell to inspect
   * @return true when the cell is the mouse start or cheese endpoint
   */
  public boolean isProtected(GridPosition position) {
    requireInsideGrid(position);
    return position.equals(levelDefinition.mouseStart())
        || position.equals(levelDefinition.cheese());
  }

  /**
   * Returns the content rendered for the given position.
   *
   * <p>Start and cheese are reported before walls, although wall placement prevents protected-cell
   * walls from existing.
   *
   * @param position cell to inspect
   * @return render-independent content for the cell
   */
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

  /**
   * Returns whether the mouse start can reach the cheese through open cells.
   *
   * @return true when at least one four-directional path exists
   */
  public boolean hasPathFromStartToCheese() {
    return hasPathFromStartToCheese(levelDefinition, walls);
  }

  /**
   * Validates that a runtime query position is inside this maze.
   *
   * @param position position to validate
   */
  private void requireInsideGrid(GridPosition position) {
    Objects.requireNonNull(position, "position");
    if (!position.isWithin(levelDefinition.gridSize())) {
      throw new IllegalArgumentException("position must be inside the grid");
    }
  }

  /**
   * Validates a wall supplied to the canonical constructor.
   *
   * @param levelDefinition level whose protected cells and bounds apply
   * @param position wall position to validate
   */
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

  /**
   * Runs breadth-first search from mouse start to cheese.
   *
   * @param levelDefinition level geometry and endpoints
   * @param walls blocked cells to avoid
   * @return true when the cheese can be reached orthogonally
   */
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

  /**
   * Returns the four orthogonal neighbors around a position.
   *
   * @param position center cell
   * @return up, down, left, and right neighbors
   */
  private static Set<GridPosition> neighbors(GridPosition position) {
    return Set.of(
        new GridPosition(position.row() - 1, position.column()),
        new GridPosition(position.row() + 1, position.column()),
        new GridPosition(position.row(), position.column() - 1),
        new GridPosition(position.row(), position.column() + 1));
  }

  /**
   * Returns whether a position can be visited by pathfinding.
   *
   * @param levelDefinition level bounds and protected endpoints
   * @param walls blocked cells
   * @param position candidate position
   * @return true when the cell is inside the grid and not blocked
   */
  private static boolean isOpen(
      LevelDefinition levelDefinition, Set<GridPosition> walls, GridPosition position) {
    return position.isWithin(levelDefinition.gridSize())
        && (position.equals(levelDefinition.mouseStart())
            || position.equals(levelDefinition.cheese())
            || !walls.contains(position));
  }
}
