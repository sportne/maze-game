package io.github.sportne.mazegame.state;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.level.Levels;
import io.github.sportne.mazegame.model.solver.SolverRunResult;
import io.github.sportne.mazegame.model.solver.SolverRunStatus;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

final class GameResultEvaluatorTest {
  private static final LevelDefinition LEVEL = Levels.levelOne();

  @Test
  void passRequiresResultPhaseAndElapsedTimeAboveTarget() {
    SolverRunResult passingResult =
        new SolverRunResult(
            LEVEL.primarySolver().goal(), Duration.ofSeconds(6L), 12, SolverRunStatus.TIMED_OUT);

    assertTrue(GameResultEvaluator.passed(GamePhase.RESULT, passingResult, LEVEL));
    assertFalse(GameResultEvaluator.passed(GamePhase.SOLVER_RUNNING, passingResult, LEVEL));
  }

  @Test
  void equalOrFasterThanTargetDoesNotPass() {
    SolverRunResult exactTarget =
        new SolverRunResult(
            LEVEL.primarySolver().goal(), LEVEL.targetSolveTime(), 8, SolverRunStatus.REACHED_GOAL);

    assertFalse(GameResultEvaluator.passed(GamePhase.RESULT, exactTarget, LEVEL));
    assertFalse(GameResultEvaluator.passed(GamePhase.RESULT, null, LEVEL));
  }

  @Test
  void requiresPhaseAndLevelDefinition() {
    SolverRunResult result =
        new SolverRunResult(
            LEVEL.primarySolver().goal(), Duration.ofSeconds(6L), 12, SolverRunStatus.TIMED_OUT);

    assertThrows(NullPointerException.class, () -> GameResultEvaluator.passed(null, result, LEVEL));
    assertThrows(
        NullPointerException.class,
        () -> GameResultEvaluator.passed(GamePhase.RESULT, result, null));
  }

  @Test
  void multiSolverPassRequiresTheFirstGoalOrAllTimeoutsAfterTheTarget() {
    LevelDefinition level = Levels.levelFive();
    SolverRunResult passing =
        new SolverRunResult(
            level.primarySolver().goal(), Duration.ofSeconds(6), 20, SolverRunStatus.REACHED_GOAL);
    SolverRunResult exact =
        new SolverRunResult(
            level.solvers().get(1).goal(),
            level.targetSolveTime(),
            18,
            SolverRunStatus.REACHED_GOAL);
    SolverRunResult runningPastTarget =
        new SolverRunResult(
            level.solvers().get(1).start(), Duration.ofSeconds(6), 18, SolverRunStatus.RUNNING);
    SolverRunResult timedOut =
        new SolverRunResult(
            level.solvers().get(1).start(),
            level.maximumSolveTime(),
            30,
            SolverRunStatus.TIMED_OUT);

    assertTrue(GameResultEvaluator.passedAll(GamePhase.RESULT, List.of(passing, passing), level));
    assertTrue(
        GameResultEvaluator.passedAll(
            GamePhase.RESULT, List.of(passing, runningPastTarget), level));
    assertTrue(GameResultEvaluator.passedAll(GamePhase.RESULT, List.of(timedOut, timedOut), level));
    assertFalse(GameResultEvaluator.passedAll(GamePhase.RESULT, List.of(passing, exact), level));
    assertFalse(
        GameResultEvaluator.passedAll(
            GamePhase.RESULT, List.of(runningPastTarget, runningPastTarget), level));
    assertFalse(
        GameResultEvaluator.passedAll(GamePhase.SOLVER_RUNNING, List.of(passing, passing), level));
    assertFalse(GameResultEvaluator.passedAll(GamePhase.RESULT, List.of(passing), level));
    assertFalse(GameResultEvaluator.passedAll(GamePhase.RESULT, List.of(), level));
    assertThrows(
        NullPointerException.class,
        () -> GameResultEvaluator.passedAll(GamePhase.RESULT, null, level));
    assertThrows(
        NullPointerException.class,
        () -> GameResultEvaluator.passedAll(null, List.of(passing, passing), level));
    assertThrows(
        NullPointerException.class,
        () -> GameResultEvaluator.passedAll(GamePhase.RESULT, List.of(passing, passing), null));
  }
}
