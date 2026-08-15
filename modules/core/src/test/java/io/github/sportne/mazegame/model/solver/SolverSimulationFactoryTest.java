package io.github.sportne.mazegame.model.solver;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.level.Levels;
import io.github.sportne.mazegame.model.level.SolverBehavior;
import io.github.sportne.mazegame.model.maze.MazeState;
import org.junit.jupiter.api.Test;

final class SolverSimulationFactoryTest {
  @Test
  void createsRandomSimulationForRandomBehavior() {
    SolverSimulation simulation =
        SolverSimulationFactory.create(MazeState.empty(Levels.milestoneOne()));

    assertInstanceOf(RandomSolverSimulation.class, simulation);
  }

  @Test
  void createsScoutSimulationForLeftPriorityBehavior() {
    LevelDefinition level = withBehavior(Levels.milestoneOne(), SolverBehavior.LEFT_PRIORITY);

    SolverSimulation simulation = SolverSimulationFactory.create(MazeState.empty(level));

    assertInstanceOf(ScoutSolverSimulation.class, simulation);
  }

  @Test
  void requiresAMaze() {
    assertThrows(NullPointerException.class, () -> SolverSimulationFactory.create(null));
  }

  private static LevelDefinition withBehavior(
      LevelDefinition source, SolverBehavior solverBehavior) {
    return new LevelDefinition(
        source.id(),
        source.name(),
        source.gridSize(),
        source.solverStart(),
        source.goal(),
        source.buildTime(),
        source.targetSolveTime(),
        source.maximumSolveTime(),
        source.solverMoveInterval(),
        source.placeableCellSupplies(),
        solverBehavior,
        source.randomSeed());
  }
}
