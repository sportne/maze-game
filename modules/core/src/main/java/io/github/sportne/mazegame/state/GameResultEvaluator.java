package io.github.sportne.mazegame.state;

import io.github.sportne.mazegame.model.GamePhase;
import io.github.sportne.mazegame.model.LevelDefinition;
import io.github.sportne.mazegame.model.MouseRunResult;
import java.util.Objects;

/** Evaluates whether a completed mouse run satisfies the level target. */
public final class GameResultEvaluator {
  /** Prevents instantiation of this stateless evaluator. */
  private GameResultEvaluator() {}

  /**
   * Returns whether the current result passes the level.
   *
   * @param phase current game phase
   * @param mouseRunResult latest mouse run result, or null before a run starts
   * @param levelDefinition current level definition
   * @return true only in result phase when elapsed solve time exceeds the target
   */
  public static boolean passed(
      GamePhase phase, MouseRunResult mouseRunResult, LevelDefinition levelDefinition) {
    Objects.requireNonNull(phase, "phase");
    Objects.requireNonNull(levelDefinition, "levelDefinition");
    if (phase != GamePhase.RESULT || mouseRunResult == null) {
      return false;
    }
    return mouseRunResult.elapsedTime().compareTo(levelDefinition.targetSolveTime()) > 0;
  }
}
