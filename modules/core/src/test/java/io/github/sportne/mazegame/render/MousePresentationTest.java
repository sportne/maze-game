package io.github.sportne.mazegame.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.sportne.mazegame.model.level.MouseBehavior;
import java.util.Locale;
import org.junit.jupiter.api.Test;

final class MousePresentationTest {
  @Test
  void selectsThePlayerFacingIdentityFromTheClosedBehaviorValue() {
    MousePresentation random = MousePresentation.forBehavior(MouseBehavior.RANDOM);
    MousePresentation scout = MousePresentation.forBehavior(MouseBehavior.LEFT_PRIORITY);

    assertEquals("Mouse", random.name());
    assertEquals("", random.initialDescription());
    assertEquals("Milestone 1", random.levelTitle("Milestone 1"));
    assertEquals("Scout", scout.name());
    assertEquals("Scout follows a consistent search pattern", scout.initialDescription());
    assertEquals("Milestone 3 | Scout", scout.levelTitle("Milestone 3"));
    assertEquals("Milestone 3 | Scout", scout.statusTitle("Milestone 3", 300.0F));
    assertEquals("Scout", scout.statusTitle("Milestone 3", 299.0F));
  }

  @Test
  void initialScoutDescriptionDoesNotRevealItsDirectionPriority() {
    String description =
        MousePresentation.forBehavior(MouseBehavior.LEFT_PRIORITY)
            .initialDescription()
            .toLowerCase(Locale.ROOT);

    assertFalse(description.contains("left"));
    assertFalse(description.contains("straight"));
    assertFalse(description.contains("right"));
    assertFalse(description.contains("back"));
  }

  @Test
  void rejectsMissingBehaviorAndLevelName() {
    assertThrows(NullPointerException.class, () -> MousePresentation.forBehavior(null));
    assertThrows(
        NullPointerException.class,
        () -> MousePresentation.forBehavior(MouseBehavior.RANDOM).levelTitle(null));
  }
}
