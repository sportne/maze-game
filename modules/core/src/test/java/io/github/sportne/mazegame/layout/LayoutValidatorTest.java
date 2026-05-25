package io.github.sportne.mazegame.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.model.GamePhase;
import java.util.List;
import org.junit.jupiter.api.Test;

final class LayoutValidatorTest {
  @Test
  void reportsNonPositiveElementSizes() {
    ScreenLayout layout =
        layoutWith(
            new LayoutElement(
                "button",
                LayoutElementKind.BUTTON,
                new ScreenRectangle(10.0F, 10.0F, 0.0F, 10.0F),
                LayoutFitPolicy.MUST_FIT));

    assertEquals(
        LayoutIssueType.NON_POSITIVE_SIZE, LayoutValidator.validate(layout).getFirst().type());
  }

  @Test
  void reportsMustFitElementsOutsideTheViewport() {
    ScreenLayout layout =
        layoutWith(
            new LayoutElement(
                "button",
                LayoutElementKind.BUTTON,
                new ScreenRectangle(90.0F, 10.0F, 20.0F, 20.0F),
                LayoutFitPolicy.MUST_FIT));

    assertEquals(
        LayoutIssueType.OUTSIDE_VIEWPORT, LayoutValidator.validate(layout).getFirst().type());
  }

  @Test
  void allowsCanOverflowElementsOutsideTheViewport() {
    ScreenLayout layout =
        layoutWith(
            new LayoutElement(
                "button",
                LayoutElementKind.BUTTON,
                new ScreenRectangle(90.0F, 10.0F, 20.0F, 20.0F),
                LayoutFitPolicy.CAN_OVERFLOW));

    assertTrue(LayoutValidator.validate(layout).isEmpty());
  }

  @Test
  void reportsOverlappingButtons() {
    ScreenLayout layout =
        layoutWith(
            new LayoutElement(
                "first",
                LayoutElementKind.BUTTON,
                new ScreenRectangle(10.0F, 10.0F, 30.0F, 30.0F),
                LayoutFitPolicy.MUST_FIT),
            new LayoutElement(
                "second",
                LayoutElementKind.BUTTON,
                new ScreenRectangle(20.0F, 20.0F, 30.0F, 30.0F),
                LayoutFitPolicy.MUST_FIT));

    assertEquals(
        LayoutIssueType.OVERLAPPING_BUTTONS, LayoutValidator.validate(layout).getFirst().type());
  }

  @Test
  void reportsButtonsOverlappingTheGrid() {
    ScreenLayout layout =
        layoutWith(
            new LayoutElement(
                "grid",
                LayoutElementKind.GRID,
                new ScreenRectangle(10.0F, 10.0F, 50.0F, 50.0F),
                LayoutFitPolicy.MUST_FIT),
            new LayoutElement(
                "button",
                LayoutElementKind.BUTTON,
                new ScreenRectangle(20.0F, 20.0F, 20.0F, 20.0F),
                LayoutFitPolicy.MUST_FIT));

    assertEquals(
        LayoutIssueType.BUTTON_OVERLAPS_GRID, LayoutValidator.validate(layout).getFirst().type());
  }

  private static ScreenLayout layoutWith(LayoutElement... elements) {
    return new ScreenLayout(
        GamePhase.MAIN_MENU, new ScreenRectangle(0.0F, 0.0F, 100.0F, 100.0F), List.of(elements));
  }
}
