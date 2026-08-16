# GAMEPLAY-02: Add Right-Turn Floor

Status: proposed

Depends on: GAMEPLAY-01

## Goal

Add the accepted Right-Turn Floor as a player-placeable and fixed cell effect without changing any
released level's palette or solver trace.

## Scope

- Implement the exact rule in the accepted
  [additional-cell design](../docs/additional-cell-types-design.md): after entry, force the relative
  right move when open; otherwise delegate the untouched decision to the solver.
- Centralize movement-effect precedence before solver-specific rules. Forced moves consume no Random
  or Seeker draw, use one normal interval, update Scout heading and Tracker visits, and preserve Slow
  Floor entry timing at the destination.
- Add mutable and fixed identities with identical gameplay, ordinary finite/infinite inventory, and
  unchanged placement, replacement, removal, repositioning, protected-cell, cancellation, and fixed-
  ownership semantics.
- Explicitly migrate every released level to zero Right-Turn supply and no fixed Right-Turn cells.
- Draw the accepted non-color clockwise perimeter mark with primitive shapes, preserving fixed-lock,
  solver, goal, badge, exhaustion, and rejection layers.
- Generalize palette descriptors and the bounded two-line tooltip. Keep the palette icon-only;
  desktop uses the existing 500 ms hover and a completed touch tap shows the same bubble for exactly
  2,000 ms. A palette-origin drag never opens or leaves the bubble open. Implement the exact normal,
  selected, unlimited, and exhausted copy from the design.
- Do not add a released level, configurable effect scripting, orientation data, or bitmap asset.

## Acceptance Criteria

- All four solvers obey the same effect precedence and reproduce exact seeded/stateful replay.
- Blocked-right fallback has the exact trace, RNG state, heading, visits, time, moves, and outcome of
  the board without the effect.
- Fixed and mutable forms are mechanically equal while fixed edits remain atomic rejections.
- Every released level retains its original visible palette, board, trace, result, progression, and
  persistence key.
- Three real palette values match the production layout, and the four-count reference still fits all
  declared viewports. Tooltip/touch behavior, JavaScript, WebAssembly, Safari, Pages, and native-
  image packaging remain valid without new runtime asset bytes.

## Verification

- Cross-check a test-side reference model against production for all four solvers, every obstruction
  direction, first-decision/goal/timeout boundaries, Slow Floor destinations, replay, and chunking.
- Exhaustively search one- and two-cell candidate fixtures, record whether either can guarantee a
  timeout, and keep every released level at zero Right-Turn supply. The domain still supports finite
  and infinite values; GAMEPLAY-04 may select only a finite released supply after exhaustive balance.
- Cover inventory, all edit operations, fixed cells, renderer layers, tooltip lifecycle, debug state,
  single/multi-solver sessions, and browser pointer/touch flows.
- Capture color and grayscale evidence at 24 px palette icons and 32 px grid cells. Right Turn must
  remain distinct from Wall and Slow Floor with its badge, infinity/zero state, selected border,
  fixed lock, 90%-cell solver sprite, goal, exhaustion slash, and rejection overlay present.
- Run formatting, static analysis, coverage, architecture, desktop, JavaScript, WebAssembly, Pages,
  Safari, and native packaging gates before review and commit.
