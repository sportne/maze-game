package io.github.sportne.mazegame.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.sportne.mazegame.model.level.SolverBehavior;
import org.junit.jupiter.api.Test;

final class SolverPresentationTest {
  @Test
  void selectsThePlayerFacingIdentityFromTheClosedBehaviorValue() {
    SolverPresentation random = SolverPresentation.forBehavior(SolverBehavior.RANDOM);
    SolverPresentation scout = SolverPresentation.forBehavior(SolverBehavior.LEFT_PRIORITY);

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
  void rejectsMissingBehaviorAndLevelName() {
    assertThrows(NullPointerException.class, () -> SolverPresentation.forBehavior(null));
    assertThrows(
        NullPointerException.class,
        () -> SolverPresentation.forBehavior(SolverBehavior.RANDOM).levelTitle(null));
  }
}
