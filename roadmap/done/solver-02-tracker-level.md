# SOLVER-02: Design and Deliver a Tracker Level

Status: complete

Archived: yes

Depends on: SOLVER-01

## Goal

Add one authored level that teaches and balances Tracker's least-visited-cell behavior without
introducing another solver behavior or cell type.

## Completed Scope

- Added Level 6 as a 5x5 Tracker/raccoon level with a trash-can goal.
- Added two fixed Walls that force Tracker to revisit its start and visibly change its next choice.
- Authored one player Wall and three Slow Floors with a 20-second build, exclusive six-second target,
  eight-second timeout, and 250 ms movement cadence.
- Added Level 6 to catalog-order progression, persistence, responsive layouts, rendering, replay,
  retry, and the desktop debug interaction flow.
- Reused existing processed character/goal assets and fixed-cell rendering without increasing startup
  transfer.

## Acceptance Evidence

- The accepted [Level 6 design](../../docs/level-6-tracker-design.md) records grid alternatives,
  exact rules, teaching geometry, fixtures, solver comparisons, balance results, and release scope.
- The empty trace enters a dead end, revisits the start, then selects the previously unvisited branch.
- Exhaustive production enumeration proves empty, Wall-only, Slow-only, Wall-plus-one-Slow, and Wall-
  plus-two-Slow layouts cannot exceed the six-second target.
- Full-inventory enumeration finds 64 passing layouts and a 6.5-second maximum, always below timeout.
- The accepted fixture reaches the goal in 6.5 seconds and 20 moves; Scout and Seeker fail the
  target, while seeded Random times out on the same board without exhibiting Tracker's memory rule.

## Verification

- Run `LevelSixTest` for authored parameters, exact traces, visit state, exhaustive balance, solver
  comparison, fixed cells, inventory, session results, and replay.
- Run the full six-level debug progression, renderer/layout/persistence suites, browser smoke, Pages,
  Safari, and native-image gates.
