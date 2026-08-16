package io.github.sportne.mazegame.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.model.cell.CellSupply;
import io.github.sportne.mazegame.model.cell.FixedCellType;
import io.github.sportne.mazegame.model.cell.PlaceableCellSupply;
import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import io.github.sportne.mazegame.model.level.FixedCell;
import io.github.sportne.mazegame.model.level.GoalType;
import io.github.sportne.mazegame.model.level.LevelCatalog;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.level.LevelSolver;
import io.github.sportne.mazegame.model.level.PresetCell;
import io.github.sportne.mazegame.model.level.SolverAppearance;
import io.github.sportne.mazegame.model.level.SolverBehavior;
import io.github.sportne.mazegame.model.maze.CellContent;
import io.github.sportne.mazegame.model.maze.MazeEditResult;
import io.github.sportne.mazegame.model.maze.MazeEditStatus;
import io.github.sportne.mazegame.model.maze.MazeState;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import org.junit.jupiter.api.Test;

final class GameSessionPresetCellTest {
  private static final GridPosition PRESET_WALL = p(2, 2);
  private static final GridPosition PRESET_SLOW_FLOOR = p(3, 1);
  private static final GridPosition FIXED_WALL = p(0, 1);

  @Test
  void freshMazeMaterializesPresetsAsConsumedMutableInventory() {
    LevelDefinition level = level();
    MazeState empty = MazeState.empty(level);
    MazeState initial = MazeState.initial(level);

    assertTrue(empty.placedCells().isEmpty());
    assertEquals(
        Map.of(
            PRESET_WALL, PlaceableCellType.WALL, PRESET_SLOW_FLOOR, PlaceableCellType.SLOW_FLOOR),
        initial.placedCells());
    assertEquals(CellSupply.finite(1), initial.remainingSupply(PlaceableCellType.WALL));
    assertEquals(CellSupply.finite(1), initial.remainingSupply(PlaceableCellType.SLOW_FLOOR));
    assertEquals(CellContent.NORMAL_WALL, initial.cellContentAt(PRESET_WALL));
    assertEquals(CellContent.SLOW_FLOOR, initial.cellContentAt(PRESET_SLOW_FLOOR));
    assertFalse(initial.hasFixedCellAt(PRESET_WALL));
    assertTrue(initial.hasFixedCellAt(FIXED_WALL));
  }

  @Test
  void paletteReportsInventoryRemainingAfterPresetMaterialization() {
    GameSession session = session();
    assertTrue(session.startLevel(level().id()));

    assertEquals(2, session.paletteState().size());
    assertEquals(
        List.of(CellSupply.finite(1), CellSupply.finite(1)),
        session.paletteState().stream().map(CellPaletteState::remainingSupply).toList());
  }

  @Test
  void presetsSupportOrdinaryMoveRemoveReplaceAndInventoryRules() {
    GameSession session = session();
    assertTrue(session.startLevel(level().id()));

    GridPosition movedWall = p(2, 3);
    MazeEditResult moved = session.moveCell(PRESET_WALL, movedWall).orElseThrow();
    assertEquals(MazeEditStatus.MOVED, moved.status());
    assertEquals(CellSupply.finite(1), session.mazeState().remainingSupply(PlaceableCellType.WALL));

    MazeEditResult removed = session.removeCell(PRESET_SLOW_FLOOR).orElseThrow();
    assertEquals(MazeEditStatus.REMOVED, removed.status());
    assertEquals(
        CellSupply.finite(2), session.mazeState().remainingSupply(PlaceableCellType.SLOW_FLOOR));

    session.selectCellType(PlaceableCellType.SLOW_FLOOR);
    MazeEditResult replaced = session.placeOrReplaceCell(movedWall).orElseThrow();
    assertEquals(MazeEditStatus.REPLACED, replaced.status());
    assertEquals(CellSupply.finite(2), session.mazeState().remainingSupply(PlaceableCellType.WALL));
    assertEquals(
        CellSupply.finite(1), session.mazeState().remainingSupply(PlaceableCellType.SLOW_FLOOR));
  }

  @Test
  void fixedCellsRemainRejectedWhilePresetCellsAreMovable() {
    GameSession session = session();
    assertTrue(session.startLevel(level().id()));
    MazeState before = session.mazeState();

    MazeEditResult fixed = session.moveCell(FIXED_WALL, p(1, 1)).orElseThrow();
    assertEquals(MazeEditStatus.REJECTED_FIXED_CELL, fixed.status());
    assertSame(before, session.mazeState());

    MazeEditResult preset = session.moveCell(PRESET_SLOW_FLOOR, p(2, 1)).orElseThrow();
    assertEquals(MazeEditStatus.MOVED, preset.status());
  }

  @Test
  void retryAndNavigationRestorePresetsWhileReplayKeepsTheEditedBoard() {
    GameSession session = session();
    assertTrue(session.startLevel(level().id()));
    GridPosition movedSlowFloor = p(2, 1);
    assertEquals(
        MazeEditStatus.MOVED,
        session.moveCell(PRESET_SLOW_FLOOR, movedSlowFloor).orElseThrow().status());
    Map<GridPosition, PlaceableCellType> edited = session.mazeState().placedCells();

    session.startRun();
    session.updateSolverRun(5.0F);
    assertEquals(GamePhase.RESULT, session.gamePhase());
    session.replayRun();
    assertEquals(edited, session.mazeState().placedCells());
    session.updateSolverRun(5.0F);

    session.retryLevel();
    assertEquals(initialCells(), session.mazeState().placedCells());
    session.returnToLevelSelect();
    assertEquals(initialCells(), session.mazeState().placedCells());
    assertTrue(session.startLevel(level().id()));
    assertEquals(initialCells(), session.mazeState().placedCells());
  }

  private static GameSession session() {
    LevelDefinition level = level();
    return new GameSession(new LevelCatalog(List.of(level)), level.id(), BestResultStore.none());
  }

  private static Map<GridPosition, PlaceableCellType> initialCells() {
    return Map.of(
        PRESET_WALL, PlaceableCellType.WALL, PRESET_SLOW_FLOOR, PlaceableCellType.SLOW_FLOOR);
  }

  private static LevelDefinition level() {
    return new LevelDefinition(
        "preset-session",
        "Preset Session",
        GridSize.square(4),
        Duration.ofSeconds(10),
        Duration.ofMillis(250),
        Duration.ofSeconds(5),
        Duration.ofMillis(250),
        List.of(
            PlaceableCellSupply.finite(PlaceableCellType.WALL, 2),
            PlaceableCellSupply.finite(PlaceableCellType.SLOW_FLOOR, 2)),
        List.of(new FixedCell(FIXED_WALL, FixedCellType.WALL)),
        List.of(
            new PresetCell(PRESET_WALL, PlaceableCellType.WALL),
            new PresetCell(PRESET_SLOW_FLOOR, PlaceableCellType.SLOW_FLOOR)),
        List.of(
            new LevelSolver(
                p(3, 0),
                p(0, 3),
                SolverBehavior.LEFT_PRIORITY,
                OptionalLong.empty(),
                SolverAppearance.SCOUT_SQUIRREL,
                GoalType.ACORN)));
  }

  private static GridPosition p(int row, int column) {
    return new GridPosition(row, column);
  }
}
