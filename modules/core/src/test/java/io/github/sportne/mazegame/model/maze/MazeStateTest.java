package io.github.sportne.mazegame.model.maze;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.level.Levels;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class MazeStateTest {
  private static final LevelDefinition LEVEL = Levels.levelOne();

  @Test
  void emptyMazeStartsWithNoWalls() {
    MazeState maze = MazeState.empty(LEVEL);

    assertTrue(maze.placedCells().isEmpty());
  }

  @Test
  void emptyMazeHasPathFromStartToGoal() {
    MazeState maze = MazeState.empty(LEVEL);

    assertTrue(maze.hasPathFromStartToGoal());
  }

  @Test
  void placingWallAddsANormalWallImmutably() {
    MazeState emptyMaze = MazeState.empty(LEVEL);
    GridPosition wall = new GridPosition(2, 2);

    MazeState updatedMaze = emptyMaze.placeOrReplace(PlaceableCellType.WALL, wall).mazeState();

    assertFalse(emptyMaze.placedCells().containsKey(wall));
    assertTrue(updatedMaze.placedCells().containsKey(wall));
  }

  @Test
  void wallEditReportsPlacementAndPreservesOriginalMaze() {
    MazeState emptyMaze = MazeState.empty(LEVEL);
    GridPosition wall = new GridPosition(2, 2);

    MazeEditResult result = emptyMaze.placeOrReplace(PlaceableCellType.WALL, wall);

    assertTrue(result.accepted());
    assertEquals(MazeEditStatus.PLACED, result.status());
    assertEquals(emptyMaze, MazeState.empty(LEVEL));
    assertFalse(emptyMaze.placedCells().containsKey(wall));
    assertTrue(result.mazeState().placedCells().containsKey(wall));
  }

  @Test
  void removingWallIsImmutable() {
    GridPosition wall = new GridPosition(2, 2);
    MazeState mazeWithWall =
        MazeState.empty(LEVEL).placeOrReplace(PlaceableCellType.WALL, wall).mazeState();

    MazeState updatedMaze = mazeWithWall.remove(wall).mazeState();

    assertTrue(mazeWithWall.placedCells().containsKey(wall));
    assertFalse(updatedMaze.placedCells().containsKey(wall));
  }

  @Test
  void wallsAreDefensivelyCopied() {
    GridPosition wall = new GridPosition(2, 2);
    Map<GridPosition, PlaceableCellType> walls = new java.util.HashMap<>();

    MazeState maze = new MazeState(LEVEL, walls);
    walls.put(wall, PlaceableCellType.WALL);

    assertFalse(maze.placedCells().containsKey(wall));
    assertThrows(UnsupportedOperationException.class, () -> maze.placedCells().keySet().add(wall));
  }

  @Test
  void constructorRejectsDisconnectedWallLayout() {
    Set<GridPosition> blockingRow =
        Set.of(
            new GridPosition(2, 0),
            new GridPosition(2, 1),
            new GridPosition(2, 2),
            new GridPosition(2, 3),
            new GridPosition(2, 4));

    assertThrows(
        IllegalArgumentException.class,
        () -> assertTrue(new MazeState(LEVEL, wallCells(blockingRow)).hasPathFromStartToGoal()));
  }

  @Test
  void wallEditRejectsSolverStartWithoutMutatingMaze() {
    MazeState maze = MazeState.empty(LEVEL);

    MazeEditResult result =
        maze.placeOrReplace(PlaceableCellType.WALL, LEVEL.primarySolver().start());

    assertFalse(result.accepted());
    assertEquals(MazeEditStatus.REJECTED_PROTECTED_CELL, result.status());
    assertEquals(maze, result.mazeState());
  }

  @Test
  void wallEditRejectsGoalWithoutMutatingMaze() {
    MazeState maze = MazeState.empty(LEVEL);

    MazeEditResult result =
        maze.placeOrReplace(PlaceableCellType.WALL, LEVEL.primarySolver().goal());

    assertFalse(result.accepted());
    assertEquals(MazeEditStatus.REJECTED_PROTECTED_CELL, result.status());
    assertEquals(maze, result.mazeState());
  }

  @Test
  void wallEditRejectsOutsideGridWithoutMutatingMaze() {
    MazeState maze = MazeState.empty(LEVEL);

    MazeEditResult result = maze.placeOrReplace(PlaceableCellType.WALL, new GridPosition(5, 2));

    assertFalse(result.accepted());
    assertEquals(MazeEditStatus.REJECTED_OUTSIDE_GRID, result.status());
    assertEquals(maze, result.mazeState());
  }

  @Test
  void wallEditRejectsWallThatWouldBlockOnlyPath() {
    MazeState maze =
        new MazeState(
            LEVEL,
            wallCells(
                Set.of(
                    new GridPosition(2, 0),
                    new GridPosition(2, 1),
                    new GridPosition(2, 3),
                    new GridPosition(2, 4))));

    MazeEditResult result = maze.placeOrReplace(PlaceableCellType.WALL, new GridPosition(2, 2));

    assertFalse(result.accepted());
    assertEquals(MazeEditStatus.REJECTED_BLOCKS_PATH, result.status());
    assertEquals(maze, result.mazeState());
  }

  @Test
  void placingSelectedWallAgainRemovesIt() {
    GridPosition wall = new GridPosition(2, 2);
    MazeState maze =
        MazeState.empty(LEVEL).placeOrReplace(PlaceableCellType.WALL, wall).mazeState();

    MazeEditResult result = maze.placeOrReplace(PlaceableCellType.WALL, wall);

    assertTrue(result.accepted());
    assertEquals(MazeEditStatus.REMOVED, result.status());
    assertTrue(result.mazeState().placedCells().isEmpty());
  }

  @Test
  void identifiesProtectedCells() {
    MazeState maze = MazeState.empty(LEVEL);

    assertTrue(maze.isProtected(LEVEL.primarySolver().start()));
    assertTrue(maze.isProtected(LEVEL.primarySolver().goal()));
    assertFalse(maze.isProtected(new GridPosition(2, 2)));
  }

  @Test
  void cellContentIdentifiesStartGoalWallAndEmptyCells() {
    MazeState maze =
        MazeState.empty(LEVEL)
            .placeOrReplace(PlaceableCellType.WALL, new GridPosition(2, 2))
            .mazeState();

    assertEquals(CellContent.SOLVER_START, maze.cellContentAt(LEVEL.primarySolver().start()));
    assertEquals(CellContent.GOAL, maze.cellContentAt(LEVEL.primarySolver().goal()));
    assertEquals(CellContent.NORMAL_WALL, maze.cellContentAt(new GridPosition(2, 2)));
    assertEquals(CellContent.EMPTY, maze.cellContentAt(new GridPosition(1, 1)));
  }

  @Test
  void acceptsWindingPathThroughMostlyBlockedMaze() {
    MazeState maze =
        new MazeState(
            LEVEL,
            wallCells(
                Set.of(
                    new GridPosition(0, 0),
                    new GridPosition(0, 1),
                    new GridPosition(0, 3),
                    new GridPosition(0, 4),
                    new GridPosition(1, 3),
                    new GridPosition(1, 4),
                    new GridPosition(2, 1),
                    new GridPosition(2, 2),
                    new GridPosition(2, 3),
                    new GridPosition(2, 4),
                    new GridPosition(3, 2),
                    new GridPosition(3, 3),
                    new GridPosition(3, 4),
                    new GridPosition(4, 0),
                    new GridPosition(4, 3),
                    new GridPosition(4, 4))));

    assertTrue(maze.hasPathFromStartToGoal());
  }

  private static Map<GridPosition, PlaceableCellType> wallCells(Set<GridPosition> walls) {
    return walls.stream()
        .collect(
            java.util.stream.Collectors.toMap(
                position -> position, ignored -> PlaceableCellType.WALL));
  }
}
