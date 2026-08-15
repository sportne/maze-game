package io.github.sportne.mazegame.model.maze;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.model.cell.CellSupply;
import io.github.sportne.mazegame.model.cell.FixedCellType;
import io.github.sportne.mazegame.model.cell.PlaceableCellSupply;
import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import io.github.sportne.mazegame.model.level.FixedCell;
import io.github.sportne.mazegame.model.level.GoalType;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.level.LevelSolver;
import io.github.sportne.mazegame.model.level.SolverAppearance;
import io.github.sportne.mazegame.model.level.SolverBehavior;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import org.junit.jupiter.api.Test;

final class MazeFixedCellTest {
  private static final GridPosition FIXED_WALL = position(1, 0);
  private static final GridPosition FIXED_SLOW_FLOOR = position(1, 2);
  private static final GridPosition PLAYER_CELL = position(2, 0);

  @Test
  void fixedEffectsParticipateInContentTraversalAndTimingWithoutUsingInventory() {
    LevelDefinition level = level();
    MazeState maze = MazeState.empty(level);

    assertEquals(CellContent.NORMAL_WALL, maze.cellContentAt(FIXED_WALL));
    assertEquals(CellContent.SLOW_FLOOR, maze.cellContentAt(FIXED_SLOW_FLOOR));
    assertFalse(maze.isTraversable(FIXED_WALL));
    assertTrue(maze.isTraversable(FIXED_SLOW_FLOOR));
    assertFalse(maze.delaysNextDecisionAt(FIXED_WALL));
    assertTrue(maze.delaysNextDecisionAt(FIXED_SLOW_FLOOR));
    assertTrue(maze.hasFixedCellAt(FIXED_WALL));
    assertFalse(maze.hasPlacedCellAt(FIXED_WALL));
    assertNull(maze.placedCellAt(FIXED_WALL));
    assertFalse(maze.isProtected(FIXED_WALL));
    assertEquals(CellSupply.finite(1), maze.remainingSupply(PlaceableCellType.WALL));
    assertEquals(CellSupply.finite(1), maze.remainingSupply(PlaceableCellType.SLOW_FLOOR));
  }

  @Test
  void everyEditPathRejectsFixedCellsAtomically() {
    MazeState empty = MazeState.empty(level());
    assertRejected(empty.placeOrReplace(PlaceableCellType.WALL, FIXED_SLOW_FLOOR), empty);
    assertRejected(empty.remove(FIXED_WALL), empty);
    assertRejected(empty.move(FIXED_WALL, PLAYER_CELL), empty);

    MazeState withPlayerCell =
        empty.placeOrReplace(PlaceableCellType.WALL, PLAYER_CELL).mazeState();
    assertRejected(withPlayerCell.move(PLAYER_CELL, FIXED_WALL), withPlayerCell);
    assertEquals(CellSupply.finite(0), withPlayerCell.remainingSupply(PlaceableCellType.WALL));
    assertEquals(
        CellSupply.finite(1), withPlayerCell.remainingSupply(PlaceableCellType.SLOW_FLOOR));
  }

  @Test
  void playerStateCannotBeConstructedOverAFixedCell() {
    LevelDefinition level = level();

    assertThrows(
        IllegalArgumentException.class,
        () -> new MazeState(level, Map.of(FIXED_WALL, PlaceableCellType.WALL)));
  }

  @Test
  void playerEditsValidatePathsAgainstFixedWalls() {
    LevelDefinition level =
        level(
            List.of(
                new FixedCell(position(1, 0), FixedCellType.WALL),
                new FixedCell(position(1, 2), FixedCellType.WALL)));
    MazeState maze = MazeState.empty(level);

    MazeEditResult result = maze.placeOrReplace(PlaceableCellType.WALL, position(1, 1));

    assertEquals(MazeEditStatus.REJECTED_BLOCKS_PATH, result.status());
    assertSame(maze, result.mazeState());
  }

  private static void assertRejected(MazeEditResult result, MazeState original) {
    assertEquals(MazeEditStatus.REJECTED_FIXED_CELL, result.status());
    assertSame(original, result.mazeState());
    assertEquals(original.remainingSupplies(), result.mazeState().remainingSupplies());
  }

  private static LevelDefinition level() {
    return level(
        List.of(
            new FixedCell(FIXED_WALL, FixedCellType.WALL),
            new FixedCell(FIXED_SLOW_FLOOR, FixedCellType.SLOW_FLOOR)));
  }

  private static LevelDefinition level(List<FixedCell> fixedCells) {
    return new LevelDefinition(
        "fixed-maze",
        "Fixed Maze",
        GridSize.square(3),
        Duration.ofSeconds(10),
        Duration.ofSeconds(2),
        Duration.ofSeconds(5),
        Duration.ofMillis(250),
        List.of(
            PlaceableCellSupply.finite(PlaceableCellType.WALL, 1),
            PlaceableCellSupply.finite(PlaceableCellType.SLOW_FLOOR, 1)),
        fixedCells,
        List.of(
            new LevelSolver(
                position(2, 1),
                position(0, 1),
                SolverBehavior.LEFT_PRIORITY,
                OptionalLong.empty(),
                SolverAppearance.SCOUT_SQUIRREL,
                GoalType.ACORN)));
  }

  private static GridPosition position(int row, int column) {
    return new GridPosition(row, column);
  }
}
