package io.github.sportne.mazegame;

import io.github.sportne.mazegame.model.GridPosition;
import io.github.sportne.mazegame.model.GridSize;
import java.util.Optional;

/**
 * Grid bounds in libGDX bottom-left screen coordinates.
 *
 * <p>The grid itself is modeled with top-left row/column coordinates. This record bridges that
 * model convention with libGDX drawing coordinates.
 *
 * @param x left edge of the grid in pixels
 * @param y bottom edge of the grid in pixels
 * @param cellSize square cell size in pixels
 * @param gridSize number of rows and columns represented by these bounds
 */
record GridBounds(float x, float y, float cellSize, GridSize gridSize) {
  /**
   * Converts a bottom-left screen point into a grid row and column.
   *
   * @param pointX x coordinate from the left edge of the window
   * @param pointY y coordinate from the bottom edge of the window
   * @return the grid cell under the point, or empty when the point is outside the grid
   */
  Optional<GridPosition> gridPositionAt(float pointX, float pointY) {
    if (pointX < x || pointY < y || pointX >= x + width() || pointY >= y + height()) {
      return Optional.empty();
    }
    int column = (int) ((pointX - x) / cellSize);
    int rowFromBottom = (int) ((pointY - y) / cellSize);
    int row = gridSize.rows() - 1 - rowFromBottom;
    return Optional.of(new GridPosition(row, column));
  }

  /**
   * Returns the total rendered grid width.
   *
   * @return grid width in pixels
   */
  float width() {
    return cellSize * gridSize.columns();
  }

  /**
   * Returns the total rendered grid height.
   *
   * @return grid height in pixels
   */
  float height() {
    return cellSize * gridSize.rows();
  }
}
