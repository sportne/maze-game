# Milestone 1 Backlog: 5x5 Playable Prototype

## Goal

Create one complete playable desktop level for the existing Java/libGDX game. The player builds a valid 5x5 maze, then a deterministic random mouse attempts to reach the cheese. The result clearly reports whether the player passed by making the mouse exceed the target time.

## Level Definition

- Grid size: 5x5.
- Mouse start: bottom center.
- Cheese: top center.
- Build time: 30 seconds.
- Target solve time: 5 seconds.
- Maximum solve time: 10 seconds.
- Mouse move cadence: 1 square every 0.25 seconds.
- Wall types: normal walls only.
- Input: mouse only.

## Definition Of Done

- The level can be launched through the desktop app.
- The player can place and clear normal walls during the build phase.
- The game rejects wall placements that would remove all paths from start to cheese.
- The mouse run begins automatically when the build timer ends.
- The player can start the mouse early.
- Editing is locked while the mouse is running.
- The random mouse is deterministic for a given level run seed.
- The result screen/state shows pass/fail, exact solve time, and move count.
- Retry resets the level for another attempt.
- Replay re-runs the same seeded mouse behavior from the start.
- No next level button appears because milestone 1 has no second level.
- `./gradlew checkAll` passes.
- `./gradlew :modules:lwjgl3:run` launches the desktop game.
- `./gradlew nativeImage` can package the desktop executable.

## Tasks

### 1. Game State Model

- [x] Add core domain types for grid coordinates, cells, walls, start, and cheese.
- [x] Add a level definition model with grid size, start cell, cheese cell, build timer, target time, maximum solve time, mouse cadence, and random seed.
- [x] Add milestone 1's authored 5x5 level definition.
- [x] Add a game phase model for build, mouse running, result, and replay.
- [x] Add unit tests for level definition values and protected start/cheese cells.

### 2. Maze Validity

- [x] Implement path validation from mouse start to cheese over non-wall cells.
- [x] Reject wall placement on start and cheese cells.
- [x] Reject wall placement when it would remove all valid paths.
- [x] Keep rejected placement attempts from mutating the maze.
- [x] Add unit tests for valid placement, invalid placement, protected cells, and edge-case paths.

### 3. Build Phase Interaction

- [x] Render the 5x5 grid in the libGDX scene.
- [x] Map mouse clicks to grid cells.
- [x] Left click toggles or places a normal wall during the build phase.
- [x] Right click clears a wall during the build phase.
- [x] Show available cells as black and placed walls as white.
- [x] Briefly show rejected placement attempts as light red.
- [x] Render mouse start and cheese cells distinctly from regular cells.
- [x] Display the 30 second build timer.
- [x] Add a start/run control so the player can launch the mouse early.

### 4. Mouse Simulation

- [x] Implement deterministic random mouse movement using the level seed.
- [x] Allow movement to any free neighboring cell, including the previous cell.
- [x] Move once every 0.25 seconds.
- [x] Stop with success when the mouse reaches the cheese.
- [x] Stop with timeout when the run reaches 10 seconds.
- [x] Track exact elapsed solve time and move count.
- [x] Add unit tests for deterministic movement, legal moves, timeout behavior, and cheese arrival.

### 5. Run And Result Flow

- [x] Automatically start the mouse when the build timer expires.
- [x] Lock maze editing during the mouse run.
- [x] Determine pass/fail by comparing elapsed solve time against the 5 second target.
- [x] Treat timeout as pass when timeout exceeds the target time.
- [x] Show result state with pass/fail, exact solve time, and move count.
- [x] Add retry support that resets the level and build timer.
- [x] Add replay support that re-runs the same seeded mouse path from the start.
- [x] Hide next level controls when no next level exists.

### 6. Desktop App Polish

- [ ] Keep milestone 1 visuals simple and functional.
- [ ] Use clear, readable layout for grid, timer, controls, and result state.
- [ ] Ensure the game works at the current desktop window size configured by `modules/lwjgl3`.
- [ ] Preserve existing background music behavior and audio disable options.
- [ ] Confirm mouse-only interaction covers all milestone 1 actions.

### 7. Verification And Packaging

- [ ] Add or update unit tests in `modules/core` for core rules and simulation.
- [ ] Add launcher or integration-adjacent tests only where desktop-specific behavior changes.
- [ ] Run `./gradlew checkAll`.
- [ ] Run `./gradlew :modules:lwjgl3:run` for a manual desktop smoke test.
- [ ] Run `./gradlew nativeImage` to verify desktop packaging.
- [ ] Record any packaging limitations or environment requirements in the README if discovered.

## Later Backlog Parking Lot

- [ ] Add level selection.
- [ ] Add persistent best results between app launches.
- [ ] Add larger grids.
- [ ] Add special block types.
- [ ] Add additional mouse types.
- [ ] Add multiple mice and multiple start positions.
- [ ] Replace simple functional visuals with a retro/pixel visual direction.
