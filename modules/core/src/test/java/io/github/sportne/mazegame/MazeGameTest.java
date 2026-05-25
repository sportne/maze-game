package io.github.sportne.mazegame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import io.github.sportne.mazegame.layout.MazeGameLayout;
import io.github.sportne.mazegame.layout.ScreenLayout;
import io.github.sportne.mazegame.layout.ScreenRectangle;
import io.github.sportne.mazegame.model.GamePhase;
import io.github.sportne.mazegame.model.GridPosition;
import io.github.sportne.mazegame.model.Levels;
import io.github.sportne.mazegame.model.MouseRunResult;
import io.github.sportne.mazegame.model.MouseRunStatus;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class MazeGameTest {
  @TempDir private Path temporaryDirectory;

  @Test
  void gameCanBeConstructed() {
    assertNotNull(new MazeGame());
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
  void lockedFutureLevelDoesNotStartGameplay() {
    MazeGame game = new MazeGame();

    game.openLevelSelect();
    game.handleScreenClick(640, 638 - 398, Input.Buttons.LEFT, 1280, 720);

    assertEquals(GamePhase.LEVEL_SELECT, game.gamePhase());
  }

  @Test
  void settingsAudioToggleUpdatesSessionStateAndBackReturnsToMenu() {
    FakeMusic music = new FakeMusic();
    MazeGame game = new MazeGame(music, null, true, () -> {});

    game.openSettings();
    game.handleScreenClick(640, 292, Input.Buttons.LEFT, 1280, 720);
    assertFalse(game.audioEnabled());
    assertTrue(music.stopped);

    game.handleScreenClick(110, 720 - 62, Input.Buttons.LEFT, 1280, 720);
    assertEquals(GamePhase.MAIN_MENU, game.gamePhase());
  }

  @Test
  void unavailableAudioStartsOffAndIgnoresToggle() {
    MazeGame game = new MazeGame(null, null, false, () -> {});

    game.openSettings();
    game.toggleAudio();

    assertFalse(game.audioEnabled());
  }

  @Test
  void quitClickRunsExitHook() {
    AtomicBoolean exitRequested = new AtomicBoolean(false);
    MazeGame game = new MazeGame(null, null, true, () -> exitRequested.set(true));

    game.handleScreenClick(640, 432, Input.Buttons.LEFT, 1280, 720);

    assertTrue(exitRequested.get());
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
  void backgroundMusicPathUsesConfiguredAssetsDirectory() {
    assertEquals(
        temporaryDirectory.resolve("audio/exploreMaze_T1.mp3").toString(),
        MazeGame.backgroundMusicPath(temporaryDirectory.toString(), temporaryDirectory.toString()));
  }

  @Test
  void backgroundMusicPathUsesAssetRelativePathFromAssetsWorkingDirectory() throws IOException {
    Files.createDirectories(temporaryDirectory.resolve("audio"));
    Files.createFile(temporaryDirectory.resolve("audio/exploreMaze_T1.mp3"));

    assertEquals(
        "audio/exploreMaze_T1.mp3",
        MazeGame.backgroundMusicPath(null, temporaryDirectory.toString()));
  }

  @Test
  void backgroundMusicPathFallsBackToProjectRelativeAssetsDirectory() {
    assertEquals(
        "assets/audio/exploreMaze_T1.mp3",
        MazeGame.backgroundMusicPath(null, temporaryDirectory.toString()));
  }

  @Test
  void spriteSheetPathPointsAtTheBundledSheet() {
    assertEquals("mouse-sprites.png", MazeGame.spriteSheetPath());
  }

  @Test
  void spriteSheetPathUsesConfiguredAssetsDirectory() {
    assertEquals(
        temporaryDirectory.resolve("mouse-sprites.png").toString(),
        MazeGame.spriteSheetPath(temporaryDirectory.toString(), temporaryDirectory.toString()));
  }

  @Test
  void spriteSheetPathUsesAssetRelativePathFromAssetsWorkingDirectory() throws IOException {
    Files.createFile(temporaryDirectory.resolve("mouse-sprites.png"));

    assertEquals(
        "mouse-sprites.png", MazeGame.spriteSheetPath(null, temporaryDirectory.toString()));
  }

  @Test
  void spriteSheetPathFallsBackToProjectRelativeAssetsDirectory() {
    assertEquals(
        "assets/mouse-sprites.png", MazeGame.spriteSheetPath(null, temporaryDirectory.toString()));
  }

  @Test
  void backgroundMusicVolumeIsComfortableForStartup() {
    assertEquals(0.1F, MazeGame.backgroundMusicVolume());
  }

  @Test
  void backgroundMusicUsesQuietLoopingPlayback() {
    FakeMusic music = new FakeMusic();

    MazeGame.configureBackgroundMusic(music);

    assertTrue(music.looping);
    assertEquals(0.1F, music.volume);
  }

  @Test
  void buildTimerCountsDownWithoutGoingNegative() {
    MazeGame game = startedGame();

    game.updateBuildTimer(31.0F);

    assertEquals(0.0F, game.buildTimeRemainingSeconds());
  }

  @Test
  void buildTimerAutomaticallyStartsMouseRunAtZero() {
    MazeGame game = startedGame();

    game.updateGame(30.0F);

    assertTrue(game.runRequested());
    assertEquals(GamePhase.MOUSE_RUNNING, game.gamePhase());
    assertEquals(Levels.milestoneOne().mouseStart(), game.mouseRunResult().position());
  }

  @Test
  void autoStartFrameDoesNotAlsoAdvanceMouseRun() {
    MazeGame game = startedGame();

    game.updateGame(31.0F);

    assertEquals(GamePhase.MOUSE_RUNNING, game.gamePhase());
    assertEquals(0, game.mouseRunResult().moveCount());
    assertEquals(0L, game.mouseRunResult().elapsedTime().toMillis());
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
    assertEquals(Levels.milestoneOne().mouseStart(), game.mouseRunResult().position());
  }

  @Test
  void startRunClearsRejectedPlacementFlash() {
    MazeGame game = startedGame();

    game.handleGridClick(Levels.milestoneOne().mouseStart(), Input.Buttons.LEFT);
    game.startRun();

    assertNull(game.rejectedPosition());
  }

  @Test
  void leftClickPlacesWallAndRightClickClearsWall() {
    MazeGame game = startedGame();
    GridPosition wall = new GridPosition(2, 2);

    game.handleGridClick(wall, Input.Buttons.LEFT);
    assertTrue(game.mazeState().hasWallAt(wall));

    game.handleGridClick(wall, Input.Buttons.RIGHT);
    assertFalse(game.mazeState().hasWallAt(wall));
  }

  @Test
  void mouseRunMovesToResultPhaseWhenTerminal() {
    MazeGame game = startedGame();

    game.startRun();
    game.updateMouseRun(10.0F);

    assertEquals(GamePhase.RESULT, game.gamePhase());
    assertEquals(MouseRunStatus.TIMED_OUT, game.mouseRunResult().status());
    assertTrue(game.resultPassed());
  }

  @Test
  void updateGameAdvancesMouseRunAfterRunStarts() {
    MazeGame game = startedGame();

    game.startRun();
    game.updateGame(0.25F);

    assertEquals(1, game.mouseRunResult().moveCount());
  }

  @Test
  void startRunIsIgnoredAfterBuildPhase() {
    MazeGame game = startedGame();

    game.startRun();
    MouseRunResult initialRun = game.mouseRunResult();
    game.startRun();

    assertEquals(initialRun, game.mouseRunResult());
  }

  @Test
  void reachingCheeseBeforeTargetFailsTheLevel() {
    MazeGame game = startedGame();
    addVerticalCorridorWalls(game);

    game.startRun();
    game.updateMouseRun(1.0F);

    assertEquals(GamePhase.RESULT, game.gamePhase());
    assertEquals(MouseRunStatus.REACHED_CHEESE, game.mouseRunResult().status());
    assertFalse(game.resultPassed());
  }

  @Test
  void retryResetsLevelToBuildPhase() {
    MazeGame game = startedGame();
    GridPosition wall = new GridPosition(2, 2);
    game.handleGridClick(wall, Input.Buttons.LEFT);
    game.startRun();
    game.updateMouseRun(10.0F);

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
    game.updateMouseRun(10.0F);

    ScreenLayout layout =
        MazeGameLayout.forPhase(GamePhase.RESULT, 1280, 720, Levels.milestoneOne().gridSize());
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
    game.updateMouseRun(10.0F);
    MouseRunResult firstResult = game.mouseRunResult();

    game.replayRun();
    assertEquals(GamePhase.REPLAY, game.gamePhase());
    game.updateMouseRun(10.0F);

    assertEquals(firstResult, game.mouseRunResult());
  }

  @Test
  void replayIsIgnoredBeforeResultPhase() {
    MazeGame game = startedGame();

    game.replayRun();

    assertEquals(GamePhase.BUILDING, game.gamePhase());
    assertNull(game.mouseRunResult());
  }

  @Test
  void resultPassedIsFalseBeforeResultPhase() {
    assertFalse(new MazeGame().resultPassed());
  }

  @Test
  void milestoneOneHasNoNextLevel() {
    assertFalse(new MazeGame().hasNextLevel());
  }

  @Test
  void rejectedPlacementDoesNotMutateMaze() {
    MazeGame game = startedGame();

    game.handleGridClick(Levels.milestoneOne().mouseStart(), Input.Buttons.LEFT);

    assertTrue(game.mazeState().walls().isEmpty());
  }

  @Test
  void rejectedPlacementFlashExpiresDuringBuildTimerUpdates() {
    MazeGame game = startedGame();

    game.handleGridClick(Levels.milestoneOne().mouseStart(), Input.Buttons.LEFT);
    assertEquals(Levels.milestoneOne().mouseStart(), game.rejectedPosition());

    game.updateBuildTimer(0.5F);

    assertNull(game.rejectedPosition());
  }

  @Test
  void cellColorReflectsCurrentCellContentAndRejectedPlacement() {
    MazeGame game = startedGame();
    GridPosition wall = new GridPosition(2, 2);

    assertEquals(Color.BLACK, game.cellColor(new GridPosition(1, 1)));
    assertEquals(
        new Color(0.24F, 0.62F, 0.95F, 1.0F), game.cellColor(Levels.milestoneOne().mouseStart()));
    assertEquals(Color.BLACK, game.cellColor(Levels.milestoneOne().cheese()));

    game.handleGridClick(wall, Input.Buttons.LEFT);
    assertEquals(Color.WHITE, game.cellColor(wall));

    game.handleGridClick(Levels.milestoneOne().mouseStart(), Input.Buttons.LEFT);
    assertEquals(
        new Color(0.95F, 0.42F, 0.42F, 1.0F), game.cellColor(Levels.milestoneOne().mouseStart()));
  }

  @Test
  void disposeReleasesBackgroundMusic() {
    FakeMusic music = new FakeMusic();

    new MazeGame(music).dispose();

    assertTrue(music.stopped);
    assertTrue(music.disposed);
  }

  @Test
  void disposeToleratesMissingBackgroundMusic() {
    MazeGame game = new MazeGame();

    game.dispose();

    assertNotNull(game);
  }

  private static MazeGame startedGame() {
    MazeGame game = new MazeGame();
    game.startMilestoneOneLevel();
    return game;
  }

  private static final class FakeMusic implements Music {
    private boolean disposed;
    private boolean looping;
    private boolean stopped;
    private float volume;

    @Override
    public void play() {}

    @Override
    public void pause() {}

    @Override
    public void stop() {
      stopped = true;
    }

    @Override
    public boolean isPlaying() {
      return false;
    }

    @Override
    public void setLooping(boolean isLooping) {
      looping = isLooping;
    }

    @Override
    public boolean isLooping() {
      return looping;
    }

    @Override
    public void setVolume(float volume) {
      this.volume = volume;
    }

    @Override
    public float getVolume() {
      return volume;
    }

    @Override
    public void setPan(float pan, float volume) {
      this.volume = volume;
    }

    @Override
    public void setPosition(float position) {}

    @Override
    public float getPosition() {
      return 0.0F;
    }

    @Override
    public void dispose() {
      disposed = true;
    }

    @Override
    public void setOnCompletionListener(OnCompletionListener listener) {}
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
}
