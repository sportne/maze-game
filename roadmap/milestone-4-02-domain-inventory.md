# M4-02: Add Authored Supplies and Transactional Maze Inventory

Status: pending

Depends on: M4-01

## Goal

Introduce the smallest complete domain model for two placeable cell types, explicit level supplies,
runtime inventory, and atomic place, replace, remove, and move operations.

## Scope

- Replace the normal-only `WallType` with the closed Wall and Slow Floor placeable-cell type and add
  validated finite/infinite supply values.
- Require every `LevelDefinition` to author exactly one supply for each supported placeable type.
- Generalize `MazeState` from a wall set to immutable placed-cell data plus derived remaining counts.
- Add `placeOrReplace`, `remove`, and `move` operations with one result type and specific rejection
  reasons for bounds, protected cells, missing move source, occupied move destination, exhausted
  supply, and path blocking.
- Enforce the accepted consume/return rules and evaluate replacement against one final transaction.
- Treat Slow Floor as walkable and Wall as blocking for path validation.
- Migrate released definitions to infinite Wall and zero Slow Floor without changing behavior.
- Retain wall-specific adapters only where required for an ordered migration; mark and test their
  compatibility boundary rather than maintaining two independent models.
- Do not add session selection, rendering, raw gesture state, or Level 4 production authoring.

## Acceptance Criteria

- Missing, duplicate, negative, or unknown authored supplies fail fast.
- Finite and infinite place/remove/replace examples match the design tables exactly, including
  same-type removal when the selected finite type has zero remaining.
- Move succeeds only to an empty unprotected cell, consumes no inventory, and validates the final
  board atomically; empty/non-placeable source, invalid destination, and no-op cases preserve the
  original immutable state.
- Every rejected edit returns the identical board and inventory values.
- Start and cheese remain protected, and all accepted edits preserve a path.
- Existing levels, wall APIs still in use, and serialization-independent best results remain compatible.

## Verification

- Parameterize domain tests across both placeable types and finite/infinite/zero supplies.
- Cover replacement at zero, exhausted same-type removal, empty/non-placeable move source, occupied
  move destination, source-equals-destination, out-of-grid, protected, path-blocking, equality, and
  defensive-copy behavior.
- Re-run released maze fixtures and architecture rules.
- Run full formatting, analysis, tests, coverage, both browser builds, Pages assembly, and native-image
  packaging before review and commit.
