package io.github.sportne.mazegame.render;

import io.github.sportne.mazegame.model.level.MouseBehavior;
import java.util.Objects;

/** Player-facing identity for one authored mouse behavior. */
record MousePresentation(String name, String initialDescription) {
  private static final MousePresentation RANDOM = new MousePresentation("Mouse", "");
  private static final MousePresentation SCOUT =
      new MousePresentation("Scout", "Scout follows a consistent search pattern");

  MousePresentation {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(initialDescription, "initialDescription");
  }

  /** Returns the closed presentation choice for an authored behavior. */
  static MousePresentation forBehavior(MouseBehavior behavior) {
    Objects.requireNonNull(behavior, "behavior");
    return switch (behavior) {
      case RANDOM -> RANDOM;
      case LEFT_PRIORITY -> SCOUT;
    };
  }

  /** Adds the mouse name only when the authored behavior has a special identity. */
  String levelTitle(String levelName) {
    Objects.requireNonNull(levelName, "levelName");
    return initialDescription.isEmpty() ? levelName : levelName + " | " + name;
  }

  /** Uses the distinct mouse name alone when a compact status row cannot fit both identities. */
  String statusTitle(String levelName, float availableWidth) {
    return !initialDescription.isEmpty() && availableWidth < 300.0F ? name : levelTitle(levelName);
  }
}
