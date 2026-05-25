package io.github.sportne.mazegame.model;

/**
 * High-level phase for the playable game loop.
 *
 * <p>The phase gates input handling and rendering: menus route navigation, building accepts wall
 * edits, running advances the mouse simulation, result shows retry/replay controls, and replay
 * reuses the completed maze.
 */
public enum GamePhase {
  /** Startup menu with navigation to level select, settings, or quit. */
  MAIN_MENU,

  /** Level selection menu showing the available and locked levels. */
  LEVEL_SELECT,

  /** Session-only settings menu. */
  SETTINGS,

  /** Player is editing the maze while the build timer counts down. */
  BUILDING,

  /** The deterministic mouse simulation is advancing for the first run. */
  MOUSE_RUNNING,

  /** A run has ended and pass/fail information is available. */
  RESULT,

  /** The completed maze is being simulated again from the same seed. */
  REPLAY
}
