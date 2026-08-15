package io.github.sportne.mazegame.model.level;

import io.github.sportne.mazegame.model.cell.PlaceableCellSupply;
import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import java.time.Duration;
import java.util.List;
import java.util.OptionalLong;

/**
 * Catalog of authored levels.
 *
 * <p>The rest of the code goes through this catalog so selection and progression remain independent
 * of individual authored definitions.
 */
public final class Levels {
  /** Initial authored 5x5 level. */
  private static final LevelDefinition LEVEL_ONE =
      new LevelDefinition(
          "milestone-1",
          "Level 1",
          GridSize.square(5),
          Duration.ofSeconds(30),
          Duration.ofSeconds(5),
          Duration.ofSeconds(10),
          Duration.ofMillis(250),
          PlaceableCellSupply.releasedDefaults(),
          List.of(
              new LevelSolver(
                  new GridPosition(4, 2),
                  new GridPosition(0, 2),
                  SolverBehavior.RANDOM,
                  OptionalLong.of(1L),
                  SolverAppearance.CLASSIC_MOUSE,
                  GoalType.CHEESE)));

  /** Larger second authored level. */
  private static final LevelDefinition LEVEL_TWO =
      new LevelDefinition(
          "milestone-2",
          "Level 2",
          GridSize.square(7),
          Duration.ofSeconds(25),
          Duration.ofSeconds(6),
          Duration.ofSeconds(15),
          Duration.ofMillis(250),
          PlaceableCellSupply.releasedDefaults(),
          List.of(
              new LevelSolver(
                  new GridPosition(6, 3),
                  new GridPosition(0, 3),
                  SolverBehavior.RANDOM,
                  OptionalLong.of(38L),
                  SolverAppearance.CLASSIC_MOUSE,
                  GoalType.CHEESE)));

  /** Third 7x7 level introducing Scout's deterministic search pattern. */
  private static final LevelDefinition LEVEL_THREE =
      new LevelDefinition(
          "milestone-3",
          "Level 3",
          GridSize.square(7),
          Duration.ofSeconds(25),
          Duration.ofSeconds(6),
          Duration.ofSeconds(8),
          Duration.ofMillis(250),
          PlaceableCellSupply.releasedDefaults(),
          List.of(
              new LevelSolver(
                  new GridPosition(6, 3),
                  new GridPosition(0, 3),
                  SolverBehavior.LEFT_PRIORITY,
                  OptionalLong.empty(),
                  SolverAppearance.SCOUT_SQUIRREL,
                  GoalType.ACORN)));

  /** Fourth 7x7 level introducing finite Walls and Slow Floors with Scout. */
  private static final LevelDefinition LEVEL_FOUR =
      new LevelDefinition(
          "milestone-4",
          "Level 4",
          GridSize.square(7),
          Duration.ofSeconds(25),
          Duration.ofMillis(5500),
          Duration.ofMillis(6500),
          Duration.ofMillis(250),
          List.of(
              PlaceableCellSupply.finite(PlaceableCellType.WALL, 4),
              PlaceableCellSupply.finite(PlaceableCellType.SLOW_FLOOR, 3)),
          List.of(
              new LevelSolver(
                  new GridPosition(6, 3),
                  new GridPosition(0, 3),
                  SolverBehavior.LEFT_PRIORITY,
                  OptionalLong.empty(),
                  SolverAppearance.SCOUT_SQUIRREL,
                  GoalType.ACORN)));

  /** Fifth 7x7 level combining Random and Scout with distinct starts and goals. */
  private static final LevelDefinition LEVEL_FIVE =
      new LevelDefinition(
          "milestone-5",
          "Level 5",
          GridSize.square(7),
          Duration.ofSeconds(25),
          Duration.ofSeconds(5),
          Duration.ofSeconds(10),
          Duration.ofMillis(250),
          List.of(
              PlaceableCellSupply.finite(PlaceableCellType.WALL, 5),
              PlaceableCellSupply.finite(PlaceableCellType.SLOW_FLOOR, 4)),
          List.of(
              new LevelSolver(
                  new GridPosition(6, 0),
                  new GridPosition(3, 3),
                  SolverBehavior.RANDOM,
                  OptionalLong.of(23L),
                  SolverAppearance.CLASSIC_MOUSE,
                  GoalType.CHEESE),
              new LevelSolver(
                  new GridPosition(1, 4),
                  new GridPosition(2, 4),
                  SolverBehavior.LEFT_PRIORITY,
                  OptionalLong.empty(),
                  SolverAppearance.SCOUT_SQUIRREL,
                  GoalType.ACORN)));

  /** Authored levels in stable display order. */
  private static final LevelCatalog CATALOG =
      new LevelCatalog(List.of(LEVEL_ONE, LEVEL_TWO, LEVEL_THREE, LEVEL_FOUR, LEVEL_FIVE));

  /** Prevents instantiation of this static catalog. */
  private Levels() {}

  /**
   * Returns the first playable 5x5 level.
   *
   * @return the first level definition
   */
  public static LevelDefinition levelOne() {
    return LEVEL_ONE;
  }

  /**
   * Returns the larger second authored level.
   *
   * @return the second level definition
   */
  public static LevelDefinition levelTwo() {
    return LEVEL_TWO;
  }

  /**
   * Returns the third authored level featuring Scout.
   *
   * @return the third level definition
   */
  public static LevelDefinition levelThree() {
    return LEVEL_THREE;
  }

  /**
   * Returns the fourth authored level featuring finite Walls and Slow Floors.
   *
   * @return the fourth level definition
   */
  public static LevelDefinition levelFour() {
    return LEVEL_FOUR;
  }

  /** Returns the fifth authored level featuring both Random and Scout. */
  public static LevelDefinition levelFive() {
    return LEVEL_FIVE;
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
