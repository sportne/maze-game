# M2-03: Generalize the Authored-Level Catalog and Selection Contract

Status: pending

Depends on: M2-02

## Goal

Replace the Milestone 1-specific selection path with a small, ordered authored-level catalog that can
select levels by stable identity without introducing a general-purpose content system.

## Scope

- Expose the authored levels as an immutable ordered catalog with lookup by stable level ID.
- Replace `START_MILESTONE_ONE` and index-zero assumptions with a level-selection action carrying a
  stable level identity.
- Let a game session initialize and reset the selected `LevelDefinition` rather than always loading
  `Levels.milestoneOne()`.
- Preserve deterministic retry and replay behavior for the selected level.
- Use test-supplied level definitions to exercise the generic contract until the production Level 2
  definition is added in M2-05.
- Keep catalog construction in code; external level files, plugins, and mod loading are out of scope.
- Preserve dependency boundaries so model and state code remain independent of desktop and browser
  adapters.

## Acceptance Criteria

- Callers can enumerate authored levels in display order and resolve each one by unique stable ID.
- Selecting any known catalog entry initializes its exact level definition and best-result key.
- Retry and replay never fall back to Level 1 accidentally.
- Unknown level identities cannot start a session; locked-level policy remains the responsibility of
  M2-04.
- No production selection or reset path is hard-coded to the first catalog entry.
- Existing Level 1 behavior and saved results remain compatible.

## Verification

- Add catalog tests for order, unique IDs, lookup, and immutability.
- Add input and session tests with multiple supplied definitions and reject unknown selection.
- Add retry and replay tests that assert the selected level is retained.
- Run architecture checks to confirm the generalized contract does not add platform dependencies.
