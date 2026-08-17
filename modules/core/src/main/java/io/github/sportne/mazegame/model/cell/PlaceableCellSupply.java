package io.github.sportne.mazegame.model.cell;

import java.util.List;
import java.util.Objects;

/** Authored supply entry for one placeable cell type. */
public record PlaceableCellSupply(PlaceableCellType type, CellSupply supply) {
  /** Creates a non-null supply entry. */
  public PlaceableCellSupply {
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(supply, "supply");
  }

  /** Creates a finite authored entry. */
  public static PlaceableCellSupply finite(PlaceableCellType type, int count) {
    return new PlaceableCellSupply(type, CellSupply.finite(count));
  }

  /** Creates an infinite authored entry. */
  public static PlaceableCellSupply infinite(PlaceableCellType type) {
    return new PlaceableCellSupply(type, CellSupply.infinite());
  }

  /** Supplies unlimited Walls and no other placeable cells. */
  public static List<PlaceableCellSupply> unlimitedWallsOnly() {
    return List.of(
        infinite(PlaceableCellType.WALL),
        finite(PlaceableCellType.SLOW_FLOOR, 0),
        finite(PlaceableCellType.ALTERNATING_GATE, 0));
  }
}
