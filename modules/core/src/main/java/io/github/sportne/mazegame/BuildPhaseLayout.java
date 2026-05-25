package io.github.sportne.mazegame;

import io.github.sportne.mazegame.model.GridPosition;
import io.github.sportne.mazegame.model.GridSize;
import java.util.Optional;

/** Pure layout calculations for the build phase. */
record BuildPhaseLayout(GridBounds gridBounds, ButtonBounds startButtonBounds) {
  private static final float GRID_SCREEN_RATIO = 0.62F;
  private static final float BUTTON_WIDTH = 180.0F;
  private static final float BUTTON_HEIGHT = 44.0F;
  private static final float BUTTON_GAP = 52.0F;

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

  Optional<GridPosition> gridPositionAt(float screenX, float inputYFromTop, float screenHeight) {
    return gridBounds.gridPositionAt(screenX, screenHeight - inputYFromTop);
  }

  boolean startButtonContains(float screenX, float inputYFromTop, float screenHeight) {
    return startButtonBounds.contains(screenX, screenHeight - inputYFromTop);
  }
}
