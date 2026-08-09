package io.github.sportne.mazegame.layout;

import java.util.ArrayList;
import java.util.List;

/** Validates declared screen layout contracts. */
public final class LayoutValidator {
  private static final float MINIMUM_TOUCH_TARGET = 44.0F;
  private static final float MINIMUM_GRID_CELL = 32.0F;

  /** Prevents instantiation of this utility. */
  private LayoutValidator() {}

  /**
   * Validates a screen layout.
   *
   * @param layout layout to inspect
   * @return validation issues, or an empty list when the layout is valid
   */
  public static List<LayoutIssue> validate(ScreenLayout layout) {
    List<LayoutIssue> issues = new ArrayList<>();
    for (LayoutElement element : layout.elements()) {
      validateElement(layout, element, issues);
    }
    validateButtonOverlaps(layout, issues);
    validateButtonGridOverlaps(layout, issues);
    return List.copyOf(issues);
  }

  private static void validateElement(
      ScreenLayout layout, LayoutElement element, List<LayoutIssue> issues) {
    if (!element.bounds().hasPositiveSize()) {
      issues.add(
          new LayoutIssue(
              LayoutIssueType.NON_POSITIVE_SIZE,
              element.id() + " has non-positive size in " + layout.phase()));
    }
    if (element.fitPolicy() == LayoutFitPolicy.MUST_FIT
        && !element.bounds().fitsWithin(layout.viewport())) {
      issues.add(
          new LayoutIssue(
              LayoutIssueType.OUTSIDE_VIEWPORT,
              element.id() + " extends outside the viewport in " + layout.phase()));
    }
    if (element.kind() == LayoutElementKind.BUTTON
        && (element.bounds().width() < MINIMUM_TOUCH_TARGET
            || element.bounds().height() < MINIMUM_TOUCH_TARGET)) {
      issues.add(
          new LayoutIssue(
              LayoutIssueType.TOUCH_TARGET_TOO_SMALL,
              element.id() + " is smaller than 44x44 in " + layout.phase()));
    }
    if (element.kind() == LayoutElementKind.GRID && layout.gridSize() != null) {
      float cellWidth = element.bounds().width() / layout.gridSize().columns();
      float cellHeight = element.bounds().height() / layout.gridSize().rows();
      if (cellWidth < MINIMUM_GRID_CELL || cellHeight < MINIMUM_GRID_CELL) {
        issues.add(
            new LayoutIssue(
                LayoutIssueType.GRID_CELL_TOO_SMALL,
                element.id() + " has cells smaller than 32x32 in " + layout.phase()));
      }
    }
  }

  private static void validateButtonOverlaps(ScreenLayout layout, List<LayoutIssue> issues) {
    List<LayoutElement> buttons =
        layout.elements().stream()
            .filter(element -> element.kind() == LayoutElementKind.BUTTON)
            .toList();
    for (int first = 0; first < buttons.size(); first++) {
      for (int second = first + 1; second < buttons.size(); second++) {
        LayoutElement firstButton = buttons.get(first);
        LayoutElement secondButton = buttons.get(second);
        if (firstButton.bounds().overlaps(secondButton.bounds())) {
          issues.add(
              new LayoutIssue(
                  LayoutIssueType.OVERLAPPING_BUTTONS,
                  firstButton.id() + " overlaps " + secondButton.id() + " in " + layout.phase()));
        }
      }
    }
  }

  private static void validateButtonGridOverlaps(ScreenLayout layout, List<LayoutIssue> issues) {
    List<LayoutElement> grids =
        layout.elements().stream()
            .filter(element -> element.kind() == LayoutElementKind.GRID)
            .toList();
    List<LayoutElement> buttons =
        layout.elements().stream()
            .filter(element -> element.kind() == LayoutElementKind.BUTTON)
            .toList();
    for (LayoutElement grid : grids) {
      for (LayoutElement button : buttons) {
        if (grid.bounds().overlaps(button.bounds())) {
          issues.add(
              new LayoutIssue(
                  LayoutIssueType.BUTTON_OVERLAPS_GRID,
                  button.id() + " overlaps " + grid.id() + " in " + layout.phase()));
        }
      }
    }
  }
}
