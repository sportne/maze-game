package io.github.sportne.mazegame.render;

import io.github.sportne.mazegame.model.level.MouseBehavior;
import java.util.Objects;

/** Player-facing identity for one authored mouse behavior. */
record MousePresentation(String name, boolean distinctIdentity) {
  private static final MousePresentation RANDOM = new MousePresentation("Mouse", false);
  private static final MousePresentation SCOUT = new MousePresentation("Scout", true);

  MousePresentation {
    Objects.requireNonNull(name, "name");
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
    return distinctIdentity ? levelName + " | " + name : levelName;
  }

  /** Uses the distinct mouse name alone when a compact status row cannot fit both identities. */
  String statusTitle(String levelName, float availableWidth) {
    return distinctIdentity && availableWidth < 300.0F ? name : levelTitle(levelName);
  }
}
