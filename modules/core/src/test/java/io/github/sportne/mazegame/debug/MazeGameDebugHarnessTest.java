package io.github.sportne.mazegame.debug;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.level.Levels;
import io.github.sportne.mazegame.model.result.BestResult;
import io.github.sportne.mazegame.model.solver.SolverDecisionState;
import io.github.sportne.mazegame.state.GamePhase;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class MazeGameDebugHarnessTest {
  private static final GridPosition LEVEL_ONE_WALL = new GridPosition(1, 2);
  private static final Set<GridPosition> MILESTONE_TWO_WALLS = Set.of(new GridPosition(2, 2));

  private static final Set<GridPosition> MILESTONE_THREE_WALLS =
      Set.of(new GridPosition(0, 3), new GridPosition(3, 1));

  private static final Set<GridPosition> MILESTONE_FOUR_WALLS =
      Set.of(new GridPosition(3, 1), new GridPosition(1, 0));

  private static final Set<GridPosition> MILESTONE_FIVE_WALLS = Set.of(new GridPosition(1, 5));

  private static final Set<GridPosition> MILESTONE_FIVE_SLOW_FLOORS =
      Set.of(new GridPosition(1, 3), new GridPosition(2, 3));

  private static final Set<GridPosition> LEVEL_SIX_WALLS = Set.of(new GridPosition(2, 3));

  private static final Set<GridPosition> LEVEL_SIX_SLOW_FLOORS =
      Set.of(new GridPosition(1, 2), new GridPosition(2, 2), new GridPosition(3, 2));

  private static final Set<GridPosition> LEVEL_SEVEN_WALLS = Set.of(new GridPosition(1, 2));

  private static final Set<GridPosition> LEVEL_SEVEN_SLOW_FLOORS =
      Set.of(new GridPosition(1, 1), new GridPosition(2, 0), new GridPosition(2, 1));

  private static final Set<GridPosition> LEVEL_EIGHT_WALLS = Set.of(new GridPosition(2, 6));

  private static final Set<GridPosition> LEVEL_EIGHT_SLOW_FLOORS =
      Set.of(
          new GridPosition(2, 5),
          new GridPosition(2, 4),
          new GridPosition(3, 4),
          new GridPosition(1, 5));

  private static final Set<GridPosition> LEVEL_NINE_WALLS = Set.of(new GridPosition(7, 1));

  private static final Set<GridPosition> LEVEL_NINE_GATES = Set.of(new GridPosition(1, 8));

  private static final Set<GridPosition> LEVEL_TEN_WALLS =
      Set.of(new GridPosition(9, 3), new GridPosition(4, 0));

  private static final Set<GridPosition> LEVEL_TEN_SLOW_FLOORS =
      Set.of(
          new GridPosition(7, 0),
          new GridPosition(8, 0),
          new GridPosition(7, 1),
          new GridPosition(9, 1),
          new GridPosition(6, 1),
          new GridPosition(8, 4));

  private static final Set<GridPosition> LEVEL_NINE_SLOW_FLOORS =
      Set.of(
          new GridPosition(7, 0),
          new GridPosition(3, 6),
          new GridPosition(7, 2),
          new GridPosition(8, 1));

  @Test
  void rejectsInvalidScreenDimensions() {
    assertThrows(IllegalArgumentException.class, () -> new MazeGameDebugHarness(0, 720));
    assertThrows(IllegalArgumentException.class, () -> new MazeGameDebugHarness(1280, 0));
  }

  @Test
  void simulatesWallPlacementAndClearingByGridCell() {
    MazeGameDebugHarness harness = new MazeGameDebugHarness();
    GridPosition wall = LEVEL_ONE_WALL;

    harness.leftClickCell(wall);
    assertTrue(harness.snapshot().mazeState().placedCells().containsKey(wall));

    harness.rightClickCell(wall);
    assertEquals(
        io.github.sportne.mazegame.model.maze.MazeState.initial(Levels.levelOne()).placedCells(),
        harness.snapshot().mazeState().placedCells());
  }

  @Test
  void snapshotOmitsCellTypesUnavailableAtLevelStart() {
    MazeGameDebugHarness harness = new MazeGameDebugHarness();

    assertEquals(1, harness.snapshot().paletteState().size());
    assertEquals(PlaceableCellType.WALL, harness.snapshot().paletteState().get(0).type());
  }

  @Test
  void canonicalSnapshotAcceptsExplicitEmptyPalette() {
    MazeGameDebugSnapshot current = new MazeGameDebugHarness().snapshot();

    MazeGameDebugSnapshot snapshot =
        new MazeGameDebugSnapshot(
            current.gamePhase(),
            current.mazeState(),
            current.buildTimeRemainingSeconds(),
            current.rejectedPosition(),
            current.solverRunResult(),
            current.bestResult(),
            current.resultPassed(),
            current.hasNextLevel(),
            java.util.List.of());

    assertTrue(snapshot.paletteState().isEmpty());
  }

  @Test
  void canonicalSnapshotDefensivelyCopiesSolverDecisionMemory() {
    MazeGameDebugSnapshot current = new MazeGameDebugHarness().snapshot();
    java.util.List<SolverDecisionState> mutable =
        new java.util.ArrayList<>(
            java.util.List.of(new SolverDecisionState(Map.of(new GridPosition(4, 2), 1))));

    MazeGameDebugSnapshot snapshot =
        new MazeGameDebugSnapshot(
            current.gamePhase(),
            current.mazeState(),
            current.buildTimeRemainingSeconds(),
            current.rejectedPosition(),
            current.solverRunResult(),
            current.bestResult(),
            current.resultPassed(),
            current.hasNextLevel(),
            current.paletteState(),
            mutable);
    mutable.clear();

    assertEquals(1, snapshot.solverDecisionStates().size());
    assertThrows(
        UnsupportedOperationException.class,
        () -> snapshot.solverDecisionStates().add(SolverDecisionState.empty()));
  }

  @Test
  void canDriveStartupMenuIntoMilestoneOne() {
    MazeGameDebugHarness harness = MazeGameDebugHarness.forStartupMenu();

    assertEquals(GamePhase.MAIN_MENU, harness.snapshot().gamePhase());

    harness.clickMainMenuStart();
    assertEquals(GamePhase.LEVEL_SELECT, harness.snapshot().gamePhase());

    harness.clickLockedLevel(1);
    assertEquals(GamePhase.LEVEL_SELECT, harness.snapshot().gamePhase());

    harness.clickLevelOne();
    assertEquals(GamePhase.BUILDING, harness.snapshot().gamePhase());
  }

  @Test
  void canReturnFromResultToStartupMenu() {
    MazeGameDebugHarness harness = new MazeGameDebugHarness();

    harness.clickStartRun().advance(Duration.ofSeconds(10)).clickResultMainMenu();

    assertEquals(GamePhase.MAIN_MENU, harness.snapshot().gamePhase());
  }

  @Test
  void simulatesRejectedPlacementFeedback() {
    MazeGameDebugHarness harness = new MazeGameDebugHarness();

    harness.leftClickCell(Levels.levelOne().primarySolver().start());

    assertEquals(Levels.levelOne().primarySolver().start(), harness.snapshot().rejectedPosition());
    assertEquals(
        io.github.sportne.mazegame.model.maze.MazeState.initial(Levels.levelOne()).placedCells(),
        harness.snapshot().mazeState().placedCells());
  }

  @Test
  void simulatesStartRetryAndReplayButtons() {
    MazeGameDebugHarness harness = new MazeGameDebugHarness();

    harness.leftClickCell(LEVEL_ONE_WALL);
    harness.clickStartRun().advance(Duration.ofSeconds(10));
    assertEquals(GamePhase.RESULT, harness.snapshot().gamePhase());
    assertEquals(new BestResult(Duration.ofSeconds(9), 36), harness.snapshot().bestResult());

    harness.clickReplay();
    assertEquals(GamePhase.REPLAY, harness.snapshot().gamePhase());

    harness.advance(Duration.ofSeconds(10)).clickRetry();
    assertEquals(GamePhase.BUILDING, harness.snapshot().gamePhase());
  }

  @Test
  void completesAllAuthoredLevelsThroughTheDesktopInteractionPath() {
    MazeGameDebugHarness harness = new MazeGameDebugHarness();

    harness.leftClickCell(LEVEL_ONE_WALL);
    harness.clickStartRun().advance(Duration.ofSeconds(10)).clickNextLevel();
    assertEquals(Levels.levelTwo(), harness.snapshot().mazeState().levelDefinition());

    MILESTONE_TWO_WALLS.forEach(harness::leftClickCell);
    harness.clickStartRun().advance(Duration.ofSeconds(15));

    assertEquals(GamePhase.RESULT, harness.snapshot().gamePhase());
    assertTrue(harness.snapshot().resultPassed());
    harness.clickNextLevel();
    assertEquals(Levels.levelThree(), harness.snapshot().mazeState().levelDefinition());

    MILESTONE_THREE_WALLS.forEach(harness::leftClickCell);
    harness.clickStartRun().advance(Duration.ofSeconds(8));

    assertEquals(GamePhase.RESULT, harness.snapshot().gamePhase());
    assertTrue(harness.snapshot().resultPassed());
    assertTrue(harness.snapshot().hasNextLevel());
    harness.clickNextLevel();
    assertEquals(Levels.levelFour(), harness.snapshot().mazeState().levelDefinition());

    GridPosition draggedWall = new GridPosition(3, 1);
    harness.dragPaletteItemToCell(PlaceableCellType.WALL, draggedWall);
    MILESTONE_FOUR_WALLS.stream()
        .filter(position -> !position.equals(draggedWall))
        .forEach(harness::leftClickCell);
    harness.clickStartRun().advance(Duration.ofSeconds(7));

    assertEquals(GamePhase.RESULT, harness.snapshot().gamePhase());
    assertTrue(harness.snapshot().resultPassed());
    assertTrue(harness.snapshot().hasNextLevel());
    harness.clickNextLevel();
    assertEquals(Levels.levelFive(), harness.snapshot().mazeState().levelDefinition());

    MILESTONE_FIVE_WALLS.forEach(harness::leftClickCell);
    harness.clickPaletteItem(PlaceableCellType.SLOW_FLOOR);
    MILESTONE_FIVE_SLOW_FLOORS.forEach(harness::leftClickCell);
    harness.clickStartRun().advance(Duration.ofSeconds(10));

    assertEquals(GamePhase.RESULT, harness.snapshot().gamePhase());
    assertTrue(harness.snapshot().resultPassed());
    assertTrue(harness.snapshot().hasNextLevel());
    harness.clickNextLevel();
    assertEquals(Levels.levelSix(), harness.snapshot().mazeState().levelDefinition());

    LEVEL_SIX_WALLS.forEach(harness::leftClickCell);
    harness.clickPaletteItem(PlaceableCellType.SLOW_FLOOR);
    LEVEL_SIX_SLOW_FLOORS.forEach(harness::leftClickCell);
    harness.clickStartRun().advance(Duration.ofSeconds(8));

    assertEquals(GamePhase.RESULT, harness.snapshot().gamePhase());
    assertTrue(harness.snapshot().resultPassed());
    assertTrue(harness.snapshot().hasNextLevel());
    harness.clickNextLevel();
    assertEquals(Levels.levelSeven(), harness.snapshot().mazeState().levelDefinition());

    LEVEL_SEVEN_WALLS.forEach(harness::leftClickCell);
    harness.clickPaletteItem(PlaceableCellType.SLOW_FLOOR);
    LEVEL_SEVEN_SLOW_FLOORS.forEach(harness::leftClickCell);
    harness.clickStartRun().advance(Duration.ofSeconds(10));

    assertEquals(GamePhase.RESULT, harness.snapshot().gamePhase());
    assertTrue(harness.snapshot().resultPassed());
    assertTrue(harness.snapshot().hasNextLevel());
    harness.clickNextLevel();
    assertEquals(Levels.levelEight(), harness.snapshot().mazeState().levelDefinition());

    LEVEL_EIGHT_WALLS.forEach(harness::leftClickCell);
    harness.clickPaletteItem(PlaceableCellType.SLOW_FLOOR);
    LEVEL_EIGHT_SLOW_FLOORS.forEach(harness::leftClickCell);
    harness.clickStartRun().advance(Duration.ofSeconds(18));

    assertEquals(GamePhase.RESULT, harness.snapshot().gamePhase());
    assertTrue(harness.snapshot().resultPassed());
    assertTrue(harness.snapshot().hasNextLevel());
    harness.clickNextLevel();
    assertEquals(Levels.levelNine(), harness.snapshot().mazeState().levelDefinition());

    LEVEL_NINE_WALLS.forEach(harness::leftClickCell);
    harness.clickPaletteItem(PlaceableCellType.SLOW_FLOOR);
    LEVEL_NINE_SLOW_FLOORS.forEach(harness::leftClickCell);
    harness.clickPaletteItem(PlaceableCellType.ALTERNATING_GATE);
    LEVEL_NINE_GATES.forEach(harness::leftClickCell);
    harness.clickStartRun().advance(Duration.ofSeconds(19));

    assertEquals(GamePhase.RESULT, harness.snapshot().gamePhase());
    assertTrue(harness.snapshot().resultPassed());
    assertTrue(harness.snapshot().hasNextLevel());
    harness.clickNextLevel();
    assertEquals(Levels.levelTen(), harness.snapshot().mazeState().levelDefinition());

    GridPosition preset = new GridPosition(6, 0);
    GridPosition movedPreset = new GridPosition(6, 1);
    assertEquals(PlaceableCellType.WALL, harness.snapshot().mazeState().placedCellAt(preset));
    harness.dragPlacedCell(preset, movedPreset).dragPlacedCell(movedPreset, preset);
    assertEquals(PlaceableCellType.WALL, harness.snapshot().mazeState().placedCellAt(preset));
    LEVEL_TEN_WALLS.forEach(harness::leftClickCell);
    harness.clickPaletteItem(PlaceableCellType.SLOW_FLOOR);
    LEVEL_TEN_SLOW_FLOORS.forEach(harness::leftClickCell);
    harness.clickStartRun().advance(Duration.ofSeconds(14));

    assertEquals(GamePhase.RESULT, harness.snapshot().gamePhase());
    assertTrue(harness.snapshot().resultPassed());
    assertFalse(harness.snapshot().hasNextLevel());
    harness.clickReplay().advance(Duration.ofSeconds(14)).clickRetry();
    assertEquals(GamePhase.BUILDING, harness.snapshot().gamePhase());
    assertEquals(Levels.levelTen(), harness.snapshot().mazeState().levelDefinition());

    harness
        .clickStartRun()
        .advance(Duration.ofSeconds(14))
        .clickResultMainMenu()
        .clickMainMenuStart()
        .clickLevelTen();
    assertEquals(GamePhase.BUILDING, harness.snapshot().gamePhase());
    assertEquals(Levels.levelTen(), harness.snapshot().mazeState().levelDefinition());
  }
}
