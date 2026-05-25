package io.github.sportne.mazegame.model;

import java.util.Objects;

/**
 * Zero-based grid coordinate measured from the top-left corner.
 *
 * @param row zero-based row, increasing downward
 * @param column zero-based column, increasing to the right
 */
public record GridPosition(int row, int column) {
  /**
   * Returns whether this position is inside the given grid size.
   *
   * @param gridSize grid bounds to check against
   * @return true when both row and column are within the grid
   */
  public boolean isWithin(GridSize gridSize) {
    Objects.requireNonNull(gridSize, "gridSize");
    return row >= 0 && row < gridSize.rows() && column >= 0 && column < gridSize.columns();
  }
}
