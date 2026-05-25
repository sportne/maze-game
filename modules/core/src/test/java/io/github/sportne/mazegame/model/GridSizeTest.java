package io.github.sportne.mazegame.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

final class GridSizeTest {
  @Test
  void squareCreatesEqualRowsAndColumns() {
    assertEquals(new GridSize(5, 5), GridSize.square(5));
  }

  @Test
  void rowsMustBePositive() {
    assertThrows(IllegalArgumentException.class, () -> new GridSize(0, 5));
  }

  @Test
  void columnsMustBePositive() {
    assertThrows(IllegalArgumentException.class, () -> new GridSize(5, -1));
  }
}
