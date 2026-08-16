# Level Progression Design

This document is the authoritative specification for the ten released levels. It supersedes the
individual layouts recorded while Levels 1–10 were first introduced; those older records remain as
implementation history for their mechanics and release milestones.

## Progression Goals

The catalog teaches one idea at a time before combining them:

1. Levels 1–4 are mostly completed mazes. Each introduces one solver behavior and leaves only one
   or two Wall placements for the player. Every initial maze offers multiple routes to the goal.
2. Level 5 removes all authored geometry. It introduces Slow Floors on a completely open grid and
   pairs them with infinite Walls so the player can experiment freely.
3. Levels 6–9 combine the known solvers and cells while growing the grid and introducing fixed and
   movable preset geometry.
4. Level 10 keeps the 10x10 finale and introduces two simultaneous solvers for the first time. The
   run ends when either solver reaches its own goal.

Stable ids and persistence keys do not change, so existing saved results still unlock the same
catalog positions. A stored result remains historical evidence of a pass even if its time is not
obtainable in the redesigned layout.

## Released Catalog

The inventory column reports units available in the palette when an attempt begins, after movable
presets have consumed their authored supply. `∞` means unlimited.

| Level | Grid | Solver and goal | Starting geometry | Palette | Target / timeout | Accepted result |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | 5x5 | Random mouse / cheese | 9 preset Walls | 1 Wall | >5s / 10s | 9s, 36 moves |
| 2 | 5x5 | Scout squirrel / acorn | 9 preset Walls | 1 Wall | >4s / 6s | 4.5s, 18 moves |
| 3 | 6x6 | Tracker raccoon / trash can | 13 preset Walls | 2 Walls | >5s / 7s | 6s, 24 moves |
| 4 | 6x6 | Seeker rabbit / carrot | 13 preset Walls | 2 Walls | >5.5s / 7s | 6s, 24 moves |
| 5 | 7x7 | Random mouse / cheese | Open grid | ∞ Walls, 2 Slow Floors | >5.5s / 7s | 6.25s, 18 moves |
| 6 | 7x7 | Scout squirrel / acorn | 12 fixed Walls | 1 Wall, 3 Slow Floors | >6.5s / 8s | 7s, 22 moves |
| 7 | 7x7 | Tracker raccoon / trash can | 14 preset Walls | 1 Wall, 3 Slow Floors | >7s / 10s | 8.75s, 26 moves |
| 8 | 8x8 | Seeker rabbit / carrot | 9 fixed + 9 preset Walls | 1 Wall, 4 Slow Floors | >14.5s / 18s | 17s, 46 moves |
| 9 | 9x9 | Random mouse / cheese | 22 fixed Walls | 1 Wall, 4 Slow Floors | >15.5s / 19s | 17.25s, 54 moves |
| 10 | 10x10 | Random mouse / cheese and Scout squirrel / acorn | 15 fixed + 10 preset Walls | 2 Walls, 6 Slow Floors | >11s / 13.5s | 11.5s, 71 total moves |

All solvers use the shared 250ms movement interval. Targets are exclusive: reaching a goal at the
target time is not enough to pass.

## Tutorial Lessons

### Levels 1–4: Read the Character

The visible preset maze narrows the problem without reducing it to a single corridor. The player
observes the character, identifies the route it currently favors, and blocks that route to send it
through a longer alternative. Level 1 requires one Wall at `(1,2)`. Level 2 uses the same one-edit
shape with Scout and accepts `(2,2)`. Levels 3 and 4 expand to 6x6 and require both remaining Walls:
`(0,3)` plus `(3,1)` for Tracker, and `(3,1)` plus `(1,0)` for Seeker. In each two-Wall tutorial,
either edit alone remains below the target.

The order exposes all four character rules before cell effects are added:

- Random makes reproducible seeded choices at branches.
- Scout prefers left, then straight, then right relative to its heading.
- Tracker prefers the least-visited destination.
- Seeker moves directly toward a visible goal, otherwise explores with seeded Random behavior.

### Level 5: Build From Nothing

Level 5 deliberately has no fixed or preset cells. It is the first appearance of Slow Floor and the
first infinite inventory. Walls alone and the two selected Slow Floors alone both remain below the
target; the accepted example combines a Wall at `(1,5)` with Slow Floors at `(1,3)` and `(2,3)`.
This makes the second cell type necessary without limiting Wall experimentation.

### Levels 6–9: Combine Ownership and Scale

Level 6 introduces locked, fixed Walls and returns to Scout. Level 7 contrasts them with movable
preset Walls around Tracker. Level 8 mixes both ownership states on an 8x8 Seeker board. Level 9
grows to 9x9 and applies the combined cell vocabulary to a long seeded Random route. These levels
retain one solver so board ownership, scale, and mixed inventory are learned before concurrency.

### Level 10: First Concurrent Run

Level 10 starts Random at `(9,0)` with cheese at `(0,9)` and Scout at `(9,9)` with an acorn at
`(0,0)`. Both starts and goals are protected. The accepted example places Walls at `(9,3)` and
`(4,0)`, and Slow Floors at `(7,0)`, `(8,0)`, `(7,1)`, `(9,1)`, `(6,1)`, and `(8,4)`.

Independently, Random reaches its goal at 12.75 seconds after 42 moves and Scout reaches its goal at
11.5 seconds after 34 moves. The shared session therefore ends on Scout's first finish at 11.5
seconds and records 71 total moves completed by both solvers at that moment.

## Authored Geometry Reference

Coordinates are `(row,column)`, zero-based from the upper-left. `P` denotes movable preset Walls;
`F` denotes fixed Walls. Passing edits are examples, not the only layouts players may discover.

| Level | Preset Walls | Fixed Walls | Passing edits |
| --- | --- | --- | --- |
| 1 | `(0,0) (0,4) (1,0) (3,0) (3,1) (3,3) (4,1) (4,3) (4,4)` | — | Wall `(1,2)` |
| 2 | `(0,1) (0,3) (1,1) (1,4) (2,4) (3,0) (3,2) (4,0) (4,4)` | — | Wall `(2,2)` |
| 3 | `(0,0) (1,0) (1,1) (1,3) (1,4) (2,3) (3,0) (4,3) (4,4) (5,1) (5,3) (5,4) (5,5)` | — | Walls `(0,3) (3,1)` |
| 4 | `(0,0) (1,3) (2,5) (3,2) (3,4) (3,5) (4,1) (4,2) (4,3) (4,5) (5,1) (5,2) (5,5)` | — | Walls `(3,1) (1,0)` |
| 5 | — | — | Wall `(1,5)`; Slow `(1,3) (2,3)` |
| 6 | — | `(0,1) (0,3) (1,0) (1,3) (2,0) (2,5) (3,1) (3,6) (4,3) (5,1) (5,4) (6,6)` | Wall `(2,3)`; Slow `(1,2) (2,2) (3,2)` |
| 7 | `(0,0) (0,1) (1,5) (2,2) (2,4) (2,6) (3,1) (3,2) (3,4) (3,6) (4,4) (5,1) (6,4) (6,5)` | — | Wall `(1,2)`; Slow `(1,1) (2,0) (2,1)` |
| 8 | `(4,4) (5,3) (6,2) (6,3) (6,5) (6,7) (7,1) (7,4) (7,7)` | `(0,2) (1,1) (1,4) (1,6) (1,7) (2,0) (2,1) (2,3) (4,0)` | Wall `(2,6)`; Slow `(2,5) (2,4) (3,4) (1,5)` |
| 9 | — | `(0,2) (0,3) (1,2) (1,3) (1,4) (1,7) (3,3) (4,1) (4,3) (4,5) (4,7) (4,8) (5,7) (6,4) (6,5) (6,6) (6,8) (7,5) (7,6) (7,7) (8,6) (8,8)` | Wall `(7,1)`; Slow `(7,0) (3,6) (7,2) (8,1)` |
| 10 | `(6,0) (6,9) (7,2) (7,3) (7,7) (8,1) (8,2) (8,7) (8,9) (9,4)` | `(0,3) (0,4) (0,8) (1,0) (1,3) (2,1) (2,7) (3,0) (3,5) (3,7) (3,9) (4,5) (5,0) (5,8) (5,9)` | Walls `(9,3) (4,0)`; Slow `(7,0) (8,0) (7,1) (9,1) (6,1) (8,4)` |

## Verification Contract

`ReleasedLevelProgressionTest` owns deterministic evidence for catalog order, character order, grid
growth, early alternate routes, exact accepted traces, Level 5's open-grid mechanic boundary, and
Level 10's first-finish result. Session, renderer, layout, debug-harness, Chromium, WebAssembly, and
Safari suites exercise the same example layouts through the production interaction paths.
