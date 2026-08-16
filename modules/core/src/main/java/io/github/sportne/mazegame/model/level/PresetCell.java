package io.github.sportne.mazegame.model.level;

import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import java.util.Objects;

/** One level-authored starting cell that becomes ordinary mutable player inventory. */
public record PresetCell(GridPosition position, PlaceableCellType type) {
  /** Creates a preset cell with required position and placeable effect. */
  public PresetCell {
    Objects.requireNonNull(position, "position");
    Objects.requireNonNull(type, "type");
  }
}
