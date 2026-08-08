package io.github.sportne.mazegame.model.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import java.time.Duration;
import org.junit.jupiter.api.Test;

final class LevelDefinitionTest {
  @Test
  void milestoneOneMatchesRoadmapValues() {
    LevelDefinition level = Levels.milestoneOne();

    assertEquals("milestone-1", level.id());
    assertEquals("Milestone 1", level.name());
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
        mouseBehavior,
        source.randomSeed());
  }
}
