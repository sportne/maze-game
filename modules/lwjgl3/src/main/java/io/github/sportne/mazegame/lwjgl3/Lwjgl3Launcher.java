package io.github.sportne.mazegame.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import io.github.sportne.mazegame.MazeGame;

/** Desktop launcher for Maze Game. */
public final class Lwjgl3Launcher {
  private Lwjgl3Launcher() {}

  public static void main(String[] args) {
    createApplication();
  }

  private static Lwjgl3Application createApplication() {
    return new Lwjgl3Application(new MazeGame(), defaultConfiguration());
  }

  private static Lwjgl3ApplicationConfiguration defaultConfiguration() {
    Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
    configuration.setTitle(MazeGame.title());
    configuration.setWindowedMode(1280, 720);
    configuration.useVsync(true);
    configuration.setForegroundFPS(60);
    return configuration;
  }
}
