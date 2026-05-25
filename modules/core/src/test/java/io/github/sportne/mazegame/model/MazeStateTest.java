package io.github.sportne.mazegame.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
  void withWallAddsANormalWallImmutably() {
    MazeState emptyMaze = MazeState.empty(LEVEL);
    GridPosition wall = new GridPosition(2, 2);

    MazeState updatedMaze = emptyMaze.withWall(wall);

    assertFalse(emptyMaze.hasWallAt(wall));
    assertTrue(updatedMaze.hasWallAt(wall));
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
  void rejectsWallOnMouseStart() {
    MazeState maze = MazeState.empty(LEVEL);

    assertThrows(
        IllegalArgumentException.class,
        () -> assertEquals(maze, maze.withWall(LEVEL.mouseStart())));
  }

  @Test
  void rejectsWallOnCheese() {
    MazeState maze = MazeState.empty(LEVEL);

    assertThrows(
        IllegalArgumentException.class, () -> assertEquals(maze, maze.withWall(LEVEL.cheese())));
  }

  @Test
  void rejectsWallOutsideGrid() {
    MazeState maze = MazeState.empty(LEVEL);

    assertThrows(
        IllegalArgumentException.class,
        () -> assertEquals(maze, maze.withWall(new GridPosition(5, 2))));
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
}
