package io.github.sportne.mazegame.model;

import java.time.Duration;

/** Catalog of authored levels. */
public final class Levels {
  private static final LevelDefinition MILESTONE_ONE =
      new LevelDefinition(
          "milestone-1",
          "Milestone 1",
          GridSize.square(5),
          new GridPosition(4, 2),
          new GridPosition(0, 2),
          Duration.ofSeconds(30),
          Duration.ofSeconds(5),
          Duration.ofSeconds(10),
          Duration.ofMillis(250),
          1L);

  private Levels() {}

  /** Returns the first playable 5x5 level. */
  public static LevelDefinition milestoneOne() {
    return MILESTONE_ONE;
  }
}
