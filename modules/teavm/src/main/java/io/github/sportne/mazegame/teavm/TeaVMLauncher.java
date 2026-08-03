package io.github.sportne.mazegame.teavm;

import com.badlogic.gdx.Gdx;
import com.github.xpenatan.gdx.teavm.backends.web.WebApplication;
import io.github.sportne.mazegame.MazeGame;

/** Launches Maze Game in a gdx-teavm browser application. */
public final class TeaVMLauncher {
  private TeaVMLauncher() {}

  /**
   * Starts the JavaScript application.
   *
   * @param args unused TeaVM entry-point arguments
   */
  public static void main(String[] args) {
    new WebApplication(
        new MazeGame(TeaVMRuntimeConfiguration.create(assetPath -> Gdx.files.internal(assetPath))),
        TeaVMApplicationConfiguration.create());
  }
}
