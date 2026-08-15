package io.github.sportne.mazegame.model.level;

import io.github.sportne.mazegame.model.cell.PlaceableCellSupply;
import io.github.sportne.mazegame.model.cell.PlaceableCellType;
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
          "Level 1",
          GridSize.square(5),
          new GridPosition(4, 2),
          new GridPosition(0, 2),
          Duration.ofSeconds(30),
          Duration.ofSeconds(5),
          Duration.ofSeconds(10),
          Duration.ofMillis(250),
          PlaceableCellSupply.releasedDefaults(),
          SolverBehavior.RANDOM,
          1L);

  /** Larger second level specified by the Milestone 2 design. */
  private static final LevelDefinition MILESTONE_TWO =
      new LevelDefinition(
          "milestone-2",
          "Level 2",
          GridSize.square(7),
          new GridPosition(6, 3),
          new GridPosition(0, 3),
          Duration.ofSeconds(25),
          Duration.ofSeconds(6),
          Duration.ofSeconds(15),
          Duration.ofMillis(250),
          PlaceableCellSupply.releasedDefaults(),
          SolverBehavior.RANDOM,
          38L);

  /** Third 7x7 level introducing Scout's deterministic search pattern. */
  private static final LevelDefinition MILESTONE_THREE =
      new LevelDefinition(
          "milestone-3",
          "Level 3",
          GridSize.square(7),
          new GridPosition(6, 3),
          new GridPosition(0, 3),
          Duration.ofSeconds(25),
          Duration.ofSeconds(6),
          Duration.ofSeconds(8),
          Duration.ofMillis(250),
          PlaceableCellSupply.releasedDefaults(),
          SolverBehavior.LEFT_PRIORITY,
          53L);

  /** Fourth 7x7 level introducing finite Walls and Slow Floors with Scout. */
  private static final LevelDefinition MILESTONE_FOUR =
      new LevelDefinition(
          "milestone-4",
          "Level 4",
          GridSize.square(7),
          new GridPosition(6, 3),
          new GridPosition(0, 3),
          Duration.ofSeconds(25),
          Duration.ofMillis(5500),
          Duration.ofMillis(6500),
          Duration.ofMillis(250),
          List.of(
              PlaceableCellSupply.finite(PlaceableCellType.WALL, 4),
              PlaceableCellSupply.finite(PlaceableCellType.SLOW_FLOOR, 3)),
          SolverBehavior.LEFT_PRIORITY,
          53L);

  /** Fifth 7x7 level combining Random and Scout with distinct starts and goals. */
  private static final LevelDefinition MILESTONE_FIVE =
      new LevelDefinition(
          "milestone-5",
          "Level 5",
          GridSize.square(7),
          new GridPosition(6, 0),
          new GridPosition(3, 3),
          Duration.ofSeconds(25),
          Duration.ofSeconds(5),
          Duration.ofSeconds(10),
          Duration.ofMillis(250),
          List.of(
              PlaceableCellSupply.finite(PlaceableCellType.WALL, 5),
              PlaceableCellSupply.finite(PlaceableCellType.SLOW_FLOOR, 4)),
          SolverBehavior.RANDOM,
          23L,
          List.of(
              new LevelSolver(
                  new GridPosition(6, 0), new GridPosition(3, 3), SolverBehavior.RANDOM, 23L),
              new LevelSolver(
                  new GridPosition(1, 4),
                  new GridPosition(2, 4),
                  SolverBehavior.LEFT_PRIORITY,
                  53L)));

  /** Authored levels in stable display order. */
  private static final LevelCatalog CATALOG =
      new LevelCatalog(
          List.of(MILESTONE_ONE, MILESTONE_TWO, MILESTONE_THREE, MILESTONE_FOUR, MILESTONE_FIVE));

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
   * Returns the third authored level featuring Scout.
   *
   * @return the milestone-three level definition
   */
  public static LevelDefinition milestoneThree() {
    return MILESTONE_THREE;
  }

  /**
   * Returns the fourth authored level featuring finite Walls and Slow Floors.
   *
   * @return the milestone-four level definition
   */
  public static LevelDefinition milestoneFour() {
    return MILESTONE_FOUR;
  }

  /** Returns the fifth authored level featuring both Random and Scout. */
  public static LevelDefinition milestoneFive() {
    return MILESTONE_FIVE;
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
