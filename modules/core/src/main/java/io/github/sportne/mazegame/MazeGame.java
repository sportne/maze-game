package io.github.sportne.mazegame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;

/** Main libGDX application for Maze Game. */
public final class MazeGame extends ApplicationAdapter {
  private static final Color BACKGROUND = new Color(0.07F, 0.08F, 0.10F, 1.0F);
  private static final String TITLE = "Maze Game";

  /** Returns the display title used by launchers. */
  public static String title() {
    return TITLE;
  }

  static Color background() {
    return new Color(BACKGROUND);
  }

  @Override
  public void render() {
    ScreenUtils.clear(background());
  }
}
