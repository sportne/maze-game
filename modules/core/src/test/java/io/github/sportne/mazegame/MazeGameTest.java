package io.github.sportne.mazegame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
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
  void disposeReleasesBackgroundMusic() {
    FakeMusic music = new FakeMusic();

    new MazeGame(music).dispose();

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
    private float volume;

    @Override
    public void play() {}

    @Override
    public void pause() {}

    @Override
    public void stop() {}

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
