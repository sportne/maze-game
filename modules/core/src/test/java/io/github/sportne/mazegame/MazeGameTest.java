package io.github.sportne.mazegame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import io.github.sportne.mazegame.model.GridPosition;
import io.github.sportne.mazegame.model.Levels;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class MazeGameTest {
  @TempDir private Path temporaryDirectory;

  @Test
  void gameCanBeConstructed() {
    assertNotNull(new MazeGame());
  }

  @Test
  void gameStartsInBuildPhaseForMilestoneOne() {
    MazeGame game = new MazeGame();

    assertFalse(game.runRequested());
    assertEquals(30.0F, game.buildTimeRemainingSeconds());
    assertEquals(Levels.milestoneOne(), game.mazeState().levelDefinition());
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
    MazeGame game = new MazeGame();

    game.updateBuildTimer(31.0F);

    assertEquals(0.0F, game.buildTimeRemainingSeconds());
  }

  @Test
  void startRunLocksOutBuildTimerUpdatesAndWallPlacement() {
    MazeGame game = new MazeGame();
    GridPosition wall = new GridPosition(2, 2);

    game.startRun();
    game.updateBuildTimer(1.0F);
    game.handleGridClick(wall, Input.Buttons.LEFT);

    assertTrue(game.runRequested());
    assertEquals(30.0F, game.buildTimeRemainingSeconds());
    assertFalse(game.mazeState().hasWallAt(wall));
  }

  @Test
  void startRunClearsRejectedPlacementFlash() {
    MazeGame game = new MazeGame();

    game.handleGridClick(Levels.milestoneOne().mouseStart(), Input.Buttons.LEFT);
    game.startRun();

    assertNull(game.rejectedPosition());
  }

  @Test
  void leftClickPlacesWallAndRightClickClearsWall() {
    MazeGame game = new MazeGame();
    GridPosition wall = new GridPosition(2, 2);

    game.handleGridClick(wall, Input.Buttons.LEFT);
    assertTrue(game.mazeState().hasWallAt(wall));

    game.handleGridClick(wall, Input.Buttons.RIGHT);
    assertFalse(game.mazeState().hasWallAt(wall));
  }

  @Test
  void rejectedPlacementDoesNotMutateMaze() {
    MazeGame game = new MazeGame();

    game.handleGridClick(Levels.milestoneOne().mouseStart(), Input.Buttons.LEFT);

    assertTrue(game.mazeState().walls().isEmpty());
  }

  @Test
  void rejectedPlacementFlashExpiresDuringBuildTimerUpdates() {
    MazeGame game = new MazeGame();

    game.handleGridClick(Levels.milestoneOne().mouseStart(), Input.Buttons.LEFT);
    assertEquals(Levels.milestoneOne().mouseStart(), game.rejectedPosition());

    game.updateBuildTimer(0.5F);

    assertNull(game.rejectedPosition());
  }

  @Test
  void cellColorReflectsCurrentCellContentAndRejectedPlacement() {
    MazeGame game = new MazeGame();
    GridPosition wall = new GridPosition(2, 2);

    assertEquals(Color.BLACK, game.cellColor(new GridPosition(1, 1)));
    assertEquals(
        new Color(0.24F, 0.62F, 0.95F, 1.0F), game.cellColor(Levels.milestoneOne().mouseStart()));
    assertEquals(
        new Color(0.95F, 0.77F, 0.18F, 1.0F), game.cellColor(Levels.milestoneOne().cheese()));

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
}
