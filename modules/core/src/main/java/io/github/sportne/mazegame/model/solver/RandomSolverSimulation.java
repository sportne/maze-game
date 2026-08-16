package io.github.sportne.mazegame.model.solver;

import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.level.LevelSolver;
import io.github.sportne.mazegame.model.maze.MazeState;
import java.util.List;
import java.util.Objects;
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
  static final List<CardinalDirection> SEEDED_MOVE_ORDER =
      List.of(
          CardinalDirection.NORTH,
          CardinalDirection.SOUTH,
          CardinalDirection.WEST,
          CardinalDirection.EAST);

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
    random = new Random(solver.randomSeed().orElseThrow());
  }

  /** Makes one random legal movement decision. */
  @Override
  void moveOnce() {
    List<GridPosition> moves = openNeighbors(SEEDED_MOVE_ORDER);
    if (!moves.isEmpty()) {
      moveTo(moves.get(nextIndex(random, moves.size())));
    }
  }

  /**
   * Returns the JDK-compatible bounded value using only the portable unbounded random primitive.
   *
   * <p>TeaVM's bounded {@link Random#nextInt(int)} uses a different rejection algorithm from the
   * JDK. Its unbounded {@link Random#nextInt()} preserves the same 48-bit generator, so deriving
   * the high 31 bits here keeps authored seeds identical on JVM, JavaScript, and WebAssembly.
   */
  static int nextIndex(Random random, int bound) {
    Objects.requireNonNull(random, "random");
    if (bound <= 0) {
      throw new IllegalArgumentException("bound must be positive");
    }
    int bits = random.nextInt() >>> 1;
    if ((bound & -bound) == bound) {
      return (int) ((bound * (long) bits) >> 31);
    }
    int value = bits % bound;
    while (bits - value + (bound - 1) < 0) {
      bits = random.nextInt() >>> 1;
      value = bits % bound;
    }
    return value;
  }
}
