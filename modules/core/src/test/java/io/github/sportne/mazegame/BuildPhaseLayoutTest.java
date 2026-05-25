package io.github.sportne.mazegame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.model.GridPosition;
import io.github.sportne.mazegame.model.GridSize;
import org.junit.jupiter.api.Test;

final class BuildPhaseLayoutTest {
  @Test
  void centeredLayoutPlacesGridInTheMiddleOfTheScreen() {
    BuildPhaseLayout layout = BuildPhaseLayout.centered(1280, 720, GridSize.square(5));

    assertEquals(417.5F, layout.gridBounds().x());
    assertEquals(137.5F, layout.gridBounds().y());
    assertEquals(89.0F, layout.gridBounds().cellSize());
  }

  @Test
  void gridPositionAtConvertsTopLeftInputCoordinatesToGridRows() {
    BuildPhaseLayout layout = BuildPhaseLayout.centered(1280, 720, GridSize.square(5));

    assertEquals(
        new GridPosition(0, 0), layout.gridPositionAt(417.5F, 138.5F, 720.0F).orElseThrow());
    assertEquals(
        new GridPosition(4, 4), layout.gridPositionAt(861.5F, 582.5F, 720.0F).orElseThrow());
  }

  @Test
  void gridPositionAtRejectsPointsOutsideTheGrid() {
    BuildPhaseLayout layout = BuildPhaseLayout.centered(1280, 720, GridSize.square(5));

    assertTrue(layout.gridPositionAt(416.5F, 138.5F, 720.0F).isEmpty());
  }

  @Test
  void startButtonContainsUsesTopLeftInputCoordinates() {
    BuildPhaseLayout layout = BuildPhaseLayout.centered(1280, 720, GridSize.square(5));

    assertTrue(layout.startButtonContains(640.0F, 628.5F, 720.0F));
    assertFalse(layout.startButtonContains(640.0F, 580.0F, 720.0F));
  }
}
