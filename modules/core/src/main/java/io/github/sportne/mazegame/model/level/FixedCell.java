package io.github.sportne.mazegame.model.level;

import io.github.sportne.mazegame.model.cell.FixedCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import java.util.Objects;

/** One level-owned grid cell that cannot be changed by player editing. */
public record FixedCell(GridPosition position, FixedCellType type) {
  /** Creates a fixed cell with required position and effect. */
  public FixedCell {
    Objects.requireNonNull(position, "position");
    Objects.requireNonNull(type, "type");
  }
}
