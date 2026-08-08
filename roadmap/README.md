# Maze Game Roadmap

## Product Shape

Maze Game is a Java/libGDX desktop and browser game where the player builds a maze under time
pressure, then watches an AI mouse try to reach the cheese. The player wins a level by making the
mouse exceed the target solve time while preserving a valid path from the mouse start to the cheese.

The first milestone is intentionally small: one 5x5 level, normal wall placement only, one deterministic random mouse, and a complete build-run-result loop.

## Current Project Context

- Platforms: desktop, JavaScript web release, and WebAssembly web preview.
- Runtime stack: existing Java/libGDX project.
- Shared game module: `modules/core`.
- Desktop launcher module: `modules/lwjgl3`.
- Primary local run command: `./gradlew :modules:lwjgl3:run`.
- Browser module: `modules/teavm` with release validation in `modules/browser-tests`.
- Quality command: `./gradlew qualityGate`.
- Atomic Pages build: `./gradlew pagesBuild`.
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

### Web Deployment: TeaVM and GitHub Pages

The JavaScript-first static website and opt-in WebAssembly preview are deployed through GitHub Pages.
See the completed [TeaVM and GitHub Pages Roadmap](teavm-github-pages.md).

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

See the archived [Milestone 1 Backlog](done/milestone-1.md).

Status: complete. The desktop app also includes a startup menu, settings screen, and a level-select
screen with Milestone 1 enabled and future level slots locked.

### Milestone 2: Mobile-Playable Two-Level Progression

Add a genuinely mobile-playable responsive layout, a harder second authored level, persistent unlock
progression, data-driven level selection, and complete cross-platform release coverage while retaining
the existing random mouse and normal walls.

See [Milestone 2](milestone-2.md) and the accepted
[release baseline](../docs/milestone-2-release.md).

Status: complete.

### Milestone 3: New Mouse Behavior

Add Scout, a visually distinct deterministic mouse with a discoverable direction preference, and a
third 7x7 level that isolates its behavior from new building mechanics.

See [Milestone 3](milestone-3.md), the accepted
[mouse design](../docs/milestone-3-mouse-design.md), and the
[release baseline](../docs/milestone-3-release.md).

Status: complete.

### Milestone 4 Candidate: Inventory-Based Block Building

Add authored grid-cell types with finite or infinite per-level supplies. Players can drag types from
a bottom palette or select and place them, then drag placed cells around the grid before the mouse
starts. This changes both game rules and input behavior and should be evaluated only after Scout can
be understood independently.

Status: direction captured; detailed planning deferred to M3-08.

## Deferred Ideas

These remain outside the currently planned Milestone 3 work and captured Milestone 4 direction:

- Grid progression beyond the three authored levels.
- Cell types beyond the initial set to be accepted during M3-08 planning.
- Additional mouse types beyond Random and Scout.
- Multiple mice and multiple start locations.
- Retro/pixel visual style.
- More authored levels with mixed grid sizes, block types, and mouse behaviors.

Completed task cards are retained in the [done archive](done/README.md).

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
- Scout begins facing north and deterministically prefers left, straight, right, then back, while the
  initial player-facing UI gives no description of its search behavior.
- A level is passed when the mouse exceeds the target time, whether it eventually reaches the cheese or times out.
- The next level option only appears when a next level exists.
