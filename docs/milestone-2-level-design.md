# Milestone 2 Level Design

## Accepted Specification

Milestone 2 keeps the existing normal-wall rules and deterministic random mouse. Its difficulty
comes from a larger board, a shorter build window, and a target that requires deliberate detours.

| Parameter | Milestone 2 value | Rationale |
| --- | --- | --- |
| Stable id | `milestone-2` | Follows the existing result-key convention. |
| Display name | `Milestone 2` | Matches the current authored-level naming. |
| Grid | 7x7 | Adds 24 editable cells over Level 1 without introducing another mechanic. |
| Mouse start | row 6, column 3 | Keeps the entry visible at bottom center. |
| Cheese | row 0, column 3 | Makes the empty route obvious while leaving room for detours on both sides. |
| Build time | 25 seconds | Five seconds less than Level 1 despite the larger board. |
| Target solve time | 6 seconds, exclusive | Requires more than the empty route and is one second above Level 1. |
| Maximum solve time | 15 seconds | Allows meaningful wandering but keeps attempts short. |
| Move interval | 250 ms | Preserves the learned mouse cadence and visible animation rate. |
| Random seed | 38 | Reaches the cheese quickly when empty and separates the accepted wall fixtures cleanly. |

The intended lesson is to shape several detours while preserving a route, rather than trying to
seal the cheese away. The game continues to reject any placement that removes the final viable
path.

## Reproducible Simulation Fixtures

Coordinates use zero-based `(row,column)` values. Diagrams show the cheese as `C`, mouse start as
`M`, normal walls as `X`, and open cells as `.`. Each fixture is encoded in
`MilestoneTwoLevelDesignTest` and simulated for the full 15-second limit.

### Empty maze — expected failure

```text
...C...
.......
.......
.......
.......
.......
...M...
```

- Walls: none
- Result: `REACHED_CHEESE`
- Elapsed: 3.00 seconds
- Moves: 12
- Target outcome: fail

### Accepted layout A — expected pass

```text
...C...
.X..X..
X.....X
...X..X
X......
X.X....
...M...
```

- Walls: `(1,1) (1,4) (2,0) (2,6) (3,3) (3,6) (4,0) (5,0) (5,2)`
- Result: `REACHED_CHEESE`
- Elapsed: 9.50 seconds
- Moves: 38
- Target outcome: pass

### Accepted layout B — expected pass

```text
...C...
..X.X.X
.X.....
...X.X.
.......
..X...X
...MX..
```

- Walls: `(1,2) (1,4) (1,6) (2,1) (3,3) (3,5) (5,2) (5,6) (6,4)`
- Result: `REACHED_CHEESE`
- Elapsed: 8.50 seconds
- Moves: 34
- Target outcome: pass

### Timeout boundary fixture

```text
...C.X.
......X
.X..X..
.....X.
.XX....
......X
.X.M...
```

- Walls: `(0,5) (1,6) (2,1) (2,4) (3,5) (4,1) (4,2) (5,6) (6,1)`
- Result: `TIMED_OUT` at row 1, column 2
- Elapsed: 15.00 seconds
- Moves: 60
- Target outcome: pass

All three wall sets retain a four-directional path from start to cheese. Repeating a fixture with
seed 38 produces the same terminal position, elapsed time, move count, and status.

## Comparison and Playtest Notes

Level 1 uses a 5x5 grid, 30-second build time, 5-second target, 10-second timeout, and the same
250ms cadence. Level 2 therefore asks the player to understand the same rule on a board that is 96%
larger by cell count, place a useful pattern five seconds sooner, and exceed a higher delay target.

Paper and deterministic-fixture playtesting found nine walls practical to place within 25 seconds.
The empty center route communicates failure quickly, while layouts A and B demonstrate that the
target does not depend on one exact solution. The timeout fixture confirms that a legal route can
remain while the seeded mouse still fails to reach the cheese. On the responsive 7x7 presentation,
cells measure 51 CSS pixels at 390x844 and 34 CSS pixels at both 844x286 and the 756x286 safe-content
landscape case, so editing remains above the 32px requirement.

This document defines and validates the authored data only. Adding it to the production catalog is
reserved for the later implementation task.
