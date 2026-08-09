# M4-05: Render the Responsive Palette and Support Select-Then-Place

Status: complete

Archived: yes

Depends on: M4-04

## Goal

Add the bottom build palette, inventory presentation, and equivalent mouse/touch selection-and-click
placement without introducing drag state yet.

## Scope

- Add declarative palette item bounds and selection/availability semantics to build layouts.
- Place the palette below the grid on desktop/portrait and in the bottom strip on constrained/safe
  landscape, respecting safe areas and existing Back/Start controls.
- Render Wall and Slow Floor with distinct non-color icons, short labels, selected/exhausted states,
  and finite counts or infinity with a text fallback. Exhausted means unavailable for placement, not
  unselectable for same-type removal.
- Route palette clicks/taps to type selection and grid clicks/taps to the session's common
  place-or-replace operation.
- Preserve tap-again removal for the active type and desktop right-click removal for any placed item.
- Keep every interactive target at least 44x44 CSS pixels and every supported grid cell at least
  32x32; fail layout validation when those contracts cannot be met.
- Do not add drag thresholds, previews, pointer capture, or existing-item movement.

## Acceptance Criteria

- Selection, placement, replacement, removal, counts, exhausted state, last-item tap-again recovery,
  and rejection feedback are readable without color and agree across mouse and real touch events.
- Both released compact landscape viewports fit the two-item palette without scrolling or overlap.
- Released levels show the single usable Wall tool without changing their build outcomes.
- Slow Floor renders consistently in the grid and palette without obscuring mouse/cheese/protected
  state.

## Verification

- Add renderer, input-router, hit-testing, and layout-validator assertions for every supported viewport.
- Add application-level mouse and touch flows for select, repeated place, replace, last-item placement,
  exhausted-type selection, tap-again recovery, rejected exhausted placement, right-click remove,
  Back, and Start.
- Visually inspect color and grayscale captures at desktop, portrait, constrained landscape, and safe
  landscape sizes.
- Run full quality, browser, artifact-budget, Pages, and native-image gates before review and commit.
