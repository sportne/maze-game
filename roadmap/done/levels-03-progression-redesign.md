# LEVELS-03: Redesign the Ten-Level Progression

Status: Complete

## Outcome

- Reframed Levels 1–4 as preset, multiple-route tutorials for Random, Scout, Tracker, and Seeker,
  requiring only one or two remaining Wall placements.
- Made Level 5 a completely open 7x7 grid that introduces two Slow Floors beside infinite Walls.
- Rebalanced Levels 6–9 around fixed/preset ownership, mixed inventory, and grid growth while keeping
  one solver active.
- Retained the 10x10 finale and moved the first two-solver experience to Level 10.
- Preserved stable ids and persistence keys, updated production/browser fixtures, and replaced the
  fragmented current-layout evidence with one deterministic progression suite.

## Verification

- Prove every early tutorial has multiple initial routes and its accepted one/two-edit layout crosses
  the exclusive target.
- Prove either single edit remains insufficient in Levels 3 and 4.
- Prove Level 5 begins without authored cells and needs the combined example mechanic.
- Verify exact outcomes for Levels 6–10, including Level 10's first-finish session result.
- Exercise the complete catalog through core, renderer, layout, debug, browser, and release gates.

The accepted catalog is documented in
[`docs/level-progression-design.md`](../../docs/level-progression-design.md).
