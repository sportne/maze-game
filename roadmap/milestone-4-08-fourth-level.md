# M4-08: Add the Fourth Authored Level and Progression

Status: pending

Depends on: M4-01, M4-03, M4-04, M4-05, M4-06, M4-07

## Goal

Ship the accepted M4-01 parameters as a fourth level that teaches finite/infinite inventory and both
placement mechanisms without changing earlier progression or saved results.

## Scope

- Add the stable `milestone-4` definition with the exact accepted geometry, existing mouse behavior,
  timing, and Wall/Slow Floor supplies.
- Unlock it only after a saved Milestone 3 pass through catalog-order progression.
- Keep its best result independent under the existing persistence format.
- Present the same concise level card format and teach palette mechanics in bounded build feedback,
  without explaining Scout's hidden rule if Scout is selected by the accepted design.
- Ensure retry, replay, Back, next-level absence, clean profile, and pre-M4 profile migration work.
- Use the accepted fixtures directly; do not rebalance production values in this implementation card.

## Acceptance Criteria

- Clean and pre-Milestone-3 profiles keep Level 4 locked; a profile with a Milestone 3 pass starts
  with it unlocked.
- Completing Level 4 preserves all earlier best results and stores only its own result key.
- The production empty, passing, failing, replacement, movement, and timeout outcomes match M4-01.
- The level requires deliberate use of both types within the accepted inventory.
- Retry restores supply and replay reproduces the completed final maze and exact mouse result.

## Verification

- Add catalog, unlock/migration, independent persistence, navigation, replay, and fixture tests.
- Extend the desktop debug harness through all four levels and both placement mechanisms.
- Re-run all three released level results, Random paths, and Scout traces unchanged.
- Run full formatting, analysis, coverage, browser builds, Pages assembly, and native-image packaging
  before review and commit.
