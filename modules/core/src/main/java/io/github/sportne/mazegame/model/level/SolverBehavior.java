package io.github.sportne.mazegame.model.level;

/** Closed set of solver movement rules supported by authored levels. */
public enum SolverBehavior {
  /** Seeded choice among every open orthogonal neighbor. */
  RANDOM(true),

  /** Deterministic preference for left, straight, right, then reverse. */
  LEFT_PRIORITY(false),

  /** Deterministic preference for least-visited cells, goal distance, then absolute direction. */
  LEAST_VISITED(false),

  /** Seeded exploration until the goal is visible along a clear row or column. */
  LINE_OF_SIGHT(true);

  private final boolean randomSeedRequired;

  SolverBehavior(boolean randomSeedRequired) {
    this.randomSeedRequired = randomSeedRequired;
  }

  /** Returns whether authored instances of this behavior require a deterministic random seed. */
  public boolean requiresRandomSeed() {
    return randomSeedRequired;
  }
}
