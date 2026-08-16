# Fixed Authored Cell Contract

Fixed authored cells are immutable level geometry. They share gameplay effects with familiar grid
contents, but they are not player inventory and never appear in the build palette.

## Authoring Model

- `LevelDefinition.fixedCells` is an immutable authored-order list of `FixedCell` values.
- Each value combines one `GridPosition` with one `FixedCellType`.
- `FixedCellType` is separate from `PlaceableCellType`, so a future fixed-only effect does not have to
  become a palette tool.
- The existing level constructor defaults the list to empty. All five released levels therefore
  retain their exact boards, supplies, solver traces, ids, and persistence keys.

A definition is rejected when a fixed cell is missing required data, lies outside the grid, shares a
position with another fixed cell, overlaps any solver start or goal, or causes any authored solver to
lose its baseline route.

## Combined Board Semantics

`MazeState.placedCells` contains mutable cells, whether placed by the player or materialized from a
level's preset starting cells. Queries combine that map with the level's fixed cells:

- Fixed Wall and player Wall are equally impassable to path validation and every solver.
- Fixed Slow Floor and player Slow Floor are equally walkable and apply the same one-interval delay.
- Fixed cells do not consume finite supply, reduce an infinite supply, or return inventory.
- A player cell cannot be constructed on a fixed position.

The shared grid pathfinder evaluates both baseline fixed geometry and the final fixed-plus-player
board, including every solver in a multi-solver level.

## Editing Contract

Every placement, replacement, removal, and movement involving a fixed position returns
`REJECTED_FIXED_CELL`. A rejection publishes the existing `MazeState` instance and leaves every
inventory count unchanged. Palette and placed-cell drag previews use that same domain result.

A press on a fixed cell may produce normal rejected tap feedback, but it never becomes a movable-cell
drag because fixed contents are not present in `placedCells`.

## Presentation and Lifecycle

Fixed cells use the same fill and effect mark as their mutable equivalent, with a small line-drawn
lock marker layered above the effect. The shape remains readable without color, while rejected-edit
feedback is drawn afterward and remains visible.

Because fixed cells belong to immutable `LevelDefinition`, they are automatically restored on fresh
attempt, navigation, retry, replay, timer start, and browser reload. Debug and render snapshots carry
the same `MazeState` and require no separate mutable fixed-cell state.

Mutable authored starting cells follow the separate
[preset-cell contract](preset-authored-cells.md); unlike fixed cells, they consume inventory and may
be moved, removed, or replaced after an attempt begins.
