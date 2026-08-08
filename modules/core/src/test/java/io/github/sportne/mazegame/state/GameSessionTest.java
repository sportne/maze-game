package io.github.sportne.mazegame.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import io.github.sportne.mazegame.model.level.LevelCatalog;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.level.Levels;
import io.github.sportne.mazegame.model.mouse.MouseRunResult;
import io.github.sportne.mazegame.model.mouse.MouseRunStatus;
import io.github.sportne.mazegame.model.result.BestResult;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        List.of(Levels.milestoneOne().id(), Levels.milestoneTwo().id()), store.loadedLevelIds);
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

  @ParameterizedTest
  @MethodSource("authoredLevels")
  void initializesEveryAuthoredLevelFromItsDefinition(LevelDefinition level) {
    RecordingBestResultStore store = new RecordingBestResultStore();
    store.results.put(Levels.milestoneOne().id(), new BestResult(Duration.ofSeconds(10), 40));
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
