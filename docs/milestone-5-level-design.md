# Milestone 5 Multi-Solver Level Design

## Authored Layout

Milestone 5 uses a 7x7 grid and combines both released character behaviors in one maze:

- Random starts at `(6,0)` and pursues the cheese at the center cell `(3,3)` with seed `23`.
- Scout starts at `(1,4)` and pursues the acorn at `(2,4)`, one cell diagonally from the cheese.
- The build timer is 25 seconds, the target is 5 seconds, and the run timeout is 10 seconds.
- Inventory is five Walls and four Slow Floors.

Both starts and both goals are protected. Every accepted edit must retain a walkable route from each
character to its own goal.

## Run and Scoring Contract

Random and Scout run independently and concurrently against the same completed maze. Each retains
its released movement behavior, movement interval, Slow Floor timing, and deterministic replay.
The attempt ends when both runs have reached their goals or timed out.

The level passes only when both elapsed times exceed the target. The saved score uses the shorter of
the two elapsed times, because that is the limiting delay, and the combined move count breaks equal
time ties. Result presentation reports each character's time and moves separately.

## Balance Fixtures

The empty layout fails: Random reaches the cheese in 1.5 seconds and Scout reaches the acorn in 0.75
seconds.

The accepted passing fixture is:

- Walls: `(0,2)`, `(1,3)`, `(3,2)`, `(4,0)`, `(6,1)`
- Slow Floors: `(1,2)`, `(2,2)`, `(2,5)`, `(6,3)`

Random times out after 10 seconds and 39 moves. Scout reaches the acorn after 9 seconds and 33
moves. Replay reproduces both results exactly.
