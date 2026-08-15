# Milestone 3: Scout Solver

Status: complete

## Goal

Add one visually distinct, deterministic solver whose discoverable search pattern changes how players
shape a maze, while preserving the released three-platform progression and the existing normal-wall
build interaction.

## Product Decisions

- The player-facing solver name is **Scout**; the internal behavior value is `LEFT_PRIORITY`.
- Scout starts facing north and chooses the first open direction in the fixed order left, straight,
  right, back, but the initial UI does not reveal that order.
- Milestone 3 adds a third authored 7x7 level so the new behavior—not a larger grid or new block—is
  the main challenge.
- Passing Milestone 2 unlocks the third level, which stores an independent best passing result under
  the stable `milestone-3` id.
- Existing levels retain the deterministic random solver and their exact replay behavior.
- Scout uses the basic squirrel paired with an acorn, making it recognizable without color alone
  while avoiding a visual spoiler. Random uses the classic mouse artwork paired with cheese.
- Level-selection cards show only the level name and best result. The build screen names Scout but
  provides no behavioral description. Result feedback may encourage watching intersections but does
  not disclose the priority order.

The complete behavior specification is recorded in
[`docs/milestone-3-mouse-design.md`](../docs/milestone-3-mouse-design.md).

## Out of Scope

- Multiple block or grid-cell types.
- Finite block inventories or per-level palettes.
- Dragging blocks from a palette or repositioning placed blocks by drag.
- Multiple solvers in one level or player-selected solver behavior.
- A behavior registry, plugin system, or externally scripted solver AI.
- Changes to the existing win condition, result format, or persistence schema.

## Definition of Done

- Scout's direction, movement, backtracking, timing, timeout, and replay behavior match the accepted
  deterministic specification for every heading and obstruction case.
- The level model selects exactly one of the two known solver behaviors without changing existing
  random paths, stable ids, saved results, or unlock state.
- A balanced third level lets players discover and exploit Scout using only normal walls and the
  existing build interaction.
- Scout has a unique, non-color-only visual identity and is named consistently on build, running,
  and result screens; its selection card matches the concise earlier-level format.
- The third level supports unlock, selection, retry, replay, next-level behavior, persistence, reload,
  and clean-profile migration.
- Desktop, portrait and landscape touch, JavaScript, WebAssembly, live Pages, branded Safari, native
  packaging, formatting, static analysis, coverage, and architecture gates remain green.
- Product-owner physical-device review accepts the new level's interface and deliberate-construction
  challenge. Because that reviewer authored Scout's rule, independent first-time discoverability is
  explicitly deferred rather than claimed as proven for this release.

## Task Order

1. [M3-01: Validate Scout and balance the third level](done/milestone-3-01-scout-design.md)
2. [M3-02: Add the minimal solver-behavior contract](done/milestone-3-02-mouse-contract.md)
3. [M3-03: Implement the deterministic Scout simulation](done/milestone-3-03-scout-simulation.md)
4. [M3-04: Add the third authored level and progression](done/milestone-3-04-third-level.md)
5. [M3-05: Give Scout a unique visual and UI identity](done/milestone-3-05-scout-presentation.md)
6. [M3-06: Extend cross-platform release coverage](done/milestone-3-06-release-coverage.md)
7. [M3-07: Playtest, tune, and release Milestone 3](done/milestone-3-07-playtest-release.md)
8. [M3-08: Define Milestone 4](done/milestone-3-08-define-milestone-4.md)

M3-08 is non-gating follow-up planning for the next milestone. Milestone 3 product work is complete
after M3-07; the final card turns the accepted Milestone 4 direction into a reviewed task-card set.

## Future Direction

Milestone 4 replaces the single implicit wall tool with Wall and Slow Floor, each with finite or
infinite authored availability. Players can drag a type from a bottom palette or select it and then
click cells, and can drag placed cells around the grid before starting the solver. The accepted
[Milestone 4 plan](milestone-4.md) specifies inventory accounting, validation, responsive input,
domain modeling, testing, and release tasks without implementing them during M3-08.
