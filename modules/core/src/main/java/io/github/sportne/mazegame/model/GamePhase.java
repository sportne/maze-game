package io.github.sportne.mazegame.model;

/**
 * High-level phase for the playable game loop.
 *
 * <p>The phase gates input handling and rendering: building accepts wall edits, running advances
 * the mouse simulation, result shows retry/replay controls, and replay reuses the completed maze.
 */
public enum GamePhase {
  /** Player is editing the maze while the build timer counts down. */
  BUILDING,

  /** The deterministic mouse simulation is advancing for the first run. */
  MOUSE_RUNNING,

  /** A run has ended and pass/fail information is available. */
  RESULT,

  /** The completed maze is being simulated again from the same seed. */
  REPLAY
}
