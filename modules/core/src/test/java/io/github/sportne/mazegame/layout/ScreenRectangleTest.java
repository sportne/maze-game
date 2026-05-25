package io.github.sportne.mazegame.layout;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class ScreenRectangleTest {
  @Test
  void containsIncludesRectangleEdges() {
    ScreenRectangle rectangle = new ScreenRectangle(10.0F, 20.0F, 30.0F, 40.0F);

    assertTrue(rectangle.contains(10.0F, 20.0F));
    assertTrue(rectangle.contains(40.0F, 60.0F));
    assertFalse(rectangle.contains(40.1F, 60.0F));
  }

  @Test
  void fitsWithinRequiresEveryEdgeInsideTheViewport() {
    ScreenRectangle viewport = new ScreenRectangle(0.0F, 0.0F, 100.0F, 100.0F);

    assertTrue(new ScreenRectangle(10.0F, 10.0F, 20.0F, 20.0F).fitsWithin(viewport));
    assertFalse(new ScreenRectangle(-1.0F, 10.0F, 20.0F, 20.0F).fitsWithin(viewport));
    assertFalse(new ScreenRectangle(90.0F, 10.0F, 20.0F, 20.0F).fitsWithin(viewport));
  }

  @Test
  void overlapsRequiresPositiveAreaIntersection() {
    ScreenRectangle rectangle = new ScreenRectangle(10.0F, 10.0F, 20.0F, 20.0F);

    assertTrue(rectangle.overlaps(new ScreenRectangle(20.0F, 20.0F, 20.0F, 20.0F)));
    assertFalse(rectangle.overlaps(new ScreenRectangle(30.0F, 10.0F, 20.0F, 20.0F)));
  }
}
