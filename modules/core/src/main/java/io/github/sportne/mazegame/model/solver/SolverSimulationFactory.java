package io.github.sportne.mazegame.model.solver;

import io.github.sportne.mazegame.model.level.LevelSolver;
import io.github.sportne.mazegame.model.maze.MazeState;
import java.util.Objects;

/** Creates the concrete solver simulation selected by immutable level authoring data. */
public final class SolverSimulationFactory {
  private SolverSimulationFactory() {}

  /**
   * Creates a fresh simulation for a maze.
   *
   * @param mazeState immutable maze to solve
   * @return simulation selected by the maze's level definition
   */
  public static SolverSimulation create(MazeState mazeState) {
    Objects.requireNonNull(mazeState, "mazeState");
    return switch (mazeState.levelDefinition().solverBehavior()) {
      case RANDOM -> new RandomSolverSimulation(mazeState);
      case LEFT_PRIORITY -> new ScoutSolverSimulation(mazeState);
    };
  }

  /** Creates an independent simulation for one solver authored by a multi-solver level. */
  public static SolverSimulation create(MazeState mazeState, LevelSolver solver) {
    Objects.requireNonNull(mazeState, "mazeState");
    Objects.requireNonNull(solver, "solver");
    MazeState solverMaze =
        new MazeState(mazeState.levelDefinition().forSolver(solver), mazeState.placedCells());
    return create(solverMaze);
  }
}
