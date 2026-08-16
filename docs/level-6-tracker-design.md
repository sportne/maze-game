# Level 6 Tracker Design

> Historical layout record. The current Level 6 returns to Scout and introduces fixed geometry as
> specified in the [ten-level progression design](level-progression-design.md).

## Decision

Level 6 is Tracker's introductory authored level. It uses a compact 5x5 grid, two fixed Walls, one
player Wall, and three Slow Floors to expose Tracker's visit memory before asking the player to delay
it past an exclusive six-second target.

| Parameter | Accepted value |
| --- | --- |
| Stable id | `level-6` |
| Display name | `Level 6` |
| Grid | 5x5 |
| Build time | 20 seconds |
| Target | More than 6 seconds |
| Timeout | 8 seconds |
| Move interval | 250 ms |
| Solver | Tracker raccoon, no random seed |
| Goal | Trash can |
| Start / goal | `(0,0)` / `(4,4)` |
| Fixed Walls | `(0,2)`, `(1,1)` |
| Player supply | 1 Wall, 3 Slow Floors |

The new id uses current level nomenclature. The first five `milestone-*` ids remain unchanged because
they are persistence keys, not player-facing names.

## Grid Choice

- A 3x3 version made the dead-end lesson obvious but left too few editable cells for a meaningful
  build decision; Slow Floors became a predetermined placement exercise.
- The accepted 5x5 version keeps the first revisit visible while leaving competing branches and 21
  editable positions. It is also comfortably legible at every existing minimum cell-size viewport.
- A 7x7 version produced many long, visually diffuse routes. That obscured which repeated visit
  changed Tracker's choice and made exhaustive inventory balancing needlessly broad.

No new cell or sprite asset is introduced. The raccoon and trash-can frames already ship in the
startup sheets, and fixed Walls use the accepted lock treatment.

## Teaching Geometry

The empty authored board is:

```text
S . # . .
. # . . .
. . . . .
. . . . .
. . . . T
```

`#` is a fixed Wall. From the start, east and south have equal visit counts and goal distance, so
Tracker's absolute tie break chooses east. `(0,1)` is a dead end, forcing Tracker back to the start.
East has then been visited while south has not, so Tracker changes its choice and moves south. This
is a visible use of memory rather than a different fixed direction priority.

The exact empty trace is:

```text
(0,0), (0,1), (0,0), (1,0), (2,0), (2,1), (2,2), (2,3),
(2,4), (3,4), (4,4)
```

It reaches the trash can in 2.5 seconds and fails the target.

## Accepted Passing Fixture

One reproducible passing board places the player Wall at `(3,4)` and Slow Floors at `(2,3)`,
`(1,3)`, and `(1,4)`:

```text
S . # . .
. # . s s
. . . s .
. . . . W
. . . . T
```

The Wall removes the empty board's final approach. Tracker explores the upper-right branch, revisits
several cells, then takes the lower approach. All three Slow Floors lie on that behavior-specific
route. The result is 6.5 seconds, 20 moves, and `REACHED_GOAL`, so the level passes without using the
timeout as a substitute for balance.

## Exhaustive Balance Evidence

`LevelSixTest` enumerates every legal placement through the authored supply. Invalid Wall placements
are rejected by the production route validator and do not enter the result set.

| Available player edits | Longest Tracker result | Passes `> 6.0s`? |
| --- | ---: | --- |
| Empty | 2.50 s | No |
| Up to three Slow Floors, no Wall | 3.25 s | No |
| One Wall, no Slow Floor | 5.00 s | No |
| One Wall and one Slow Floor | 5.50 s | No |
| One Wall and two Slow Floors | 6.00 s | No; target is exclusive |
| One Wall and three Slow Floors | 6.50 s | Yes |

There are 64 full-inventory passing layouts. Every accepted Tracker layout reaches the goal by 6.5
seconds, below the eight-second timeout. The result is therefore deliberate but not a single-pixel
solution.

## Behavior Comparison

On the accepted passing fixture:

| Behavior | Result | Why it is not equivalent |
| --- | --- | --- |
| Tracker | Goal at 6.50 s / 20 moves | Repeated visit counts redirect the upper-right exploration |
| Scout | Goal at 4.75 s / 16 moves | Relative heading priority follows a different loop and fails |
| Seeker | Goal at 4.00 s / 16 moves | Goal sight ends exploration earlier and fails |
| Random, seed 23 | Timeout at 8.00 s / 32 moves | Its seeded wandering does not exhibit the authored memory rule |

The fixed dead end and combined passing fixture therefore do not reduce to Scout's heading rule or a
chosen Random seed.

## Progression and Presentation

Level 5 unlocks Level 6 through the existing catalog-order progression. Level 6 stores its result
under `level-6`, unlocks Level 7 after a pass, and retains the same retry, replay, Back, and Main Menu
behavior as earlier entries. Fixed cells restore automatically on every fresh attempt while player
inventory and placed cells reset normally.

The existing dynamic selection, build, result, desktop, portrait, compact-landscape, JavaScript, and
WebAssembly layouts consume the sixth catalog entry without a new special-case layout. Production
renderer coverage uses Level 6 for the Tracker/trash-can pairing; the catalog's last level owns the
final-level label.

## Verification

- Cross-check exact empty and passing traces, results, visits, replay, inventory, and fixed-cell edit
  rejection against production simulations.
- Exhaustively enumerate legal Wall/Slow-Floor combinations through the complete supply.
- Compare the accepted board under Tracker, Random, Scout, and Seeker behavior.
- Exercise the complete six-level progression through the desktop debug interaction path.
- Run formatting, analysis, coverage, architecture, JavaScript, WebAssembly, Pages, Safari, and
  native-image gates before release.
