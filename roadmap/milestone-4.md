# Milestone 4: Inventory-Based Cell Building

Status: in progress

## Goal

Replace the single implicit wall tool with a small, authored cell palette whose finite or infinite
supplies support selection, placement, replacement, and pre-run repositioning without changing the
released win condition or mouse identities.

## Product Decisions

- The initial placeable set is exactly Wall and Slow Floor.
- Wall blocks both mice. Slow Floor remains walkable and adds one movement interval before the next
  decision, without changing route choice or move count.
- Every level explicitly authors finite-zero-or-greater or infinite supply for both types.
- The three released levels retain infinite Walls and zero Slow Floors.
- Palette drag and select-then-place use the same atomic domain edit.
- Tapping a cell containing the active type removes it; selecting a different type and tapping an
  occupied cell atomically replaces it.
- Existing items can be dragged only to empty cells. Invalid and cancelled moves preserve the source
  and all inventory counts.
- The palette is visible at the bottom of every build layout and uses compact 44-pixel minimum icon
  targets with numeric/infinity supply badges, half-second desktop hover tooltips, and non-color
  selection and availability cues.
- Milestone 4 adds one authored level with finite Wall and Slow Floor supplies that demonstrates both
  types while allowing a four-Wall fallback; released levels provide the infinite-Wall compatibility
  case. It does not add another mouse.

The exact mechanic and gesture contract is recorded in
[`docs/milestone-4-cell-building-design.md`](../docs/milestone-4-cell-building-design.md).

## Out of Scope

- More placeable types, directional cells, teleporters, traps, or mouse-specific effects.
- Inventory purchases, random drops, crafting, an item registry, or externally scripted effects.
- Swapping two occupied cells, multi-cell pieces, rotation, stacking, undo/redo history, or saving
  unfinished layouts.
- Multiple mice, another mouse behavior, or player-selected mouse behavior.
- Changes to the win condition, best-result format, or JavaScript-first release strategy.
- Keyboard navigation and comprehensive screen-reader support beyond preserving current behavior;
  pointer and touch are the supported build inputs.

## Definition of Done

- Wall and Slow Floor behavior, finite/infinite supply, consume/return rules, and atomic edit outcomes
  match the accepted design for both Random and Scout.
- Drag-from-palette and select-then-place are semantically equivalent, while tap-again removal and
  desktop right-click clearing remain predictable.
- Existing-item drag handles threshold, capture, cancellation, invalid drops, timer expiry, resize,
  rotation, and exploration lock without losing an item or corrupting inventory.
- The bottom palette and action controls fit desktop, 390x844 portrait, 844x286 constrained landscape,
  and 756x286 safe landscape with current minimum target and cell sizes.
- Released levels, progression, best results, random paths, Scout traces, retry, replay, and reload
  remain compatible.
- A balanced fourth level supports the deliberate mixed-type route plus a four-Wall fallback and
  remains fair on a physical phone in portrait and landscape.
- JavaScript, WebAssembly, live Pages, branded Safari, native packaging, formatting, static analysis,
  coverage, architecture, and browser evidence remain green.

## Task Order

1. [M4-01: Validate cell mechanics and balance Level 4](done/milestone-4-01-cell-design.md)
2. [M4-02: Add authored supplies and transactional maze inventory](done/milestone-4-02-domain-inventory.md)
3. [M4-03: Apply Slow Floor timing to both mouse simulations](done/milestone-4-03-slow-floor-simulation.md)
4. [M4-04: Integrate selected tools and atomic edits into the build session](done/milestone-4-04-session-editing.md)
5. [M4-05: Render the responsive palette and support select-then-place](done/milestone-4-05-palette-placement.md)
6. [M4-06: Add drag-from-palette placement](done/milestone-4-06-palette-drag.md)
7. [M4-07: Reposition placed cells by drag](done/milestone-4-07-cell-repositioning.md)
8. [M4-08: Add the fourth authored level and progression](done/milestone-4-08-fourth-level.md)
9. [M4-09: Extend cross-platform release coverage](milestone-4-09-release-coverage.md)
10. [M4-10: Playtest, tune, and release Milestone 4](milestone-4-10-playtest-release.md)

Each card is an independently reviewable commit. A card moves to `roadmap/done/` only after its
tests and quality gates pass and its diff receives independent review. Release work uses one push
after the ordered task commits unless the execution request explicitly sets another push policy.
