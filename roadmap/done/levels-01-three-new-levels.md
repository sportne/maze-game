# LEVELS-01: Add Three Authored Levels

Status: complete

Archived: yes

Depends on: SOLVER-02

## Goal

Extend the released progression with three distinct, balanced levels using the existing solver and
cell vocabulary unless a demonstrated mechanic gap requires another type.

## Completed Scope

- Added Level 7 as Seeker's first authored rabbit/carrot puzzle.
- Added Level 8 as a 6x6 Scout route containing fixed Walls and fixed Slow Floors.
- Added Level 9 as a 7x7 Tracker finale built around revisits and fixed cell effects.
- Expanded compact level selection to keep all nine cards touch-sized and on-screen.
- Added progression, persistence, debug, renderer, browser, WebAssembly, Pages, Safari, and native
  release coverage without adding startup assets.

## Acceptance Evidence

- The accepted [Levels 7–9 design](../../docs/levels-7-9-design.md) records authored parameters,
  teaching geometry, passing fixtures, exact traces, and release behavior.
- Exhaustive enumeration proves no level passes with Slow Floors alone or with a Wall and one fewer
  Slow Floor than the authored supply.
- Full inventory retains 8 passing layouts for Level 7, 196 for Level 8, and 9 for Level 9.
- The accepted results are 6.5 seconds / 16 moves, 7.5 seconds / 22 moves, and 7.75 seconds / 20
  moves.

## Verification

- Run `LevelsSevenToNineTest` and the complete core suite.
- Run the nine-level debug progression and responsive layout suites.
- Run the full quality gate, JavaScript and WebAssembly browser smoke, live Safari validation, and
  native-image packaging.
