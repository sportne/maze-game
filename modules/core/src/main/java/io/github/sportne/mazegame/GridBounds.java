package io.github.sportne.mazegame;

import io.github.sportne.mazegame.model.GridPosition;
import io.github.sportne.mazegame.model.GridSize;
import java.util.Optional;

/** Grid bounds in libGDX bottom-left screen coordinates. */
record GridBounds(float x, float y, float cellSize, GridSize gridSize) {
  Optional<GridPosition> gridPositionAt(float pointX, float pointY) {
    if (pointX < x || pointY < y || pointX >= x + width() || pointY >= y + height()) {
      return Optional.empty();
    }
    int column = (int) ((pointX - x) / cellSize);
    int rowFromBottom = (int) ((pointY - y) / cellSize);
    int row = gridSize.rows() - 1 - rowFromBottom;
    return Optional.of(new GridPosition(row, column));
  }

  float width() {
    return cellSize * gridSize.columns();
  }

  float height() {
    return cellSize * gridSize.rows();
  }
}
