# Level 10 Design

> Historical layout record. The current 10x10 Level 10 introduces two solvers and is specified in
> the [ten-level progression design](level-progression-design.md).

## Decision

Level 10 grows the board directly from 7x7 to 10x10 while retaining one active solver. It returns
to the classic Random mouse and cheese, uses a reproducible seed, and combines a larger authored
wall pattern with fixed and player Slow Floors. This increases route-reading and placement scope
without escalating the number of simultaneous characters.

The level deliberately authors zero player Walls and six player Slow Floors. Two Slow Floors begin
on the grid as movable presets and consume two units, leaving four in the palette. The established
palette rule hides the unavailable Wall tool and presents only the usable Slow Floor. Fixed Walls
supply the topology; the player's puzzle is to recognize revisited cells on the seeded route and
either keep or reposition the presets before spending the remaining delay inventory.

## Authored Definition

| Parameter | Value |
| --- | --- |
| Stable id | `level-10` |
| Grid | 10x10 |
| Build / target / timeout | 35 s / more than 12.5 s / 13.5 s |
| Solver | Random classic mouse, seed 1484 |
| Start / cheese | `(9,0)` / `(0,9)` |
| Fixed Walls | `(0,3)`, `(1,3)`, `(2,1)`–`(2,3)`, `(1,7)`, `(2,7)`, `(3,5)`–`(5,5)`, `(5,3)`, `(5,4)`, `(6,7)`–`(8,7)`, `(7,1)`, `(7,2)` |
| Fixed Slow Floors | `(8,2)`, `(3,8)` |
| Player supply | 0 Walls, 6 Slow Floors total; 2 preset and 4 remaining |
| Preset Slow Floors | `(7,4)`, `(2,8)` |

The accepted Slow Floors are `(7,4)`, `(2,8)`, `(5,6)`, `(4,6)`, `(3,6)`, and `(2,9)`.

```text
. . . # . . . . . g
. . . # . . . # . .
. # # # . . . # p s
. . . . . # s . ~ .
. . . . . # s . . .
. . . # # # s . . .
. . . . . . . # . .
. # # . p . . # . .
. . ~ . . . . # . .
R . . . . . . . . .
```

In the diagram, `#` is a fixed Wall, `~` is a fixed Slow Floor, `p` is a movable preset Slow Floor,
`s` is an additional player Slow Floor, and `R`/`g` are Random and its cheese goal.

## Balance Evidence

Fixed Slow Floors make the empty 34-move trace complete in 9.0 seconds. The two accepted preset
positions add 1.75 seconds, so the actual unedited starting board completes in 10.75 seconds.
Because Slow Floors do not change movement choices, the exhaustive maximum for each remaining
inventory size is obtained by sorting every empty editable cell by its exact number of entries on
that production trace.

| Additional Slow Floors used | 0 | 1 | 2 | 3 | 4 |
| --- | ---: | ---: | ---: | ---: | ---: |
| Greatest completion | 10.75 s | 11.25 s | 11.75 s | 12.25 s | 12.75 s |

Thus the presets plus three or fewer additional Slow Floors cannot exceed the exclusive 12.5-second
target. The accepted six-cell total layout reaches the cheese in 12.75 seconds and 34 moves, before
the 13.5-second timeout. Its saved result is `12750:34`.

## Layout and Progression

Level 9 unlocks Level 10, and Level 10 owns the final-level presentation and the
`maze-game.best-result.level-10` persistence key. Ten selection cards use five columns over two rows
on wide layouts and three columns over four rows on compact portrait layouts.

The 10x10 grid retains 35-pixel cells at the 390x844 phone reference and 48-pixel cells at
1280x720. At the shortest 844x286 and 756x286 supported landscape references it uses 24-pixel
cells—the largest square grid that fits vertically with margins. Existing smaller boards retain
their larger cell sizes, while action and palette controls remain at least 44 pixels.

## Verification

- Assert exact metadata, supplies, fixed-cell counts, seeded trace, and terminal results.
- Prove the exact best completion for every remaining inventory count from zero through four.
- Move a preset through desktop and browser drag paths, then restore and solve the level.
- Exercise Level 9-to-10 progression, retry, replay, final-level presentation, and persistence.
- Validate all declared viewports, the ten-card selector, JavaScript/WebAssembly browser flow, and
  Safari release flow.
- Run formatting, static analysis, coverage, architecture, browser, and native-image gates.
