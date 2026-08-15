# GRID-01: Add Fixed Authored Grid Cells

Status: proposed

Depends on: none

## Goal

Let a level begin with prepopulated grid cells whose gameplay effects are visible to solvers and path
validation but which players cannot place, replace, remove, or reposition.

## Scope

- Add immutable level-definition data for fixed authored cells, separate from player-placed cells and
  their finite or infinite inventory.
- Support fixed forms of every cell effect deliberately approved for authoring; do not assume that a
  fixed cell must also be exposed as a player-placeable palette type.
- Validate fixed positions for grid bounds, duplicate occupancy, solver start/goal conflicts, known
  cell effects, and a viable baseline route from every solver to its matching goal.
- Make cell-content queries, walkability, path validation, Slow Floor timing, solver simulations,
  debug snapshots, replay, retry, and rendering observe the combined fixed and player-authored board.
- Keep inventory derived only from player-placed cells. Fixed cells never consume supply and never
  return supply.
- Reject place, replace, remove, and move operations involving a fixed source or destination with one
  explicit domain outcome while preserving board identity and every inventory count.
- Give fixed cells a non-color visual marker that communicates permanence without obscuring the
  underlying cell effect, solver, goal, grid boundary, or rejection feedback.
- Ensure pointer previews and click/drag input expose the same rejection semantics on desktop and
  touch layouts.
- Preserve every released level by authoring an empty fixed-cell collection; do not change existing
  level ids, results, solver traces, progression, or persistence keys.
- Do not add a new level or invent a new cell mechanic in this infrastructure task.

## Acceptance Criteria

- A level can author multiple fixed cells and exposes them through an immutable, validated definition.
- Fixed Wall blocks movement and pathfinding, while fixed Slow Floor remains walkable and applies the
  same delay as its player-placed equivalent.
- Every edit path rejects fixed sources and destinations atomically, with unchanged maze state and
  inventory.
- Fixed cells remain identical across fresh attempts, retry, replay, navigation away and back, timer
  expiry, and browser reload.
- Multi-solver validation accounts for fixed cells when preserving every start-to-goal route.
- Rendering distinguishes fixed versus player-placed ownership without relying on color, and all
  supported viewports keep the marker readable.
- Existing levels retain their exact authored boards, results, saved scores, and runtime behavior.

## Verification

- Add definition tests for empty, single, multiple, out-of-bounds, duplicate, protected-overlap,
  missing-effect, and baseline-path-invalid authoring.
- Add domain tests across fixed Wall and Slow Floor for placement, replacement, removal, movement,
  inventory accounting, cell content, path validation, and state equality.
- Compare fixed and player-placed versions of each supported effect in Random, Scout, and
  multi-solver timing traces.
- Add renderer, debug-harness, replay/retry, responsive input, JavaScript, and WebAssembly browser
  coverage for fixed-cell appearance and rejected edits.
- Run formatting, static analysis, coverage, architecture, browser smoke, Pages, Safari, and native
  packaging gates before independent review and commit.
