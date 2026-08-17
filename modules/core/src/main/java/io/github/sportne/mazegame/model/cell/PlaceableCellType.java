package io.github.sportne.mazegame.model.cell;

/** Closed set of grid-cell types the player may place before a solver run. */
public enum PlaceableCellType {
  /** A solid cell that neither solver may enter. */
  WALL,

  /** A walkable cell that delays the next solver decision. */
  SLOW_FLOOR,

  /** A gate that starts open and alternates between open and blocking during a run. */
  ALTERNATING_GATE
}
