package io.github.sportne.mazegame.state;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.level.Levels;
import io.github.sportne.mazegame.model.mouse.MouseRunResult;
import io.github.sportne.mazegame.model.mouse.MouseRunStatus;
import java.time.Duration;
import org.junit.jupiter.api.Test;

final class GameResultEvaluatorTest {
  private static final LevelDefinition LEVEL = Levels.milestoneOne();

  @Test
  void passRequiresResultPhaseAndElapsedTimeAboveTarget() {
    MouseRunResult passingResult =
        new MouseRunResult(LEVEL.cheese(), Duration.ofSeconds(6L), 12, MouseRunStatus.TIMED_OUT);

    assertTrue(GameResultEvaluator.passed(GamePhase.RESULT, passingResult, LEVEL));
    assertFalse(GameResultEvaluator.passed(GamePhase.MOUSE_RUNNING, passingResult, LEVEL));
  }

  @Test
  void equalOrFasterThanTargetDoesNotPass() {
    MouseRunResult exactTarget =
        new MouseRunResult(
            LEVEL.cheese(), LEVEL.targetSolveTime(), 8, MouseRunStatus.REACHED_CHEESE);

    assertFalse(GameResultEvaluator.passed(GamePhase.RESULT, exactTarget, LEVEL));
    assertFalse(GameResultEvaluator.passed(GamePhase.RESULT, null, LEVEL));
  }

  @Test
  void requiresPhaseAndLevelDefinition() {
    MouseRunResult result =
        new MouseRunResult(LEVEL.cheese(), Duration.ofSeconds(6L), 12, MouseRunStatus.TIMED_OUT);

    assertThrows(NullPointerException.class, () -> GameResultEvaluator.passed(null, result, LEVEL));
    assertThrows(
        NullPointerException.class,
        () -> GameResultEvaluator.passed(GamePhase.RESULT, result, null));
  }
}
