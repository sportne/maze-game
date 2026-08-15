# Maze Game Roadmap

## Product Shape

Maze Game is a Java/libGDX desktop and browser game where the player builds a maze under time
pressure, then watches an AI solver try to reach its goal. The player wins a level by making every
solver exceed the target solve time while preserving a valid path from each start to its goal.

The first milestone is intentionally small: one 5x5 level, normal wall placement only, one
deterministic Random solver, and a complete build-run-result loop.

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
3. Each attempted wall placement must preserve a path from every solver start to its matching goal.
4. Invalid wall placements are rejected immediately and briefly highlighted light red.
5. The player may start the solvers early, or they start automatically when the build timer expires.
6. During the solver run, editing is locked.
7. Each solver moves according to its authored AI behavior until it reaches its goal or times out.
8. The result shows pass/fail, elapsed solve time, move count, and retry/replay actions.

## Milestones

### Web Deployment: TeaVM and GitHub Pages

The JavaScript-first static website and opt-in WebAssembly preview are deployed through GitHub Pages.
See the completed [TeaVM and GitHub Pages Roadmap](teavm-github-pages.md).

### Milestone 1: 5x5 Playable Prototype

Deliver a fully playable desktop level with:

- 5x5 grid.
- Solver starts at bottom center.
- Cheese is at top center.
- 30 second build timer.
- 5 second target solve time.
- 10 second maximum solve time.
- Solver moves 1 grid square every 0.25 seconds.
- Normal walls only.
- Deterministic Random solver movement.
- Exact time and move count result display.
- Retry and replay support.
- Native desktop package/build included in the definition of done.

See the archived [Milestone 1 Backlog](done/milestone-1.md).

Status: complete. The desktop app also includes a startup menu, settings screen, and a level-select
screen with Level 1 enabled and future level slots locked.

### Milestone 2: Mobile-Playable Two-Level Progression

Add a genuinely mobile-playable responsive layout, a harder second authored level, persistent unlock
progression, data-driven level selection, and complete cross-platform release coverage while retaining
the existing Random solver and normal walls.

See [Milestone 2](milestone-2.md) and the accepted
[release baseline](../docs/milestone-2-release.md).

Status: complete.

### Milestone 3: New Solver Behavior

Add Scout, a visually distinct deterministic solver with a discoverable direction preference, and a
third 7x7 level that isolates its behavior from new building mechanics.

See [Milestone 3](milestone-3.md), the accepted
[solver design](../docs/milestone-3-mouse-design.md), and the
[release baseline](../docs/milestone-3-release.md).

Status: complete.

### Milestone 4: Inventory-Based Cell Building

Add Wall and Slow Floor with finite or infinite per-level supplies. Players can drag types from a
bottom palette or select and place them, then drag placed cells around the grid before the solver
run starts. Slow Floor delays either existing solver without changing its route choice.

See [Milestone 4](milestone-4.md) and the accepted
[cell-building design](../docs/milestone-4-cell-building-design.md).

Status: planned.

### Milestone 5: Combined Random and Scout Level

Add a fifth 7x7 level where Random and Scout run concurrently from different starts. Random pursues
centered cheese while Scout pursues an acorn one diagonal cell away. Both routes must remain valid,
and both characters must exceed the target for the level to pass.

See the accepted [multi-solver level design](../docs/milestone-5-level-design.md) and completed
[implementation card](done/milestone-5-01-fifth-level.md).

Status: complete.

### Cross-Cutting Asset Delivery

- [ASSET-01: Ship optional art separately and load it on demand](asset-01-lazy-delivery.md) is a
  proposed, non-gating follow-up. It becomes a prerequisite when a future character, cosmetic, or
  campaign feature selects any of the optional processed sprite atlases for runtime use.
- [ASSET-02: Optimize and stream web music](asset-02-web-audio-delivery.md) is a proposed,
  non-gating follow-up for selecting a short production loop, adding a modern browser format with
  fallback, and bounding music transfer and decode costs.

## Deferred Ideas

These remain outside the planned Milestone 4 scope:

- Grid progression beyond the five authored levels.
- Cell types beyond Wall and Slow Floor.
- Additional solver types beyond Random and Scout.
- Retro/pixel visual style.
- More authored levels with mixed grid sizes, block types, and solver behaviors.

Completed task cards are retained in the [done archive](done/README.md).

## Design Decisions Captured

- Invalid wall placement is rejected immediately.
- Available wall cells render black.
- Placed walls render white.
- Temporarily invalid placement feedback renders light red.
- Start and cheese cells cannot be converted into walls.
- Light red invalid placement feedback appears briefly, not persistently.
- Mouse-only interaction is enough for milestone 1.
- The first solver may move in any free direction, including immediately moving backward.
- Replay re-runs the same seeded AI from the beginning, producing the same path.
- Scout begins facing north and deterministically prefers left, straight, right, then back, while the
  initial player-facing UI gives no description of its search behavior.
- A level is passed when every solver exceeds the target time, whether it reaches its goal or times
  out.
- The next level option only appears when a next level exists.
- Milestone 4 adds Wall and Slow Floor only; Slow Floor adds one movement interval after entry without
  changing route choice or move count.
- Each level explicitly authors finite or infinite supply for every placeable type.
- Multi-solver levels protect every start and goal, preserve every matching route, and pass only when
  every character exceeds the target; the weakest elapsed time is the saved score.
- Palette drag and select-then-place use one atomic edit, and existing-item drag never changes inventory.
