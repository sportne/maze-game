package io.github.sportne.mazegame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import io.github.sportne.mazegame.layout.MazeGameLayout;
import io.github.sportne.mazegame.layout.ScreenLayout;
import io.github.sportne.mazegame.layout.ScreenRectangle;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.level.Levels;
import io.github.sportne.mazegame.model.result.BestResult;
import io.github.sportne.mazegame.model.solver.SolverRunResult;
import io.github.sportne.mazegame.model.solver.SolverRunStatus;
import io.github.sportne.mazegame.runtime.MazeGameRuntimeConfiguration;
import io.github.sportne.mazegame.state.BestResultStore;
import io.github.sportne.mazegame.state.GamePhase;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

final class MazeGameTest {
  @Test
  void gameCanBeConstructed() {
    assertNotNull(new MazeGame());
  }

  @Test
  void gameRejectsNullRuntimeConfiguration() {
    assertThrows(
        NullPointerException.class, () -> new MazeGame((MazeGameRuntimeConfiguration) null));
  }

  @Test
  void gameStartsAtMainMenuWithMilestoneOneStateReady() {
    MazeGame game = new MazeGame();

    assertFalse(game.runRequested());
    assertEquals(GamePhase.MAIN_MENU, game.gamePhase());
    assertEquals(30.0F, game.buildTimeRemainingSeconds());
    assertEquals(Levels.milestoneOne(), game.mazeState().levelDefinition());
  }

  @Test
  void mainMenuStartOpensLevelSelect() {
    MazeGame game = new MazeGame();

    game.handleScreenClick(640, 292, Input.Buttons.LEFT, 1280, 720);

    assertEquals(GamePhase.LEVEL_SELECT, game.gamePhase());
  }

  @Test
  void selectingMilestoneOneStartsFreshBuildPhase() {
    MazeGame game = new MazeGame();

    game.openLevelSelect();
    game.handleScreenClick(396, 638 - 398, Input.Buttons.LEFT, 1280, 720);

    assertEquals(GamePhase.BUILDING, game.gamePhase());
    assertFalse(game.runRequested());
    assertTrue(game.mazeState().walls().isEmpty());
  }

  @Test
  void buildBackReturnsToLevelSelection() {
    MazeGame game = new MazeGame();
    game.openLevelSelect();
    game.handleScreenClick(396, 638 - 398, Input.Buttons.LEFT, 1280, 720);
    ScreenRectangle back =
        game.debugScreenLayout(GamePhase.BUILDING, 1280, 720).bounds(MazeGameLayout.BUILD_BACK);

    game.handleScreenClick(
        Math.round(back.x() + back.width() / 2.0F),
        Math.round(720.0F - back.y() - back.height() / 2.0F),
        Input.Buttons.LEFT,
        1280,
        720);

    assertEquals(GamePhase.LEVEL_SELECT, game.gamePhase());
  }

  @Test
  void lockedFutureLevelDoesNotStartGameplay() {
    MazeGame game = new MazeGame();

    game.openLevelSelect();
    game.handleScreenClick(640, 638 - 398, Input.Buttons.LEFT, 1280, 720);

    assertEquals(GamePhase.LEVEL_SELECT, game.gamePhase());
  }

  @Test
  void settingsAudioToggleUpdatesSessionStateAndBackReturnsToMenu() {
    RecordingMusic music = new RecordingMusic();
    MazeGame game =
        new MazeGame(music, runtimeConfiguration(true, () -> {}), new RecordingBestResultStore());

    game.openSettings();
    game.handleScreenClick(640, 292, Input.Buttons.LEFT, 1280, 720);
    assertFalse(game.audioEnabled());
    assertTrue(music.stopped());

    game.handleScreenClick(110, 720 - 62, Input.Buttons.LEFT, 1280, 720);
    assertEquals(GamePhase.MAIN_MENU, game.gamePhase());
  }

  @Test
  void unavailableAudioStartsOffAndIgnoresToggle() {
    MazeGame game =
        new MazeGame(null, runtimeConfiguration(false, () -> {}), new RecordingBestResultStore());

    game.openSettings();
    game.toggleAudio();

    assertFalse(game.audioEnabled());
  }

  @Test
  void injectedMusicIsNotStartedFromConstructor() {
    RecordingMusic music = new RecordingMusic();

    new MazeGame(music, runtimeConfiguration(true, () -> {}), new RecordingBestResultStore());

    assertFalse(music.playing());
  }

  @Test
  void gestureGatedAudioStartsOnTheFirstScreenClick() {
    RecordingMusic music = new RecordingMusic();
    MazeGameRuntimeConfiguration configuration =
        new MazeGameRuntimeConfiguration(
            FileHandle::new, ignoredDelta -> {}, () -> {}, false, true, true);
    MazeGame game = new MazeGame(music, configuration, new RecordingBestResultStore());

    game.handleScreenClick(640, 292, Input.Buttons.LEFT, 1280, 720);

    assertTrue(music.playing());
    assertEquals(GamePhase.LEVEL_SELECT, game.gamePhase());
  }

  @Test
  void lifecycleCallbacksPauseAndResumePlayingMusic() {
    RecordingMusic music = new RecordingMusic();
    MazeGame game =
        new MazeGame(music, runtimeConfiguration(true, () -> {}), new RecordingBestResultStore());
    game.toggleAudio();
    game.toggleAudio();

    game.pause();
    assertTrue(music.paused());

    game.resume();
    assertTrue(music.playing());
  }

  @Test
  void unavailableAudioStillDisposesInjectedMusic() {
    RecordingMusic music = new RecordingMusic();

    new MazeGame(music, runtimeConfiguration(false, () -> {}), new RecordingBestResultStore())
        .dispose();

    assertTrue(music.stopped());
    assertTrue(music.disposed());
  }

  @Test
  void quitClickRunsExitHook() {
    AtomicBoolean exitRequested = new AtomicBoolean(false);
    MazeGame game =
        new MazeGame(
            null,
            runtimeConfiguration(true, () -> exitRequested.set(true)),
            new RecordingBestResultStore());

    game.handleScreenClick(640, 432, Input.Buttons.LEFT, 1280, 720);

    assertTrue(exitRequested.get());
  }

  @Test
  void unavailableQuitIsAbsentAndCannotRunTheExitHook() {
    AtomicBoolean exitRequested = new AtomicBoolean(false);
    MazeGameRuntimeConfiguration configuration =
        new MazeGameRuntimeConfiguration(
            FileHandle::new, ignoredDelta -> {}, () -> exitRequested.set(true), false, true, false);
    MazeGame game = new MazeGame(null, configuration, new RecordingBestResultStore());

    assertFalse(
        game.debugScreenLayout(GamePhase.MAIN_MENU, 1280, 720)
            .element(MazeGameLayout.MAIN_MENU_QUIT)
            .isPresent());
    assertFalse(game.handleScreenClick(640, 432, Input.Buttons.LEFT, 1280, 720));
    assertFalse(exitRequested.get());
  }

  @Test
  void completeFrameInvokesAfterRenderHookWithFrameDelta() {
    float[] observedDelta = {Float.NaN};
    int[] invocationCount = {0};
    MazeGameRuntimeConfiguration runtimeConfiguration =
        new MazeGameRuntimeConfiguration(
            FileHandle::new,
            deltaSeconds -> {
              observedDelta[0] = deltaSeconds;
              invocationCount[0]++;
            },
            () -> {},
            true,
            true,
            false);
    MazeGame game = new MazeGame(null, runtimeConfiguration, new RecordingBestResultStore());

    game.completeFrame(0.375F);

    assertEquals(0.375F, observedDelta[0]);
    assertEquals(1, invocationCount[0]);
  }

  @Test
  void titleProvidesTheDesktopWindowTitle() {
    assertEquals("Maze Game", MazeGame.title());
  }

  @Test
  void backgroundProvidesTheInitialClearColor() {
    assertEquals(new Color(0.07F, 0.08F, 0.10F, 1.0F), MazeGame.background());
  }

  @Test
  void backgroundMusicPathPointsAtTheBundledTrack() {
    assertEquals("audio/exploreMaze_T1.mp3", MazeGame.backgroundMusicPath());
  }

  @Test
  void classicMousePathPointsAtTheProcessedSheet() {
    assertEquals("processed/classic-mouse.png", MazeGame.classicMouseSpriteSheetPath());
  }

  @Test
  void basicCharacterPathPointsAtTheProcessedSheet() {
    assertEquals("processed/basic-characters.png", MazeGame.basicCharacterSpriteSheetPath());
  }

  @Test
  void goalPathPointsAtTheProcessedSheet() {
    assertEquals("processed/goals.png", MazeGame.goalSpriteSheetPath());
  }

  @Test
  void backgroundMusicVolumeIsComfortableForStartup() {
    assertEquals(0.1F, MazeGame.backgroundMusicVolume());
  }

  @Test
  void backgroundMusicUsesQuietLoopingPlayback() {
    RecordingMusic music = new RecordingMusic();

    MazeGame.configureBackgroundMusic(music);

    assertTrue(music.looping());
    assertEquals(0.1F, music.volume());
  }

  @Test
  void buildTimerCountsDownWithoutGoingNegative() {
    MazeGame game = startedGame();

    game.updateBuildTimer(31.0F);

    assertEquals(0.0F, game.buildTimeRemainingSeconds());
  }

  @Test
  void buildTimerAutomaticallyStartsSolverRunAtZero() {
    MazeGame game = startedGame();

    game.updateGame(30.0F);

    assertTrue(game.runRequested());
    assertEquals(GamePhase.SOLVER_RUNNING, game.gamePhase());
    assertEquals(Levels.milestoneOne().solverStart(), game.solverRunResult().position());
  }

  @Test
  void autoStartFrameDoesNotAlsoAdvanceSolverRun() {
    MazeGame game = startedGame();

    game.updateGame(31.0F);

    assertEquals(GamePhase.SOLVER_RUNNING, game.gamePhase());
    assertEquals(0, game.solverRunResult().moveCount());
    assertEquals(0L, game.solverRunResult().elapsedTime().toMillis());
  }

  @Test
  void startRunLocksOutBuildTimerUpdatesAndWallPlacement() {
    MazeGame game = startedGame();
    GridPosition wall = new GridPosition(2, 2);

    game.startRun();
    game.updateBuildTimer(1.0F);
    game.handleGridClick(wall, Input.Buttons.LEFT);

    assertTrue(game.runRequested());
    assertEquals(30.0F, game.buildTimeRemainingSeconds());
    assertFalse(game.mazeState().hasWallAt(wall));
    assertEquals(Levels.milestoneOne().solverStart(), game.solverRunResult().position());
  }

  @Test
  void startRunClearsRejectedPlacementFlash() {
    MazeGame game = startedGame();

    game.handleGridClick(Levels.milestoneOne().solverStart(), Input.Buttons.LEFT);
    game.startRun();

    assertNull(game.rejectedPosition());
  }

  @Test
  void leftClickTogglesWallAndRightClickStillClearsWall() {
    MazeGame game = startedGame();
    GridPosition wall = new GridPosition(2, 2);

    game.handleGridClick(wall, Input.Buttons.LEFT);
    assertTrue(game.mazeState().hasWallAt(wall));

    game.handleGridClick(wall, Input.Buttons.LEFT);
    assertFalse(game.mazeState().hasWallAt(wall));

    game.handleGridClick(wall, Input.Buttons.LEFT);
    game.handleGridClick(wall, Input.Buttons.RIGHT);
    assertFalse(game.mazeState().hasWallAt(wall));
  }

  @Test
  void primaryPointerClearsAnOccupiedCellOnItsSecondClick() {
    MazeGame game = startedGame();
    GridPosition wall = new GridPosition(2, 2);

    game.handleScreenClick(640, 360, Input.Buttons.LEFT, 1280, 720);
    assertTrue(game.mazeState().hasWallAt(wall));

    game.handleScreenClick(640, 360, Input.Buttons.LEFT, 1280, 720);
    assertFalse(game.mazeState().hasWallAt(wall));
  }

  @Test
  void solverRunMovesToResultPhaseWhenTerminal() {
    MazeGame game = startedGame();

    game.startRun();
    game.updateSolverRun(10.0F);

    assertEquals(GamePhase.RESULT, game.gamePhase());
    assertEquals(SolverRunStatus.TIMED_OUT, game.solverRunResult().status());
    assertTrue(game.resultPassed());
  }

  @Test
  void appBoundaryPersistsPassingBestResultThroughInjectedStore() {
    RecordingBestResultStore store = new RecordingBestResultStore();
    MazeGame game = new MazeGame(null, runtimeConfiguration(true, () -> {}), store);

    game.startLevel(Levels.milestoneOne().id());
    game.startRun();
    game.updateSolverRun(10.0F);

    assertEquals(new BestResult(Duration.ofSeconds(10), 40), game.bestResult());
    assertEquals(new BestResult(Duration.ofSeconds(10), 40), store.savedBestResult);
  }

  @Test
  void updateGameAdvancesSolverRunAfterRunStarts() {
    MazeGame game = startedGame();

    game.startRun();
    game.updateGame(0.25F);

    assertEquals(1, game.solverRunResult().moveCount());
  }

  @Test
  void startRunIsIgnoredAfterBuildPhase() {
    MazeGame game = startedGame();

    game.startRun();
    SolverRunResult initialRun = game.solverRunResult();
    game.startRun();

    assertEquals(initialRun, game.solverRunResult());
  }

  @Test
  void reachingCheeseBeforeTargetFailsTheLevel() {
    MazeGame game = startedGame();
    addVerticalCorridorWalls(game);

    game.startRun();
    game.updateSolverRun(1.0F);

    assertEquals(GamePhase.RESULT, game.gamePhase());
    assertEquals(SolverRunStatus.REACHED_CHEESE, game.solverRunResult().status());
    assertFalse(game.resultPassed());
  }

  @Test
  void retryResetsLevelToBuildPhase() {
    MazeGame game = startedGame();
    GridPosition wall = new GridPosition(2, 2);
    game.handleGridClick(wall, Input.Buttons.LEFT);
    game.startRun();
    game.updateSolverRun(10.0F);

    game.retryLevel();

    assertEquals(GamePhase.BUILDING, game.gamePhase());
    assertFalse(game.runRequested());
    assertTrue(game.mazeState().walls().isEmpty());
    assertEquals(30.0F, game.buildTimeRemainingSeconds());
  }

  @Test
  void resultMainMenuReturnsToStartupMenuAndResetsLevelState() {
    MazeGame game = startedGame();
    GridPosition wall = new GridPosition(2, 2);
    game.handleGridClick(wall, Input.Buttons.LEFT);
    game.startRun();
    game.updateSolverRun(10.0F);

    ScreenLayout layout = game.debugScreenLayout(GamePhase.RESULT, 1280, 720);
    ScreenRectangle mainMenuButton = layout.bounds(MazeGameLayout.RESULT_MAIN_MENU);
    game.handleScreenClick(
        Math.round(mainMenuButton.x() + mainMenuButton.width() / 2.0F),
        Math.round(720.0F - mainMenuButton.y() - mainMenuButton.height() / 2.0F),
        Input.Buttons.LEFT,
        1280,
        720);

    assertEquals(GamePhase.MAIN_MENU, game.gamePhase());
    assertFalse(game.runRequested());
    assertTrue(game.mazeState().walls().isEmpty());
  }

  @Test
  void resultButtonsFitInsideTheVirtualScreen() {
    ScreenLayout layout =
        MazeGameLayout.forPhase(GamePhase.RESULT, 1280, 720, Levels.milestoneOne().gridSize());
    ScreenRectangle retry = layout.bounds(MazeGameLayout.RESULT_RETRY);
    ScreenRectangle replay = layout.bounds(MazeGameLayout.RESULT_REPLAY);
    ScreenRectangle mainMenu = layout.bounds(MazeGameLayout.RESULT_MAIN_MENU);

    assertTrue(retry.x() >= 0.0F);
    assertTrue(retry.y() >= 0.0F);
    assertTrue(replay.y() >= 0.0F);
    assertTrue(mainMenu.y() >= 0.0F);
    assertTrue(mainMenu.x() + mainMenu.width() <= 1280.0F);
    assertEquals(retry.y(), replay.y());
    assertEquals(retry.y(), mainMenu.y());
  }

  @Test
  void replayRunsSameMazeAndSeedAgain() {
    MazeGame game = startedGame();
    game.startRun();
    game.updateSolverRun(10.0F);
    SolverRunResult firstResult = game.solverRunResult();

    game.replayRun();
    assertEquals(GamePhase.REPLAY, game.gamePhase());
    game.updateSolverRun(10.0F);

    assertEquals(firstResult, game.solverRunResult());
  }

  @Test
  void replayIsIgnoredBeforeResultPhase() {
    MazeGame game = startedGame();

    game.replayRun();

    assertEquals(GamePhase.BUILDING, game.gamePhase());
    assertNull(game.solverRunResult());
  }

  @Test
  void resultPassedIsFalseBeforeResultPhase() {
    assertFalse(new MazeGame().resultPassed());
  }

  @Test
  void noNextLevelIsAvailableBeforeAPassingResult() {
    assertFalse(new MazeGame().hasNextLevel());
  }

  @Test
  void nextLevelResultActionStartsTheUnlockedCatalogEntry() {
    MazeGame game = startedGame();
    game.startRun();
    game.updateSolverRun(10.0F);
    ScreenLayout layout = game.debugScreenLayout(GamePhase.RESULT, 1280, 720);
    ScreenRectangle nextLevel = layout.bounds(MazeGameLayout.RESULT_NEXT_LEVEL);

    game.handleScreenClick(
        Math.round(nextLevel.x() + nextLevel.width() / 2.0F),
        Math.round(720.0F - nextLevel.y() - nextLevel.height() / 2.0F),
        Input.Buttons.LEFT,
        1280,
        720);

    assertEquals(GamePhase.BUILDING, game.gamePhase());
    assertEquals(Levels.milestoneTwo(), game.mazeState().levelDefinition());
  }

  @Test
  void rejectedPlacementDoesNotMutateMaze() {
    MazeGame game = startedGame();

    game.handleGridClick(Levels.milestoneOne().solverStart(), Input.Buttons.LEFT);

    assertTrue(game.mazeState().walls().isEmpty());
  }

  @Test
  void rejectedPlacementFlashExpiresDuringBuildTimerUpdates() {
    MazeGame game = startedGame();

    game.handleGridClick(Levels.milestoneOne().solverStart(), Input.Buttons.LEFT);
    assertEquals(Levels.milestoneOne().solverStart(), game.rejectedPosition());

    game.updateBuildTimer(0.5F);

    assertNull(game.rejectedPosition());
  }

  @Test
  void cellColorReflectsCurrentCellContentAndRejectedPlacement() {
    MazeGame game = startedGame();
    GridPosition wall = new GridPosition(2, 2);

    assertEquals(Color.BLACK, game.cellColor(new GridPosition(1, 1)));
    assertEquals(
        new Color(0.24F, 0.62F, 0.95F, 1.0F), game.cellColor(Levels.milestoneOne().solverStart()));
    assertEquals(Color.BLACK, game.cellColor(Levels.milestoneOne().cheese()));

    game.handleGridClick(wall, Input.Buttons.LEFT);
    assertEquals(Color.WHITE, game.cellColor(wall));

    game.handleGridClick(Levels.milestoneOne().solverStart(), Input.Buttons.LEFT);
    assertEquals(
        new Color(0.95F, 0.42F, 0.42F, 1.0F), game.cellColor(Levels.milestoneOne().solverStart()));
  }

  @Test
  void disposeReleasesBackgroundMusic() {
    RecordingMusic music = new RecordingMusic();

    new MazeGame(music).dispose();

    assertTrue(music.stopped());
    assertTrue(music.disposed());
  }

  @Test
  void disposeToleratesMissingBackgroundMusic() {
    MazeGame game = new MazeGame();

    game.dispose();

    assertNotNull(game);
  }

  private static MazeGame startedGame() {
    MazeGame game = new MazeGame();
    game.startLevel(Levels.milestoneOne().id());
    return game;
  }

  private static MazeGameRuntimeConfiguration runtimeConfiguration(
      boolean audioAvailable, Runnable exitAction) {
    return new MazeGameRuntimeConfiguration(
        FileHandle::new, ignoredDelta -> {}, exitAction, true, audioAvailable, false);
  }

  private static void addVerticalCorridorWalls(MazeGame game) {
    game.handleGridClick(new GridPosition(4, 1), Input.Buttons.LEFT);
    game.handleGridClick(new GridPosition(4, 3), Input.Buttons.LEFT);
    game.handleGridClick(new GridPosition(3, 1), Input.Buttons.LEFT);
    game.handleGridClick(new GridPosition(3, 3), Input.Buttons.LEFT);
    game.handleGridClick(new GridPosition(2, 1), Input.Buttons.LEFT);
    game.handleGridClick(new GridPosition(2, 3), Input.Buttons.LEFT);
    game.handleGridClick(new GridPosition(1, 1), Input.Buttons.LEFT);
    game.handleGridClick(new GridPosition(1, 3), Input.Buttons.LEFT);
    game.handleGridClick(new GridPosition(0, 1), Input.Buttons.LEFT);
    game.handleGridClick(new GridPosition(0, 3), Input.Buttons.LEFT);
  }

  private static final class RecordingBestResultStore implements BestResultStore {
    private BestResult savedBestResult;

    @Override
    public Optional<BestResult> load(String levelId) {
      return Optional.ofNullable(savedBestResult);
    }

    @Override
    public void save(String levelId, BestResult bestResult) {
      savedBestResult = bestResult;
    }
  }
}
