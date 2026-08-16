# Preset Mutable Cell Contract

Preset cells are level-authored starting contents that become ordinary mutable inventory when an
attempt begins. Together with empty cells and fixed cells, they complete the three grid ownership
states:

- Empty cells have no starting effect and accept normal placement.
- Fixed cells are immutable level geometry, do not consume supply, and display a lock marker.
- Preset cells start occupied, consume normal supply, and can be moved, removed, or replaced.

## Authoring and Validation

`LevelDefinition.presetCells` is an immutable authored-order list of `PresetCell` values. Each value
combines a `GridPosition` with a `PlaceableCellType`; preset-only effects are intentionally not
supported because the cell must participate in ordinary palette inventory after initialization.

A level definition is rejected when a preset is null, outside the grid, duplicated, overlaps a
solver start or goal, overlaps a fixed cell, exceeds its type's finite authored supply, or combines
with fixed Walls to remove any solver's baseline route. Infinite supply accepts any otherwise-valid
preset count.

A type whose entire finite supply is already represented by presets remains absent from the
palette, preserving the rule that only types with at least one unit available at level start are
shown. Players can still reposition those presets directly on the grid.

Existing level constructors default presets to an empty list, preserving source compatibility and
the authored state of levels that do not opt in.

## Attempt and Inventory Semantics

`MazeState.initial(level)` materializes every preset into `placedCells`. Remaining inventory is then
derived from the complete mutable map, so a finite supply of six with two presets begins with four
remaining. `MazeState.empty(level)` remains available for tests and balance analysis that explicitly
need a board without presets.

Once materialized, preset origin is not retained in runtime state. This is deliberate: moving a
preset does not leave a special source behind, removing it returns supply, replacing it exchanges
inventory between types, and a newly placed cell is behaviorally identical to one that began on the
board. Ordinary path validation remains atomic for every operation.

## Lifecycle and Presentation

Fresh level selection, retry, navigation away and back, and application reload reconstruct the
authored presets. Replay retains the exact edited board from the completed run, matching existing
player-placement behavior.

Preset cells use the normal Wall or Slow Floor rendering without a lock marker. Their ordinary
appearance and drag behavior communicate mutability; fixed cells remain the only contents with the
non-color lock treatment. Debug, desktop pointer, touch, JavaScript, WebAssembly, and Safari paths
all consume the same materialized `MazeState`.

Preset Walls now shape the introductory tutorials and selected later levels. They consume authored
Wall supply, which leaves only the intended one or two tutorial edits in the palette while still
allowing the initial maze to be rearranged. Level 10 also uses ten preset Walls alongside fixed
geometry. Exact released coordinates and remaining inventory are recorded in the
[level progression design](level-progression-design.md).
