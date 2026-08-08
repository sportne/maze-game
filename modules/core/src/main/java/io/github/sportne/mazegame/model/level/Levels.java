package io.github.sportne.mazegame.model.level;

import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import java.time.Duration;
import java.util.List;

/**
 * Catalog of authored levels.
 *
 * <p>The rest of the code goes through this catalog so selection and progression remain independent
 * of individual authored definitions.
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
          MouseBehavior.RANDOM,
          1L);

  /** Larger second level specified by the Milestone 2 design. */
  private static final LevelDefinition MILESTONE_TWO =
      new LevelDefinition(
          "milestone-2",
          "Milestone 2",
          GridSize.square(7),
          new GridPosition(6, 3),
          new GridPosition(0, 3),
          Duration.ofSeconds(25),
          Duration.ofSeconds(6),
          Duration.ofSeconds(15),
          Duration.ofMillis(250),
          MouseBehavior.RANDOM,
          38L);

  /** Authored levels in stable display order. */
  private static final LevelCatalog CATALOG =
      new LevelCatalog(List.of(MILESTONE_ONE, MILESTONE_TWO));

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

  /**
   * Returns the larger second authored level.
   *
   * @return the milestone-two level definition
   */
  public static LevelDefinition milestoneTwo() {
    return MILESTONE_TWO;
  }

  /**
   * Returns the immutable authored-level catalog.
   *
   * @return production level catalog
   */
  public static LevelCatalog catalog() {
    return CATALOG;
  }
}
