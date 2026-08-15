# Tracker Solver Design

## Decision

The third solver is **Tracker**, a deterministic raccoon that remembers how often it has visited
each cell. Tracker uses the existing processed raccoon frames and trash-can goal. Both sprites are
already present in the runtime `basic-characters.png` and `goals.png` sheets, so the behavior adds no
startup asset transfer and does not depend on ASSET-01.

## Candidate Comparison

| Candidate | State | Decision | Distinct challenge | Decision |
| --- | --- | --- | --- | --- |
| Right-hand follower | Heading | Right, straight, left, back | Mirrored Scout layouts | Reject: equivalent to Scout with a different relative priority |
| Goal greedy | None | Smallest Manhattan distance, then absolute direction | Direct routes and simple traps | Reject: oscillates in traps and provides little authored variation |
| Depth-first explorer | Stack and discovered edges | First unexplored edge, otherwise pop | Long deliberate backtracking routes | Reject: correct but its invisible stack is harder to explain and inspect |
| Least-visited Tracker | Per-cell visit counts | Fewest visits, then goal distance, then absolute direction | Loops, revisits, and route-balancing choices | **Accept** |

Tracker is not a different Random seed because it has no random choice and its decisions change
only when its recorded visits change. It is not Scout with another starting heading because its
tie-break order is absolute and its visit memory can make the same position choose a different exit
on a later visit.

## Behavior Contract

Tracker starts with its authored start cell recorded as visited once. It has no heading and requires
no random seed. At each shared movement decision it evaluates the four absolute directions in this
order: north, east, south, west.

For every traversable orthogonal neighbor, Tracker derives this key:

1. the neighbor's recorded visit count, where an unrecorded cell has count zero;
2. the neighbor's Manhattan distance to the authored goal;
3. the absolute north/east/south/west direction order.

Tracker moves to the neighbor with the lexicographically smallest key, then increments that
destination's visit count. Boundaries and Walls are not candidates. A sole legal neighbor is chosen,
which provides dead-end backtracking. With no legal neighbor Tracker stays in place and does not
change visit memory; the shared simulation still records the attempted movement decision.

Arrival at the matching goal wins immediately after the move. The shared timeout wins only when no
goal move completed at that boundary. Entering either an authored or player-placed Slow Floor adds
one movement interval before the next decision, without changing the selected route, move count, or
visit memory. Zero-duration updates do nothing, negative durations are rejected, oversized updates
stop at the first terminal result, and completed results ignore later updates.

## Reference Traces

Coordinates are `(row,column)`. Each trace includes the start position. Visit state is the complete
non-zero map after the final shown position.

### Open goal comparison

On an open 3x3 board from `(2,1)` to `(0,1)`:

- Tracker: `(2,1) -> (1,1) -> (0,1)`.
- Scout (north-facing): `(2,1) -> (2,0) -> (1,0) -> (0,0) -> (0,1)`.
- Random with seed 2: `(2,1) -> (2,0) -> (1,0) -> (1,1) -> (0,1)`; other seeds can differ.

Tracker's final visits are `{(2,1)=1, (1,1)=1, (0,1)=1}`.

### Loop and changed decision state

On a 3x3 board from `(0,0)` to `(1,2)`, with Walls at `(0,2)` and `(1,1)`, Tracker follows
`(0,0) -> (0,1) -> (0,0) -> (1,0) -> (2,0) -> (2,1) -> (2,2) -> (1,2)`. The east branch is a dead
end, so the sole exit backtracks to the start. On that repeated start, east has one visit while south
is unvisited, changing the decision to south. Final visits are `{(0,0)=2, (0,1)=1, (1,0)=1,
(2,0)=1, (2,1)=1, (2,2)=1, (1,2)=1}`.

### Boundaries, obstruction, and backtracking

From center `(1,1)` on a 3x3 board, blocking north and east leaves south and west. Both are unvisited;
when south is closer to the goal it is chosen. If south then becomes a dead end, its sole open north
neighbor is chosen even though that cell has already been visited. With all four neighbors blocked,
the solver remains at `(1,1)` and the visit map remains `{(1,1)=1}`.

### Timing

With a 250 ms movement interval, the open goal comparison reaches the goal at 500 ms in two moves.
If `(1,1)` is Slow Floor, the positions and visits are identical but goal arrival is 750 ms. If the
maximum solve time is 500 ms and the second decision is delayed by Slow Floor, Tracker times out at
`(1,1)` with one move and no extra visit.

## Integration Boundaries

- `SolverBehavior` remains the closed authored behavior set; the factory handles every value with an
  exhaustive switch.
- `LevelSolver` requires no seed for Tracker and rejects one if supplied.
- Shared fixed-step timing, results, replay, multi-solver stopping, persistence, and cell effects are
  unchanged.
- Visit memory is exposed as immutable generic solver decision state for tests and the debug harness.
- Appearance and goal mapping remain presentation-only. Simulation does not depend on libGDX,
  rendering, persistence, launchers, or platform code.
- No released level selects Tracker in SOLVER-01. A separate follow-up card owns authored level
  design and balancing.
