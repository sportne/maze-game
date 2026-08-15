package io.github.sportne.mazegame.render;

import io.github.sportne.mazegame.model.level.SolverBehavior;
import java.util.Objects;

/** Player-facing identity for one authored solver behavior. */
record SolverPresentation(String name, String goalName, boolean distinctIdentity) {
  private static final SolverPresentation RANDOM =
      new SolverPresentation("Solver", "cheese", false);
  private static final SolverPresentation SCOUT = new SolverPresentation("Scout", "acorn", true);

  SolverPresentation {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(goalName, "goalName");
  }

  /** Returns the closed presentation choice for an authored behavior. */
  static SolverPresentation forBehavior(SolverBehavior behavior) {
    Objects.requireNonNull(behavior, "behavior");
    return switch (behavior) {
      case RANDOM -> RANDOM;
      case LEFT_PRIORITY -> SCOUT;
    };
  }

  /** Adds the solver name only when the authored behavior has a special identity. */
  String levelTitle(String levelName) {
    Objects.requireNonNull(levelName, "levelName");
    return distinctIdentity ? levelName + " | " + name : levelName;
  }

  /** Uses the distinct solver name alone when a compact status row cannot fit both identities. */
  String statusTitle(String levelName, float availableWidth) {
    return distinctIdentity && availableWidth < 300.0F ? name : levelTitle(levelName);
  }
}
