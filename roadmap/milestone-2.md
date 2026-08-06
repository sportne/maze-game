# Milestone 2: Mobile-Playable Two-Level Progression

Status: planned

## Goal

Turn the single-level prototype into a small progression that is comfortable to play on desktop and
mobile. Milestone 2 adds one deliberately harder authored level while retaining the existing random
mouse and normal-wall rules, so difficulty and progression can be evaluated before another mouse or
block mechanic is introduced.

## Product Decisions

- Level 1 remains the current easy 5x5 introduction.
- Level 2 is a larger authored maze challenge, with 7x7 as the design baseline to validate and tune.
- Level 2 unlocks after the player passes Level 1.
- Each level stores and displays its own best passing result.
- Portrait and landscape mobile layouts must provide readable text and practical touch targets; merely
  shrinking the 1280x720 desktop canvas is not sufficient.
- Desktop, JavaScript, and WebAssembly builds continue to share the same game and progression rules.
- Normal walls and the existing deterministic random mouse are the only gameplay mechanics in this
  milestone.

## Out of Scope

- A new mouse behavior; Milestone 3 will define and introduce that mechanic.
- Multiple block types, limited inventories, or drag-and-drop placement.
- Multiple mice or multiple start positions.
- A complete retro/pixel-art overhaul.

## Definition of Done

- The complete Level 1 loop remains unchanged and unlocks Level 2 after a passing result.
- Level 2 can be selected, played, passed or failed, retried, replayed, and revisited after restart.
- Locked and unlocked states are clear, deterministic, and persisted without corrupting existing
  Level 1 best-result data.
- Both levels remain playable with mouse or touch in supported portrait and landscape viewports.
- Automated unit, architecture, desktop, JavaScript, WebAssembly, and live release checks cover the
  multi-level flow in proportion to their existing responsibilities.
- Formatting, static analysis, coverage, and packaging requirements remain green.
- Playtesting confirms that Level 1 feels introductory and Level 2 provides a noticeable but fair
  increase in challenge.

## Task Order

1. [M2-01: Make mobile controls genuinely playable](milestone-2-01-mobile-controls.md)
2. [M2-02: Design and balance the second authored level](milestone-2-02-level-design.md)
3. [M2-03: Generalize the authored-level catalog and selection contract](milestone-2-03-level-catalog.md)
4. [M2-04: Add progression, unlocking, and per-level persistence](milestone-2-04-progression.md)
5. [M2-05: Implement the second authored level](milestone-2-05-level-implementation.md)
6. [M2-06: Make level-selection and game UI data-driven](milestone-2-06-multi-level-ui.md)
7. [M2-07: Add multi-level end-to-end and release coverage](milestone-2-07-release-coverage.md)
8. [M2-08: Playtest, tune, and release Milestone 2](milestone-2-08-playtest-release.md)
9. [M2-09: Define Milestone 3's new mouse type](milestone-2-09-define-milestone-3.md)

## Future Direction

Milestone 3 should add one meaningfully different mouse behavior after the two-level progression is
stable. A candidate Milestone 4 would change building into an inventory-driven interaction with
multiple block types, limited quantities, and blocks dragged from a palette onto the grid. That is a
larger change to game rules and input behavior and is intentionally deferred until the new mouse can
be evaluated independently.
