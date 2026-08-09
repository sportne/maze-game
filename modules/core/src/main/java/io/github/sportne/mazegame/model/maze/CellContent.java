package io.github.sportne.mazegame.model.maze;

/**
 * Render-independent cell content for one grid square.
 *
 * <p>The model reports these values so the libGDX layer can decide how to draw a cell without
 * duplicating start, cheese, and wall precedence rules.
 */
public enum CellContent {
  /** A walkable cell with no special marker. */
  EMPTY,

  /** A player-placed normal wall that blocks mouse movement. */
  NORMAL_WALL,

  /** A player-placed walkable floor that delays the next mouse decision. */
  SLOW_FLOOR,

  /** The fixed cell where the mouse begins a run. */
  MOUSE_START,

  /** The fixed endpoint cell that the mouse tries to reach. */
  CHEESE
}
