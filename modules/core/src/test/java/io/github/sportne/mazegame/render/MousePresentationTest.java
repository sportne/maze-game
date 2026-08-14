package io.github.sportne.mazegame.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.sportne.mazegame.model.level.MouseBehavior;
import org.junit.jupiter.api.Test;

final class MousePresentationTest {
  @Test
  void selectsThePlayerFacingIdentityFromTheClosedBehaviorValue() {
    MousePresentation random = MousePresentation.forBehavior(MouseBehavior.RANDOM);
    MousePresentation scout = MousePresentation.forBehavior(MouseBehavior.LEFT_PRIORITY);

    assertEquals("Mouse", random.name());
    assertEquals("cheese", random.goalName());
    assertEquals("Milestone 1", random.levelTitle("Milestone 1"));
    assertEquals("Scout", scout.name());
    assertEquals("acorn", scout.goalName());
    assertEquals("Milestone 3 | Scout", scout.levelTitle("Milestone 3"));
    assertEquals("Milestone 3 | Scout", scout.statusTitle("Milestone 3", 300.0F));
    assertEquals("Scout", scout.statusTitle("Milestone 3", 299.0F));
  }

  @Test
  void rejectsMissingBehaviorAndLevelName() {
    assertThrows(NullPointerException.class, () -> MousePresentation.forBehavior(null));
    assertThrows(
        NullPointerException.class,
        () -> MousePresentation.forBehavior(MouseBehavior.RANDOM).levelTitle(null));
  }
}
