package io.github.sportne.mazegame;

import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.maze.MazeState;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Test fixtures for constructing canonical maze states from concise authored layouts. */
public final class TestMazeStates {
  private TestMazeStates() {}

  /** Creates a canonical maze whose placed cells are all Walls. */
  public static MazeState withWalls(LevelDefinition level, Set<GridPosition> walls) {
    return new MazeState(level, wallCells(walls));
  }

  /** Converts a position set into canonical Wall cell data. */
  public static Map<GridPosition, PlaceableCellType> wallCells(Set<GridPosition> walls) {
    return walls.stream()
        .collect(
            Collectors.toUnmodifiableMap(position -> position, ignored -> PlaceableCellType.WALL));
  }
}
