# Milestone 3 Scout Mouse Design

## Player-Facing Rule

Milestone 3 introduces **Scout**, a deterministic mouse that internally prefers turns in this order:

1. Left relative to its current direction.
2. Straight ahead.
3. Right relative to its current direction.
4. Back to the cell it came from, when that is the only open direction.

The exact order is deliberately not shown before play. Selection and build screens say only:
**“Scout follows a consistent search pattern.”** The player discovers the preference by watching
runs while still trying to delay the mouse and preserve a valid path to the cheese. Result feedback
may prompt the player to watch Scout's choices at intersections, but must not state the order.

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
- Reaching the cheese wins the mouse run immediately. Reaching the authored maximum solve time first
  produces a timeout. Exact-boundary behavior remains shared with the random mouse.

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
straight blocked, a dead end, a corridor after backtracking, a grid boundary, and a maze that causes
Scout to loop until timeout despite retaining a separate viable path to the cheese.

## Minimal Runtime Contract

The immutable level definition gains one closed mouse-behavior value with exactly the two supported
choices: `RANDOM` and `LEFT_PRIORITY`. Existing levels select `RANDOM`; the new third level selects
`LEFT_PRIORITY`.
The session creates a simulation through one small shared simulation contract rather than depending
directly on `RandomMouseSimulation`.

The shared contract exposes only update and current-result operations already used by the session.
There is no behavior registry, plugin system, reflection, service loading, scriptable AI, or generic
behavior configuration. Random-seed behavior remains unchanged for the existing mouse, and Scout's
north initial heading stays inside its concrete rule until another real level requires authoring it.

## Authored Level Baseline

Milestone 3 adds one new 7x7 level after Milestone 2. Keeping the same grid size, normal walls, and
build interaction isolates the new mouse behavior as the source of difficulty.

- Stable id: `milestone-3`
- Working display name: `Milestone 3`
- Mouse: Scout (`LEFT_PRIORITY` internally)
- Grid: 7x7
- Start: bottom center
- Cheese: top center
- Initial heading: north
- Wall type: existing normal wall only
- Unlock: pass Milestone 2
- Persistence: existing per-level best-result format under the new stable id

Build time, target time, timeout, and any final geometry adjustment are balancing outputs of M3-01.
They must be backed by reproducible empty, passing, failing, backtracking, and timeout fixtures before
the level enters the production catalog.

## Visual Identity

Scout uses a new mouse sprite compatible with the current pixel-art presentation and transparent
asset pipeline. It keeps a recognizable mouse silhouette but adds a blue cap with a high-contrast
star badge. The cap/badge makes Scout distinguishable without relying only on color and does not
reveal the turning preference; the existing random mouse retains its red scarf and current sprite.

Level selection, build, running, and result presentation identify the active mouse by the player-
facing name. Before the first run, the UI reveals only that Scout is deterministic. Subsequent result
text may encourage observation without naming the left-first rule or adding a tutorial flow.

## Compatibility and Release Boundaries

- Existing level ids, results, unlocks, random paths, replays, and saved data remain compatible.
- Mouse behavior is authored level data, not stored separately in browser preferences or results.
- Desktop, JavaScript, WebAssembly, portrait touch, constrained-landscape touch, and branded Safari
  must exercise the new level without duplicating domain assertions in every harness.
- New cell types, inventories, palette selection, and drag/drop editing remain Milestone 4 work.
