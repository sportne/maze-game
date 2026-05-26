package io.github.sportne.mazegame.model.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
                1L));
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
        1L);
  }
}
