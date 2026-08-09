# M4-06: Add Drag-From-Palette Placement

Status: complete

Archived: yes

Depends on: M4-05

## Goal

Let players drag either palette type onto the grid while guaranteeing the same domain command and
outcome as select-then-place.

## Scope

- Add one controller-owned gesture state with pointer id, palette origin type, press coordinate,
  current coordinate, and whether the CSS-pixel drag threshold was crossed.
- Select on release before threshold; after threshold, capture the pointer and show a clamped drag
  preview plus valid/rejected destination feedback.
- On a grid release, call the same session place-or-replace intent used by click placement.
- Cancel without editing on outside release, pointer cancel/loss, blur, resize, orientation change,
  Back, Start, or timer expiry.
- Introduce the narrow application/controller cancellation boundary here, where real gesture state
  first exists, and invoke it before manual or automatic exploration freezes the maze.
- Ignore additional pointers while one owns the gesture and prevent browser scrolling only for the
  active canvas interaction.
- Keep libGDX input/lifecycle adaptation separate from domain inventory logic.
- Do not add existing-grid-item dragging in this card.

## Acceptance Criteria

- For every empty, same-type, replacement, protected, exhausted, and path-blocked destination, drag
  and click placement return identical state, inventory, and rejection reason.
- Dragging an exhausted type onto that same placed type removes it and returns one; dragging it onto
  an empty or different-type destination rejects unchanged.
- A sub-threshold motion remains a palette selection; threshold behavior is based on CSS pixels and
  is stable across device scale factors.
- Every cancellation path leaves maze and inventory unchanged and clears transient preview/capture.
- Timer expiry cannot commit a drop after exploration starts.

## Verification

- Add coordinate-only gesture tests for threshold boundaries, pointer ownership, capture, cancellation,
  resize, and timer races.
- Parameterize click-versus-drag equivalence cases through the application boundary, including
  exhausted same-type recovery and exhausted empty/different rejection.
- Exercise real Playwright touch drags in portrait and constrained landscape plus desktop mouse drag.
- Run full quality, browser, Pages, Safari-where-available, and native-image gates before review and
  commit.
