# Levels 7–9 Design

## Decision

Levels 7–9 extend the released progression through larger grids and richer authored cells rather
than increasing the number of simultaneous solvers. Each level has one solver, one matching goal,
and a complete player inventory solution:

- Level 7 introduces Seeker on a 5x5 grid with fixed Walls.
- Level 8 grows to 6x6, returns to Scout, and introduces fixed Slow Floors in released geometry.
- Level 9 grows to 7x7 and uses fixed Walls and Slow Floors to exercise Tracker's revisit rule.

No new runtime mechanic is necessary. Player Walls and Slow Floors retain their established edit,
inventory, path-validation, timing, and accessibility contracts. Fixed Slow Floors have the same
timing effect as player Slow Floors, but their lock marker communicates that they cannot be edited
and they never consume player supply.

All targets are exclusive: a completion exactly equal to the target fails. Slow Floors do not alter
movement choices, so exhaustive timing evidence counts entries on production traces and then
cross-checks every accepted fixture with the complete production simulation.

## Level 7: Seeker Introduction

| Parameter | Value |
| --- | --- |
| Stable id | `level-7` |
| Grid | 5x5 |
| Build / target / timeout | 20 s / more than 6 s / 10 s |
| Solver | Seeker rabbit, seed 107 |
| Start / carrot | `(4,0)` / `(0,4)` |
| Fixed Walls | `(0,0)`, `(2,0)`–`(2,3)`, `(4,1)` |
| Player supply | 1 Wall, 3 Slow Floors |

The fixed row separates the lower exploration area from the carrot's visible right-hand approach.
On the empty board, Seeker reaches the carrot in 2.5 seconds and 10 moves. The accepted player Wall
at `(4,2)` removes the early lower exit. Seeker revisits the lower-left cells before acquiring the
goal column; Slow Floors at `(3,0)`, `(3,1)`, and `(3,2)` turn those revisits into a 6.5-second,
16-move pass.

```text
# . . . C
. . . . .
# # # # .
s s s . .
S # W . .
```

## Level 8: 6x6 Fixed-Floor Route

| Parameter | Value |
| --- | --- |
| Stable id | `level-8` |
| Grid | 6x6 |
| Build / target / timeout | 25 s / more than 7.3 s / 8 s |
| Solver | Scout squirrel |
| Start / acorn | `(5,0)` / `(0,5)` |
| Fixed Walls | `(0,4)`, `(1,0)`, `(2,5)`, `(3,0)`, `(3,5)`, `(4,2)`, `(5,5)` |
| Fixed Slow Floors | `(1,4)`, `(4,3)` |
| Player supply | 1 Wall, 4 Slow Floors |

The accepted Wall at `(3,1)` sends Scout through the center and then along the upper corridor. The
fixed Slow Floors demonstrate that authored floor effects are part of the route rather than player
inventory. Player Slow Floors at `(0,0)`, `(0,1)`, `(0,2)`, and `(2,1)` exploit Scout's detour and
revisit. The completed route reaches the acorn in 7.5 seconds and 22 moves.

```text
s s s . # a
# . . . ~ .
. s . . . #
# W . . . #
. . # ~ . .
S . . . . #
```

## Level 9: 7x7 Tracker Revisit Puzzle

| Parameter | Value |
| --- | --- |
| Stable id | `level-9` |
| Grid | 7x7 |
| Build / target / timeout | 30 s / more than 7.5 s / 9 s |
| Solver | Tracker raccoon |
| Start / trash can | `(6,0)` / `(0,6)` |
| Fixed Walls | `(1,1)`, `(1,4)`, `(1,5)`, `(2,5)`, `(3,1)`, `(4,1)`, `(5,5)`, `(5,6)`, `(6,4)` |
| Fixed Slow Floors | `(1,3)`, `(2,4)`, `(4,5)` |
| Player supply | 1 Wall, 5 Slow Floors |

The accepted Wall at `(0,2)` closes the direct top-row exit. Tracker repeatedly samples the western
branch before its least-visited rule leads east. Slow Floors at `(0,0)`, `(0,1)`, `(1,0)`, `(2,0)`,
and `(0,3)` reward recognizing those revisits. The completed 20-move route reaches the trash can in
7.75 seconds.

```text
s s W s . . t
s # . ~ # # .
s . . . ~ # .
. # . . . . .
. # . . . ~ .
. . . . . # #
T . . . # . .
```

In the diagrams, `#` is a fixed Wall, `~` is a fixed Slow Floor, `W` and `s` are player cells, and
uppercase/lowercase character letters are the solver and its matching goal.

## Exhaustive Balance

Every legal player-Wall position and every Slow Floor subset through the authored supply is
enumerated. The table records the greatest completion time for exactly the shown number of Slow
Floors. Timeout is counted as its full duration, so a partial layout cannot pass by trapping a
solver in deterministic movement.

| Level | No Wall: 0…full Slow Floors | With Wall: 0…full Slow Floors | Full passing layouts |
| --- | --- | --- | ---: |
| 7 | 2.50, 3.00, 3.25, 3.50 s | 4.00, 5.25, 6.00, 6.50 s | 8 |
| 8 | 4.25, 4.75, 5.25, 5.50, 5.75 s | 6.00, 6.50, 7.00, 7.25, 7.50 s | 196 |
| 9 | 3.00, 3.25, 3.50, 3.75, 4.00, 4.25 s | 5.50, 6.00, 6.50, 7.00, 7.50, 7.75 s | 9 |

Thus Slow-Floor-only layouts fail, every Wall-plus-one-short inventory layout fails, and deliberate
full-inventory solutions remain for every level. The accepted saved results are `6500:16`,
`7500:22`, and `7750:20`.

## Progression and Presentation

Level 6 unlocks Level 7, followed by Levels 8 and 9 in stable catalog order. Results use `level-7`,
`level-8`, and `level-9` persistence keys. Level 9 originally owned the final-level presentation;
it now unlocks Level 10.

Nine level cards require three desktop rows, five compact-landscape columns over two rows, and three
compact-portrait columns over three rows. Dense compact cards use a reduced gap and 72-pixel height;
all cards remain at least 44 pixels in each dimension and avoid the title and Back control at the
declared minimum viewports. No runtime asset is added: Seeker, Scout, Tracker, and all matching goals
already ship in the startup sprite sheets.

## Verification

- Assert exact authored metadata, fixed Walls and Slow Floors, traces, and terminal results.
- Enumerate every legal partial and full inventory layout and assert the exact maxima and passing
  counts above.
- Exercise nine-level progression, retry, replay, persistence, final-level presentation, and all
  reference layouts through the desktop debug path.
- Run JavaScript, WebAssembly, GitHub Pages, and real-Safari flows through Levels 7–9 and verify exact
  saved results.
- Run formatting, analysis, coverage, architecture, browser, and native-image gates before release.
