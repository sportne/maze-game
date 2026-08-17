package io.github.sportne.mazegame.model.maze;

/**
 * Render-independent cell content for one grid square.
 *
 * <p>The model reports these values so the libGDX layer can decide how to draw a cell without
 * duplicating start, goal, and wall precedence rules.
 */
public enum CellContent {
  /** A walkable cell with no special marker. */
  EMPTY,

  /** A normal wall that blocks solver movement. */
  NORMAL_WALL,

  /** A walkable floor that delays the next solver decision. */
  SLOW_FLOOR,

  /** A gate cell whose run-time phase determines whether it blocks entry. */
  ALTERNATING_GATE,

  /** The fixed cell where the solver begins a run. */
  SOLVER_START,

  /** The fixed endpoint cell that the solver tries to reach. */
  GOAL
}
