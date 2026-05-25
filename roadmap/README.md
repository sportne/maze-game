# Maze Game Roadmap

## Product Shape

Maze Game is a desktop Java/libGDX game where the player builds a maze under time pressure, then watches an AI mouse try to reach the cheese. The player wins a level by making the mouse exceed the target solve time while preserving a valid path from the mouse start to the cheese.

The first milestone is intentionally small: one 5x5 level, normal wall placement only, one deterministic random mouse, and a complete build-run-result loop.

## Current Project Context

- Platform: desktop.
- Runtime stack: existing Java/libGDX project.
- Shared game module: `modules/core`.
- Desktop launcher module: `modules/lwjgl3`.
- Primary local run command: `./gradlew :modules:lwjgl3:run`.
- Quality command: `./gradlew checkAll`.
- Packaging command already exposed by the root build: `./gradlew nativeImage`.

## Core Loop

1. Start a level.
2. Player has a limited build timer to place walls.
3. Each attempted wall placement must preserve at least one path from mouse start to cheese.
4. Invalid wall placements are rejected immediately and briefly highlighted light red.
5. The player may start the mouse early, or the mouse starts automatically when the build timer expires.
6. During the mouse run, editing is locked.
7. The mouse moves according to the level's AI behavior until it reaches the cheese or times out.
8. The result shows pass/fail, elapsed solve time, move count, and retry/replay actions.

## Milestones

### Milestone 1: 5x5 Playable Prototype

Deliver a fully playable desktop level with:

- 5x5 grid.
- Mouse starts at bottom center.
- Cheese is at top center.
- 30 second build timer.
- 5 second target solve time.
- 10 second maximum solve time.
- Mouse moves 1 grid square every 0.25 seconds.
- Normal walls only.
- Deterministic random mouse movement.
- Exact time and move count result display.
- Retry and replay support.
- Native desktop package/build included in the definition of done.

See [Milestone 1 Backlog](milestone-1.md).

## Deferred Ideas

These are intentionally out of scope for milestone 1, but should remain visible for later planning:

- Level selection screen.
- Larger grid progression from 5x5 up to 25x25.
- Additional block types.
- Additional mouse types.
- Multiple mice and multiple start locations.
- Persistent best results between app launches.
- Retro/pixel visual style.
- More authored levels with mixed grid sizes, block types, and mouse behaviors.

## Design Decisions Captured

- Invalid wall placement is rejected immediately.
- Available wall cells render black.
- Placed walls render white.
- Temporarily invalid placement feedback renders light red.
- Start and cheese cells cannot be converted into walls.
- Light red invalid placement feedback appears briefly, not persistently.
- Mouse-only interaction is enough for milestone 1.
- The first mouse may move in any free direction, including immediately moving backward.
- Replay re-runs the same seeded AI from the beginning, producing the same path.
- A level is passed when the mouse exceeds the target time, whether it eventually reaches the cheese or times out.
- The next level option only appears when a next level exists.

