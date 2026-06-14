package io.github.sportne.mazegame.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.level.Levels;
import io.github.sportne.mazegame.model.mouse.MouseRunResult;
import io.github.sportne.mazegame.model.mouse.MouseRunStatus;
import io.github.sportne.mazegame.model.result.BestResult;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.Test;

final class GameSessionTest {
  @Test
  void startsAtMainMenuWithMilestoneOneDefaultsReady() {
    GameSession session = new GameSession();

    assertEquals(GamePhase.MAIN_MENU, session.gamePhase());
    assertEquals(Levels.milestoneOne(), session.levelDefinition());
    assertTrue(session.mazeState().walls().isEmpty());
    assertEquals(30.0F, session.buildTimeRemainingSeconds());
    assertFalse(session.runRequested());
    assertNull(session.mouseRunResult());
    assertNull(session.bestResult());
  }

  @Test
  void loadsBestResultForCurrentLevel() {
    RecordingBestResultStore store = new RecordingBestResultStore();
    BestResult bestResult = new BestResult(Duration.ofSeconds(10), 40);
    store.savedBestResult = bestResult;

    GameSession session = new GameSession(store);

    assertEquals(bestResult, session.bestResult());
    assertEquals(Levels.milestoneOne().id(), store.loadedLevelId);
  }

  @Test
  void opensMenusOnlyFromMainMenu() {
    GameSession session = new GameSession();

    session.openLevelSelect();
    assertEquals(GamePhase.LEVEL_SELECT, session.gamePhase());

    session.openSettings();
    assertEquals(GamePhase.LEVEL_SELECT, session.gamePhase());

    session.returnToMainMenu();
    session.openSettings();
    assertEquals(GamePhase.SETTINGS, session.gamePhase());
  }

  @Test
  void buildTimerAutoStartsRunAtZero() {
    GameSession session = startedSession();

    session.updateBuildTimer(31.0F);

    assertEquals(GamePhase.MOUSE_RUNNING, session.gamePhase());
    assertEquals(0.0F, session.buildTimeRemainingSeconds());
    assertTrue(session.runRequested());
    assertEquals(Levels.milestoneOne().mouseStart(), session.mouseRunResult().position());
  }

  @Test
  void wallPlacementAndClearingMutateOnlyDuringBuildPhase() {
    GameSession session = startedSession();
    GridPosition wall = new GridPosition(2, 2);

    session.placeWall(wall);
    assertTrue(session.mazeState().hasWallAt(wall));

    session.clearWall(wall);
    assertFalse(session.mazeState().hasWallAt(wall));

    session.startRun();
    session.placeWall(wall);
    assertFalse(session.mazeState().hasWallAt(wall));
  }

  @Test
  void rejectedPlacementFlashesAndExpires() {
    GameSession session = startedSession();

    session.placeWall(Levels.milestoneOne().mouseStart());
    assertEquals(Levels.milestoneOne().mouseStart(), session.rejectedPosition());
    assertEquals(0.5F, session.rejectedFlashRemainingSeconds());

    session.updateBuildTimer(0.5F);
    assertNull(session.rejectedPosition());
    assertEquals(0.0F, session.rejectedFlashRemainingSeconds());
  }

  @Test
  void startRunIsIgnoredOutsideBuildPhase() {
    GameSession session = new GameSession();

    session.startRun();

    assertEquals(GamePhase.MAIN_MENU, session.gamePhase());
    assertNull(session.mouseRunResult());
  }

  @Test
  void updateMouseRunMovesToResultWhenTerminal() {
    GameSession session = startedSession();

    session.startRun();
    session.updateMouseRun(10.0F);

    assertEquals(GamePhase.RESULT, session.gamePhase());
    assertEquals(MouseRunStatus.TIMED_OUT, session.mouseRunResult().status());
    assertTrue(session.resultPassed());
  }

  @Test
  void passingNormalRunSavesBestResult() {
    RecordingBestResultStore store = new RecordingBestResultStore();
    GameSession session = new GameSession(store);
    session.startMilestoneOneLevel();

    session.startRun();
    session.updateMouseRun(10.0F);

    assertEquals(new BestResult(Duration.ofSeconds(10), 40), session.bestResult());
    assertEquals(new BestResult(Duration.ofSeconds(10), 40), store.savedBestResult);
    assertEquals(1, store.saveCount);
  }

  @Test
  void worsePassingRunDoesNotReplaceSavedBestResult() {
    RecordingBestResultStore store = new RecordingBestResultStore();
    store.savedBestResult = new BestResult(Duration.ofSeconds(11), 1);
    GameSession session = new GameSession(store);
    session.startMilestoneOneLevel();

    session.startRun();
    session.updateMouseRun(10.0F);

    assertEquals(new BestResult(Duration.ofSeconds(11), 1), session.bestResult());
    assertEquals(0, store.saveCount);
  }

  @Test
  void failedRunDoesNotSaveBestResult() {
    RecordingBestResultStore store = new RecordingBestResultStore();
    GameSession session = new GameSession(store);
    session.startMilestoneOneLevel();
    addVerticalCorridorWalls(session);

    session.startRun();
    session.updateMouseRun(1.0F);

    assertNull(session.bestResult());
    assertEquals(0, store.saveCount);
  }

  @Test
  void replayDoesNotSaveBestResultAgain() {
    RecordingBestResultStore store = new RecordingBestResultStore();
    GameSession session = new GameSession(store);
    session.startMilestoneOneLevel();
    session.startRun();
    session.updateMouseRun(10.0F);

    session.replayRun();
    session.updateMouseRun(10.0F);

    assertEquals(1, store.saveCount);
  }

  @Test
  void retryResetsTheLevel() {
    GameSession session = startedSession();
    GridPosition wall = new GridPosition(2, 2);
    session.placeWall(wall);
    session.startRun();
    session.updateMouseRun(10.0F);

    session.retryLevel();

    assertEquals(GamePhase.BUILDING, session.gamePhase());
    assertFalse(session.runRequested());
    assertTrue(session.mazeState().walls().isEmpty());
    assertNull(session.mouseRunResult());
  }

  @Test
  void replayUsesSameMazeAndSeed() {
    GameSession session = startedSession();
    session.startRun();
    session.updateMouseRun(10.0F);
    MouseRunResult firstResult = session.mouseRunResult();

    session.replayRun();
    session.updateMouseRun(10.0F);

    assertEquals(firstResult, session.mouseRunResult());
  }

  @Test
  void wallMutationsRequirePositions() {
    GameSession session = startedSession();

    assertThrows(NullPointerException.class, () -> session.placeWall(null));
    assertThrows(NullPointerException.class, () -> session.clearWall(null));
  }

  private static GameSession startedSession() {
    GameSession session = new GameSession();
    session.startMilestoneOneLevel();
    return session;
  }

  private static void addVerticalCorridorWalls(GameSession session) {
    session.placeWall(new GridPosition(4, 1));
    session.placeWall(new GridPosition(4, 3));
    session.placeWall(new GridPosition(3, 1));
    session.placeWall(new GridPosition(3, 3));
    session.placeWall(new GridPosition(2, 1));
    session.placeWall(new GridPosition(2, 3));
    session.placeWall(new GridPosition(1, 1));
    session.placeWall(new GridPosition(1, 3));
    session.placeWall(new GridPosition(0, 1));
    session.placeWall(new GridPosition(0, 3));
  }

  private static final class RecordingBestResultStore implements BestResultStore {
    private String loadedLevelId;
    private BestResult savedBestResult;
    private int saveCount;

    @Override
    public Optional<BestResult> load(String levelId) {
      loadedLevelId = levelId;
      return Optional.ofNullable(savedBestResult);
    }

    @Override
    public void save(String levelId, BestResult bestResult) {
      savedBestResult = bestResult;
      saveCount++;
    }
  }
}
