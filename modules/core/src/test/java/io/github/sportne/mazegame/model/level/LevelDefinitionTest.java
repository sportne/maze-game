package io.github.sportne.mazegame.model.level;

import static io.github.sportne.mazegame.TestLevels.singleSolverLevel;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.sportne.mazegame.model.cell.CellSupply;
import io.github.sportne.mazegame.model.cell.PlaceableCellSupply;
import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;
import org.junit.jupiter.api.Test;

final class LevelDefinitionTest {
  @Test
  void milestoneOneMatchesRoadmapValues() {
    LevelDefinition level = Levels.levelOne();

    assertEquals("milestone-1", level.id());
    assertEquals("Level 1", level.name());
    assertEquals(GridSize.square(5), level.gridSize());
    assertEquals(new GridPosition(4, 2), level.primarySolver().start());
    assertEquals(new GridPosition(0, 2), level.primarySolver().goal());
    assertEquals(Duration.ofSeconds(30), level.buildTime());
    assertEquals(Duration.ofSeconds(5), level.targetSolveTime());
    assertEquals(Duration.ofSeconds(10), level.maximumSolveTime());
    assertEquals(Duration.ofMillis(250), level.solverMoveInterval());
    assertEquals(SolverBehavior.RANDOM, level.primarySolver().behavior());
    assertEquals(1L, level.primarySolver().randomSeed().orElseThrow());
  }

  @Test
  void catalogUsesLevelNamesWhileKeepingStablePersistenceIds() {
    assertEquals(
        List.of("Level 1", "Level 2", "Level 3", "Level 4", "Level 5", "Level 6"),
        Levels.catalog().levels().stream().map(LevelDefinition::name).toList());
    assertEquals(
        List.of(
            "milestone-1", "milestone-2", "milestone-3", "milestone-4", "milestone-5", "level-6"),
        Levels.catalog().levels().stream().map(LevelDefinition::id).toList());
    assertEquals(
        List.of(
            List.of(), List.of(), List.of(), List.of(), List.of(), Levels.levelSix().fixedCells()),
        Levels.catalog().levels().stream().map(LevelDefinition::fixedCells).toList());
  }

  @Test
  void idMustNotBeBlank() {
    assertThrows(
        IllegalArgumentException.class,
        () -> level(" ", "Level", new GridPosition(4, 2), new GridPosition(0, 2)));
  }

  @Test
  void nameMustNotBeBlank() {
    assertThrows(
        IllegalArgumentException.class,
        () -> level("level", "", new GridPosition(4, 2), new GridPosition(0, 2)));
  }

  @Test
  void solverStartMustBeInsideGrid() {
    assertThrows(
        IllegalArgumentException.class,
        () -> level("level", "Level", new GridPosition(5, 2), new GridPosition(0, 2)));
  }

  @Test
  void goalMustBeInsideGrid() {
    assertThrows(
        IllegalArgumentException.class,
        () -> level("level", "Level", new GridPosition(4, 2), new GridPosition(-1, 2)));
  }

  @Test
  void solverStartAndGoalMustBeDifferent() {
    GridPosition position = new GridPosition(2, 2);

    assertThrows(IllegalArgumentException.class, () -> level("level", "Level", position, position));
  }

  @Test
  void durationsMustBePositive() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            singleSolverLevel(
                "level",
                "Level",
                GridSize.square(5),
                new GridPosition(4, 2),
                new GridPosition(0, 2),
                Duration.ZERO,
                Duration.ofSeconds(5),
                Duration.ofSeconds(10),
                Duration.ofMillis(250),
                PlaceableCellSupply.unlimitedWallsOnly(),
                SolverBehavior.RANDOM,
                1L));
  }

  @Test
  void targetSolveTimeMustNotExceedMaximumSolveTime() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            singleSolverLevel(
                "level",
                "Level",
                GridSize.square(5),
                new GridPosition(4, 2),
                new GridPosition(0, 2),
                Duration.ofSeconds(30),
                Duration.ofSeconds(11),
                Duration.ofSeconds(10),
                Duration.ofMillis(250),
                PlaceableCellSupply.unlimitedWallsOnly(),
                SolverBehavior.RANDOM,
                1L));
  }

  @Test
  void solverBehaviorParticipatesInEquality() {
    LevelDefinition random = levelWithBehavior(SolverBehavior.RANDOM);
    LevelDefinition scout = levelWithBehavior(SolverBehavior.LEFT_PRIORITY);

    assertEquals(SolverBehavior.RANDOM, random.primarySolver().behavior());
    assertEquals(SolverBehavior.LEFT_PRIORITY, scout.primarySolver().behavior());
    assertNotEquals(random, scout);
  }

  @Test
  void releasedLevelsExplicitlyAuthorInfiniteWallsAndZeroSlowFloors() {
    for (LevelDefinition level :
        List.of(Levels.levelOne(), Levels.levelTwo(), Levels.levelThree())) {
      assertEquals(CellSupply.infinite(), level.supplyFor(PlaceableCellType.WALL));
      assertEquals(CellSupply.finite(0), level.supplyFor(PlaceableCellType.SLOW_FLOOR));
      assertEquals(PlaceableCellSupply.unlimitedWallsOnly(), level.placeableCellSupplies());
      assertEquals(List.of(PlaceableCellType.WALL), level.initiallyAvailableCellTypes());
    }
  }

  @Test
  void initiallyAvailableCellTypesPreserveAuthoredOrderAndOmitZeroSupplies() {
    LevelDefinition level =
        levelWithSupplies(
            List.of(
                PlaceableCellSupply.finite(PlaceableCellType.SLOW_FLOOR, 2),
                PlaceableCellSupply.finite(PlaceableCellType.WALL, 0)));

    assertEquals(List.of(PlaceableCellType.SLOW_FLOOR), level.initiallyAvailableCellTypes());
  }

  @Test
  void suppliesRequireExactlyOneEntryForEverySupportedType() {
    assertThrows(NullPointerException.class, () -> levelWithSupplies(null));
    assertThrows(IllegalArgumentException.class, () -> levelWithSupplies(List.of()));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            levelWithSupplies(
                List.of(
                    PlaceableCellSupply.infinite(PlaceableCellType.WALL),
                    PlaceableCellSupply.finite(PlaceableCellType.WALL, 2),
                    PlaceableCellSupply.finite(PlaceableCellType.SLOW_FLOOR, 1))));
    assertThrows(
        NullPointerException.class, () -> new PlaceableCellSupply(null, CellSupply.finite(1)));
    assertThrows(
        NullPointerException.class, () -> new PlaceableCellSupply(PlaceableCellType.WALL, null));
    assertThrows(
        IllegalArgumentException.class,
        () -> PlaceableCellSupply.finite(PlaceableCellType.WALL, -1));
  }

  @Test
  void authoredSuppliesAreOrderedDefensivelyCopiedAndParticipateInEquality() {
    List<PlaceableCellSupply> mutable = new ArrayList<>(PlaceableCellSupply.unlimitedWallsOnly());
    LevelDefinition released = levelWithSupplies(mutable);
    mutable.clear();
    LevelDefinition finite =
        levelWithSupplies(
            List.of(
                PlaceableCellSupply.finite(PlaceableCellType.WALL, 3),
                PlaceableCellSupply.finite(PlaceableCellType.SLOW_FLOOR, 2)));

    assertEquals(PlaceableCellSupply.unlimitedWallsOnly(), released.placeableCellSupplies());
    assertNotEquals(released, finite);
    assertThrows(
        UnsupportedOperationException.class,
        () ->
            released
                .placeableCellSupplies()
                .add(PlaceableCellSupply.infinite(PlaceableCellType.WALL)));
  }

  @Test
  void levelSolverRequiresValidMovementAndPresentationData() {
    GridPosition start = new GridPosition(4, 2);
    GridPosition goal = new GridPosition(0, 2);

    assertThrows(
        NullPointerException.class,
        () ->
            new LevelSolver(
                null,
                goal,
                SolverBehavior.RANDOM,
                OptionalLong.of(1L),
                SolverAppearance.CLASSIC_MOUSE,
                GoalType.CHEESE));
    assertThrows(
        NullPointerException.class,
        () ->
            new LevelSolver(
                start,
                null,
                SolverBehavior.RANDOM,
                OptionalLong.of(1L),
                SolverAppearance.CLASSIC_MOUSE,
                GoalType.CHEESE));
    assertThrows(
        NullPointerException.class,
        () ->
            new LevelSolver(
                start,
                goal,
                null,
                OptionalLong.of(1L),
                SolverAppearance.CLASSIC_MOUSE,
                GoalType.CHEESE));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new LevelSolver(
                start,
                start,
                SolverBehavior.RANDOM,
                OptionalLong.of(1L),
                SolverAppearance.CLASSIC_MOUSE,
                GoalType.CHEESE));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new LevelSolver(
                start,
                goal,
                SolverBehavior.RANDOM,
                OptionalLong.empty(),
                SolverAppearance.CLASSIC_MOUSE,
                GoalType.CHEESE));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new LevelSolver(
                start,
                goal,
                SolverBehavior.LEFT_PRIORITY,
                OptionalLong.of(1L),
                SolverAppearance.SCOUT_SQUIRREL,
                GoalType.ACORN));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new LevelSolver(
                start,
                goal,
                SolverBehavior.LEAST_VISITED,
                OptionalLong.of(1L),
                SolverAppearance.TRACKER_RACCOON,
                GoalType.TRASH_CAN));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new LevelSolver(
                start,
                goal,
                SolverBehavior.LINE_OF_SIGHT,
                OptionalLong.empty(),
                SolverAppearance.SEEKER_RABBIT,
                GoalType.CARROT));
    assertEquals(
        SolverBehavior.LEAST_VISITED,
        new LevelSolver(
                start,
                goal,
                SolverBehavior.LEAST_VISITED,
                OptionalLong.empty(),
                SolverAppearance.TRACKER_RACCOON,
                GoalType.TRASH_CAN)
            .behavior());
    assertEquals(
        23L,
        new LevelSolver(
                start,
                goal,
                SolverBehavior.LINE_OF_SIGHT,
                OptionalLong.of(23L),
                SolverAppearance.SEEKER_RABBIT,
                GoalType.CARROT)
            .randomSeed()
            .orElseThrow());
    assertThrows(
        NullPointerException.class,
        () ->
            new LevelSolver(
                start,
                goal,
                SolverBehavior.RANDOM,
                null,
                SolverAppearance.CLASSIC_MOUSE,
                GoalType.CHEESE));
    assertThrows(
        NullPointerException.class,
        () ->
            new LevelSolver(
                start, goal, SolverBehavior.RANDOM, OptionalLong.of(1L), null, GoalType.CHEESE));
    assertThrows(
        NullPointerException.class,
        () ->
            new LevelSolver(
                start,
                goal,
                SolverBehavior.RANDOM,
                OptionalLong.of(1L),
                SolverAppearance.CLASSIC_MOUSE,
                null));
  }

  @Test
  void multiSolverDefinitionsValidateAndDefensivelyCopyEveryProtectedPosition() {
    LevelSolver primary =
        new LevelSolver(
            new GridPosition(4, 2),
            new GridPosition(0, 2),
            SolverBehavior.RANDOM,
            OptionalLong.of(1L),
            SolverAppearance.CLASSIC_MOUSE,
            GoalType.CHEESE);
    LevelSolver secondary =
        new LevelSolver(
            new GridPosition(3, 3),
            new GridPosition(1, 1),
            SolverBehavior.LEFT_PRIORITY,
            OptionalLong.empty(),
            SolverAppearance.SCOUT_SQUIRREL,
            GoalType.ACORN);
    List<LevelSolver> mutable = new ArrayList<>(List.of(primary, secondary));
    LevelDefinition level = multiSolverLevel(mutable);
    mutable.clear();

    assertEquals(List.of(primary, secondary), level.solvers());
    assertEquals(primary, level.primarySolver());
    assertEquals(primary.start(), level.primarySolver().start());
    assertEquals(primary.goal(), level.primarySolver().goal());
    assertEquals(primary.behavior(), level.primarySolver().behavior());
    assertEquals(
        primary.randomSeed(), OptionalLong.of(level.primarySolver().randomSeed().orElseThrow()));
    assertThrows(NullPointerException.class, () -> multiSolverLevel(null));
    assertThrows(IllegalArgumentException.class, () -> multiSolverLevel(List.of()));
    assertThrows(
        NullPointerException.class, () -> multiSolverLevel(java.util.Arrays.asList(primary, null)));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            multiSolverLevel(
                List.of(
                    primary,
                    new LevelSolver(
                        primary.start(),
                        new GridPosition(1, 1),
                        SolverBehavior.LEFT_PRIORITY,
                        OptionalLong.empty(),
                        SolverAppearance.SCOUT_SQUIRREL,
                        GoalType.ACORN))));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            multiSolverLevel(
                List.of(
                    primary,
                    new LevelSolver(
                        new GridPosition(3, 3),
                        primary.goal(),
                        SolverBehavior.LEFT_PRIORITY,
                        OptionalLong.empty(),
                        SolverAppearance.SCOUT_SQUIRREL,
                        GoalType.ACORN))));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            multiSolverLevel(
                List.of(
                    primary,
                    new LevelSolver(
                        new GridPosition(5, 0),
                        new GridPosition(1, 1),
                        SolverBehavior.LEFT_PRIORITY,
                        OptionalLong.empty(),
                        SolverAppearance.SCOUT_SQUIRREL,
                        GoalType.ACORN))));
  }

  private static LevelDefinition level(
      String id, String name, GridPosition solverStart, GridPosition goal) {
    return singleSolverLevel(
        id,
        name,
        GridSize.square(5),
        solverStart,
        goal,
        Duration.ofSeconds(30),
        Duration.ofSeconds(5),
        Duration.ofSeconds(10),
        Duration.ofMillis(250),
        PlaceableCellSupply.unlimitedWallsOnly(),
        SolverBehavior.RANDOM,
        1L);
  }

  private static LevelDefinition levelWithBehavior(SolverBehavior solverBehavior) {
    LevelDefinition source = Levels.levelOne();
    return singleSolverLevel(
        source.id(),
        source.name(),
        source.gridSize(),
        source.primarySolver().start(),
        source.primarySolver().goal(),
        source.buildTime(),
        source.targetSolveTime(),
        source.maximumSolveTime(),
        source.solverMoveInterval(),
        source.placeableCellSupplies(),
        solverBehavior,
        source.primarySolver().randomSeed().orElseThrow());
  }

  private static LevelDefinition levelWithSupplies(List<PlaceableCellSupply> supplies) {
    LevelDefinition source = Levels.levelOne();
    return singleSolverLevel(
        "supply-test",
        "Supply Test",
        source.gridSize(),
        source.primarySolver().start(),
        source.primarySolver().goal(),
        source.buildTime(),
        source.targetSolveTime(),
        source.maximumSolveTime(),
        source.solverMoveInterval(),
        supplies,
        source.primarySolver().behavior(),
        source.primarySolver().randomSeed().orElseThrow());
  }

  private static LevelDefinition multiSolverLevel(List<LevelSolver> solvers) {
    LevelDefinition source = Levels.levelOne();
    return new LevelDefinition(
        "multi-solver-test",
        "Multi Solver Test",
        source.gridSize(),
        source.buildTime(),
        source.targetSolveTime(),
        source.maximumSolveTime(),
        source.solverMoveInterval(),
        source.placeableCellSupplies(),
        solvers);
  }
}
