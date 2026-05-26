package io.github.sportne.mazegame;

import io.github.sportne.mazegame.layout.MazeGameLayout;
import io.github.sportne.mazegame.layout.ScreenLayout;
import io.github.sportne.mazegame.layout.ScreenRectangle;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import io.github.sportne.mazegame.state.GamePhase;
import java.util.Optional;

/**
 * Pure layout calculations for the build phase screen.
 *
 * <p>The libGDX renderer and the debug harness both use this type so that drawing and simulated
 * clicks agree about where the grid and controls live. Input coordinates are accepted in the same
 * top-left origin convention used by desktop mouse events, while {@link GridBounds} and {@link
 * ButtonBounds} remain in libGDX's bottom-left coordinate space.
 *
 * @param gridBounds bounds of the playable grid in bottom-left screen coordinates
 * @param startButtonBounds bounds of the Start Mouse button in bottom-left screen coordinates
 */
record BuildPhaseLayout(GridBounds gridBounds, ButtonBounds startButtonBounds) {
  /**
   * Creates a centered layout for a screen and level grid.
   *
   * @param screenWidth current window width in pixels
   * @param screenHeight current window height in pixels
   * @param gridSize number of rows and columns in the level grid
   * @return a layout whose grid is centered and whose button sits below it
   */
  static BuildPhaseLayout centered(int screenWidth, int screenHeight, GridSize gridSize) {
    ScreenLayout layout =
        MazeGameLayout.forPhase(GamePhase.BUILDING, screenWidth, screenHeight, gridSize);
    ScreenRectangle grid = layout.bounds(MazeGameLayout.GAME_GRID);
    ScreenRectangle startButton = layout.bounds(MazeGameLayout.BUILD_START);
    return new BuildPhaseLayout(
        new GridBounds(grid.x(), grid.y(), grid.width() / gridSize.columns(), gridSize),
        new ButtonBounds(
            startButton.x(), startButton.y(), startButton.width(), startButton.height()));
  }

  /**
   * Converts a desktop mouse coordinate into the corresponding grid cell.
   *
   * @param screenX x coordinate from the left edge of the window
   * @param inputYFromTop y coordinate from the top edge of the window
   * @param screenHeight current window height in pixels
   * @return the grid position under the point, or empty when the point is outside the grid
   */
  Optional<GridPosition> gridPositionAt(float screenX, float inputYFromTop, float screenHeight) {
    return gridBounds.gridPositionAt(screenX, screenHeight - inputYFromTop);
  }

  /**
   * Returns whether a desktop mouse coordinate is inside the Start Mouse button.
   *
   * @param screenX x coordinate from the left edge of the window
   * @param inputYFromTop y coordinate from the top edge of the window
   * @param screenHeight current window height in pixels
   * @return true when the point is inside the button bounds
   */
  boolean startButtonContains(float screenX, float inputYFromTop, float screenHeight) {
    return startButtonBounds.contains(screenX, screenHeight - inputYFromTop);
  }
}
