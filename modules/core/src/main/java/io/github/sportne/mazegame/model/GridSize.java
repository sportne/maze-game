package io.github.sportne.mazegame.model;

/**
 * Dimensions for a rectangular maze grid.
 *
 * @param rows number of rows, required to be positive
 * @param columns number of columns, required to be positive
 */
public record GridSize(int rows, int columns) {
  /**
   * Creates validated grid dimensions.
   *
   * @param rows number of rows in the grid
   * @param columns number of columns in the grid
   * @throws IllegalArgumentException when either dimension is zero or negative
   */
  public GridSize {
    if (rows <= 0) {
      throw new IllegalArgumentException("rows must be positive");
    }
    if (columns <= 0) {
      throw new IllegalArgumentException("columns must be positive");
    }
  }

  /**
   * Creates a square grid size.
   *
   * @param size row and column count
   * @return a grid size with equal rows and columns
   */
  public static GridSize square(int size) {
    return new GridSize(size, size);
  }
}
