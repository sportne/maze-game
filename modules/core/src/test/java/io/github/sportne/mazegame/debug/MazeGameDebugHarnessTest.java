package io.github.sportne.mazegame.debug;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.level.Levels;
import io.github.sportne.mazegame.model.result.BestResult;
import io.github.sportne.mazegame.state.GamePhase;
import java.time.Duration;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class MazeGameDebugHarnessTest {
  private static final Set<GridPosition> MILESTONE_TWO_WALLS =
      Set.of(
          new GridPosition(1, 1),
          new GridPosition(1, 4),
          new GridPosition(2, 0),
          new GridPosition(2, 6),
          new GridPosition(3, 3),
          new GridPosition(3, 6),
          new GridPosition(4, 0),
          new GridPosition(5, 0),
          new GridPosition(5, 2));

  private static final Set<GridPosition> MILESTONE_THREE_WALLS =
      Set.of(
          new GridPosition(2, 2),
          new GridPosition(3, 1),
          new GridPosition(4, 0),
          new GridPosition(5, 1));

  @Test
  void rejectsInvalidScreenDimensions() {
    assertThrows(IllegalArgumentException.class, () -> new MazeGameDebugHarness(0, 720));
    assertThrows(IllegalArgumentException.class, () -> new MazeGameDebugHarness(1280, 0));
  }

  @Test
  void simulatesWallPlacementAndClearingByGridCell() {
    MazeGameDebugHarness harness = new MazeGameDebugHarness();
    GridPosition wall = new GridPosition(2, 2);

    harness.leftClickCell(wall);
    assertTrue(harness.snapshot().mazeState().hasWallAt(wall));

    harness.rightClickCell(wall);
    assertTrue(harness.snapshot().mazeState().walls().isEmpty());
  }

  @Test
  void snapshotExposesPaletteSelectionAndExhaustedReleasedSupply() {
    MazeGameDebugHarness harness = new MazeGameDebugHarness();

    harness.clickPaletteItem(PlaceableCellType.SLOW_FLOOR);

    assertTrue(
        harness.snapshot().paletteState().stream()
            .anyMatch(
                state ->
                    state.type() == PlaceableCellType.SLOW_FLOOR
                        && state.selected()
                        && !state.available()));
  }

  @Test
  void compatibilitySnapshotConstructorDefaultsToEmptyPalette() {
    MazeGameDebugSnapshot current = new MazeGameDebugHarness().snapshot();

    MazeGameDebugSnapshot compatible =
        new MazeGameDebugSnapshot(
            current.gamePhase(),
            current.mazeState(),
            current.buildTimeRemainingSeconds(),
            current.rejectedPosition(),
            current.mouseRunResult(),
            current.bestResult(),
            current.resultPassed(),
            current.hasNextLevel());

    assertTrue(compatible.paletteState().isEmpty());
  }

  @Test
  void canDriveStartupMenuIntoMilestoneOne() {
    MazeGameDebugHarness harness = MazeGameDebugHarness.forStartupMenu();

    assertEquals(GamePhase.MAIN_MENU, harness.snapshot().gamePhase());

    harness.clickMainMenuStart();
    assertEquals(GamePhase.LEVEL_SELECT, harness.snapshot().gamePhase());

    harness.clickLockedLevel(1);
    assertEquals(GamePhase.LEVEL_SELECT, harness.snapshot().gamePhase());

    harness.clickMilestoneOneLevel();
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

    harness.leftClickCell(Levels.milestoneOne().mouseStart());

    assertEquals(Levels.milestoneOne().mouseStart(), harness.snapshot().rejectedPosition());
    assertTrue(harness.snapshot().mazeState().walls().isEmpty());
  }

  @Test
  void simulatesStartRetryAndReplayButtons() {
    MazeGameDebugHarness harness = new MazeGameDebugHarness();

    harness.clickStartRun().advance(Duration.ofSeconds(10));
    assertEquals(GamePhase.RESULT, harness.snapshot().gamePhase());
    assertEquals(new BestResult(Duration.ofSeconds(10), 40), harness.snapshot().bestResult());

    harness.clickReplay();
    assertEquals(GamePhase.REPLAY, harness.snapshot().gamePhase());

    harness.advance(Duration.ofSeconds(10)).clickRetry();
    assertEquals(GamePhase.BUILDING, harness.snapshot().gamePhase());
  }

  @Test
  void completesAllAuthoredLevelsThroughTheDesktopInteractionPath() {
    MazeGameDebugHarness harness = new MazeGameDebugHarness();

    harness.clickStartRun().advance(Duration.ofSeconds(10)).clickNextLevel();
    assertEquals(Levels.milestoneTwo(), harness.snapshot().mazeState().levelDefinition());

    MILESTONE_TWO_WALLS.forEach(harness::leftClickCell);
    harness.clickStartRun().advance(Duration.ofSeconds(15));

    assertEquals(GamePhase.RESULT, harness.snapshot().gamePhase());
    assertTrue(harness.snapshot().resultPassed());
    harness.clickNextLevel();
    assertEquals(Levels.milestoneThree(), harness.snapshot().mazeState().levelDefinition());

    MILESTONE_THREE_WALLS.forEach(harness::leftClickCell);
    harness.clickStartRun().advance(Duration.ofSeconds(8));

    assertEquals(GamePhase.RESULT, harness.snapshot().gamePhase());
    assertTrue(harness.snapshot().resultPassed());
    assertFalse(harness.snapshot().hasNextLevel());
    harness.clickReplay().advance(Duration.ofSeconds(8)).clickRetry();
    assertEquals(GamePhase.BUILDING, harness.snapshot().gamePhase());
    assertEquals(Levels.milestoneThree(), harness.snapshot().mazeState().levelDefinition());

    MILESTONE_THREE_WALLS.forEach(harness::leftClickCell);
    harness
        .clickStartRun()
        .advance(Duration.ofSeconds(8))
        .clickResultMainMenu()
        .clickMainMenuStart()
        .clickMilestoneThreeLevel();
    assertEquals(GamePhase.BUILDING, harness.snapshot().gamePhase());
    assertEquals(Levels.milestoneThree(), harness.snapshot().mazeState().levelDefinition());
  }
}
