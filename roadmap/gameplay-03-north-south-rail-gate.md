# GAMEPLAY-03: Add North–South Rail Gate

Status: proposed

Depends on: GAMEPLAY-01 and GAMEPLAY-02's shared palette descriptor boundary

## Goal

Add the accepted North–South Rail Gate so a board can preserve vertical traversal while closing
horizontal shortcuts.

## Scope

- Implement the exact static edge rule from the accepted
  [additional-cell design](../docs/additional-cell-types-design.md).
- Generalize pathfinding and combined-board movement queries from destination walkability to explicit
  edge legality, then validate every solver's matching route after each authored or player edit.
- Make Random, Scout, Tracker, and Seeker filter the same edge rules. Seeker line of sight must inspect
  each edge and may cross Rail only vertically.
- Add mutable and fixed identities, ordinary cell supply and editing, released-level finite-zero
  migration, primitive rail rendering, descriptor/tooltip copy, and debug/browser visibility.
- Keep orientation fixed north–south. Do not add rotation, a horizontal variant, parameterized cell
  data, a released level, or bitmap art.

## Acceptance Criteria

- The reference multi-solver fixture retains its vertical route and horizontal detour; Wall removes
  the required vertical route and Slow Floor leaves the horizontal shortcut.
- Path validation rejects any final board that strands any matching solver when edge rules apply.
- All solvers, replay, retry, timing, fixed/mutable equivalence, inventory, and edit transactions are
  deterministic and retain current behavior away from Rail.
- Released palettes, traces, results, persistence, asset transfer, and supported viewports remain
  unchanged.
- All four real enum values fit and remain operable in every declared reference/minimum viewport and
  real JavaScript/WebAssembly touch flow.

## Verification

- Add exhaustive edge-entry/exit tests in all four directions, pathfinder comparison fixtures,
  multi-solver validation, Seeker sight, Random seeds, Scout heading, Tracker visits, Slow Floor
  timing, timeout, replay, and chunking.
- Cover finite/infinite inventory, replacement/removal/movement, fixed rejections, renderer layers,
  tooltips, touch gestures, debug snapshots, and real JavaScript/WebAssembly browser flows.
- Capture color and grayscale evidence at 24 px palette icons and 32 px grid cells. Rail must remain
  distinct from Wall, Slow Floor, and Right Turn with its badge, infinity/zero state, selected border,
  fixed lock, 90%-cell solver sprite, goal, exhaustion slash, and rejection overlay present.
- Run formatting, static analysis, coverage, architecture, desktop, Pages, Safari, and native
  packaging gates before review and commit.
