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
    SolverPresentation tracker =
        SolverPresentation.forSolver(
            new LevelSolver(
                new GridPosition(2, 1),
                new GridPosition(0, 1),
                SolverBehavior.LEAST_VISITED,
                OptionalLong.empty(),
                SolverAppearance.TRACKER_RACCOON,
                GoalType.TRASH_CAN));
    SolverPresentation seeker =
        SolverPresentation.forSolver(
            new LevelSolver(
                new GridPosition(2, 1),
                new GridPosition(0, 1),
                SolverBehavior.LINE_OF_SIGHT,
                OptionalLong.of(17L),
                SolverAppearance.SEEKER_RABBIT,
                GoalType.CARROT));

    assertEquals("Solver", random.name());
    assertEquals("cheese", random.goalName());
    assertEquals("Level 1", random.levelTitle("Level 1"));
    assertEquals("Scout", scout.name());
    assertEquals("acorn", scout.goalName());
    assertEquals("Level 3 | Scout", scout.levelTitle("Level 3"));
    assertEquals("Level 3 | Scout", scout.statusTitle("Level 3", 300.0F));
    assertEquals("Scout", scout.statusTitle("Level 3", 299.0F));
    assertEquals("Tracker", tracker.name());
    assertEquals("trash can", tracker.goalName());
    assertEquals("Tracker", tracker.statusTitle("Tracker Fixture", 299.0F));
    assertEquals("Seeker", seeker.name());
    assertEquals("carrot", seeker.goalName());
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
