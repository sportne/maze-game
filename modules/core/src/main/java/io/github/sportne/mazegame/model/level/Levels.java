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
  /** 5x5 tutorial introducing Random and one remaining Wall. */
  private static final LevelDefinition LEVEL_ONE =
      new LevelDefinition(
          "milestone-1",
          "Level 1",
          GridSize.square(5),
          Duration.ofSeconds(20),
          Duration.ofSeconds(5),
          Duration.ofSeconds(10),
          Duration.ofMillis(250),
          finiteSupplies(10, 0),
          List.of(),
          presetWalls(
              p(0, 0), p(0, 4), p(1, 0), p(3, 0), p(3, 1), p(3, 3), p(4, 1), p(4, 3), p(4, 4)),
          List.of(
              new LevelSolver(
                  new GridPosition(4, 2),
                  new GridPosition(0, 2),
                  SolverBehavior.RANDOM,
                  OptionalLong.of(58L),
                  SolverAppearance.CLASSIC_MOUSE,
                  GoalType.CHEESE)));

  /** 5x5 tutorial introducing Scout and one remaining Wall. */
  private static final LevelDefinition LEVEL_TWO =
      new LevelDefinition(
          "milestone-2",
          "Level 2",
          GridSize.square(5),
          Duration.ofSeconds(20),
          Duration.ofSeconds(4),
          Duration.ofSeconds(6),
          Duration.ofMillis(250),
          finiteSupplies(10, 0),
          List.of(),
          presetWalls(
              p(0, 1), p(0, 3), p(1, 1), p(1, 4), p(2, 4), p(3, 0), p(3, 2), p(4, 0), p(4, 4)),
          List.of(
              new LevelSolver(
                  new GridPosition(4, 2),
                  new GridPosition(0, 2),
                  SolverBehavior.LEFT_PRIORITY,
                  OptionalLong.empty(),
                  SolverAppearance.SCOUT_SQUIRREL,
                  GoalType.ACORN)));

  /** 6x6 tutorial introducing Tracker and two remaining Walls. */
  private static final LevelDefinition LEVEL_THREE =
      new LevelDefinition(
          "milestone-3",
          "Level 3",
          GridSize.square(6),
          Duration.ofSeconds(25),
          Duration.ofSeconds(5),
          Duration.ofSeconds(7),
          Duration.ofMillis(250),
          finiteSupplies(15, 0),
          List.of(),
          presetWalls(
              p(0, 0), p(1, 0), p(1, 1), p(1, 3), p(1, 4), p(2, 3), p(3, 0), p(4, 3), p(4, 4),
              p(5, 1), p(5, 3), p(5, 4), p(5, 5)),
          List.of(
              new LevelSolver(
                  new GridPosition(5, 0),
                  new GridPosition(0, 5),
                  SolverBehavior.LEAST_VISITED,
                  OptionalLong.empty(),
                  SolverAppearance.TRACKER_RACCOON,
                  GoalType.TRASH_CAN)));

  /** 6x6 tutorial introducing Seeker and two remaining Walls. */
  private static final LevelDefinition LEVEL_FOUR =
      new LevelDefinition(
          "milestone-4",
          "Level 4",
          GridSize.square(6),
          Duration.ofSeconds(25),
          Duration.ofMillis(5500),
          Duration.ofSeconds(7),
          Duration.ofMillis(250),
          finiteSupplies(15, 0),
          List.of(),
          presetWalls(
              p(0, 0), p(1, 3), p(2, 5), p(3, 2), p(3, 4), p(3, 5), p(4, 1), p(4, 2), p(4, 3),
              p(4, 5), p(5, 1), p(5, 2), p(5, 5)),
          List.of(
              new LevelSolver(
                  new GridPosition(5, 0),
                  new GridPosition(0, 5),
                  SolverBehavior.LINE_OF_SIGHT,
                  OptionalLong.of(112L),
                  SolverAppearance.SEEKER_RABBIT,
                  GoalType.CARROT)));

  /** Open 7x7 level introducing Slow Floors alongside infinite Walls. */
  private static final LevelDefinition LEVEL_FIVE =
      new LevelDefinition(
          "milestone-5",
          "Level 5",
          GridSize.square(7),
          Duration.ofSeconds(30),
          Duration.ofMillis(5500),
          Duration.ofSeconds(7),
          Duration.ofMillis(250),
          List.of(
              PlaceableCellSupply.infinite(PlaceableCellType.WALL),
              PlaceableCellSupply.finite(PlaceableCellType.SLOW_FLOOR, 2),
              PlaceableCellSupply.finite(PlaceableCellType.ALTERNATING_GATE, 0)),
          List.of(
              new LevelSolver(
                  new GridPosition(6, 3),
                  new GridPosition(0, 3),
                  SolverBehavior.RANDOM,
                  OptionalLong.of(38L),
                  SolverAppearance.CLASSIC_MOUSE,
                  GoalType.CHEESE)));

  /** 7x7 level introducing fixed geometry while combining both cell types. */
  private static final LevelDefinition LEVEL_SIX =
      new LevelDefinition(
          "level-6",
          "Level 6",
          GridSize.square(7),
          Duration.ofSeconds(25),
          Duration.ofMillis(6500),
          Duration.ofSeconds(8),
          Duration.ofMillis(250),
          finiteSupplies(1, 3),
          fixedWalls(
              p(0, 1), p(0, 3), p(1, 0), p(1, 3), p(2, 0), p(2, 5), p(3, 1), p(3, 6), p(4, 3),
              p(5, 1), p(5, 4), p(6, 6)),
          List.of(
              new LevelSolver(
                  new GridPosition(6, 0),
                  new GridPosition(0, 6),
                  SolverBehavior.LEFT_PRIORITY,
                  OptionalLong.empty(),
                  SolverAppearance.SCOUT_SQUIRREL,
                  GoalType.ACORN)));

  /** 7x7 level revisiting movable preset geometry with Tracker. */
  private static final LevelDefinition LEVEL_SEVEN =
      new LevelDefinition(
          "level-7",
          "Level 7",
          GridSize.square(7),
          Duration.ofSeconds(25),
          Duration.ofSeconds(7),
          Duration.ofSeconds(10),
          Duration.ofMillis(250),
          finiteSupplies(15, 3),
          List.of(),
          presetWalls(
              p(0, 0), p(0, 1), p(1, 5), p(2, 2), p(2, 4), p(2, 6), p(3, 1), p(3, 2), p(3, 4),
              p(3, 6), p(4, 4), p(5, 1), p(6, 4), p(6, 5)),
          List.of(
              new LevelSolver(
                  new GridPosition(6, 0),
                  new GridPosition(0, 6),
                  SolverBehavior.LEAST_VISITED,
                  OptionalLong.empty(),
                  SolverAppearance.TRACKER_RACCOON,
                  GoalType.TRASH_CAN)));

  /** 8x8 level combining fixed and preset geometry around Seeker. */
  private static final LevelDefinition LEVEL_EIGHT =
      new LevelDefinition(
          "level-8",
          "Level 8",
          GridSize.square(8),
          Duration.ofSeconds(30),
          Duration.ofMillis(14500),
          Duration.ofSeconds(18),
          Duration.ofMillis(250),
          finiteSupplies(10, 4),
          fixedWalls(
              p(0, 2), p(1, 1), p(1, 4), p(1, 6), p(1, 7), p(2, 0), p(2, 1), p(2, 3), p(4, 0)),
          presetWalls(
              p(4, 4), p(5, 3), p(6, 2), p(6, 3), p(6, 5), p(6, 7), p(7, 1), p(7, 4), p(7, 7)),
          List.of(
              new LevelSolver(
                  new GridPosition(7, 0),
                  new GridPosition(0, 7),
                  SolverBehavior.LINE_OF_SIGHT,
                  OptionalLong.of(124L),
                  SolverAppearance.SEEKER_RABBIT,
                  GoalType.CARROT)));

  /** 9x9 level introducing an Alternating Gate on a larger seeded Random route. */
  private static final LevelDefinition LEVEL_NINE =
      new LevelDefinition(
          "level-9",
          "Level 9",
          GridSize.square(9),
          Duration.ofSeconds(30),
          Duration.ofMillis(17500),
          Duration.ofSeconds(19),
          Duration.ofMillis(250),
          finiteSupplies(1, 4, 1),
          fixedWalls(
              p(0, 2), p(0, 3), p(1, 2), p(1, 3), p(1, 4), p(1, 7), p(3, 3), p(4, 1), p(4, 3),
              p(4, 5), p(4, 7), p(4, 8), p(5, 7), p(6, 4), p(6, 5), p(6, 6), p(6, 8), p(7, 5),
              p(7, 6), p(7, 7), p(8, 6), p(8, 8)),
          List.of(
              new LevelSolver(
                  new GridPosition(8, 0),
                  new GridPosition(0, 8),
                  SolverBehavior.RANDOM,
                  OptionalLong.of(1387L),
                  SolverAppearance.CLASSIC_MOUSE,
                  GoalType.CHEESE)));

  /** 10x10 finale introducing concurrent Random and Scout solvers. */
  private static final LevelDefinition LEVEL_TEN =
      new LevelDefinition(
          "level-10",
          "Level 10",
          GridSize.square(10),
          Duration.ofSeconds(40),
          Duration.ofSeconds(11),
          Duration.ofMillis(13500),
          Duration.ofMillis(250),
          finiteSupplies(12, 6),
          fixedWalls(
              p(0, 3), p(0, 4), p(0, 8), p(1, 0), p(1, 3), p(2, 1), p(2, 7), p(3, 0), p(3, 5),
              p(3, 7), p(3, 9), p(4, 5), p(5, 0), p(5, 8), p(5, 9)),
          presetWalls(
              p(6, 0), p(6, 9), p(7, 2), p(7, 3), p(7, 7), p(8, 1), p(8, 2), p(8, 7), p(8, 9),
              p(9, 4)),
          List.of(
              new LevelSolver(
                  new GridPosition(9, 0),
                  new GridPosition(0, 9),
                  SolverBehavior.RANDOM,
                  OptionalLong.of(3306L),
                  SolverAppearance.CLASSIC_MOUSE,
                  GoalType.CHEESE),
              new LevelSolver(
                  new GridPosition(9, 9),
                  new GridPosition(0, 0),
                  SolverBehavior.LEFT_PRIORITY,
                  OptionalLong.empty(),
                  SolverAppearance.SCOUT_SQUIRREL,
                  GoalType.ACORN)));

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
              LEVEL_NINE,
              LEVEL_TEN));

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
   * Returns the second authored level featuring Scout.
   *
   * @return the second level definition
   */
  public static LevelDefinition levelTwo() {
    return LEVEL_TWO;
  }

  /**
   * Returns the third authored level featuring Tracker.
   *
   * @return the third level definition
   */
  public static LevelDefinition levelThree() {
    return LEVEL_THREE;
  }

  /**
   * Returns the fourth authored level featuring Seeker.
   *
   * @return the fourth level definition
   */
  public static LevelDefinition levelFour() {
    return LEVEL_FOUR;
  }

  /** Returns the fifth authored level introducing Slow Floors on an open grid. */
  public static LevelDefinition levelFive() {
    return LEVEL_FIVE;
  }

  /** Returns the sixth authored level combining Scout with fixed geometry. */
  public static LevelDefinition levelSix() {
    return LEVEL_SIX;
  }

  /** Returns the seventh authored level combining Tracker with preset geometry. */
  public static LevelDefinition levelSeven() {
    return LEVEL_SEVEN;
  }

  /** Returns the eighth authored level featuring Seeker on an 8x8 grid. */
  public static LevelDefinition levelEight() {
    return LEVEL_EIGHT;
  }

  /** Returns the ninth authored level featuring Random on a 9x9 grid. */
  public static LevelDefinition levelNine() {
    return LEVEL_NINE;
  }

  /** Returns the tenth authored level featuring two solvers on a 10x10 grid. */
  public static LevelDefinition levelTen() {
    return LEVEL_TEN;
  }

  /**
   * Returns the immutable authored-level catalog.
   *
   * @return production level catalog
   */
  public static LevelCatalog catalog() {
    return CATALOG;
  }

  private static List<PlaceableCellSupply> finiteSupplies(int walls, int slowFloors) {
    return finiteSupplies(walls, slowFloors, 0);
  }

  private static List<PlaceableCellSupply> finiteSupplies(
      int walls, int slowFloors, int alternatingGates) {
    return List.of(
        PlaceableCellSupply.finite(PlaceableCellType.WALL, walls),
        PlaceableCellSupply.finite(PlaceableCellType.SLOW_FLOOR, slowFloors),
        PlaceableCellSupply.finite(PlaceableCellType.ALTERNATING_GATE, alternatingGates));
  }

  private static List<FixedCell> fixedWalls(GridPosition... positions) {
    return java.util.Arrays.stream(positions)
        .map(position -> new FixedCell(position, FixedCellType.WALL))
        .toList();
  }

  private static List<PresetCell> presetWalls(GridPosition... positions) {
    return java.util.Arrays.stream(positions)
        .map(position -> new PresetCell(position, PlaceableCellType.WALL))
        .toList();
  }

  private static GridPosition p(int row, int column) {
    return new GridPosition(row, column);
  }
}
