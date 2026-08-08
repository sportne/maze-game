# M3-08: Define Milestone 4's Cell Inventory and Building Interactions

Status: pending

Depends on: M3-07

## Goal

Turn the inventory-based building direction into a reviewed Milestone 4 design and executable
task-card set without implementing it prematurely or coupling it to Scout's release.

## Scope

- Review playtest evidence from the three released levels and identify the building limitations that
  authored grid-cell types should solve.
- Define each initial placeable cell type by gameplay effect, visual identity, path/mouse interaction,
  and whether a level supplies a finite count or infinite availability.
- Define immutable per-level inventory authoring, runtime remaining-count accounting, and the display
  of finite versus infinite supplies.
- Specify a bottom-screen palette that remains usable on desktop, portrait, constrained landscape,
  and safe-area layouts.
- Support two equivalent placement mechanisms: drag a type from its palette position onto the grid,
  or click/tap a palette type to activate it and then click/tap grid cells.
- Allow placed cell types to be dragged between grid cells before the mouse starts. Define source-cell
  reservation, valid drop, invalid drop, cancellation, pointer capture, and restoration semantics.
- Preserve the rule that every edit leaves a viable mouse-to-cheese path. Treat a move as one
  transactional edit so an invalid destination restores the original cell and inventory count.
- Define removal/replacement behavior, protected mouse/cheese cells, inventory return, infinite-use
  behavior, build-timer expiration during a gesture, and editing lock once exploration starts.
- Decide how cell types extend `CellContent`, maze state, level definitions, rendering, hit testing,
  input actions, replay, and deterministic mouse simulations without a speculative item framework.
- Define mouse interaction with each accepted cell type for both Random and Scout mice.
- Plan pointer/touch accessibility, drag thresholds, tap-versus-drag disambiguation, rotation/resize
  during gestures, and browser cancellation behavior.
- Create `roadmap/milestone-4.md` and the complete ordered Milestone 4 task-card set covering design,
  domain model, inventories, transactional edits, palette selection, drag/drop, rendering, responsive
  UI, mouse behavior, persistence compatibility, tests, playtesting, and release.

## Acceptance Criteria

- The initial cell-type set is small, mechanically distinct, teachable, and fully specified for both
  mouse behaviors.
- Every level can author a nonnegative finite count or explicit infinite availability for each
  supported placeable type, with unambiguous consume/return rules.
- Drag-from-palette and select-then-place produce the same validated domain command and outcome.
- Dragging an existing placed cell is atomic: success moves it once; invalid or cancelled drops leave
  the original grid and inventory unchanged.
- Input rules cover mouse, touch, pointer cancellation, build timeout, resize, rotation, and attempts
  to edit while exploration is running.
- The design adds only abstractions required by the accepted cell types and two placement mechanisms.
- Existing levels, normal-wall behavior, saved best results, replays, Random, and Scout remain
  compatible.
- Milestone 4 has an explicit definition of done, scope exclusions, ordered cards, dependencies,
  balancing plan, and desktop/browser/mobile/Safari verification.
- Every generated Milestone 4 card receives reviewer approval before implementation begins.

## Verification

- Walk finite and infinite inventory examples through place, replace, remove, move, invalid drop,
  cancellation, retry, auto-start, and exploration-lock transitions.
- Prototype coordinate-only drag state and transactional domain commands in tests or diagrams, not
  production code, to validate ambiguous gesture cases.
- Compare drag and click-placement outcome tables for semantic equivalence.
- Review the milestone and every generated card for testability, sequencing, simplicity, and
  compatibility with both mouse types.
- Confirm all generated links resolve and the parent roadmap presents Milestone 4 consistently.
- Run the repository's full quality and release gates for the planning commit.
