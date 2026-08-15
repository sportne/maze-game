# Milestone 3 Scout Solver Design

## Player-Facing Rule

Milestone 3 introduces **Scout**, a deterministic solver that internally prefers turns in this order:

1. Left relative to its current direction.
2. Straight ahead.
3. Right relative to its current direction.
4. Back to the cell it came from, when that is the only open direction.

The exact order is deliberately not shown before play. Level-selection cards retain the simple
level-name and best-result format used by earlier levels, and the build screen identifies Scout by
name without describing its search. The player discovers the preference by watching runs while
still trying to delay the solver and preserve a valid path to the goal. Result feedback may prompt
the player to watch Scout's choices at intersections, but must not state the order.

## Complete Decision Rules

Scout begins every run facing north. Its introductory authored level therefore starts at the bottom
center with the cheese at the top center, making the initial heading visible and unsurprising. A
different authored starting heading is deferred until a level actually needs one.

At each movement interval:

- Build the four candidate directions from the current heading in the fixed order left, straight,
  right, reverse.
- Select the first candidate whose adjacent cell is inside the grid and has no wall.
- Move one cell and make that absolute movement direction the new heading.
- A reverse move is ordinary movement: after backtracking, later priorities are relative to the
  reversed heading.
- If no candidate is legal, remain in place with the heading unchanged. The decision still consumes
  one movement interval and increments the move count, matching the existing simulation contract.
- Reaching the cheese wins the solver run immediately. Reaching the authored maximum solve time first
  produces a timeout. Exact-boundary behavior remains shared with the random solver.

Scout never consults the level random seed. Replay is deterministic from the immutable maze, initial
north heading, movement interval, and timeout. Splitting elapsed time across update calls must not
change its path, elapsed result, move count, or terminal status.

## Direction Examples

With a north heading, the preference order is west, north, east, south. With a west heading, it is
south, west, north, east. The remaining headings follow the same rotation:

| Current heading | Left | Straight | Right | Back |
| --- | --- | --- | --- | --- |
| North | West | North | East | South |
| East | North | East | South | West |
| South | East | South | West | North |
| West | South | West | North | East |

Required deterministic examples include an open four-way intersection, left blocked, left and
straight blocked, a dead end, a corridor after backtracking, a grid boundary, and a path-preserving
maze whose deterministic detour reaches the authored timeout before Scout reaches the cheese.

## Minimal Runtime Contract

The immutable level definition gains one closed solver-behavior value with exactly the two supported
choices: `RANDOM` and `LEFT_PRIORITY`. Existing levels select `RANDOM`; the new third level selects
`LEFT_PRIORITY`.
The session creates a simulation through one small shared simulation contract rather than depending
directly on `RandomSolverSimulation`.

The shared contract exposes only update and current-result operations already used by the session.
There is no behavior registry, plugin system, reflection, service loading, scriptable AI, or generic
behavior configuration. Random-seed behavior remains unchanged for the existing solver, and Scout's
north initial heading stays inside its concrete rule until another real level requires authoring it.

## Authored Level Baseline

Milestone 3 adds one new 7x7 level after Milestone 2. Keeping the same grid size, normal walls, and
build interaction isolates the new solver behavior as the source of difficulty.

- Stable id: `milestone-3`
- Display name: `Level 3`
- Solver: Scout (`LEFT_PRIORITY` internally)
- Grid: 7x7
- Start: bottom center
- Cheese: top center
- Initial heading: north
- Wall type: existing normal wall only
- Unlock: pass Milestone 2
- Persistence: existing per-level best-result format under the new stable id

M3-01 accepted the following authored parameters:

- Build time: 25 seconds.
- Target solve time: 6 seconds.
- Maximum solve time: 8 seconds.
- Movement interval: 250 milliseconds.
- Geometry: no authored starting walls; players begin from an empty 7x7 grid.

The empty maze reaches the cheese in 12 moves (3 seconds), so doing nothing fails. Two deliberately
small, path-preserving layouts pass without depending on a random seed:

| Fixture | Wall coordinates `(row,column)` | Result |
| --- | --- | --- |
| Passing A | `(2,2)`, `(3,1)`, `(4,0)`, `(5,1)` | Cheese in 26 moves / 6.5 seconds |
| Passing B | `(2,1)`, `(3,0)`, `(3,2)`, `(3,4)`, `(4,3)` | Cheese in 30 moves / 7.5 seconds |
| Timeout | `(3,2)`, `(3,4)`, `(4,3)`, `(5,2)`, `(5,4)`, `(6,1)` | Timeout after 32 moves / 8 seconds |

The timeout layout still has a viable start-to-cheese path; without the authored timeout Scout would
reach the cheese after 34 moves. These fixtures exercise repeated corridors and reverse moves while
keeping both cheese-reaching passes achievable with four or five well-chosen walls. The test-side
reference model records the complete traces and proves that whole-duration and chunked updates
agree.

The accepted traces, including the starting cell, are:

- Passing A: `(6,3) → (6,2) → (6,1) → (6,0) → (5,0) → (6,0) → (6,1) → (6,2) →
  (5,2) → (4,2) → (4,1) → (4,2) → (3,2) → (3,3) → (2,3) → (1,3) → (1,2) →
  (1,1) → (2,1) → (2,0) → (3,0) → (2,0) → (1,0) → (0,0) → (0,1) → (0,2) →
  (0,3)`.
- Passing B: `(6,3) → (6,2) → (6,1) → (6,0) → (5,0) → (4,0) → (4,1) → (3,1) →
  (4,1) → (4,2) → (5,2) → (5,3) → (5,4) → (4,4) → (4,5) → (3,5) → (2,5) →
  (2,4) → (2,3) → (3,3) → (2,3) → (2,2) → (1,2) → (1,1) → (1,0) → (2,0) →
  (1,0) → (0,0) → (0,1) → (0,2) → (0,3)`.
- Timeout: `(6,3) → (6,2) → (6,3) → (5,3) → (6,3) → (6,4) → (6,5) → (5,5) →
  (4,5) → (4,4) → (4,5) → (3,5) → (2,5) → (2,4) → (2,3) → (3,3) → (2,3) →
  (2,2) → (2,1) → (3,1) → (4,1) → (4,2) → (4,1) → (5,1) → (5,0) → (6,0) →
  (5,0) → (4,0) → (3,0) → (2,0) → (1,0) → (0,0) → (0,1)`.

For behavior comparison, the reference suite runs both solvers over three accepted Milestone 2 mazes:

| Milestone 2 fixture | Seeded Random | Scout |
| --- | --- | --- |
| Passing A | Cheese in 38 moves / 9.5 seconds | Cheese in 20 moves / 5 seconds |
| Passing B | Cheese in 34 moves / 8.5 seconds | Cheese in 12 moves / 3 seconds |
| Timeout | Timeout after 60 moves / 15 seconds | Cheese in 14 moves / 3.5 seconds |

All three layouts pass Milestone 2 with Random but fail its six-second target with Scout. This shows
that Scout changes the maze-building problem rather than merely renaming the existing random
configuration.

## Visual Identity

Scout uses the basic squirrel from the normalized character sheet, paired with an acorn goal. Its
silhouette distinguishes Scout without relying only on color and does not reveal the turning
preference. Random behavior uses the classic mouse artwork paired with cheese. Both identities come from
the checked-in processed sprite pipeline rather than one-off derivatives.

Build, running, and result presentation identify the active solver by the player-facing name. Level
selection intentionally uses the same concise level-name and best-result structure for every card.
Before the first run, the UI reveals no behavioral description. Subsequent result text may encourage
observation without naming the left-first rule or adding a tutorial flow.

## Compatibility and Release Boundaries

- Existing level ids, results, unlocks, random paths, replays, and saved data remain compatible.
- Solver behavior is authored level data, not stored separately in browser preferences or results.
- Desktop, JavaScript, WebAssembly, portrait touch, constrained-landscape touch, and branded Safari
  must exercise the new level without duplicating domain assertions in every harness.
- New cell types, inventories, palette selection, and drag/drop editing remain Milestone 4 work.
