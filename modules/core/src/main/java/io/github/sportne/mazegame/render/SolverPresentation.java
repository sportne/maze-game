package io.github.sportne.mazegame.render;

import io.github.sportne.mazegame.model.level.GoalType;
import io.github.sportne.mazegame.model.level.LevelSolver;
import io.github.sportne.mazegame.model.level.SolverAppearance;
import java.util.Objects;

/** Player-facing identity for one authored solver. */
record SolverPresentation(String name, String goalName, boolean distinctIdentity) {
  SolverPresentation {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(goalName, "goalName");
  }

  /** Returns the presentation selected by authored appearance and goal type. */
  static SolverPresentation forSolver(LevelSolver solver) {
    Objects.requireNonNull(solver, "solver");
    String name = solver.appearance() == SolverAppearance.CLASSIC_MOUSE ? "Solver" : "Scout";
    String goalName = solver.goalType() == GoalType.CHEESE ? "cheese" : "acorn";
    return new SolverPresentation(
        name, goalName, solver.appearance() != SolverAppearance.CLASSIC_MOUSE);
  }

  /** Adds the solver name only when the authored appearance has a special identity. */
  String levelTitle(String levelName) {
    Objects.requireNonNull(levelName, "levelName");
    return distinctIdentity ? levelName + " | " + name : levelName;
  }

  /** Uses the distinct solver name alone when a compact status row cannot fit both identities. */
  String statusTitle(String levelName, float availableWidth) {
    return distinctIdentity && availableWidth < 300.0F ? name : levelTitle(levelName);
  }
}
