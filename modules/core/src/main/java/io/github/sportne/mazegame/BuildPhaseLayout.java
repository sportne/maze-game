package io.github.sportne.mazegame;

import io.github.sportne.mazegame.model.GridPosition;
import io.github.sportne.mazegame.model.GridSize;
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
  /** Fraction of the shorter screen dimension used for the square grid. */
  private static final float GRID_SCREEN_RATIO = 0.62F;

  /** Pixel width of the primary build-phase button. */
  private static final float BUTTON_WIDTH = 180.0F;

  /** Pixel height of the primary build-phase button. */
  private static final float BUTTON_HEIGHT = 44.0F;

  /** Vertical space between the grid/instructions area and the primary button. */
  private static final float BUTTON_GAP = 52.0F;

  /**
   * Creates a centered layout for a screen and level grid.
   *
   * @param screenWidth current window width in pixels
   * @param screenHeight current window height in pixels
   * @param gridSize number of rows and columns in the level grid
   * @return a layout whose grid is centered and whose button sits below it
   */
  static BuildPhaseLayout centered(int screenWidth, int screenHeight, GridSize gridSize) {
    int longestGridSide = Math.max(gridSize.rows(), gridSize.columns());
    float availableGridSize = Math.min(screenWidth, screenHeight) * GRID_SCREEN_RATIO;
    float cellSize = (float) Math.floor(availableGridSize / longestGridSide);
    float gridWidth = cellSize * gridSize.columns();
    float gridHeight = cellSize * gridSize.rows();
    float gridX = (screenWidth - gridWidth) / 2.0F;
    float gridY = (screenHeight - gridHeight) / 2.0F;
    float buttonX = (screenWidth - BUTTON_WIDTH) / 2.0F;
    float buttonY = Math.max(24.0F, gridY - BUTTON_GAP - BUTTON_HEIGHT);
    return new BuildPhaseLayout(
        new GridBounds(gridX, gridY, cellSize, gridSize),
        new ButtonBounds(buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT));
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
