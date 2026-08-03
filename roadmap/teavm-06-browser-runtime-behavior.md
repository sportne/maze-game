# WEB-06: Harden Browser Audio, Lifecycle, and Persistence

Status: pending

Depends on: WEB-04

## Goal

Make runtime behavior conform to browser policies and degrade safely when optional browser features
are unavailable.

## Scope

- Defer web audio startup until a user gesture while preserving desktop startup behavior.
- Hide or replace the Quit action for a page that cannot close its own tab.
- Pause/resume audio and game updates appropriately when page visibility changes.
- Use a stable, game-specific browser preference prefix.
- Make best-result writes best-effort when local storage is unavailable, blocked, or full.
- Confirm saved results survive reloads and remain isolated from other hosted applications.

## Acceptance Criteria

- No autoplay-policy exception or rejected playback promise reaches the user console.
- Audio can be enabled and disabled after the first interaction.
- The browser UI does not offer a nonfunctional Quit action.
- Storage failures do not crash or interrupt a game session.
- Best results persist across a normal reload when local storage is available.

## Verification

- `./gradlew spotlessApply`
- `./gradlew qualityGate`
- `./gradlew webBuild`
- Manual normal, private-browsing, storage-disabled, and background-tab checks.
