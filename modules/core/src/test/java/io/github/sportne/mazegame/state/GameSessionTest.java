package io.github.sportne.mazegame.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.model.cell.CellSupply;
import io.github.sportne.mazegame.model.cell.PlaceableCellSupply;
import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import io.github.sportne.mazegame.model.level.LevelCatalog;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.level.Levels;
import io.github.sportne.mazegame.model.level.MouseBehavior;
import io.github.sportne.mazegame.model.mouse.MouseRunResult;
import io.github.sportne.mazegame.model.mouse.MouseRunStatus;
import io.github.sportne.mazegame.model.result.BestResult;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

final class GameSessionTest {
  private static final String MISSING_LEVEL_ID = "missing-level";

  private static final LevelDefinition SECOND_LEVEL =
      new LevelDefinition(
          "test-level-2",
          "Test Level 2",
          GridSize.square(5),
          new GridPosition(4, 2),
          new GridPosition(0, 2),
          Duration.ofSeconds(20),
          Duration.ofSeconds(5),
          Duration.ofSeconds(10),
          Duration.ofMillis(250),
          PlaceableCellSupply.releasedDefaults(),
          MouseBehavior.RANDOM,
          1L);

  private static final LevelCatalog TEST_CATALOG =
      new LevelCatalog(List.of(Levels.milestoneOne(), SECOND_LEVEL));

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
    store.results.put(Levels.milestoneOne().id(), bestResult);

    GameSession session = new GameSession(store);

    assertEquals(bestResult, session.bestResult());
    assertEquals(
        List.of(
            Levels.milestoneOne().id(),
            Levels.milestoneTwo().id(),
            Levels.milestoneThree().id(),
            Levels.milestoneFour().id(),
            Levels.milestoneFive().id()),
        store.loadedLevelIds);
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
  void returnsFromAnUnstartedLevelToSelectionAndClearsTheAttempt() {
    GameSession session = startedSession();
    session.placeWall(new GridPosition(2, 2));

    session.returnToLevelSelect();

    assertEquals(GamePhase.LEVEL_SELECT, session.gamePhase());
    assertTrue(session.mazeState().walls().isEmpty());
    assertFalse(session.runRequested());
    assertNull(session.mouseRunResult());
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
    session.startLevel(Levels.milestoneOne().id());

    session.startRun();
    session.updateMouseRun(10.0F);

    assertEquals(new BestResult(Duration.ofSeconds(10), 40), session.bestResult());
    assertEquals(new BestResult(Duration.ofSeconds(10), 40), store.savedBestResult);
    assertEquals(1, store.saveCount);
  }

  @Test
  void worsePassingRunDoesNotReplaceSavedBestResult() {
    RecordingBestResultStore store = new RecordingBestResultStore();
    store.results.put(Levels.milestoneOne().id(), new BestResult(Duration.ofSeconds(11), 1));
    GameSession session = new GameSession(store);
    session.startLevel(Levels.milestoneOne().id());

    session.startRun();
    session.updateMouseRun(10.0F);

    assertEquals(new BestResult(Duration.ofSeconds(11), 1), session.bestResult());
    assertEquals(0, store.saveCount);
  }

  @Test
  void failedRunDoesNotSaveBestResult() {
    RecordingBestResultStore store = new RecordingBestResultStore();
    GameSession session = new GameSession(store);
    session.startLevel(Levels.milestoneOne().id());
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
    session.startLevel(Levels.milestoneOne().id());
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
  void sessionUsesTheAuthoredMouseBehavior() {
    LevelDefinition source = Levels.milestoneOne();
    LevelDefinition scoutLevel =
        new LevelDefinition(
            "scout-session",
            "Scout Session",
            source.gridSize(),
            source.mouseStart(),
            source.cheese(),
            source.buildTime(),
            source.targetSolveTime(),
            source.maximumSolveTime(),
            source.mouseMoveInterval(),
            source.placeableCellSupplies(),
            MouseBehavior.LEFT_PRIORITY,
            256L);
    GameSession session =
        new GameSession(
            new LevelCatalog(List.of(scoutLevel)), scoutLevel.id(), BestResultStore.none());

    session.startLevel(scoutLevel.id());
    session.startRun();
    List<GridPosition> firstTrace = nextFourPositions(session);
    session.updateMouseRun(10.0F);
    MouseRunResult firstResult = session.mouseRunResult();

    session.replayRun();
    List<GridPosition> replayTrace = nextFourPositions(session);
    session.updateMouseRun(10.0F);

    assertEquals(
        List.of(
            new GridPosition(4, 1),
            new GridPosition(4, 0),
            new GridPosition(3, 0),
            new GridPosition(2, 0)),
        firstTrace);
    assertEquals(firstTrace, replayTrace);
    assertEquals(firstResult, session.mouseRunResult());
  }

  @Test
  void startsAnyCatalogLevelByStableId() {
    RecordingBestResultStore store = new RecordingBestResultStore();
    store.results.put(Levels.milestoneOne().id(), new BestResult(Duration.ofSeconds(10), 40));
    GameSession session = new GameSession(TEST_CATALOG, Levels.milestoneOne().id(), store);

    assertTrue(session.startLevel(SECOND_LEVEL.id()));

    assertEquals(SECOND_LEVEL, session.levelDefinition());
    assertEquals(SECOND_LEVEL, session.mazeState().levelDefinition());
    assertEquals(GamePhase.BUILDING, session.gamePhase());
    assertEquals(List.of(Levels.milestoneOne().id(), SECOND_LEVEL.id()), store.loadedLevelIds);
  }

  @Test
  void unknownLevelCannotStartOrChangeTheSession() {
    RecordingBestResultStore store = new RecordingBestResultStore();
    GameSession session = new GameSession(TEST_CATALOG, Levels.milestoneOne().id(), store);
    LevelDefinition originalLevel = session.levelDefinition();
    GamePhase originalPhase = session.gamePhase();

    assertFalse(session.startLevel(MISSING_LEVEL_ID));

    assertEquals(originalLevel, session.levelDefinition());
    assertEquals(originalPhase, session.gamePhase());
    assertEquals(List.of(Levels.milestoneOne().id(), SECOND_LEVEL.id()), store.loadedLevelIds);
  }

  @Test
  void retryAndReplayRetainTheSelectedLevel() {
    RecordingBestResultStore store = new RecordingBestResultStore();
    store.results.put(Levels.milestoneOne().id(), new BestResult(Duration.ofSeconds(10), 40));
    GameSession session = new GameSession(TEST_CATALOG, Levels.milestoneOne().id(), store);
    session.startLevel(SECOND_LEVEL.id());
    session.startRun();
    session.updateMouseRun(10.0F);
    MouseRunResult firstResult = session.mouseRunResult();

    session.retryLevel();
    assertEquals(SECOND_LEVEL, session.levelDefinition());
    assertEquals(SECOND_LEVEL, session.mazeState().levelDefinition());

    session.startRun();
    session.updateMouseRun(10.0F);
    session.replayRun();
    session.updateMouseRun(10.0F);

    assertEquals(SECOND_LEVEL, session.levelDefinition());
    assertEquals(firstResult, session.mouseRunResult());
  }

  @Test
  void savesBestResultUnderTheSelectedLevelId() {
    RecordingBestResultStore store = new RecordingBestResultStore();
    store.results.put(Levels.milestoneOne().id(), new BestResult(Duration.ofSeconds(10), 40));
    GameSession session = new GameSession(TEST_CATALOG, Levels.milestoneOne().id(), store);
    session.startLevel(SECOND_LEVEL.id());

    session.startRun();
    session.updateMouseRun(10.0F);

    assertEquals(SECOND_LEVEL.id(), store.savedLevelId);
  }

  @Test
  void unlocksLevelsInCatalogOrderFromPassingResults() {
    RecordingBestResultStore store = new RecordingBestResultStore();
    GameSession session = new GameSession(TEST_CATALOG, Levels.milestoneOne().id(), store);

    assertTrue(session.levelProgress().get(0).unlocked());
    assertFalse(session.levelProgress().get(1).unlocked());
    assertFalse(session.startLevel(SECOND_LEVEL.id()));

    session.startLevel(Levels.milestoneOne().id());
    session.startRun();
    session.updateMouseRun(10.0F);

    assertTrue(session.levelProgress().get(1).unlocked());
    assertEquals(Optional.of(SECOND_LEVEL.id()), session.nextLevelId());
    assertTrue(session.hasNextLevel());
    assertTrue(session.startLevel(SECOND_LEVEL.id()));
  }

  @Test
  void failedResultDoesNotUnlockTheNextLevel() {
    GameSession session =
        new GameSession(TEST_CATALOG, Levels.milestoneOne().id(), BestResultStore.none());
    session.startLevel(Levels.milestoneOne().id());
    addVerticalCorridorWalls(session);

    session.startRun();
    session.updateMouseRun(1.0F);

    assertFalse(session.resultPassed());
    assertFalse(session.levelProgress().get(1).unlocked());
    assertFalse(session.hasNextLevel());
  }

  @Test
  void restoredFirstLevelResultUnlocksTheNextLevelWithoutSecondaryState() {
    RecordingBestResultStore store = new RecordingBestResultStore();
    store.results.put(Levels.milestoneOne().id(), new BestResult(Duration.ofSeconds(10), 40));

    GameSession restored = new GameSession(TEST_CATALOG, Levels.milestoneOne().id(), store);
    assertTrue(restored.levelProgress().get(1).unlocked());

    store.results.remove(Levels.milestoneOne().id());
    GameSession cleared = new GameSession(TEST_CATALOG, Levels.milestoneOne().id(), store);
    assertFalse(cleared.levelProgress().get(1).unlocked());
  }

  @Test
  void exposesIndependentBestResultsForEachLevel() {
    RecordingBestResultStore store = new RecordingBestResultStore();
    BestResult firstBest = new BestResult(Duration.ofSeconds(11), 20);
    BestResult secondBest = new BestResult(Duration.ofSeconds(12), 30);
    store.results.put(Levels.milestoneOne().id(), firstBest);
    store.results.put(SECOND_LEVEL.id(), secondBest);

    GameSession session = new GameSession(TEST_CATALOG, Levels.milestoneOne().id(), store);

    assertEquals(firstBest, session.levelProgress().get(0).bestResult());
    assertEquals(secondBest, session.levelProgress().get(1).bestResult());
    session.startLevel(SECOND_LEVEL.id());
    assertEquals(secondBest, session.bestResult());
  }

  @Test
  void finalLevelNeverOffersAnotherLevel() {
    RecordingBestResultStore store = new RecordingBestResultStore();
    store.results.put(Levels.milestoneOne().id(), new BestResult(Duration.ofSeconds(10), 40));
    GameSession session = new GameSession(TEST_CATALOG, Levels.milestoneOne().id(), store);
    session.startLevel(SECOND_LEVEL.id());

    session.startRun();
    session.updateMouseRun(10.0F);

    assertTrue(session.resultPassed());
    assertFalse(session.hasNextLevel());
    assertTrue(session.nextLevelId().isEmpty());
  }

  @Test
  void unavailableStorageDoesNotInterruptProgressionInTheCurrentSession() {
    BestResultStore unavailableStore =
        new BestResultStore() {
          @Override
          public Optional<BestResult> load(String levelId) {
            throw new IllegalStateException("storage unavailable");
          }

          @Override
          public void save(String levelId, BestResult bestResult) {
            throw new IllegalStateException("storage unavailable");
          }
        };
    GameSession session =
        new GameSession(TEST_CATALOG, Levels.milestoneOne().id(), unavailableStore);

    session.startLevel(Levels.milestoneOne().id());
    session.startRun();
    session.updateMouseRun(10.0F);

    assertTrue(session.levelProgress().get(1).unlocked());
    assertTrue(session.startLevel(SECOND_LEVEL.id()));
  }

  @Test
  void playsTheAuthoredSecondLevelWithoutLeakingStateBetweenLevels() {
    RecordingBestResultStore store = new RecordingBestResultStore();
    BestResult firstBest = new BestResult(Duration.ofSeconds(10), 40);
    store.results.put(Levels.milestoneOne().id(), firstBest);
    GameSession session = new GameSession(store);

    assertTrue(session.startLevel(Levels.milestoneTwo().id()));
    assertEquals(Levels.milestoneTwo(), session.levelDefinition());
    assertEquals(25.0F, session.buildTimeRemainingSeconds());
    session.placeWall(new GridPosition(7, 0));
    assertEquals(new GridPosition(7, 0), session.rejectedPosition());
    for (int column = 0; column < 7; column++) {
      session.placeWall(new GridPosition(1, column));
    }
    assertEquals(6, session.mazeState().walls().size());
    assertEquals(new GridPosition(1, 6), session.rejectedPosition());

    session.retryLevel();
    addMilestoneTwoTimeoutWalls(session);

    session.updateBuildTimer(25.0F);
    session.updateMouseRun(15.0F);

    assertTrue(session.resultPassed());
    assertEquals(MouseRunStatus.TIMED_OUT, session.mouseRunResult().status());
    assertEquals(Levels.milestoneTwo().id(), store.savedLevelId);
    assertEquals(9, session.mazeState().walls().size());

    session.retryLevel();
    assertEquals(Levels.milestoneTwo(), session.levelDefinition());
    assertTrue(session.mazeState().walls().isEmpty());
    assertEquals(25.0F, session.buildTimeRemainingSeconds());

    session.startRun();
    session.updateMouseRun(3.0F);
    MouseRunResult failedResult = session.mouseRunResult();
    assertFalse(session.resultPassed());
    assertEquals(MouseRunStatus.REACHED_CHEESE, failedResult.status());

    session.replayRun();
    session.updateMouseRun(15.0F);
    assertEquals(failedResult, session.mouseRunResult());
    assertEquals(1, store.saveCount);

    session.returnToMainMenu();
    session.openLevelSelect();
    assertEquals(GamePhase.LEVEL_SELECT, session.gamePhase());
    assertEquals(Levels.milestoneTwo(), session.levelDefinition());

    assertTrue(session.startLevel(Levels.milestoneOne().id()));
    assertEquals(Levels.milestoneOne(), session.levelDefinition());
    assertEquals(firstBest, session.bestResult());
    assertTrue(session.mazeState().walls().isEmpty());
    assertEquals(30.0F, session.buildTimeRemainingSeconds());
  }

  @Test
  void existingProfilesDeriveThirdAndFourthLevelUnlocksWithoutMigrationState() {
    RecordingBestResultStore cleanStore = new RecordingBestResultStore();
    GameSession clean = new GameSession(cleanStore);
    assertFalse(clean.levelProgress().get(2).unlocked());
    assertFalse(clean.levelProgress().get(3).unlocked());
    assertFalse(clean.startLevel(Levels.milestoneThree().id()));
    assertFalse(clean.startLevel(Levels.milestoneFour().id()));

    RecordingBestResultStore firstLevelOnlyStore = new RecordingBestResultStore();
    firstLevelOnlyStore.results.put(
        Levels.milestoneOne().id(), new BestResult(Duration.ofSeconds(10), 40));
    GameSession firstLevelOnly = new GameSession(firstLevelOnlyStore);
    assertTrue(firstLevelOnly.levelProgress().get(1).unlocked());
    assertFalse(firstLevelOnly.levelProgress().get(2).unlocked());
    assertFalse(firstLevelOnly.levelProgress().get(3).unlocked());

    RecordingBestResultStore existingTwoLevelStore = new RecordingBestResultStore();
    existingTwoLevelStore.results.put(
        Levels.milestoneOne().id(), new BestResult(Duration.ofSeconds(10), 40));
    existingTwoLevelStore.results.put(
        Levels.milestoneTwo().id(), new BestResult(Duration.ofSeconds(15), 60));
    GameSession existingTwoLevel = new GameSession(existingTwoLevelStore);
    assertTrue(existingTwoLevel.levelProgress().get(2).unlocked());
    assertFalse(existingTwoLevel.levelProgress().get(3).unlocked());
    assertTrue(existingTwoLevel.startLevel(Levels.milestoneThree().id()));

    RecordingBestResultStore existingThreeLevelStore = new RecordingBestResultStore();
    existingThreeLevelStore.results.putAll(existingTwoLevelStore.results);
    existingThreeLevelStore.results.put(
        Levels.milestoneThree().id(), new BestResult(Duration.ofMillis(6500), 26));
    GameSession existingThreeLevel = new GameSession(existingThreeLevelStore);
    assertTrue(existingThreeLevel.levelProgress().get(3).unlocked());
    assertTrue(existingThreeLevel.startLevel(Levels.milestoneFour().id()));

    RecordingBestResultStore outOfOrderStore = new RecordingBestResultStore();
    outOfOrderStore.results.put(
        Levels.milestoneTwo().id(), new BestResult(Duration.ofSeconds(15), 60));
    GameSession outOfOrder = new GameSession(outOfOrderStore);
    assertFalse(outOfOrder.levelProgress().get(1).unlocked());
    assertFalse(outOfOrder.levelProgress().get(2).unlocked());
    assertFalse(outOfOrder.levelProgress().get(3).unlocked());

    RecordingBestResultStore fourthOnlyStore = new RecordingBestResultStore();
    fourthOnlyStore.results.put(
        Levels.milestoneFour().id(), new BestResult(Duration.ofMillis(5750), 20));
    GameSession fourthOnly = new GameSession(fourthOnlyStore);
    assertFalse(fourthOnly.levelProgress().get(3).unlocked());
    assertFalse(fourthOnly.startLevel(Levels.milestoneFour().id()));
  }

  @Test
  void thirdLevelSupportsUnlockRunRetryReplayPersistenceAndReload() {
    RecordingBestResultStore store = new RecordingBestResultStore();
    BestResult firstBest = new BestResult(Duration.ofSeconds(10), 40);
    store.results.put(Levels.milestoneOne().id(), firstBest);
    GameSession session = new GameSession(store);

    session.startLevel(Levels.milestoneTwo().id());
    addMilestoneTwoTimeoutWalls(session);
    session.startRun();
    session.updateMouseRun(15.0F);
    BestResult secondBest = session.bestResult();
    assertEquals(Optional.of(Levels.milestoneThree().id()), session.nextLevelId());
    assertTrue(session.startLevel(Levels.milestoneThree().id()));

    addMilestoneThreePassingWalls(session);
    Set<GridPosition> acceptedMaze = session.mazeState().walls();
    session.startRun();
    session.updateMouseRun(8.0F);
    MouseRunResult firstRun = session.mouseRunResult();
    BestResult thirdBest = session.bestResult();

    assertTrue(session.resultPassed());
    assertEquals(
        new MouseRunResult(
            Levels.milestoneThree().cheese(),
            Duration.ofMillis(6500),
            26,
            MouseRunStatus.REACHED_CHEESE),
        firstRun);
    assertEquals(Levels.milestoneThree().id(), store.savedLevelId);
    assertEquals(firstBest, store.results.get(Levels.milestoneOne().id()));
    assertEquals(secondBest, store.results.get(Levels.milestoneTwo().id()));
    assertEquals(thirdBest, store.results.get(Levels.milestoneThree().id()));
    assertTrue(session.hasNextLevel());
    assertEquals(Optional.of(Levels.milestoneFour().id()), session.nextLevelId());

    session.replayRun();
    session.updateMouseRun(8.0F);
    assertEquals(firstRun, session.mouseRunResult());
    assertEquals(acceptedMaze, session.mazeState().walls());

    session.retryLevel();
    assertEquals(Levels.milestoneThree(), session.levelDefinition());
    assertTrue(session.mazeState().walls().isEmpty());
    assertEquals(25.0F, session.buildTimeRemainingSeconds());

    session.returnToMainMenu();
    assertTrue(session.startLevel(Levels.milestoneThree().id()));
    assertEquals(thirdBest, session.bestResult());

    GameSession reloaded = new GameSession(store);
    assertTrue(reloaded.levelProgress().get(2).unlocked());
    assertTrue(reloaded.levelProgress().get(3).unlocked());
    assertTrue(reloaded.startLevel(Levels.milestoneThree().id()));
    assertEquals(thirdBest, reloaded.bestResult());
  }

  @Test
  void fourthLevelPreservesResultsAndSupportsBackRetryReplayAndFinalNavigation() {
    RecordingBestResultStore store = new RecordingBestResultStore();
    BestResult firstBest = new BestResult(Duration.ofSeconds(10), 40);
    BestResult secondBest = new BestResult(Duration.ofSeconds(15), 60);
    BestResult thirdBest = new BestResult(Duration.ofMillis(6500), 26);
    store.results.put(Levels.milestoneOne().id(), firstBest);
    store.results.put(Levels.milestoneTwo().id(), secondBest);
    store.results.put(Levels.milestoneThree().id(), thirdBest);
    GameSession session = new GameSession(store);

    assertTrue(session.startLevel(Levels.milestoneFour().id()));
    session.placeWall(new GridPosition(4, 4));
    session.returnToLevelSelect();
    assertEquals(GamePhase.LEVEL_SELECT, session.gamePhase());
    assertTrue(session.mazeState().placedCells().isEmpty());
    assertTrue(session.startLevel(Levels.milestoneFour().id()));

    addMilestoneFourPassingCells(session);
    Map<GridPosition, PlaceableCellType> acceptedMaze = session.mazeState().placedCells();
    session.startRun();
    session.updateMouseRun(6.5F);
    MouseRunResult firstRun = session.mouseRunResult();
    BestResult fourthBest = session.bestResult();

    assertTrue(session.resultPassed());
    assertEquals(
        new MouseRunResult(
            Levels.milestoneFour().cheese(),
            Duration.ofMillis(5750),
            20,
            MouseRunStatus.REACHED_CHEESE),
        firstRun);
    assertEquals(Levels.milestoneFour().id(), store.savedLevelId);
    assertEquals(1, store.saveCount);
    assertEquals(firstBest, store.results.get(Levels.milestoneOne().id()));
    assertEquals(secondBest, store.results.get(Levels.milestoneTwo().id()));
    assertEquals(thirdBest, store.results.get(Levels.milestoneThree().id()));
    assertEquals(fourthBest, store.results.get(Levels.milestoneFour().id()));
    assertTrue(session.hasNextLevel());
    assertEquals(Optional.of(Levels.milestoneFive().id()), session.nextLevelId());

    session.replayRun();
    session.updateMouseRun(6.5F);
    assertEquals(firstRun, session.mouseRunResult());
    assertEquals(acceptedMaze, session.mazeState().placedCells());
    assertEquals(1, store.saveCount);

    session.retryLevel();
    assertEquals(Levels.milestoneFour(), session.levelDefinition());
    assertTrue(session.mazeState().placedCells().isEmpty());
    assertEquals(CellSupply.finite(4), session.mazeState().remainingSupply(PlaceableCellType.WALL));
    assertEquals(
        CellSupply.finite(3), session.mazeState().remainingSupply(PlaceableCellType.SLOW_FLOOR));
    assertEquals(Optional.of(PlaceableCellType.WALL), session.selectedCellType());

    GameSession reloaded = new GameSession(store);
    assertTrue(reloaded.levelProgress().get(3).unlocked());
    assertTrue(reloaded.startLevel(Levels.milestoneFour().id()));
    assertEquals(fourthBest, reloaded.bestResult());
  }

  @ParameterizedTest
  @MethodSource("authoredLevels")
  void initializesEveryAuthoredLevelFromItsDefinition(LevelDefinition level) {
    RecordingBestResultStore store = new RecordingBestResultStore();
    store.results.put(Levels.milestoneOne().id(), new BestResult(Duration.ofSeconds(10), 40));
    store.results.put(Levels.milestoneTwo().id(), new BestResult(Duration.ofSeconds(15), 60));
    store.results.put(Levels.milestoneThree().id(), new BestResult(Duration.ofMillis(6500), 26));
    store.results.put(Levels.milestoneFour().id(), new BestResult(Duration.ofMillis(5750), 20));
    GameSession session = new GameSession(store);

    assertTrue(session.startLevel(level.id()));
    assertEquals(level, session.levelDefinition());
    assertEquals(level, session.mazeState().levelDefinition());
    assertEquals(level.buildTime().toMillis() / 1000.0F, session.buildTimeRemainingSeconds());

    session.placeWall(level.mouseStart());
    assertEquals(level.mouseStart(), session.rejectedPosition());
    session.placeWall(level.cheese());
    assertEquals(level.cheese(), session.rejectedPosition());
    assertTrue(session.mazeState().walls().isEmpty());
  }

  @Test
  void rejectsAnInitialLevelOutsideTheCatalog() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new GameSession(TEST_CATALOG, MISSING_LEVEL_ID, BestResultStore.none()));
  }

  @Test
  void wallMutationsRequirePositions() {
    GameSession session = startedSession();

    assertThrows(NullPointerException.class, () -> session.placeWall(null));
    assertThrows(NullPointerException.class, () -> session.clearWall(null));
  }

  private static GameSession startedSession() {
    GameSession session = new GameSession();
    session.startLevel(Levels.milestoneOne().id());
    return session;
  }

  private static List<GridPosition> nextFourPositions(GameSession session) {
    List<GridPosition> positions = new ArrayList<>();
    for (int index = 0; index < 4; index++) {
      session.updateMouseRun(0.25F);
      positions.add(session.mouseRunResult().position());
    }
    return List.copyOf(positions);
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

  private static void addMilestoneTwoTimeoutWalls(GameSession session) {
    int[][] coordinates = {{0, 5}, {1, 6}, {2, 1}, {2, 4}, {3, 5}, {4, 1}, {4, 2}, {5, 6}, {6, 1}};
    for (int[] coordinate : coordinates) {
      session.placeWall(new GridPosition(coordinate[0], coordinate[1]));
    }
  }

  private static void addMilestoneThreePassingWalls(GameSession session) {
    int[][] coordinates = {{2, 2}, {3, 1}, {4, 0}, {5, 1}};
    for (int[] coordinate : coordinates) {
      session.placeWall(new GridPosition(coordinate[0], coordinate[1]));
    }
  }

  private static void addMilestoneFourPassingCells(GameSession session) {
    int[][] wallCoordinates = {{0, 0}, {1, 1}, {2, 2}};
    for (int[] coordinate : wallCoordinates) {
      session.placeWall(new GridPosition(coordinate[0], coordinate[1]));
    }
    session.selectCellType(PlaceableCellType.SLOW_FLOOR);
    int[][] slowFloorCoordinates = {{6, 2}, {6, 1}, {6, 0}};
    for (int[] coordinate : slowFloorCoordinates) {
      assertTrue(
          session
              .placeOrReplaceCell(new GridPosition(coordinate[0], coordinate[1]))
              .orElseThrow()
              .accepted());
    }
  }

  private static Stream<LevelDefinition> authoredLevels() {
    return Levels.catalog().levels().stream();
  }

  private static final class RecordingBestResultStore implements BestResultStore {
    private String savedLevelId;
    private BestResult savedBestResult;
    private int saveCount;
    private final List<String> loadedLevelIds = new ArrayList<>();
    private final Map<String, BestResult> results = new HashMap<>();

    @Override
    public Optional<BestResult> load(String levelId) {
      loadedLevelIds.add(levelId);
      return Optional.ofNullable(results.get(levelId));
    }

    @Override
    public void save(String levelId, BestResult bestResult) {
      savedLevelId = levelId;
      savedBestResult = bestResult;
      results.put(levelId, bestResult);
      saveCount++;
    }
  }
}
