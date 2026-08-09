package io.github.sportne.mazegame.render;

import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import java.util.Objects;

/**
 * Transient palette-drag presentation data.
 *
 * @param type dragged palette type
 * @param pointerX clamped-preview source x in bottom-left screen coordinates
 * @param pointerY clamped-preview source y in bottom-left screen coordinates
 * @param destination grid destination under the pointer, or null outside the grid
 * @param validDestination whether dropping on the destination would be accepted
 * @param sourcePosition reserved occupied-cell source, or null for a palette drag
 */
public record PaletteDragPreview(
    PlaceableCellType type,
    float pointerX,
    float pointerY,
    GridPosition destination,
    boolean validDestination,
    GridPosition sourcePosition) {
  /** Validates required preview state. */
  public PaletteDragPreview {
    Objects.requireNonNull(type, "type");
    if (!Float.isFinite(pointerX) || !Float.isFinite(pointerY)) {
      throw new IllegalArgumentException("preview coordinates must be finite");
    }
    if (destination == null && validDestination) {
      throw new IllegalArgumentException("an outside-grid preview cannot be valid");
    }
  }

  /** Creates a palette-origin preview without a reserved grid source. */
  public PaletteDragPreview(
      PlaceableCellType type,
      float pointerX,
      float pointerY,
      GridPosition destination,
      boolean validDestination) {
    this(type, pointerX, pointerY, destination, validDestination, null);
  }
}
