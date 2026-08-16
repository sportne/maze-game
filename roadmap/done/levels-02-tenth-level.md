# LEVELS-02: Add a Tenth Authored Level

Status: complete

Archived: yes

Depends on: LEVELS-01

## Goal

Add a distinctive one-solver level featuring a 10x10 grid and richer use of the existing cell
vocabulary, without increasing concurrent solver count.

## Completed Scope

- Added Level 10 as a seeded Random mouse/cheese puzzle on a 10x10 grid.
- Authored 17 fixed Walls, two fixed Slow Floors, and two movable preset Slow Floors around a
  six-Slow-Floor final arrangement.
- Hid the zero-supply Wall tool so the build palette contains only usable cells.
- Expanded level selection, progression, persistence, debug, renderer, browser, Safari, responsive,
  and native release coverage through Level 10.
- Bounded dense-grid layout validation at 24 pixels for the shortest supported landscape viewports
  while preserving every existing level's rendered dimensions.

## Acceptance Evidence

- The accepted [Level 10 design](../../docs/level-10-design.md) records the full authored definition,
  diagram, exact trace, timing proof, and responsive behavior.
- With both presets in place, every arrangement using at most three of the four remaining Slow
  Floors completes at or before 12.25 seconds and fails.
- The accepted six-cell total layout reaches the cheese in 12.75 seconds and 34 moves before
  timeout.

## Verification

- Run `LevelTenTest`, the full core suite, browser compilation and smoke tests, and native-image
  packaging.
- Run formatting, static analysis, coverage, architecture, and the complete quality gate.
