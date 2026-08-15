package io.github.sportne.mazegame.model.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.model.cell.FixedCellType;
import io.github.sportne.mazegame.model.cell.PlaceableCellSupply;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalLong;
import org.junit.jupiter.api.Test;

final class FixedCellDefinitionTest {
  private static final GridPosition START = position(2, 1);
  private static final GridPosition GOAL = position(0, 1);

  @Test
  void oldLevelConstructorAuthorsAnEmptyFixedCellCollection() {
    assertTrue(Levels.levelOne().fixedCells().isEmpty());
    assertTrue(Levels.levelOne().fixedCellAt(position(2, 2)).isEmpty());
    assertThrows(NullPointerException.class, () -> Levels.levelOne().fixedCellAt(null));
  }

  @Test
  void fixedCellsAreOrderedDefensivelyCopiedAndParticipateInEquality() {
    List<FixedCell> mutable =
        new ArrayList<>(
            List.of(
                new FixedCell(position(1, 0), FixedCellType.WALL),
                new FixedCell(position(1, 2), FixedCellType.SLOW_FLOOR)));
    LevelDefinition level = level(mutable);
    mutable.clear();

    assertEquals(2, level.fixedCells().size());
    assertEquals(FixedCellType.WALL, level.fixedCellAt(position(1, 0)).orElseThrow());
    assertEquals(FixedCellType.SLOW_FLOOR, level.fixedCellAt(position(1, 2)).orElseThrow());
    assertThrows(
        UnsupportedOperationException.class,
        () -> level.fixedCells().add(new FixedCell(position(2, 0), FixedCellType.WALL)));
    assertNotEquals(level(List.of()), level);
  }

  @Test
  void fixedCellValueAndEffectsAreRequiredAndExplicit() {
    assertThrows(NullPointerException.class, () -> new FixedCell(null, FixedCellType.WALL));
    assertThrows(NullPointerException.class, () -> new FixedCell(position(1, 0), null));

    assertTrue(FixedCellType.WALL.blocksMovement());
    assertFalse(FixedCellType.WALL.delaysNextDecision());
    assertFalse(FixedCellType.SLOW_FLOOR.blocksMovement());
    assertTrue(FixedCellType.SLOW_FLOOR.delaysNextDecision());
  }

  @Test
  void fixedCellAuthoringRejectsMissingDuplicateOutsideAndProtectedEntries() {
    assertThrows(NullPointerException.class, () -> level(null));
    assertThrows(NullPointerException.class, () -> level(Arrays.asList((FixedCell) null)));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            level(
                List.of(
                    new FixedCell(position(1, 0), FixedCellType.WALL),
                    new FixedCell(position(1, 0), FixedCellType.SLOW_FLOOR))));
    assertThrows(
        IllegalArgumentException.class,
        () -> level(List.of(new FixedCell(position(3, 0), FixedCellType.WALL))));
    assertThrows(
        IllegalArgumentException.class,
        () -> level(List.of(new FixedCell(START, FixedCellType.WALL))));
    assertThrows(
        IllegalArgumentException.class,
        () -> level(List.of(new FixedCell(GOAL, FixedCellType.SLOW_FLOOR))));
  }

  @Test
  void fixedWallsMustKeepABaselinePathForEverySolver() {
    List<FixedCell> barrier =
        List.of(
            new FixedCell(position(1, 0), FixedCellType.WALL),
            new FixedCell(position(1, 1), FixedCellType.WALL),
            new FixedCell(position(1, 2), FixedCellType.WALL));

    assertThrows(IllegalArgumentException.class, () -> level(barrier));
  }

  @Test
  void fixedWallsMustKeepPathsForEverySolverOnAMultiSolverLevel() {
    List<LevelSolver> solvers =
        List.of(solver(position(2, 0), position(1, 0)), solver(position(2, 2), position(0, 2)));
    List<FixedCell> trapsSecondSolver =
        List.of(
            new FixedCell(position(1, 2), FixedCellType.WALL),
            new FixedCell(position(2, 1), FixedCellType.WALL));

    assertThrows(IllegalArgumentException.class, () -> level(trapsSecondSolver, solvers));
  }

  private static LevelDefinition level(List<FixedCell> fixedCells) {
    return level(fixedCells, List.of(solver(START, GOAL)));
  }

  private static LevelDefinition level(List<FixedCell> fixedCells, List<LevelSolver> solvers) {
    return new LevelDefinition(
        "fixed-definition",
        "Fixed Definition",
        GridSize.square(3),
        Duration.ofSeconds(10),
        Duration.ofSeconds(2),
        Duration.ofSeconds(5),
        Duration.ofMillis(250),
        PlaceableCellSupply.unlimitedWallsOnly(),
        fixedCells,
        solvers);
  }

  private static LevelSolver solver(GridPosition start, GridPosition goal) {
    return new LevelSolver(
        start,
        goal,
        SolverBehavior.LEFT_PRIORITY,
        OptionalLong.empty(),
        SolverAppearance.SCOUT_SQUIRREL,
        GoalType.ACORN);
  }

  private static GridPosition position(int row, int column) {
    return new GridPosition(row, column);
  }
}
