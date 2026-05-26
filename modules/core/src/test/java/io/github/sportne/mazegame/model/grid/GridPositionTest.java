package io.github.sportne.mazegame.model.grid;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class GridPositionTest {
  @Test
  void topLeftZeroBasedPositionIsWithinGrid() {
    assertTrue(new GridPosition(0, 0).isWithin(GridSize.square(5)));
  }

  @Test
  void bottomRightZeroBasedPositionIsWithinGrid() {
    assertTrue(new GridPosition(4, 4).isWithin(GridSize.square(5)));
  }

  @Test
  void negativeRowsAreOutsideGrid() {
    assertFalse(new GridPosition(-1, 0).isWithin(GridSize.square(5)));
  }

  @Test
  void rowsEqualToSizeAreOutsideGrid() {
    assertFalse(new GridPosition(5, 0).isWithin(GridSize.square(5)));
  }

  @Test
  void columnsEqualToSizeAreOutsideGrid() {
    assertFalse(new GridPosition(0, 5).isWithin(GridSize.square(5)));
  }
}
