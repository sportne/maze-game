# Alternating Gate Contract

Alternating Gate is a placeable and fixed grid-cell effect that changes between open and blocking
on a shared deterministic run clock. It is introduced as the third build tool in Level 9.

## Player Rule

- Every gate starts open when a solver run or replay begins.
- The open phase lasts from 0.0 seconds through 0.999 seconds.
- The gate is closed from 1.0 seconds through 1.999 seconds, reopens at 2.0 seconds, and continues
  alternating every second.
- A closed gate cannot be entered and blocks Seeker's line of sight. An open gate behaves like an
  ordinary walkable cell.
- A solver already occupying a gate may leave it after the phase closes. Gate state never pushes,
  traps, or teleports an occupant.
- When a closed gate removes every legal exit, the solver waits in place for that ordinary decision
  interval. That decision still participates in the established move-count statistic.

The phase is shared wall-clock state, not a per-solver decision count. All concurrent solvers see
the same state even when Slow Floors cause their movement decisions to occur at different times.
Retry and replay create fresh simulations at time zero, so the phase resets and deterministic input
produces the same result. Frame updates are split at movement boundaries; one large update and many
small updates therefore observe identical gate phases.

## Building and Validation

`ALTERNATING_GATE` uses the same finite/infinite inventory and transactional place, replace,
tap-again remove, and reposition rules as Wall and Slow Floor. Starts, goals, fixed cells, bounds,
and authored supply remain protected by the common editor.

Static build validation treats a gate as topologically open. Because every gate reopens and solvers
may wait when no exit is available, a gate does not permanently remove an authored route. Runtime
movement and Seeker sight apply the exact phase at each decision boundary. Fixed and player-placed
gates have identical runtime behavior; fixed gates add the standard lock marker and never consume
player supply.

Old authored definitions and external fixtures that predate a new placeable enum value are
normalized with a finite-zero supply for the omitted type. Authored nonempty entries retain their
order, duplicate entries remain invalid, and every production level explicitly records all current
supplies.

## Presentation

The palette normally shows only the icon and its lower-right supply badge. The icon uses a dark
cell, cyan rails, and three vertical bars; color is supplementary to the barred silhouette. After
the existing half-second hover delay, its two-line tooltip reads `Alternating Gate n` and `Toggles
every second.` The normal finite count, exhaustion slash, selected outline, and line-drawn infinity
badge remain unchanged.

On the grid, the open state keeps the ordinary dark floor and draws separated diagonal gate leaves
between horizontal rails. The closed state adds a cool fill and three connected vertical bars.
These shape changes keep the states distinguishable without color and remain visible below solver,
goal, fixed-lock, and rejection overlays.

## Level 9 Introduction

Level 9 supplies one Wall, four Slow Floors, and one Alternating Gate. Its exclusive target is 17.5
seconds with a 19-second timeout. The accepted example places the Wall at `(7,1)`, Slow Floors at
`(7,0)`, `(3,6)`, `(7,2)`, and `(8,1)`, and the gate at `(1,8)`. Without the gate, that board reaches
the cheese in 17.25 seconds and fails. With the gate, seeded Random times out at `(2,5)` after 61
decisions and passes.

## Verification

- Assert exact 0.999/1.000/1.999/2.000-second phase boundaries.
- Exercise all four solver behaviors when the gate is the only exit.
- Compare whole-duration and chunked updates, last direction, and solver decision memory.
- Compare fixed and player-placed runtime behavior.
- Cover open/closed fills, non-color marks, palette count state, tooltip copy, and responsive layouts.
- Run JVM, JavaScript, WebAssembly, and Safari release flows through the Level 9 solution.
