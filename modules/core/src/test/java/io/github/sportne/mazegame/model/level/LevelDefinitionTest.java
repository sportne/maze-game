package io.github.sportne.mazegame.model.level;

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
import org.junit.jupiter.api.Test;

final class LevelDefinitionTest {
  @Test
  void milestoneOneMatchesRoadmapValues() {
    LevelDefinition level = Levels.milestoneOne();

    assertEquals("milestone-1", level.id());
    assertEquals("Level 1", level.name());
    assertEquals(GridSize.square(5), level.gridSize());
    assertEquals(new GridPosition(4, 2), level.mouseStart());
    assertEquals(new GridPosition(0, 2), level.cheese());
    assertEquals(Duration.ofSeconds(30), level.buildTime());
    assertEquals(Duration.ofSeconds(5), level.targetSolveTime());
    assertEquals(Duration.ofSeconds(10), level.maximumSolveTime());
    assertEquals(Duration.ofMillis(250), level.mouseMoveInterval());
    assertEquals(MouseBehavior.RANDOM, level.mouseBehavior());
    assertEquals(1L, level.randomSeed());
  }

  @Test
  void catalogUsesLevelNamesWhileKeepingStablePersistenceIds() {
    assertEquals(
        List.of("Level 1", "Level 2", "Level 3", "Level 4", "Level 5"),
        Levels.catalog().levels().stream().map(LevelDefinition::name).toList());
    assertEquals(
        List.of("milestone-1", "milestone-2", "milestone-3", "milestone-4", "milestone-5"),
        Levels.catalog().levels().stream().map(LevelDefinition::id).toList());
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
  void mouseStartMustBeInsideGrid() {
    assertThrows(
        IllegalArgumentException.class,
        () -> level("level", "Level", new GridPosition(5, 2), new GridPosition(0, 2)));
  }

  @Test
  void cheeseMustBeInsideGrid() {
    assertThrows(
        IllegalArgumentException.class,
        () -> level("level", "Level", new GridPosition(4, 2), new GridPosition(-1, 2)));
  }

  @Test
  void mouseStartAndCheeseMustBeDifferent() {
    GridPosition position = new GridPosition(2, 2);

    assertThrows(IllegalArgumentException.class, () -> level("level", "Level", position, position));
  }

  @Test
  void durationsMustBePositive() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new LevelDefinition(
                "level",
                "Level",
                GridSize.square(5),
                new GridPosition(4, 2),
                new GridPosition(0, 2),
                Duration.ZERO,
                Duration.ofSeconds(5),
                Duration.ofSeconds(10),
                Duration.ofMillis(250),
                PlaceableCellSupply.releasedDefaults(),
                MouseBehavior.RANDOM,
                1L));
  }

  @Test
  void targetSolveTimeMustNotExceedMaximumSolveTime() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new LevelDefinition(
                "level",
                "Level",
                GridSize.square(5),
                new GridPosition(4, 2),
                new GridPosition(0, 2),
                Duration.ofSeconds(30),
                Duration.ofSeconds(11),
                Duration.ofSeconds(10),
                Duration.ofMillis(250),
                PlaceableCellSupply.releasedDefaults(),
                MouseBehavior.RANDOM,
                1L));
  }

  @Test
  void mouseBehaviorIsRequiredAndParticipatesInEquality() {
    LevelDefinition random = levelWithBehavior(MouseBehavior.RANDOM);
    LevelDefinition scout = levelWithBehavior(MouseBehavior.LEFT_PRIORITY);

    assertEquals(MouseBehavior.RANDOM, random.mouseBehavior());
    assertEquals(MouseBehavior.LEFT_PRIORITY, scout.mouseBehavior());
    assertNotEquals(random, scout);
    assertThrows(NullPointerException.class, () -> levelWithBehavior(null));
  }

  @Test
  void releasedLevelsExplicitlyAuthorInfiniteWallsAndZeroSlowFloors() {
    for (LevelDefinition level :
        List.of(Levels.milestoneOne(), Levels.milestoneTwo(), Levels.milestoneThree())) {
      assertEquals(CellSupply.infinite(), level.supplyFor(PlaceableCellType.WALL));
      assertEquals(CellSupply.finite(0), level.supplyFor(PlaceableCellType.SLOW_FLOOR));
      assertEquals(PlaceableCellSupply.releasedDefaults(), level.placeableCellSupplies());
    }
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
    List<PlaceableCellSupply> mutable = new ArrayList<>(PlaceableCellSupply.releasedDefaults());
    LevelDefinition released = levelWithSupplies(mutable);
    mutable.clear();
    LevelDefinition finite =
        levelWithSupplies(
            List.of(
                PlaceableCellSupply.finite(PlaceableCellType.WALL, 3),
                PlaceableCellSupply.finite(PlaceableCellType.SLOW_FLOOR, 2)));

    assertEquals(PlaceableCellSupply.releasedDefaults(), released.placeableCellSupplies());
    assertNotEquals(released, finite);
    assertThrows(
        UnsupportedOperationException.class,
        () ->
            released
                .placeableCellSupplies()
                .add(PlaceableCellSupply.infinite(PlaceableCellType.WALL)));
  }

  @Test
  void levelMouseRequiresDistinctPositionsAndBehavior() {
    GridPosition start = new GridPosition(4, 2);
    GridPosition goal = new GridPosition(0, 2);

    assertThrows(
        NullPointerException.class, () -> new LevelMouse(null, goal, MouseBehavior.RANDOM, 1L));
    assertThrows(
        NullPointerException.class, () -> new LevelMouse(start, null, MouseBehavior.RANDOM, 1L));
    assertThrows(NullPointerException.class, () -> new LevelMouse(start, goal, null, 1L));
    assertThrows(
        IllegalArgumentException.class,
        () -> new LevelMouse(start, start, MouseBehavior.RANDOM, 1L));
  }

  @Test
  void multiMouseDefinitionsValidateAndDefensivelyCopyEveryProtectedPosition() {
    LevelMouse primary =
        new LevelMouse(new GridPosition(4, 2), new GridPosition(0, 2), MouseBehavior.RANDOM, 1L);
    LevelMouse secondary =
        new LevelMouse(
            new GridPosition(3, 3), new GridPosition(1, 1), MouseBehavior.LEFT_PRIORITY, 2L);
    List<LevelMouse> mutable = new ArrayList<>(List.of(primary, secondary));
    LevelDefinition level = multiMouseLevel(mutable);
    mutable.clear();

    assertEquals(List.of(primary, secondary), level.mice());
    assertEquals(secondary.start(), level.forMouse(secondary).mouseStart());
    assertThrows(NullPointerException.class, () -> assertEquals(level, level.forMouse(null)));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            assertEquals(
                level,
                level.forMouse(
                    new LevelMouse(
                        new GridPosition(2, 1),
                        new GridPosition(2, 2),
                        MouseBehavior.RANDOM,
                        3L))));
    assertThrows(NullPointerException.class, () -> multiMouseLevel(null));
    assertThrows(IllegalArgumentException.class, () -> multiMouseLevel(List.of()));
    assertThrows(
        NullPointerException.class, () -> multiMouseLevel(java.util.Arrays.asList(primary, null)));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            multiMouseLevel(
                List.of(
                    primary,
                    new LevelMouse(
                        primary.start(),
                        new GridPosition(1, 1),
                        MouseBehavior.LEFT_PRIORITY,
                        2L))));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            multiMouseLevel(
                List.of(
                    primary,
                    new LevelMouse(
                        new GridPosition(3, 3), primary.goal(), MouseBehavior.LEFT_PRIORITY, 2L))));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            multiMouseLevel(
                List.of(
                    primary,
                    new LevelMouse(
                        new GridPosition(5, 0),
                        new GridPosition(1, 1),
                        MouseBehavior.LEFT_PRIORITY,
                        2L))));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            multiMouseLevel(
                List.of(
                    new LevelMouse(
                        primary.start(), primary.goal(), MouseBehavior.LEFT_PRIORITY, 2L))));
  }

  private static LevelDefinition level(
      String id, String name, GridPosition mouseStart, GridPosition cheese) {
    return new LevelDefinition(
        id,
        name,
        GridSize.square(5),
        mouseStart,
        cheese,
        Duration.ofSeconds(30),
        Duration.ofSeconds(5),
        Duration.ofSeconds(10),
        Duration.ofMillis(250),
        PlaceableCellSupply.releasedDefaults(),
        MouseBehavior.RANDOM,
        1L);
  }

  private static LevelDefinition levelWithBehavior(MouseBehavior mouseBehavior) {
    LevelDefinition source = Levels.milestoneOne();
    return new LevelDefinition(
        source.id(),
        source.name(),
        source.gridSize(),
        source.mouseStart(),
        source.cheese(),
        source.buildTime(),
        source.targetSolveTime(),
        source.maximumSolveTime(),
        source.mouseMoveInterval(),
        source.placeableCellSupplies(),
        mouseBehavior,
        source.randomSeed());
  }

  private static LevelDefinition levelWithSupplies(List<PlaceableCellSupply> supplies) {
    LevelDefinition source = Levels.milestoneOne();
    return new LevelDefinition(
        "supply-test",
        "Supply Test",
        source.gridSize(),
        source.mouseStart(),
        source.cheese(),
        source.buildTime(),
        source.targetSolveTime(),
        source.maximumSolveTime(),
        source.mouseMoveInterval(),
        supplies,
        source.mouseBehavior(),
        source.randomSeed());
  }

  private static LevelDefinition multiMouseLevel(List<LevelMouse> mice) {
    LevelDefinition source = Levels.milestoneOne();
    return new LevelDefinition(
        "multi-mouse-test",
        "Multi Mouse Test",
        source.gridSize(),
        source.mouseStart(),
        source.cheese(),
        source.buildTime(),
        source.targetSolveTime(),
        source.maximumSolveTime(),
        source.mouseMoveInterval(),
        source.placeableCellSupplies(),
        source.mouseBehavior(),
        source.randomSeed(),
        mice);
  }
}
