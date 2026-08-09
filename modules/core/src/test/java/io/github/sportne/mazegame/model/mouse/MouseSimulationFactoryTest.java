package io.github.sportne.mazegame.model.mouse;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.level.Levels;
import io.github.sportne.mazegame.model.level.MouseBehavior;
import io.github.sportne.mazegame.model.maze.MazeState;
import org.junit.jupiter.api.Test;

final class MouseSimulationFactoryTest {
  @Test
  void createsRandomSimulationForRandomBehavior() {
    MouseSimulation simulation =
        MouseSimulationFactory.create(MazeState.empty(Levels.milestoneOne()));

    assertInstanceOf(RandomMouseSimulation.class, simulation);
  }

  @Test
  void createsScoutSimulationForLeftPriorityBehavior() {
    LevelDefinition level = withBehavior(Levels.milestoneOne(), MouseBehavior.LEFT_PRIORITY);

    MouseSimulation simulation = MouseSimulationFactory.create(MazeState.empty(level));

    assertInstanceOf(ScoutMouseSimulation.class, simulation);
  }

  @Test
  void requiresAMaze() {
    assertThrows(NullPointerException.class, () -> MouseSimulationFactory.create(null));
  }

  private static LevelDefinition withBehavior(LevelDefinition source, MouseBehavior mouseBehavior) {
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
}
