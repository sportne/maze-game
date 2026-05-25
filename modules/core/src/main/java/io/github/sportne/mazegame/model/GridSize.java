package io.github.sportne.mazegame.model;

/** Dimensions for a rectangular grid. */
public record GridSize(int rows, int columns) {
  public GridSize {
    if (rows <= 0) {
      throw new IllegalArgumentException("rows must be positive");
    }
    if (columns <= 0) {
      throw new IllegalArgumentException("columns must be positive");
    }
  }

  /** Creates a square grid size. */
  public static GridSize square(int size) {
    return new GridSize(size, size);
  }
}
