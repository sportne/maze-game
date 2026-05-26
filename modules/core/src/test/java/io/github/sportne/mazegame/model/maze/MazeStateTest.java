package io.github.sportne.mazegame.model.maze;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.level.Levels;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class MazeStateTest {
  private static final LevelDefinition LEVEL = Levels.milestoneOne();

  @Test
  void emptyMazeStartsWithNoWalls() {
    MazeState maze = MazeState.empty(LEVEL);

    assertTrue(maze.walls().isEmpty());
  }

  @Test
  void emptyMazeHasPathFromStartToCheese() {
    MazeState maze = MazeState.empty(LEVEL);

    assertTrue(maze.hasPathFromStartToCheese());
  }

  @Test
  void withWallAddsANormalWallImmutably() {
    MazeState emptyMaze = MazeState.empty(LEVEL);
    GridPosition wall = new GridPosition(2, 2);

    MazeState updatedMaze = emptyMaze.withWall(wall);

    assertFalse(emptyMaze.hasWallAt(wall));
    assertTrue(updatedMaze.hasWallAt(wall));
  }

  @Test
  void placeWallAddsANormalWallImmutably() {
    MazeState emptyMaze = MazeState.empty(LEVEL);
    GridPosition wall = new GridPosition(2, 2);

    WallPlacementResult result = emptyMaze.placeWall(wall);

    assertTrue(result.accepted());
    assertEquals(WallPlacementStatus.PLACED, result.status());
    assertEquals(emptyMaze, MazeState.empty(LEVEL));
    assertFalse(emptyMaze.hasWallAt(wall));
    assertTrue(result.mazeState().hasWallAt(wall));
  }

  @Test
  void withoutWallRemovesWallImmutably() {
    GridPosition wall = new GridPosition(2, 2);
    MazeState mazeWithWall = MazeState.empty(LEVEL).withWall(wall);

    MazeState updatedMaze = mazeWithWall.withoutWall(wall);

    assertTrue(mazeWithWall.hasWallAt(wall));
    assertFalse(updatedMaze.hasWallAt(wall));
  }

  @Test
  void wallsAreDefensivelyCopied() {
    GridPosition wall = new GridPosition(2, 2);
    Set<GridPosition> walls = new HashSet<>();

    MazeState maze = new MazeState(LEVEL, walls);
    walls.add(wall);

    assertFalse(maze.hasWallAt(wall));
    assertThrows(UnsupportedOperationException.class, () -> maze.walls().add(wall));
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
        () -> assertTrue(new MazeState(LEVEL, blockingRow).hasPathFromStartToCheese()));
  }

  @Test
  void rejectsWallOnMouseStart() {
    MazeState maze = MazeState.empty(LEVEL);

    assertThrows(
        IllegalArgumentException.class,
        () -> assertEquals(maze, maze.withWall(LEVEL.mouseStart())));
  }

  @Test
  void placeWallRejectsMouseStartWithoutMutatingMaze() {
    MazeState maze = MazeState.empty(LEVEL);

    WallPlacementResult result = maze.placeWall(LEVEL.mouseStart());

    assertFalse(result.accepted());
    assertEquals(WallPlacementStatus.REJECTED_PROTECTED_CELL, result.status());
    assertEquals(maze, result.mazeState());
  }

  @Test
  void rejectsWallOnCheese() {
    MazeState maze = MazeState.empty(LEVEL);

    assertThrows(
        IllegalArgumentException.class, () -> assertEquals(maze, maze.withWall(LEVEL.cheese())));
  }

  @Test
  void placeWallRejectsCheeseWithoutMutatingMaze() {
    MazeState maze = MazeState.empty(LEVEL);

    WallPlacementResult result = maze.placeWall(LEVEL.cheese());

    assertFalse(result.accepted());
    assertEquals(WallPlacementStatus.REJECTED_PROTECTED_CELL, result.status());
    assertEquals(maze, result.mazeState());
  }

  @Test
  void rejectsWallOutsideGrid() {
    MazeState maze = MazeState.empty(LEVEL);

    assertThrows(
        IllegalArgumentException.class,
        () -> assertEquals(maze, maze.withWall(new GridPosition(5, 2))));
  }

  @Test
  void placeWallRejectsOutsideGridWithoutMutatingMaze() {
    MazeState maze = MazeState.empty(LEVEL);

    WallPlacementResult result = maze.placeWall(new GridPosition(5, 2));

    assertFalse(result.accepted());
    assertEquals(WallPlacementStatus.REJECTED_OUTSIDE_GRID, result.status());
    assertEquals(maze, result.mazeState());
  }

  @Test
  void placeWallRejectsWallThatWouldBlockOnlyPath() {
    MazeState maze =
        new MazeState(
            LEVEL,
            Set.of(
                new GridPosition(2, 0),
                new GridPosition(2, 1),
                new GridPosition(2, 3),
                new GridPosition(2, 4)));

    WallPlacementResult result = maze.placeWall(new GridPosition(2, 2));

    assertFalse(result.accepted());
    assertEquals(WallPlacementStatus.REJECTED_BLOCKS_PATH, result.status());
    assertEquals(maze, result.mazeState());
  }

  @Test
  void withWallRejectsWallThatWouldBlockOnlyPath() {
    MazeState maze =
        new MazeState(
            LEVEL,
            Set.of(
                new GridPosition(2, 0),
                new GridPosition(2, 1),
                new GridPosition(2, 3),
                new GridPosition(2, 4)));

    assertThrows(
        IllegalArgumentException.class,
        () -> assertEquals(maze, maze.withWall(new GridPosition(2, 2))));
  }

  @Test
  void placeWallReportsAlreadyPresentWallAsAccepted() {
    GridPosition wall = new GridPosition(2, 2);
    MazeState maze = MazeState.empty(LEVEL).withWall(wall);

    WallPlacementResult result = maze.placeWall(wall);

    assertTrue(result.accepted());
    assertEquals(WallPlacementStatus.ALREADY_PRESENT, result.status());
    assertEquals(maze, result.mazeState());
  }

  @Test
  void identifiesProtectedCells() {
    MazeState maze = MazeState.empty(LEVEL);

    assertTrue(maze.isProtected(LEVEL.mouseStart()));
    assertTrue(maze.isProtected(LEVEL.cheese()));
    assertFalse(maze.isProtected(new GridPosition(2, 2)));
  }

  @Test
  void cellContentIdentifiesStartCheeseWallAndEmptyCells() {
    MazeState maze = MazeState.empty(LEVEL).withWall(new GridPosition(2, 2));

    assertEquals(CellContent.MOUSE_START, maze.cellContentAt(LEVEL.mouseStart()));
    assertEquals(CellContent.CHEESE, maze.cellContentAt(LEVEL.cheese()));
    assertEquals(CellContent.NORMAL_WALL, maze.cellContentAt(new GridPosition(2, 2)));
    assertEquals(CellContent.EMPTY, maze.cellContentAt(new GridPosition(1, 1)));
  }

  @Test
  void acceptsWindingPathThroughMostlyBlockedMaze() {
    MazeState maze =
        new MazeState(
            LEVEL,
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
                new GridPosition(4, 4)));

    assertTrue(maze.hasPathFromStartToCheese());
  }
}
