package io.github.sportne.mazegame.model.level;

import io.github.sportne.mazegame.model.cell.FixedCellType;
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
          PlaceableCellSupply.unlimitedWallsOnly(),
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
          PlaceableCellSupply.unlimitedWallsOnly(),
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
          PlaceableCellSupply.unlimitedWallsOnly(),
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

  /** Sixth 5x5 level introducing Tracker's visit-memory behavior and fixed geometry. */
  private static final LevelDefinition LEVEL_SIX =
      new LevelDefinition(
          "level-6",
          "Level 6",
          GridSize.square(5),
          Duration.ofSeconds(20),
          Duration.ofSeconds(6),
          Duration.ofSeconds(8),
          Duration.ofMillis(250),
          List.of(
              PlaceableCellSupply.finite(PlaceableCellType.WALL, 1),
              PlaceableCellSupply.finite(PlaceableCellType.SLOW_FLOOR, 3)),
          List.of(
              new FixedCell(new GridPosition(0, 2), FixedCellType.WALL),
              new FixedCell(new GridPosition(1, 1), FixedCellType.WALL)),
          List.of(
              new LevelSolver(
                  new GridPosition(0, 0),
                  new GridPosition(4, 4),
                  SolverBehavior.LEAST_VISITED,
                  OptionalLong.empty(),
                  SolverAppearance.TRACKER_RACCOON,
                  GoalType.TRASH_CAN)));

  /** Seventh 5x5 level introducing Seeker's line-of-sight behavior. */
  private static final LevelDefinition LEVEL_SEVEN =
      new LevelDefinition(
          "level-7",
          "Level 7",
          GridSize.square(5),
          Duration.ofSeconds(20),
          Duration.ofSeconds(6),
          Duration.ofSeconds(10),
          Duration.ofMillis(250),
          List.of(
              PlaceableCellSupply.finite(PlaceableCellType.WALL, 1),
              PlaceableCellSupply.finite(PlaceableCellType.SLOW_FLOOR, 3)),
          List.of(
              new FixedCell(new GridPosition(0, 0), FixedCellType.WALL),
              new FixedCell(new GridPosition(2, 0), FixedCellType.WALL),
              new FixedCell(new GridPosition(2, 1), FixedCellType.WALL),
              new FixedCell(new GridPosition(2, 2), FixedCellType.WALL),
              new FixedCell(new GridPosition(2, 3), FixedCellType.WALL),
              new FixedCell(new GridPosition(4, 1), FixedCellType.WALL)),
          List.of(
              new LevelSolver(
                  new GridPosition(4, 0),
                  new GridPosition(0, 4),
                  SolverBehavior.LINE_OF_SIGHT,
                  OptionalLong.of(107L),
                  SolverAppearance.SEEKER_RABBIT,
                  GoalType.CARROT)));

  /** Eighth 6x6 level growing the grid around Scout and fixed Slow Floors. */
  private static final LevelDefinition LEVEL_EIGHT =
      new LevelDefinition(
          "level-8",
          "Level 8",
          GridSize.square(6),
          Duration.ofSeconds(25),
          Duration.ofMillis(7300),
          Duration.ofSeconds(8),
          Duration.ofMillis(250),
          List.of(
              PlaceableCellSupply.finite(PlaceableCellType.WALL, 1),
              PlaceableCellSupply.finite(PlaceableCellType.SLOW_FLOOR, 4)),
          List.of(
              new FixedCell(new GridPosition(0, 4), FixedCellType.WALL),
              new FixedCell(new GridPosition(1, 0), FixedCellType.WALL),
              new FixedCell(new GridPosition(2, 5), FixedCellType.WALL),
              new FixedCell(new GridPosition(3, 0), FixedCellType.WALL),
              new FixedCell(new GridPosition(3, 5), FixedCellType.WALL),
              new FixedCell(new GridPosition(4, 2), FixedCellType.WALL),
              new FixedCell(new GridPosition(5, 5), FixedCellType.WALL),
              new FixedCell(new GridPosition(1, 4), FixedCellType.SLOW_FLOOR),
              new FixedCell(new GridPosition(4, 3), FixedCellType.SLOW_FLOOR)),
          List.of(
              new LevelSolver(
                  new GridPosition(5, 0),
                  new GridPosition(0, 5),
                  SolverBehavior.LEFT_PRIORITY,
                  OptionalLong.empty(),
                  SolverAppearance.SCOUT_SQUIRREL,
                  GoalType.ACORN)));

  /** Ninth 7x7 level growing the grid around Tracker and fixed Slow Floors. */
  private static final LevelDefinition LEVEL_NINE =
      new LevelDefinition(
          "level-9",
          "Level 9",
          GridSize.square(7),
          Duration.ofSeconds(30),
          Duration.ofMillis(7500),
          Duration.ofSeconds(9),
          Duration.ofMillis(250),
          List.of(
              PlaceableCellSupply.finite(PlaceableCellType.WALL, 1),
              PlaceableCellSupply.finite(PlaceableCellType.SLOW_FLOOR, 5)),
          List.of(
              new FixedCell(new GridPosition(1, 1), FixedCellType.WALL),
              new FixedCell(new GridPosition(1, 4), FixedCellType.WALL),
              new FixedCell(new GridPosition(1, 5), FixedCellType.WALL),
              new FixedCell(new GridPosition(2, 5), FixedCellType.WALL),
              new FixedCell(new GridPosition(3, 1), FixedCellType.WALL),
              new FixedCell(new GridPosition(4, 1), FixedCellType.WALL),
              new FixedCell(new GridPosition(5, 5), FixedCellType.WALL),
              new FixedCell(new GridPosition(5, 6), FixedCellType.WALL),
              new FixedCell(new GridPosition(6, 4), FixedCellType.WALL),
              new FixedCell(new GridPosition(1, 3), FixedCellType.SLOW_FLOOR),
              new FixedCell(new GridPosition(2, 4), FixedCellType.SLOW_FLOOR),
              new FixedCell(new GridPosition(4, 5), FixedCellType.SLOW_FLOOR)),
          List.of(
              new LevelSolver(
                  new GridPosition(6, 0),
                  new GridPosition(0, 6),
                  SolverBehavior.LEAST_VISITED,
                  OptionalLong.empty(),
                  SolverAppearance.TRACKER_RACCOON,
                  GoalType.TRASH_CAN)));

  /** Authored levels in stable display order. */
  private static final LevelCatalog CATALOG =
      new LevelCatalog(
          List.of(
              LEVEL_ONE,
              LEVEL_TWO,
              LEVEL_THREE,
              LEVEL_FOUR,
              LEVEL_FIVE,
              LEVEL_SIX,
              LEVEL_SEVEN,
              LEVEL_EIGHT,
              LEVEL_NINE));

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

  /** Returns the sixth authored level featuring Tracker and fixed geometry. */
  public static LevelDefinition levelSix() {
    return LEVEL_SIX;
  }

  /** Returns the seventh authored level featuring Seeker and fixed geometry. */
  public static LevelDefinition levelSeven() {
    return LEVEL_SEVEN;
  }

  /** Returns the eighth authored level featuring Scout on a 6x6 grid. */
  public static LevelDefinition levelEight() {
    return LEVEL_EIGHT;
  }

  /** Returns the ninth authored level featuring Tracker on a 7x7 grid. */
  public static LevelDefinition levelNine() {
    return LEVEL_NINE;
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
