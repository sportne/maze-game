package io.github.sportne.mazegame.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import java.util.Optional;
import org.junit.jupiter.api.Test;

final class BuildGestureControllerTest {
  @Test
  void thresholdBoundaryUsesCssPixelsAcrossDeviceScales() {
    BuildGestureController oneX = new BuildGestureController();
    BuildGestureController threeX = new BuildGestureController();
    oneX.press(0, PlaceableCellType.WALL, 10.0F, 20.0F, 1.0F);
    threeX.press(0, PlaceableCellType.WALL, 30.0F, 60.0F, 3.0F);

    oneX.move(0, 17.99F, 20.0F, 1.0F);
    threeX.move(0, 53.97F, 60.0F, 3.0F);
    assertFalse(oneX.state().orElseThrow().dragThresholdCrossed());
    assertFalse(threeX.state().orElseThrow().dragThresholdCrossed());
    assertFalse(oneX.pointerCaptured());

    oneX.move(0, 18.0F, 20.0F, 1.0F);
    threeX.move(0, 54.0F, 60.0F, 3.0F);
    assertTrue(oneX.state().orElseThrow().dragThresholdCrossed());
    assertTrue(threeX.state().orElseThrow().dragThresholdCrossed());
    assertTrue(oneX.pointerCaptured());
    assertEquals(oneX.state().orElseThrow().currentX(), threeX.state().orElseThrow().currentX());
  }

  @Test
  void firstPointerOwnsGestureUntilReleaseOrCancellation() {
    BuildGestureController controller = new BuildGestureController();

    assertTrue(controller.press(4, PlaceableCellType.SLOW_FLOOR, 5.0F, 6.0F, 1.0F));
    assertFalse(controller.press(7, PlaceableCellType.WALL, 50.0F, 60.0F, 1.0F));
    assertFalse(controller.move(7, 70.0F, 80.0F, 1.0F));
    assertTrue(controller.release(7, 70.0F, 80.0F, 1.0F).isEmpty());
    assertTrue(controller.owns(4));

    BuildGestureState released = controller.release(4, 13.0F, 6.0F, 1.0F).orElseThrow();
    assertTrue(released.dragThresholdCrossed());
    assertTrue(controller.state().isEmpty());
    assertFalse(controller.pointerCaptured());

    assertTrue(controller.press(7, PlaceableCellType.WALL, 1.0F, 2.0F, 1.0F));
    controller.cancel();
    assertTrue(controller.state().isEmpty());
  }

  @Test
  void rejectsInvalidCoordinateScale() {
    BuildGestureController controller = new BuildGestureController();

    assertThrows(
        IllegalArgumentException.class,
        () -> controller.press(0, PlaceableCellType.WALL, 0.0F, 0.0F, 0.0F));
    assertThrows(NullPointerException.class, () -> controller.press(0, null, 0.0F, 0.0F, 1.0F));
  }

  @Test
  void occupiedCellCrossingThresholdCapturesWhileEmptyCellDoesNot() {
    GridPosition source = new GridPosition(2, 1);
    BuildGestureController occupied = new BuildGestureController();
    BuildGestureController empty = new BuildGestureController();

    assertTrue(
        occupied.pressCell(
            0, source, Optional.of(PlaceableCellType.SLOW_FLOOR), 10.0F, 20.0F, 1.0F));
    assertTrue(empty.pressCell(0, source, Optional.empty(), 10.0F, 20.0F, 1.0F));
    occupied.move(0, 18.0F, 20.0F, 1.0F);
    empty.move(0, 18.0F, 20.0F, 1.0F);

    assertTrue(occupied.pointerCaptured());
    assertTrue(occupied.state().orElseThrow().occupiedCellOrigin());
    assertEquals(source, occupied.state().orElseThrow().originPosition());
    assertFalse(empty.pointerCaptured());
    assertTrue(empty.state().orElseThrow().dragThresholdCrossed());
    assertFalse(empty.state().orElseThrow().dragging());
  }
}
