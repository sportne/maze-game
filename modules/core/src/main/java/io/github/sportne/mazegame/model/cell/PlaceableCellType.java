package io.github.sportne.mazegame.model.cell;

/** Closed set of grid-cell types the player may place before a mouse run. */
public enum PlaceableCellType {
  /** A solid cell that neither mouse may enter. */
  WALL,

  /** A walkable cell that delays the next mouse decision. */
  SLOW_FLOOR
}
