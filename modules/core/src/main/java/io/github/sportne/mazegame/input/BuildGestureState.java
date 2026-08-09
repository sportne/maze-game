package io.github.sportne.mazegame.input;

import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;

/**
 * Immutable controller-owned state for one build pointer gesture.
 *
 * @param pointerId owning pointer id
 * @param originType palette type or occupied-cell type where the press began; null for an empty
 *     cell
 * @param originPosition grid source where the press began; null for a palette press
 * @param pressX initial CSS-pixel x coordinate
 * @param pressY initial CSS-pixel y coordinate
 * @param currentX latest CSS-pixel x coordinate
 * @param currentY latest CSS-pixel y coordinate
 * @param dragThresholdCrossed whether movement reached the drag threshold
 */
public record BuildGestureState(
    int pointerId,
    PlaceableCellType originType,
    GridPosition originPosition,
    float pressX,
    float pressY,
    float currentX,
    float currentY,
    boolean dragThresholdCrossed) {
  /** Validates that every gesture has either a palette or grid origin. */
  public BuildGestureState {
    if (originType == null && originPosition == null) {
      throw new IllegalArgumentException("a build gesture must have a palette or grid origin");
    }
  }

  /** Compatibility constructor for palette-only callers. */
  public BuildGestureState(
      int pointerId,
      PlaceableCellType originType,
      float pressX,
      float pressY,
      float currentX,
      float currentY,
      boolean dragThresholdCrossed) {
    this(pointerId, originType, null, pressX, pressY, currentX, currentY, dragThresholdCrossed);
  }

  /** Returns whether this gesture began on a palette item. */
  public boolean paletteOrigin() {
    return originPosition == null;
  }

  /** Returns whether this gesture began on an occupied grid cell that can be dragged. */
  public boolean occupiedCellOrigin() {
    return originPosition != null && originType != null;
  }

  /** Returns whether threshold crossing creates an active palette or placed-cell drag. */
  public boolean dragging() {
    return dragThresholdCrossed && (paletteOrigin() || occupiedCellOrigin());
  }
}
