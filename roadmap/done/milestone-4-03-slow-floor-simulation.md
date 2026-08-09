# M4-03: Apply Slow Floor Timing to Both Mouse Simulations

Status: complete

Archived: yes

Depends on: M4-02

## Goal

Make one shared timing rule delay both Random and Scout after entering Slow Floor without changing
their direction choices, seeded paths, move counts, or replay determinism.

## Scope

- Extend the shared timed simulation so a Slow Floor entry schedules exactly one extra movement
  interval before the next decision.
- Keep cell traversability and entry delay as model queries; simulations must not depend on renderer,
  palette, inventory, or level ids.
- Preserve Random's seeded candidate choice and Scout's left/straight/right/back priority.
- Define cheese arrival and maximum-timeout precedence when an entry or delay reaches a boundary.
- Ensure the wait adds elapsed time but does not increment move count or choose another direction.
- Do not add a generalized status-effect engine, duration authoring, or per-mouse Slow Floor behavior.

## Acceptance Criteria

- Removing Slow Floor from a fixture reproduces the exact released Random and Scout traces/results.
- Adding Slow Floor changes only the documented decision timestamps and terminal elapsed time.
- Whole, fractional, oversized, and chunked updates remain equivalent.
- Replay remains deterministic for both mouse behaviors.
- Timeout during a pending wait ends without an extra move; cheese arrival still ends immediately.

## Verification

- Add shared timing-contract tests plus focused Random and Scout integration cases.
- Re-run all released seeded-random and Scout literal-trace fixtures unchanged.
- Reproduce every M4-01 timing fixture against production simulation.
- Run full formatting, analysis, coverage, browser builds, Pages assembly, and native-image packaging
  before review and commit.
