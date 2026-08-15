package io.github.sportne.mazegame.state;

import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.solver.SolverRunResult;
import java.util.List;
import java.util.Objects;

/** Evaluates whether a completed solver run satisfies the level target. */
public final class GameResultEvaluator {
  /** Prevents instantiation of this stateless evaluator. */
  private GameResultEvaluator() {}

  /**
   * Returns whether the current result passes the level.
   *
   * @param phase current game phase
   * @param solverRunResult latest solver run result, or null before a run starts
   * @param levelDefinition current level definition
   * @return true only in result phase when elapsed solve time exceeds the target
   */
  public static boolean passed(
      GamePhase phase, SolverRunResult solverRunResult, LevelDefinition levelDefinition) {
    Objects.requireNonNull(phase, "phase");
    Objects.requireNonNull(levelDefinition, "levelDefinition");
    if (phase != GamePhase.RESULT || solverRunResult == null) {
      return false;
    }
    return solverRunResult.elapsedTime().compareTo(levelDefinition.targetSolveTime()) > 0;
  }

  /** Returns whether every solver in a completed multi-solver run exceeded the target. */
  public static boolean passedAll(
      GamePhase phase, List<SolverRunResult> solverRunResults, LevelDefinition levelDefinition) {
    Objects.requireNonNull(phase, "phase");
    Objects.requireNonNull(solverRunResults, "solverRunResults");
    Objects.requireNonNull(levelDefinition, "levelDefinition");
    if (phase != GamePhase.RESULT
        || solverRunResults.size() != levelDefinition.solvers().size()
        || solverRunResults.isEmpty()) {
      return false;
    }
    return solverRunResults.stream()
        .allMatch(result -> result.elapsedTime().compareTo(levelDefinition.targetSolveTime()) > 0);
  }
}
