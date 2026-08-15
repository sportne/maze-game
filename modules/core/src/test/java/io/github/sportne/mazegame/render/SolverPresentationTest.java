package io.github.sportne.mazegame.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.level.GoalType;
import io.github.sportne.mazegame.model.level.LevelSolver;
import io.github.sportne.mazegame.model.level.Levels;
import io.github.sportne.mazegame.model.level.SolverAppearance;
import io.github.sportne.mazegame.model.level.SolverBehavior;
import java.util.OptionalLong;
import org.junit.jupiter.api.Test;

final class SolverPresentationTest {
  @Test
  void selectsPlayerFacingIdentityFromAppearanceAndGoalType() {
    SolverPresentation random = SolverPresentation.forSolver(Levels.levelOne().primarySolver());
    SolverPresentation scout = SolverPresentation.forSolver(Levels.levelThree().primarySolver());

    assertEquals("Solver", random.name());
    assertEquals("cheese", random.goalName());
    assertEquals("Level 1", random.levelTitle("Level 1"));
    assertEquals("Scout", scout.name());
    assertEquals("acorn", scout.goalName());
    assertEquals("Level 3 | Scout", scout.levelTitle("Level 3"));
    assertEquals("Level 3 | Scout", scout.statusTitle("Level 3", 300.0F));
    assertEquals("Scout", scout.statusTitle("Level 3", 299.0F));
  }

  @Test
  void presentationIsIndependentOfMovementBehavior() {
    LevelSolver randomScoutWithCheese =
        new LevelSolver(
            new GridPosition(2, 1),
            new GridPosition(0, 1),
            SolverBehavior.RANDOM,
            OptionalLong.of(7L),
            SolverAppearance.SCOUT_SQUIRREL,
            GoalType.CHEESE);

    SolverPresentation presentation = SolverPresentation.forSolver(randomScoutWithCheese);

    assertEquals("Scout", presentation.name());
    assertEquals("cheese", presentation.goalName());
  }

  @Test
  void rejectsMissingSolverAndLevelName() {
    assertThrows(NullPointerException.class, () -> SolverPresentation.forSolver(null));
    assertThrows(
        NullPointerException.class,
        () -> SolverPresentation.forSolver(Levels.levelOne().primarySolver()).levelTitle(null));
  }
}
