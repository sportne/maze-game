package io.github.sportne.mazegame.model.level;

import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import java.time.Duration;

/**
 * Catalog of authored levels.
 *
 * <p>Milestone 1 only contains a single 5x5 level, but the rest of the code goes through this
 * catalog so later milestones can add selection and progression without changing callers.
 */
public final class Levels {
  /** Initial 5x5 level specified by the milestone roadmap. */
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

  /** Prevents instantiation of this static catalog. */
  private Levels() {}

  /**
   * Returns the first playable 5x5 level.
   *
   * @return the milestone-one level definition
   */
  public static LevelDefinition milestoneOne() {
    return MILESTONE_ONE;
  }
}
