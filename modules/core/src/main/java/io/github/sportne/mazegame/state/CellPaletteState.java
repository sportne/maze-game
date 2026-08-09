package io.github.sportne.mazegame.state;

import io.github.sportne.mazegame.model.cell.CellSupply;
import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import java.util.Objects;

/** Immutable authored and remaining inventory state for one build-palette type. */
public record CellPaletteState(
    PlaceableCellType type,
    CellSupply authoredSupply,
    CellSupply remainingSupply,
    boolean selected) {
  /** Creates a palette entry with non-null type and supply values. */
  public CellPaletteState {
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(authoredSupply, "authoredSupply");
    Objects.requireNonNull(remainingSupply, "remainingSupply");
  }

  /** Returns whether another cell of this type can be placed or used for replacement. */
  public boolean available() {
    return remainingSupply.available();
  }
}
