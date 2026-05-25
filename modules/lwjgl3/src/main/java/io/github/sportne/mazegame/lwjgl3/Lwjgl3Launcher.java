package io.github.sportne.mazegame.lwjgl3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowListener;
import io.github.sportne.mazegame.MazeGame;
import java.util.Arrays;

/** Desktop launcher for Maze Game. */
public final class Lwjgl3Launcher {
  private static final String AUDIO_PROPERTY = "mazeGame.audio";
  private static final String AUDIO_ENVIRONMENT_VARIABLE = "MAZE_GAME_AUDIO";

  private Lwjgl3Launcher() {}

  public static void main(String[] args) {
    createApplication(args);
  }

  private static Lwjgl3Application createApplication(String[] args) {
    return new Lwjgl3Application(new MazeGame(), defaultConfiguration(args));
  }

  static Lwjgl3ApplicationConfiguration defaultConfiguration(String... args) {
    Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
    configuration.setTitle(MazeGame.title());
    configuration.setWindowedMode(1280, 720);
    configuration.useVsync(true);
    configuration.setForegroundFPS(60);
    configuration.disableAudio(!audioEnabled(args));
    configuration.setWindowListener(closeThroughApplicationExit());
    return configuration;
  }

  static boolean audioEnabled(String... args) {
    if (Arrays.asList(args).contains("--no-audio")) {
      return false;
    }
    if (Arrays.asList(args).contains("--audio")) {
      return true;
    }
    String audioProperty = System.getProperty(AUDIO_PROPERTY);
    if (audioProperty != null) {
      return Boolean.parseBoolean(audioProperty);
    }
    String audioEnvironment = System.getenv(AUDIO_ENVIRONMENT_VARIABLE);
    if (audioEnvironment != null) {
      return Boolean.parseBoolean(audioEnvironment);
    }
    return true;
  }

  static Lwjgl3WindowListener closeThroughApplicationExit() {
    return new Lwjgl3WindowListener() {
      @Override
      public void created(Lwjgl3Window window) {}

      @Override
      public void iconified(boolean isIconified) {}

      @Override
      public void maximized(boolean isMaximized) {}

      @Override
      public void focusLost() {}

      @Override
      public void focusGained() {}

      @Override
      public boolean closeRequested() {
        if (Gdx.app != null) {
          Gdx.app.exit();
        }
        return false;
      }

      @Override
      public void filesDropped(String[] files) {}

      @Override
      public void refreshRequested() {}
    };
  }
}
