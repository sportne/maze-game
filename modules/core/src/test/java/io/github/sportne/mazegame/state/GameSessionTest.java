package io.github.sportne.mazegame.state;

import static io.github.sportne.mazegame.TestLevels.singleSolverLevel;
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
import io.github.sportne.mazegame.model.level.GoalType;
import io.github.sportne.mazegame.model.level.LevelCatalog;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.level.LevelSolver;
import io.github.sportne.mazegame.model.level.Levels;
import io.github.sportne.mazegame.model.level.SolverAppearance;
import io.github.sportne.mazegame.model.level.SolverBehavior;
import io.github.sportne.mazegame.model.result.BestResult;
import io.github.sportne.mazegame.model.solver.CardinalDirection;
import io.github.sportne.mazegame.model.solver.SolverDecisionState;
import io.github.sportne.mazegame.model.solver.SolverRunResult;
import io.github.sportne.mazegame.model.solver.SolverRunStatus;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

final class GameSessionTest {
  private static final String MISSING_LEVEL_ID = "missing-level";

  private static final LevelDefinition SECOND_LEVEL =
      singleSolverLevel(
          "test-level-2",
          "Test Level 2",
          GridSize.square(5),
          new GridPosition(4, 2),
          new GridPosition(0, 2),
          Duration.ofSeconds(20),
          Duration.ofSeconds(5),
          Duration.ofSeconds(10),
          Duration.ofMillis(250),
          PlaceableCellSupply.unlimitedWallsOnly(),
          SolverBehavior.RANDOM,
          1L);

  private static final LevelCatalog TEST_CATALOG =
      new LevelCatalog(List.of(Levels.levelOne(), SECOND_LEVEL));

  @Test
  void startsAtMainMenuWithMilestoneOneDefaultsReady() {
    GameSession session = new GameSession();

    assertEquals(GamePhase.MAIN_MENU, session.gamePhase());
    assertEquals(Levels.levelOne(), session.levelDefinition());
    assertTrue(session.mazeState().placedCells().isEmpty());
    assertEquals(30.0F, session.buildTimeRemainingSeconds());
    assertFalse(session.runRequested());
    assertNull(session.solverRunResult());
    assertNull(session.bestResult());
  }

  @Test
  void loadsBestResultForCurrentLevel() {
    RecordingBestResultStore store = new RecordingBestResultStore();
    BestResult bestResult = new BestResult(Duration.ofSeconds(10), 40);
    store.results.put(Levels.levelOne().id(), bestResult);

    GameSession session = new GameSession(store);

    assertEquals(bestResult, session.bestResult());
    assertEquals(
        List.of(
            Levels.levelOne().id(),
            Levels.levelTwo().id(),
            Levels.levelThree().id(),
            Levels.levelFour().id(),
            Levels.levelFive().id(),
            Levels.levelSix().id(),
            Levels.levelSeven().id(),
            Levels.levelEight().id(),
            Levels.levelNine().id(),
            Levels.levelTen().id()),
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
    placeWall(session, new GridPosition(2, 2));

    session.returnToLevelSelect();

    assertEquals(GamePhase.LEVEL_SELECT, session.gamePhase());
    assertTrue(session.mazeState().placedCells().isEmpty());
    assertFalse(session.runRequested());
    assertNull(session.solverRunResult());
  }

  @Test
  void buildTimerAutoStartsRunAtZero() {
    GameSession session = startedSession();

    session.updateBuildTimer(31.0F);

    assertEquals(GamePhase.SOLVER_RUNNING, session.gamePhase());
    assertEquals(0.0F, session.buildTimeRemainingSeconds());
    assertTrue(session.runRequested());
    assertEquals(Levels.levelOne().primarySolver().start(), session.solverRunResult().position());
  }

  @Test
  void wallPlacementAndClearingMutateOnlyDuringBuildPhase() {
    GameSession session = startedSession();
    GridPosition wall = new GridPosition(2, 2);

    placeWall(session, wall);
    assertTrue(session.mazeState().placedCells().containsKey(wall));

    session.removeCell(wall);
    assertFalse(session.mazeState().placedCells().containsKey(wall));

    session.startRun();
    placeWall(session, wall);
    assertFalse(session.mazeState().placedCells().containsKey(wall));
  }

  @Test
  void rejectedPlacementFlashesAndExpires() {
    GameSession session = startedSession();

    placeWall(session, Levels.levelOne().primarySolver().start());
    assertEquals(Levels.levelOne().primarySolver().start(), session.rejectedPosition());
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
    assertNull(session.solverRunResult());
  }

  @Test
  void updateSolverRunMovesToResultWhenTerminal() {
    GameSession session = startedSession();

    session.startRun();
    session.updateSolverRun(10.0F);

    assertEquals(GamePhase.RESULT, session.gamePhase());
    assertEquals(SolverRunStatus.TIMED_OUT, session.solverRunResult().status());
    assertTrue(session.resultPassed());
  }

  @Test
  void exposesLatestSolverDirectionsAndResetsThemWithTheRun() {
    GameSession session = startedSession();
    addVerticalCorridorWalls(session);

    assertTrue(session.solverDirections().isEmpty());
    session.startRun();
    assertEquals(List.of(Optional.empty()), session.solverDirections());

    session.updateSolverRun(0.25F);

    assertEquals(List.of(Optional.of(CardinalDirection.NORTH)), session.solverDirections());
    session.retryLevel();
    assertTrue(session.solverDirections().isEmpty());
  }

  @Test
  void exposesTrackerDecisionMemoryAndRecreatesItForReplay() {
    LevelDefinition trackerLevel =
        singleSolverLevel(
            "tracker-session",
            "Tracker Session",
            GridSize.square(3),
            new GridPosition(2, 1),
            new GridPosition(0, 1),
            Duration.ofSeconds(1),
            Duration.ofMillis(250),
            Duration.ofSeconds(2),
            Duration.ofMillis(250),
            PlaceableCellSupply.unlimitedWallsOnly(),
            SolverBehavior.LEAST_VISITED,
            1L);
    GameSession session = sessionFor(trackerLevel);

    assertTrue(session.solverDecisionStates().isEmpty());
    session.startRun();
    assertEquals(
        List.of(new SolverDecisionState(Map.of(trackerLevel.primarySolver().start(), 1))),
        session.solverDecisionStates());
    session.updateSolverRun(0.25F);
    List<SolverDecisionState> firstDecision = session.solverDecisionStates();
    session.updateSolverRun(0.25F);
    session.replayRun();
    session.updateSolverRun(0.25F);

    assertEquals(firstDecision, session.solverDecisionStates());
  }

  @Test
  void trackerParticipatesInFirstGoalMultiSolverStopping() {
    LevelSolver random =
        new LevelSolver(
            new GridPosition(2, 0),
            new GridPosition(0, 0),
            SolverBehavior.RANDOM,
            OptionalLong.of(1L),
            SolverAppearance.CLASSIC_MOUSE,
            GoalType.CHEESE);
    LevelSolver tracker =
        new LevelSolver(
            new GridPosition(2, 2),
            new GridPosition(1, 2),
            SolverBehavior.LEAST_VISITED,
            OptionalLong.empty(),
            SolverAppearance.TRACKER_RACCOON,
            GoalType.TRASH_CAN);
    LevelDefinition level =
        new LevelDefinition(
            "tracker-multi",
            "Tracker Multi",
            GridSize.square(3),
            Duration.ofSeconds(1),
            Duration.ofMillis(100),
            Duration.ofSeconds(2),
            Duration.ofMillis(250),
            PlaceableCellSupply.unlimitedWallsOnly(),
            List.of(random, tracker));
    GameSession session = sessionFor(level);

    session.startRun();
    session.updateSolverRun(1.0F);

    assertEquals(GamePhase.RESULT, session.gamePhase());
    assertEquals(SolverRunStatus.RUNNING, session.solverRunResults().get(0).status());
    assertEquals(SolverRunStatus.REACHED_GOAL, session.solverRunResults().get(1).status());
    assertEquals(Duration.ofMillis(250), session.solverRunResults().get(0).elapsedTime());
    assertEquals(Duration.ofMillis(250), session.solverRunResults().get(1).elapsedTime());
    assertEquals(
        new SolverDecisionState(Map.of(tracker.start(), 1, tracker.goal(), 1)),
        session.solverDecisionStates().get(1));
  }

  @Test
  void passingNormalRunSavesBestResult() {
    RecordingBestResultStore store = new RecordingBestResultStore();
    GameSession session = new GameSession(store);
    session.startLevel(Levels.levelOne().id());

    session.startRun();
    session.updateSolverRun(10.0F);

    assertEquals(new BestResult(Duration.ofSeconds(10), 40), session.bestResult());
    assertEquals(new BestResult(Duration.ofSeconds(10), 40), store.savedBestResult);
    assertEquals(1, store.saveCount);
  }

  @Test
  void worsePassingRunDoesNotReplaceSavedBestResult() {
    RecordingBestResultStore store = new RecordingBestResultStore();
    store.results.put(Levels.levelOne().id(), new BestResult(Duration.ofSeconds(11), 1));
    GameSession session = new GameSession(store);
    session.startLevel(Levels.levelOne().id());

    session.startRun();
    session.updateSolverRun(10.0F);

    assertEquals(new BestResult(Duration.ofSeconds(11), 1), session.bestResult());
    assertEquals(0, store.saveCount);
  }

  @Test
  void failedRunDoesNotSaveBestResult() {
    RecordingBestResultStore store = new RecordingBestResultStore();
    GameSession session = new GameSession(store);
    session.startLevel(Levels.levelOne().id());
    addVerticalCorridorWalls(session);

    session.startRun();
    session.updateSolverRun(1.0F);

    assertNull(session.bestResult());
    assertEquals(0, store.saveCount);
  }

  @Test
  void replayDoesNotSaveBestResultAgain() {
    RecordingBestResultStore store = new RecordingBestResultStore();
    GameSession session = new GameSession(store);
    session.startLevel(Levels.levelOne().id());
    session.startRun();
    session.updateSolverRun(10.0F);

    session.replayRun();
    session.updateSolverRun(10.0F);

    assertEquals(1, store.saveCount);
  }

  @Test
  void retryResetsTheLevel() {
    GameSession session = startedSession();
    GridPosition wall = new GridPosition(2, 2);
    placeWall(session, wall);
    session.startRun();
    session.updateSolverRun(10.0F);

    session.retryLevel();

    assertEquals(GamePhase.BUILDING, session.gamePhase());
    assertFalse(session.runRequested());
    assertTrue(session.mazeState().placedCells().isEmpty());
    assertNull(session.solverRunResult());
  }

  @Test
  void replayUsesSameMazeAndSeed() {
    GameSession session = startedSession();
    session.startRun();
    session.updateSolverRun(10.0F);
    SolverRunResult firstResult = session.solverRunResult();

    session.replayRun();
    session.updateSolverRun(10.0F);

    assertEquals(firstResult, session.solverRunResult());
  }

  @Test
  void sessionUsesTheAuthoredSolverBehavior() {
    LevelDefinition source = Levels.levelOne();
    LevelDefinition scoutLevel =
        singleSolverLevel(
            "scout-session",
            "Scout Session",
            source.gridSize(),
            source.primarySolver().start(),
            source.primarySolver().goal(),
            source.buildTime(),
            source.targetSolveTime(),
            source.maximumSolveTime(),
            source.solverMoveInterval(),
            source.placeableCellSupplies(),
            SolverBehavior.LEFT_PRIORITY,
            256L);
    GameSession session =
        new GameSession(
            new LevelCatalog(List.of(scoutLevel)), scoutLevel.id(), BestResultStore.none());

    session.startLevel(scoutLevel.id());
    session.startRun();
    List<GridPosition> firstTrace = nextFourPositions(session);
    session.updateSolverRun(10.0F);
    SolverRunResult firstResult = session.solverRunResult();

    session.replayRun();
    List<GridPosition> replayTrace = nextFourPositions(session);
    session.updateSolverRun(10.0F);

    assertEquals(
        List.of(
            new GridPosition(4, 1),
            new GridPosition(4, 0),
            new GridPosition(3, 0),
            new GridPosition(2, 0)),
        firstTrace);
    assertEquals(firstTrace, replayTrace);
    assertEquals(firstResult, session.solverRunResult());
  }

  @Test
  void startsAnyCatalogLevelByStableId() {
    RecordingBestResultStore store = new RecordingBestResultStore();
    store.results.put(Levels.levelOne().id(), new BestResult(Duration.ofSeconds(10), 40));
    GameSession session = new GameSession(TEST_CATALOG, Levels.levelOne().id(), store);

    assertTrue(session.startLevel(SECOND_LEVEL.id()));

    assertEquals(SECOND_LEVEL, session.levelDefinition());
    assertEquals(SECOND_LEVEL, session.mazeState().levelDefinition());
    assertEquals(GamePhase.BUILDING, session.gamePhase());
    assertEquals(List.of(Levels.levelOne().id(), SECOND_LEVEL.id()), store.loadedLevelIds);
  }

  @Test
  void unknownLevelCannotStartOrChangeTheSession() {
    RecordingBestResultStore store = new RecordingBestResultStore();
    GameSession session = new GameSession(TEST_CATALOG, Levels.levelOne().id(), store);
    LevelDefinition originalLevel = session.levelDefinition();
    GamePhase originalPhase = session.gamePhase();

    assertFalse(session.startLevel(MISSING_LEVEL_ID));

    assertEquals(originalLevel, session.levelDefinition());
    assertEquals(originalPhase, session.gamePhase());
    assertEquals(List.of(Levels.levelOne().id(), SECOND_LEVEL.id()), store.loadedLevelIds);
  }

  @Test
  void retryAndReplayRetainTheSelectedLevel() {
    RecordingBestResultStore store = new RecordingBestResultStore();
    store.results.put(Levels.levelOne().id(), new BestResult(Duration.ofSeconds(10), 40));
    GameSession session = new GameSession(TEST_CATALOG, Levels.levelOne().id(), store);
    session.startLevel(SECOND_LEVEL.id());
    session.startRun();
    session.updateSolverRun(10.0F);
    SolverRunResult firstResult = session.solverRunResult();

    session.retryLevel();
    assertEquals(SECOND_LEVEL, session.levelDefinition());
    assertEquals(SECOND_LEVEL, session.mazeState().levelDefinition());

    session.startRun();
    session.updateSolverRun(10.0F);
    session.replayRun();
    session.updateSolverRun(10.0F);

    assertEquals(SECOND_LEVEL, session.levelDefinition());
    assertEquals(firstResult, session.solverRunResult());
  }

  @Test
  void savesBestResultUnderTheSelectedLevelId() {
    RecordingBestResultStore store = new RecordingBestResultStore();
    store.results.put(Levels.levelOne().id(), new BestResult(Duration.ofSeconds(10), 40));
    GameSession session = new GameSession(TEST_CATALOG, Levels.levelOne().id(), store);
    session.startLevel(SECOND_LEVEL.id());

    session.startRun();
    session.updateSolverRun(10.0F);

    assertEquals(SECOND_LEVEL.id(), store.savedLevelId);
  }

  @Test
  void unlocksLevelsInCatalogOrderFromPassingResults() {
    RecordingBestResultStore store = new RecordingBestResultStore();
    GameSession session = new GameSession(TEST_CATALOG, Levels.levelOne().id(), store);

    assertTrue(session.levelProgress().get(0).unlocked());
    assertFalse(session.levelProgress().get(1).unlocked());
    assertFalse(session.startLevel(SECOND_LEVEL.id()));

    session.startLevel(Levels.levelOne().id());
    session.startRun();
    session.updateSolverRun(10.0F);

    assertTrue(session.levelProgress().get(1).unlocked());
    assertEquals(Optional.of(SECOND_LEVEL.id()), session.nextLevelId());
    assertTrue(session.hasNextLevel());
    assertTrue(session.startLevel(SECOND_LEVEL.id()));
  }

  @Test
  void failedResultDoesNotUnlockTheNextLevel() {
    GameSession session =
        new GameSession(TEST_CATALOG, Levels.levelOne().id(), BestResultStore.none());
    session.startLevel(Levels.levelOne().id());
    addVerticalCorridorWalls(session);

    session.startRun();
    session.updateSolverRun(1.0F);

    assertFalse(session.resultPassed());
    assertFalse(session.levelProgress().get(1).unlocked());
    assertFalse(session.hasNextLevel());
  }

  @Test
  void restoredFirstLevelResultUnlocksTheNextLevelWithoutSecondaryState() {
    RecordingBestResultStore store = new RecordingBestResultStore();
    store.results.put(Levels.levelOne().id(), new BestResult(Duration.ofSeconds(10), 40));

    GameSession restored = new GameSession(TEST_CATALOG, Levels.levelOne().id(), store);
    assertTrue(restored.levelProgress().get(1).unlocked());

    store.results.remove(Levels.levelOne().id());
    GameSession cleared = new GameSession(TEST_CATALOG, Levels.levelOne().id(), store);
    assertFalse(cleared.levelProgress().get(1).unlocked());
  }

  @Test
  void exposesIndependentBestResultsForEachLevel() {
    RecordingBestResultStore store = new RecordingBestResultStore();
    BestResult firstBest = new BestResult(Duration.ofSeconds(11), 20);
    BestResult secondBest = new BestResult(Duration.ofSeconds(12), 30);
    store.results.put(Levels.levelOne().id(), firstBest);
    store.results.put(SECOND_LEVEL.id(), secondBest);

    GameSession session = new GameSession(TEST_CATALOG, Levels.levelOne().id(), store);

    assertEquals(firstBest, session.levelProgress().get(0).bestResult());
    assertEquals(secondBest, session.levelProgress().get(1).bestResult());
    session.startLevel(SECOND_LEVEL.id());
    assertEquals(secondBest, session.bestResult());
  }

  @Test
  void finalLevelNeverOffersAnotherLevel() {
    RecordingBestResultStore store = new RecordingBestResultStore();
    store.results.put(Levels.levelOne().id(), new BestResult(Duration.ofSeconds(10), 40));
    GameSession session = new GameSession(TEST_CATALOG, Levels.levelOne().id(), store);
    session.startLevel(SECOND_LEVEL.id());

    session.startRun();
    session.updateSolverRun(10.0F);

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
    GameSession session = new GameSession(TEST_CATALOG, Levels.levelOne().id(), unavailableStore);

    session.startLevel(Levels.levelOne().id());
    session.startRun();
    session.updateSolverRun(10.0F);

    assertTrue(session.levelProgress().get(1).unlocked());
    assertTrue(session.startLevel(SECOND_LEVEL.id()));
  }

  @Test
  void playsTheAuthoredSecondLevelWithoutLeakingStateBetweenLevels() {
    RecordingBestResultStore store = new RecordingBestResultStore();
    BestResult firstBest = new BestResult(Duration.ofSeconds(10), 40);
    store.results.put(Levels.levelOne().id(), firstBest);
    GameSession session = new GameSession(store);

    assertTrue(session.startLevel(Levels.levelTwo().id()));
    assertEquals(Levels.levelTwo(), session.levelDefinition());
    assertEquals(25.0F, session.buildTimeRemainingSeconds());
    placeWall(session, new GridPosition(7, 0));
    assertEquals(new GridPosition(7, 0), session.rejectedPosition());
    for (int column = 0; column < 7; column++) {
      placeWall(session, new GridPosition(1, column));
    }
    assertEquals(6, session.mazeState().placedCells().size());
    assertEquals(new GridPosition(1, 6), session.rejectedPosition());

    session.retryLevel();
    addMilestoneTwoTimeoutWalls(session);

    session.updateBuildTimer(25.0F);
    session.updateSolverRun(15.0F);

    assertTrue(session.resultPassed());
    assertEquals(SolverRunStatus.TIMED_OUT, session.solverRunResult().status());
    assertEquals(Levels.levelTwo().id(), store.savedLevelId);
    assertEquals(9, session.mazeState().placedCells().size());

    session.retryLevel();
    assertEquals(Levels.levelTwo(), session.levelDefinition());
    assertTrue(session.mazeState().placedCells().isEmpty());
    assertEquals(25.0F, session.buildTimeRemainingSeconds());

    session.startRun();
    session.updateSolverRun(3.0F);
    SolverRunResult failedResult = session.solverRunResult();
    assertFalse(session.resultPassed());
    assertEquals(SolverRunStatus.REACHED_GOAL, failedResult.status());

    session.replayRun();
    session.updateSolverRun(15.0F);
    assertEquals(failedResult, session.solverRunResult());
    assertEquals(1, store.saveCount);

    session.returnToMainMenu();
    session.openLevelSelect();
    assertEquals(GamePhase.LEVEL_SELECT, session.gamePhase());
    assertEquals(Levels.levelTwo(), session.levelDefinition());

    assertTrue(session.startLevel(Levels.levelOne().id()));
    assertEquals(Levels.levelOne(), session.levelDefinition());
    assertEquals(firstBest, session.bestResult());
    assertTrue(session.mazeState().placedCells().isEmpty());
    assertEquals(30.0F, session.buildTimeRemainingSeconds());
  }

  @Test
  void existingProfilesDeriveThirdAndFourthLevelUnlocksWithoutMigrationState() {
    RecordingBestResultStore cleanStore = new RecordingBestResultStore();
    GameSession clean = new GameSession(cleanStore);
    assertFalse(clean.levelProgress().get(2).unlocked());
    assertFalse(clean.levelProgress().get(3).unlocked());
    assertFalse(clean.startLevel(Levels.levelThree().id()));
    assertFalse(clean.startLevel(Levels.levelFour().id()));

    RecordingBestResultStore firstLevelOnlyStore = new RecordingBestResultStore();
    firstLevelOnlyStore.results.put(
        Levels.levelOne().id(), new BestResult(Duration.ofSeconds(10), 40));
    GameSession firstLevelOnly = new GameSession(firstLevelOnlyStore);
    assertTrue(firstLevelOnly.levelProgress().get(1).unlocked());
    assertFalse(firstLevelOnly.levelProgress().get(2).unlocked());
    assertFalse(firstLevelOnly.levelProgress().get(3).unlocked());

    RecordingBestResultStore existingTwoLevelStore = new RecordingBestResultStore();
    existingTwoLevelStore.results.put(
        Levels.levelOne().id(), new BestResult(Duration.ofSeconds(10), 40));
    existingTwoLevelStore.results.put(
        Levels.levelTwo().id(), new BestResult(Duration.ofSeconds(15), 60));
    GameSession existingTwoLevel = new GameSession(existingTwoLevelStore);
    assertTrue(existingTwoLevel.levelProgress().get(2).unlocked());
    assertFalse(existingTwoLevel.levelProgress().get(3).unlocked());
    assertTrue(existingTwoLevel.startLevel(Levels.levelThree().id()));

    RecordingBestResultStore existingThreeLevelStore = new RecordingBestResultStore();
    existingThreeLevelStore.results.putAll(existingTwoLevelStore.results);
    existingThreeLevelStore.results.put(
        Levels.levelThree().id(), new BestResult(Duration.ofMillis(6500), 26));
    GameSession existingThreeLevel = new GameSession(existingThreeLevelStore);
    assertTrue(existingThreeLevel.levelProgress().get(3).unlocked());
    assertTrue(existingThreeLevel.startLevel(Levels.levelFour().id()));

    RecordingBestResultStore outOfOrderStore = new RecordingBestResultStore();
    outOfOrderStore.results.put(Levels.levelTwo().id(), new BestResult(Duration.ofSeconds(15), 60));
    GameSession outOfOrder = new GameSession(outOfOrderStore);
    assertFalse(outOfOrder.levelProgress().get(1).unlocked());
    assertFalse(outOfOrder.levelProgress().get(2).unlocked());
    assertFalse(outOfOrder.levelProgress().get(3).unlocked());

    RecordingBestResultStore fourthOnlyStore = new RecordingBestResultStore();
    fourthOnlyStore.results.put(
        Levels.levelFour().id(), new BestResult(Duration.ofMillis(5750), 20));
    GameSession fourthOnly = new GameSession(fourthOnlyStore);
    assertFalse(fourthOnly.levelProgress().get(3).unlocked());
    assertFalse(fourthOnly.startLevel(Levels.levelFour().id()));
  }

  @Test
  void thirdLevelSupportsUnlockRunRetryReplayPersistenceAndReload() {
    RecordingBestResultStore store = new RecordingBestResultStore();
    BestResult firstBest = new BestResult(Duration.ofSeconds(10), 40);
    store.results.put(Levels.levelOne().id(), firstBest);
    GameSession session = new GameSession(store);

    session.startLevel(Levels.levelTwo().id());
    addMilestoneTwoTimeoutWalls(session);
    session.startRun();
    session.updateSolverRun(15.0F);
    BestResult secondBest = session.bestResult();
    assertEquals(Optional.of(Levels.levelThree().id()), session.nextLevelId());
    assertTrue(session.startLevel(Levels.levelThree().id()));

    addMilestoneThreePassingWalls(session);
    Set<GridPosition> acceptedMaze = session.mazeState().placedCells().keySet();
    session.startRun();
    session.updateSolverRun(8.0F);
    SolverRunResult firstRun = session.solverRunResult();
    BestResult thirdBest = session.bestResult();

    assertTrue(session.resultPassed());
    assertEquals(
        new SolverRunResult(
            Levels.levelThree().primarySolver().goal(),
            Duration.ofMillis(6500),
            26,
            SolverRunStatus.REACHED_GOAL),
        firstRun);
    assertEquals(Levels.levelThree().id(), store.savedLevelId);
    assertEquals(firstBest, store.results.get(Levels.levelOne().id()));
    assertEquals(secondBest, store.results.get(Levels.levelTwo().id()));
    assertEquals(thirdBest, store.results.get(Levels.levelThree().id()));
    assertTrue(session.hasNextLevel());
    assertEquals(Optional.of(Levels.levelFour().id()), session.nextLevelId());

    session.replayRun();
    session.updateSolverRun(8.0F);
    assertEquals(firstRun, session.solverRunResult());
    assertEquals(acceptedMaze, session.mazeState().placedCells().keySet());

    session.retryLevel();
    assertEquals(Levels.levelThree(), session.levelDefinition());
    assertTrue(session.mazeState().placedCells().isEmpty());
    assertEquals(25.0F, session.buildTimeRemainingSeconds());

    session.returnToMainMenu();
    assertTrue(session.startLevel(Levels.levelThree().id()));
    assertEquals(thirdBest, session.bestResult());

    GameSession reloaded = new GameSession(store);
    assertTrue(reloaded.levelProgress().get(2).unlocked());
    assertTrue(reloaded.levelProgress().get(3).unlocked());
    assertTrue(reloaded.startLevel(Levels.levelThree().id()));
    assertEquals(thirdBest, reloaded.bestResult());
  }

  @Test
  void fourthLevelPreservesResultsAndSupportsBackRetryReplayAndFinalNavigation() {
    RecordingBestResultStore store = new RecordingBestResultStore();
    BestResult firstBest = new BestResult(Duration.ofSeconds(10), 40);
    BestResult secondBest = new BestResult(Duration.ofSeconds(15), 60);
    BestResult thirdBest = new BestResult(Duration.ofMillis(6500), 26);
    store.results.put(Levels.levelOne().id(), firstBest);
    store.results.put(Levels.levelTwo().id(), secondBest);
    store.results.put(Levels.levelThree().id(), thirdBest);
    GameSession session = new GameSession(store);

    assertTrue(session.startLevel(Levels.levelFour().id()));
    placeWall(session, new GridPosition(4, 4));
    session.returnToLevelSelect();
    assertEquals(GamePhase.LEVEL_SELECT, session.gamePhase());
    assertTrue(session.mazeState().placedCells().isEmpty());
    assertTrue(session.startLevel(Levels.levelFour().id()));

    addMilestoneFourPassingCells(session);
    Map<GridPosition, PlaceableCellType> acceptedMaze = session.mazeState().placedCells();
    session.startRun();
    session.updateSolverRun(6.5F);
    SolverRunResult firstRun = session.solverRunResult();
    BestResult fourthBest = session.bestResult();

    assertTrue(session.resultPassed());
    assertEquals(
        new SolverRunResult(
            Levels.levelFour().primarySolver().goal(),
            Duration.ofMillis(5750),
            20,
            SolverRunStatus.REACHED_GOAL),
        firstRun);
    assertEquals(Levels.levelFour().id(), store.savedLevelId);
    assertEquals(1, store.saveCount);
    assertEquals(firstBest, store.results.get(Levels.levelOne().id()));
    assertEquals(secondBest, store.results.get(Levels.levelTwo().id()));
    assertEquals(thirdBest, store.results.get(Levels.levelThree().id()));
    assertEquals(fourthBest, store.results.get(Levels.levelFour().id()));
    assertTrue(session.hasNextLevel());
    assertEquals(Optional.of(Levels.levelFive().id()), session.nextLevelId());

    session.replayRun();
    session.updateSolverRun(6.5F);
    assertEquals(firstRun, session.solverRunResult());
    assertEquals(acceptedMaze, session.mazeState().placedCells());
    assertEquals(1, store.saveCount);

    session.retryLevel();
    assertEquals(Levels.levelFour(), session.levelDefinition());
    assertTrue(session.mazeState().placedCells().isEmpty());
    assertEquals(CellSupply.finite(4), session.mazeState().remainingSupply(PlaceableCellType.WALL));
    assertEquals(
        CellSupply.finite(3), session.mazeState().remainingSupply(PlaceableCellType.SLOW_FLOOR));
    assertEquals(Optional.of(PlaceableCellType.WALL), session.selectedCellType());

    GameSession reloaded = new GameSession(store);
    assertTrue(reloaded.levelProgress().get(3).unlocked());
    assertTrue(reloaded.startLevel(Levels.levelFour().id()));
    assertEquals(fourthBest, reloaded.bestResult());
  }

  @ParameterizedTest
  @MethodSource("authoredLevels")
  void initializesEveryAuthoredLevelFromItsDefinition(LevelDefinition level) {
    RecordingBestResultStore store = new RecordingBestResultStore();
    for (LevelDefinition authored : Levels.catalog().levels()) {
      store.results.put(authored.id(), new BestResult(Duration.ofSeconds(10), 40));
    }
    GameSession session = new GameSession(store);

    assertTrue(session.startLevel(level.id()));
    assertEquals(level, session.levelDefinition());
    assertEquals(level, session.mazeState().levelDefinition());
    assertEquals(level.buildTime().toMillis() / 1000.0F, session.buildTimeRemainingSeconds());

    placeWall(session, level.primarySolver().start());
    assertEquals(level.primarySolver().start(), session.rejectedPosition());
    placeWall(session, level.primarySolver().goal());
    assertEquals(level.primarySolver().goal(), session.rejectedPosition());
    assertTrue(session.mazeState().placedCells().isEmpty());
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

    assertThrows(NullPointerException.class, () -> placeWall(session, null));
    assertThrows(NullPointerException.class, () -> session.removeCell(null));
  }

  private static GameSession startedSession() {
    GameSession session = new GameSession();
    session.startLevel(Levels.levelOne().id());
    return session;
  }

  private static GameSession sessionFor(LevelDefinition level) {
    GameSession session =
        new GameSession(new LevelCatalog(List.of(level)), level.id(), BestResultStore.none());
    session.startLevel(level.id());
    return session;
  }

  private static List<GridPosition> nextFourPositions(GameSession session) {
    List<GridPosition> positions = new ArrayList<>();
    for (int index = 0; index < 4; index++) {
      session.updateSolverRun(0.25F);
      positions.add(session.solverRunResult().position());
    }
    return List.copyOf(positions);
  }

  private static void placeWall(GameSession session, GridPosition position) {
    session.selectCellType(PlaceableCellType.WALL);
    session.placeOrReplaceCell(position);
  }

  private static void addVerticalCorridorWalls(GameSession session) {
    placeWall(session, new GridPosition(4, 1));
    placeWall(session, new GridPosition(4, 3));
    placeWall(session, new GridPosition(3, 1));
    placeWall(session, new GridPosition(3, 3));
    placeWall(session, new GridPosition(2, 1));
    placeWall(session, new GridPosition(2, 3));
    placeWall(session, new GridPosition(1, 1));
    placeWall(session, new GridPosition(1, 3));
    placeWall(session, new GridPosition(0, 1));
    placeWall(session, new GridPosition(0, 3));
  }

  private static void addMilestoneTwoTimeoutWalls(GameSession session) {
    int[][] coordinates = {{0, 5}, {1, 6}, {2, 1}, {2, 4}, {3, 5}, {4, 1}, {4, 2}, {5, 6}, {6, 1}};
    for (int[] coordinate : coordinates) {
      placeWall(session, new GridPosition(coordinate[0], coordinate[1]));
    }
  }

  private static void addMilestoneThreePassingWalls(GameSession session) {
    int[][] coordinates = {{2, 2}, {3, 1}, {4, 0}, {5, 1}};
    for (int[] coordinate : coordinates) {
      placeWall(session, new GridPosition(coordinate[0], coordinate[1]));
    }
  }

  private static void addMilestoneFourPassingCells(GameSession session) {
    int[][] wallCoordinates = {{0, 0}, {1, 1}, {2, 2}};
    for (int[] coordinate : wallCoordinates) {
      placeWall(session, new GridPosition(coordinate[0], coordinate[1]));
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
