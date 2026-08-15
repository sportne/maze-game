package io.github.sportne.mazegame.model.solver;

import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.level.LevelSolver;
import io.github.sportne.mazegame.model.maze.MazeState;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Deterministic random solver movement for a maze.
 *
 * <p>The simulation advances in fixed movement intervals from the level definition. At each move it
 * randomly chooses one currently open orthogonal neighbor, including the previous cell when that
 * cell is open, so backtracking is allowed. The random generator is seeded from the level so replay
 * can reproduce the same path from the same maze.
 */
public final class RandomSolverSimulation extends TimedSolverSimulation {
  /** Seeded source of deterministic movement choices. */
  private final Random random;

  /**
   * Starts a deterministic run for the given maze.
   *
   * @param mazeState maze to solve
   */
  public RandomSolverSimulation(MazeState mazeState) {
    this(mazeState, mazeState.levelDefinition().primarySolver());
  }

  RandomSolverSimulation(MazeState mazeState, LevelSolver solver) {
    super(mazeState, solver);
    random = new Random(solver.randomSeed());
  }

  /** Makes one random legal movement decision. */
  @Override
  void moveOnce() {
    List<GridPosition> moves = availableMoves();
    if (!moves.isEmpty()) {
      moveTo(moves.get(random.nextInt(moves.size())));
    }
  }

  /**
   * Collects all currently legal orthogonal moves.
   *
   * @return open neighboring cells
   */
  private List<GridPosition> availableMoves() {
    List<GridPosition> moves = new ArrayList<>();
    addIfOpen(moves, new GridPosition(position().row() - 1, position().column()));
    addIfOpen(moves, new GridPosition(position().row() + 1, position().column()));
    addIfOpen(moves, new GridPosition(position().row(), position().column() - 1));
    addIfOpen(moves, new GridPosition(position().row(), position().column() + 1));
    return moves;
  }

  /**
   * Adds a candidate move when it is inside the grid and not blocked by a wall.
   *
   * @param moves mutable list of legal moves being built
   * @param candidate candidate neighboring cell
   */
  private void addIfOpen(List<GridPosition> moves, GridPosition candidate) {
    if (isOpen(candidate)) {
      moves.add(candidate);
    }
  }
}
