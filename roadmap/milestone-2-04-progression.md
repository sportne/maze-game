# M2-04: Add Progression, Unlocking, and Per-Level Persistence

Status: pending

Depends on: M2-03

## Goal

Add the smallest persistent progression model needed for Level 1 to unlock Level 2 while keeping best
results isolated by stable level identity.

## Scope

- Define Level 1 as initially unlocked and Level 2 as unlocked after a recorded passing result for
  Level 1.
- Implement the rule generically over an ordered two-entry test catalog; M2-05 integrates the real
  Level 2 definition into that already-tested progression path.
- Prefer deriving unlock state from existing per-level passing results instead of storing a second,
  potentially inconsistent progression record.
- Load and expose the best result and unlock state for each catalog entry through a compact
  progression/presentation model; rendering remains M2-06's responsibility.
- Make next-level availability depend on catalog order, unlock state, and the current passing result.
- Preserve existing Level 1 storage keys and tolerate absent, malformed, or unavailable browser
  storage.
- Keep progression local to the current browser profile or desktop user data; synchronization and
  accounts are out of scope.

## Acceptance Criteria

- A new profile can select Level 1 but not Level 2.
- Passing Level 1 unlocks Level 2 immediately and the unlock survives application restart.
- Failing Level 1 does not unlock Level 2.
- Starting or restoring a session after the store no longer contains the qualifying Level 1 result
  returns Level 2 to the locked state without stale secondary state; no in-game clear command is
  required.
- Each level loads, saves, compares, and exposes only its own best result.
- Replay does not overwrite best results, matching existing behavior.
- Storage failure never prevents the player from continuing the current session.

## Verification

- Add state tests with a two-entry catalog fixture for initial, failed, newly unlocked, and restored
  progression.
- Add persistence tests for independent level IDs, malformed data, and storage failure.
- Add result-flow tests for next-level availability from the first entry and its absence at the final
  entry.
- Confirm existing saved Level 1 results unlock Level 2 without migration or key changes.
