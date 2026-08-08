package io.github.sportne.mazegame.model.level;

/** Closed set of mouse movement rules supported by authored levels. */
public enum MouseBehavior {
  /** Seeded choice among every open orthogonal neighbor. */
  RANDOM,

  /** Deterministic preference for left, straight, right, then reverse. */
  LEFT_PRIORITY
}
