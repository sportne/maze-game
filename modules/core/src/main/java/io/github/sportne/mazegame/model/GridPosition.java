package io.github.sportne.mazegame.model;

import java.util.Objects;

/** Zero-based grid coordinate measured from the top-left corner. */
public record GridPosition(int row, int column) {
  /** Returns whether this position is inside the given grid size. */
  public boolean isWithin(GridSize gridSize) {
    Objects.requireNonNull(gridSize, "gridSize");
    return row >= 0 && row < gridSize.rows() && column >= 0 && column < gridSize.columns();
  }
}
