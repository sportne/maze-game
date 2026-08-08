# M3-04: Add the Third Authored Level and Progression

Status: pending

Depends on: M3-01, M3-03

## Goal

Add the accepted Scout level to the production catalog and carry its identity through the complete
unlock, play, result, replay, and persistence lifecycle.

## Scope

- Add the accepted `milestone-3` 7x7 definition and final timing parameters from M3-01.
- Select Scout for the new level while keeping Milestone 1 and 2 on the random mouse.
- Unlock the third level by passing Milestone 2 using the existing catalog-order derivation.
- Preserve independent best results under the new stable id without changing saved-data format.
- Support selection, build, start, auto-start, result, retry, replay, next-level navigation, return to
  selection, restart, and reload.
- Confirm clean profiles and pre-Milestone-3 profiles without a saved Milestone 2 pass begin with the
  third level locked. Profiles that already contain a passing Milestone 2 result begin with the third
  level unlocked without requiring another pass.
- Keep block inventories, new cell contents, and alternate build interactions out of this task.

## Acceptance Criteria

- The catalog contains exactly three levels in stable order and resolves `milestone-3` by id.
- A Milestone 2 pass unlocks Milestone 3; earlier results cannot unlock it out of order.
- Each level retains an independent best result and replay uses its authored mouse behavior.
- Retry and replay retain the third level and exact accepted maze/run state where appropriate.
- Passing the final level exposes no nonexistent next-level action.
- Existing persisted Milestone 1 and 2 results remain readable and semantically unchanged.

## Verification

- Parameterize catalog, progression, persistence, session, retry, replay, and result tests across all
  three authored levels.
- Add clean-profile, pre-Milestone-3 locked-profile, and already-passed-Milestone-2 migration
  scenarios.
- Run the desktop debug harness through unlock and complete play of the third level.
- Re-run every accepted balancing fixture against the production catalog entry.
- Run formatting, static analysis, tests, coverage, architecture checks, browser builds, Pages
  assembly, and native-image packaging before commit.
