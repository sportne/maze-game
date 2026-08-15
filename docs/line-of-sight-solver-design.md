# Seeker Line-of-Sight Solver Design

## Identity

Seeker is a rabbit paired with a carrot goal. Both appearances are already present in the shipped
`basic-characters.png` and `goals.png` atlases, so Seeker adds no runtime asset or startup transfer.

## Movement Contract

Seeker has two decision modes evaluated independently before every movement:

1. **Visible goal:** when Seeker and its goal share a row or column and every intervening cell is
   traversable, move one cell along that row or column toward the goal.
2. **Exploration:** otherwise choose one open orthogonal neighbor with the level-authored seeded
   random generator.

Exploration uses the released Random solver's exact candidate order: north, south, west, east. The
goal itself is never random when visible. Because the maze cannot change during a run, a clear
straight path remains clear and Seeker continues directly to the goal after first acquiring it.

Grid boundaries, player Walls, and fixed Walls block movement and line of sight. Empty cells and
both player-placed and fixed Slow Floors preserve line of sight. Entering Slow Floor delays the next
decision by one shared movement interval without changing the selected route, seeded random state,
or move count.

Seeker requires a random seed. Missing seeds are rejected by level authoring, as are seeds on fully
deterministic Scout and Tracker behaviors. Replay recreates the seeded generator from the authored
value and therefore reproduces positions, directions, times, moves, and terminal outcomes.

## Boundary Semantics

- A goal in the same row is approached east or west as appropriate.
- A goal in the same column is approached north or south as appropriate.
- Seeker is never asked to select a direction while already on its goal because goal arrival is
  terminal immediately after the entering move.
- A Wall anywhere strictly between Seeker and the goal disables direct pursuit for that decision.
- Oversized updates stop at goal arrival or timeout through the shared fixed-step simulation.
- A normal goal-entering decision at the exact timeout boundary reaches the goal; a pending Slow
  Floor wait at the boundary times out without another move.

## Integration Boundaries

- `LINE_OF_SIGHT` is a closed `SolverBehavior` value selected exhaustively by the simulation factory.
- Simulation stays independent of rendering, libGDX, persistence, debug UI, and platform launchers.
- Appearance and naming remain presentation mappings: Seeker uses the rabbit and carrot.
- SOLVER-01's Tracker follow-up level remains separate; this change adds no released level.
