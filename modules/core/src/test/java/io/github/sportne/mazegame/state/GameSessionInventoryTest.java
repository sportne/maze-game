package io.github.sportne.mazegame.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.model.cell.CellSupply;
import io.github.sportne.mazegame.model.cell.PlaceableCellSupply;
import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import io.github.sportne.mazegame.model.level.LevelCatalog;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.level.MouseBehavior;
import io.github.sportne.mazegame.model.maze.MazeEditResult;
import io.github.sportne.mazegame.model.maze.MazeEditStatus;
import io.github.sportne.mazegame.model.maze.MazeState;
import io.github.sportne.mazegame.model.result.BestResult;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

final class GameSessionInventoryTest {
  private static final GridPosition FIRST = position(2, 0);
  private static final GridPosition SECOND = position(2, 1);
  private static final GridPosition THIRD = position(2, 3);

  @Test
  void initializesTheFirstAvailableAuthoredTypeAndExposesImmutablePaletteState() {
    LevelDefinition level =
        level(
            "first-available",
            List.of(
                PlaceableCellSupply.finite(PlaceableCellType.SLOW_FLOOR, 0),
                PlaceableCellSupply.finite(PlaceableCellType.WALL, 2)));
    GameSession session = session(level);

    assertTrue(session.startLevel(level.id()));

    assertEquals(PlaceableCellType.WALL, session.selectedCellType().orElseThrow());
    assertEquals(
        List.of(
            new CellPaletteState(
                PlaceableCellType.SLOW_FLOOR, CellSupply.finite(0), CellSupply.finite(0), false),
            new CellPaletteState(
                PlaceableCellType.WALL, CellSupply.finite(2), CellSupply.finite(2), true)),
        session.paletteState());
    assertFalse(session.paletteState().get(0).available());
    assertTrue(session.paletteState().get(1).available());
  }

  @Test
  void leavesSelectionEmptyWhenEveryAuthoredTypeStartsExhausted() {
    LevelDefinition level =
        level(
            "all-exhausted",
            List.of(
                PlaceableCellSupply.finite(PlaceableCellType.WALL, 0),
                PlaceableCellSupply.finite(PlaceableCellType.SLOW_FLOOR, 0)));
    GameSession session = startedSession(level);
    MazeState initial = session.mazeState();

    assertTrue(session.selectedCellType().isEmpty());
    assertTrue(session.paletteState().stream().noneMatch(CellPaletteState::selected));
    assertTrue(session.placeOrReplaceCell(FIRST).isEmpty());
    assertSame(initial, session.mazeState());

    session.selectCellType(PlaceableCellType.SLOW_FLOOR);
    assertEquals(PlaceableCellType.SLOW_FLOOR, session.selectedCellType().orElseThrow());
    assertEdit(
        session.placeOrReplaceCell(FIRST).orElseThrow(), MazeEditStatus.REJECTED_EXHAUSTED_SUPPLY);
    assertSame(initial, session.mazeState());
  }

  @Test
  void finiteSessionKeepsInventoryAndSelectionAtomicAcrossEveryEdit() {
    GameSession session = startedSession(finiteLevel("finite-edits"));

    assertEdit(session.placeOrReplaceCell(FIRST).orElseThrow(), MazeEditStatus.PLACED);
    assertEquals(CellSupply.finite(0), remaining(session, PlaceableCellType.WALL));
    assertEquals(PlaceableCellType.WALL, session.selectedCellType().orElseThrow());

    session.selectCellType(PlaceableCellType.SLOW_FLOOR);
    assertEdit(session.placeOrReplaceCell(SECOND).orElseThrow(), MazeEditStatus.PLACED);
    assertEquals(CellSupply.finite(0), remaining(session, PlaceableCellType.SLOW_FLOOR));

    session.selectCellType(PlaceableCellType.WALL);
    MazeState exhausted = session.mazeState();
    assertEdit(
        session.placeOrReplaceCell(THIRD).orElseThrow(), MazeEditStatus.REJECTED_EXHAUSTED_SUPPLY);
    assertSame(exhausted, session.mazeState());
    assertEquals(THIRD, session.rejectedPosition());

    assertEdit(session.placeOrReplaceCell(FIRST).orElseThrow(), MazeEditStatus.REMOVED);
    assertEquals(CellSupply.finite(1), remaining(session, PlaceableCellType.WALL));
    assertEdit(session.placeOrReplaceCell(THIRD).orElseThrow(), MazeEditStatus.PLACED);

    session.selectCellType(PlaceableCellType.SLOW_FLOOR);
    MazeState rejectedReplacement = session.mazeState();
    assertEdit(
        session.placeOrReplaceCell(THIRD).orElseThrow(), MazeEditStatus.REJECTED_EXHAUSTED_SUPPLY);
    assertSame(rejectedReplacement, session.mazeState());

    assertEdit(session.placeOrReplaceCell(SECOND).orElseThrow(), MazeEditStatus.REMOVED);
    assertEdit(session.placeOrReplaceCell(THIRD).orElseThrow(), MazeEditStatus.REPLACED);
    assertEquals(CellSupply.finite(1), remaining(session, PlaceableCellType.WALL));
    assertEquals(CellSupply.finite(0), remaining(session, PlaceableCellType.SLOW_FLOOR));

    List<CellPaletteState> beforeMove = session.paletteState();
    assertEdit(session.moveCell(THIRD, FIRST).orElseThrow(), MazeEditStatus.MOVED);
    assertEquals(beforeMove, session.paletteState());
    assertEdit(session.removeCell(FIRST).orElseThrow(), MazeEditStatus.REMOVED);
    assertEquals(CellSupply.finite(1), remaining(session, PlaceableCellType.SLOW_FLOOR));
  }

  @Test
  void releasedInfiniteWallSessionRetainsItsExistingToggleBehavior() {
    LevelDefinition level = level("released", PlaceableCellSupply.releasedDefaults());
    GameSession session = startedSession(level);

    assertEquals(PlaceableCellType.WALL, session.selectedCellType().orElseThrow());
    assertEdit(session.placeOrReplaceCell(FIRST).orElseThrow(), MazeEditStatus.PLACED);
    assertEquals(CellSupply.infinite(), remaining(session, PlaceableCellType.WALL));
    assertEdit(session.placeOrReplaceCell(FIRST).orElseThrow(), MazeEditStatus.REMOVED);
    assertEquals(CellSupply.infinite(), remaining(session, PlaceableCellType.WALL));

    session.selectCellType(PlaceableCellType.SLOW_FLOOR);
    assertEdit(
        session.placeOrReplaceCell(FIRST).orElseThrow(), MazeEditStatus.REJECTED_EXHAUSTED_SUPPLY);
    assertTrue(session.mazeState().placedCells().isEmpty());
  }

  @Test
  void manualStartAndTimerExpiryFreezeTheCompleteImmutableMaze() {
    assertFrozenAfterStart("freeze-manual", false);
    assertFrozenAfterStart("freeze-timeout", true);
  }

  @Test
  void retryBackAndFreshLevelSelectionRestoreAuthoredInventoryAndSelection() {
    LevelDefinition first = finiteLevel("reset-first");
    LevelDefinition second =
        level(
            "reset-second",
            List.of(
                PlaceableCellSupply.finite(PlaceableCellType.SLOW_FLOOR, 2),
                PlaceableCellSupply.finite(PlaceableCellType.WALL, 1)));
    BestResultStore unlockedStore =
        new BestResultStore() {
          @Override
          public Optional<BestResult> load(String levelId) {
            return levelId.equals(first.id())
                ? Optional.of(new BestResult(Duration.ofSeconds(6), 24))
                : Optional.empty();
          }

          @Override
          public void save(String levelId, BestResult bestResult) {
            // This reset-focused fixture never completes a run.
          }
        };
    GameSession session =
        new GameSession(new LevelCatalog(List.of(first, second)), first.id(), unlockedStore);
    assertTrue(session.startLevel(first.id()));

    session.placeOrReplaceCell(FIRST);
    session.selectCellType(PlaceableCellType.SLOW_FLOOR);
    session.retryLevel();
    assertFresh(session, first, PlaceableCellType.WALL);

    session.placeOrReplaceCell(FIRST);
    session.selectCellType(PlaceableCellType.SLOW_FLOOR);
    session.returnToLevelSelect();
    assertTrue(session.mazeState().placedCells().isEmpty());
    assertEquals(PlaceableCellType.WALL, session.selectedCellType().orElseThrow());
    assertTrue(session.startLevel(first.id()));
    assertFresh(session, first, PlaceableCellType.WALL);

    session.returnToMainMenu();
    assertTrue(session.startLevel(second.id()));
    assertFresh(session, second, PlaceableCellType.SLOW_FLOOR);
  }

  @Test
  void selectionAndAllEditIntentsAreIgnoredOutsideBuilding() {
    GameSession session = session(finiteLevel("phase-lock"));
    MazeState menuMaze = session.mazeState();

    session.selectCellType(PlaceableCellType.SLOW_FLOOR);
    assertTrue(session.placeOrReplaceCell(FIRST).isEmpty());
    assertTrue(session.removeCell(FIRST).isEmpty());
    assertTrue(session.moveCell(FIRST, SECOND).isEmpty());
    assertSame(menuMaze, session.mazeState());
    assertEquals(PlaceableCellType.WALL, session.selectedCellType().orElseThrow());
  }

  @Test
  void sessionEditsAndPaletteStateRequireTheirInputs() {
    GameSession session = startedSession(finiteLevel("required-inputs"));

    assertThrows(NullPointerException.class, () -> session.selectCellType(null));
    assertThrows(NullPointerException.class, () -> session.placeOrReplaceCell(null));
    assertThrows(NullPointerException.class, () -> session.removeCell(null));
    assertThrows(NullPointerException.class, () -> session.moveCell(null, FIRST));
    assertThrows(NullPointerException.class, () -> session.moveCell(FIRST, null));
    assertThrows(
        NullPointerException.class,
        () -> new CellPaletteState(null, CellSupply.finite(1), CellSupply.finite(1), false));
    assertThrows(
        NullPointerException.class,
        () -> new CellPaletteState(PlaceableCellType.WALL, null, CellSupply.finite(1), false));
    assertThrows(
        NullPointerException.class,
        () -> new CellPaletteState(PlaceableCellType.WALL, CellSupply.finite(1), null, false));
  }

  private static void assertFrozenAfterStart(String levelId, boolean expireTimer) {
    GameSession session = startedSession(finiteLevel(levelId));
    session.placeOrReplaceCell(FIRST);
    session.selectCellType(PlaceableCellType.SLOW_FLOOR);
    session.placeOrReplaceCell(SECOND);
    MazeState frozen = session.mazeState();
    List<CellPaletteState> frozenPalette = session.paletteState();

    if (expireTimer) {
      session.updateBuildTimer(60.0F);
    } else {
      session.startRun();
    }

    assertEquals(GamePhase.MOUSE_RUNNING, session.gamePhase());
    assertTrue(session.placeOrReplaceCell(THIRD).isEmpty());
    assertTrue(session.removeCell(FIRST).isEmpty());
    assertTrue(session.moveCell(SECOND, THIRD).isEmpty());
    session.selectCellType(PlaceableCellType.WALL);
    assertSame(frozen, session.mazeState());
    assertEquals(frozenPalette, session.paletteState());

    session.updateMouseRun(10.0F);
    MazeState completed = session.mazeState();
    session.replayRun();
    assertSame(completed, session.mazeState());
    assertTrue(session.removeCell(FIRST).isEmpty());
    assertSame(completed, session.mazeState());
  }

  private static void assertFresh(
      GameSession session, LevelDefinition level, PlaceableCellType selectedType) {
    assertEquals(GamePhase.BUILDING, session.gamePhase());
    assertEquals(level, session.levelDefinition());
    assertTrue(session.mazeState().placedCells().isEmpty());
    assertEquals(selectedType, session.selectedCellType().orElseThrow());
    for (CellPaletteState paletteEntry : session.paletteState()) {
      assertEquals(paletteEntry.authoredSupply(), paletteEntry.remainingSupply());
    }
  }

  private static CellSupply remaining(GameSession session, PlaceableCellType type) {
    return session.paletteState().stream()
        .filter(entry -> entry.type() == type)
        .findFirst()
        .orElseThrow()
        .remainingSupply();
  }

  private static void assertEdit(MazeEditResult result, MazeEditStatus expectedStatus) {
    assertEquals(expectedStatus, result.status());
  }

  private static GameSession startedSession(LevelDefinition level) {
    GameSession session = session(level);
    assertTrue(session.startLevel(level.id()));
    return session;
  }

  private static GameSession session(LevelDefinition level) {
    return new GameSession(new LevelCatalog(List.of(level)), level.id(), BestResultStore.none());
  }

  private static LevelDefinition finiteLevel(String id) {
    return level(
        id,
        List.of(
            PlaceableCellSupply.finite(PlaceableCellType.WALL, 1),
            PlaceableCellSupply.finite(PlaceableCellType.SLOW_FLOOR, 1)));
  }

  private static LevelDefinition level(String id, List<PlaceableCellSupply> supplies) {
    return new LevelDefinition(
        id,
        id,
        GridSize.square(5),
        position(4, 2),
        position(0, 2),
        Duration.ofSeconds(30),
        Duration.ofSeconds(5),
        Duration.ofSeconds(10),
        Duration.ofMillis(250),
        supplies,
        MouseBehavior.RANDOM,
        1L);
  }

  private static GridPosition position(int row, int column) {
    return new GridPosition(row, column);
  }
}
