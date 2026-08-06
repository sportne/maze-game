# WEB-06: Harden Browser Audio, Lifecycle, and Persistence

Status: complete

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

## Completion Notes

Completed on 2026-08-03.

- Deferred browser music startup until the first pointer gesture while retaining immediate desktop
  startup, and paused both gameplay updates and active music across page visibility changes.
- Removed the unavailable Quit control from browser layout, rendering, and input routing without
  weakening required-control validation on desktop.
- Added an atomic browser best-result store that writes one complete result under the stable
  `maze-game.best-result.` namespace. Browser-side guards make blocked reads and writes harmless,
  and a regression test verifies a failed improvement cannot erase the prior result.
- Guarded the gdx-teavm page-hide/visibility event race so reloads dispose cleanly without a console
  error.
- Passed `spotlessApply`, the full `qualityGate`, and the optimized `webBuild`.
- Verified gesture-gated audio, audio pause/resume, gameplay pause/resume, normal and incognito
  reload persistence, namespace isolation, forced quota failure with prior-result retention, a
  blocked local-storage getter, and active-audio reload in headless Chromium without page errors.
- Received approval from both general and simplicity-focused reviewers with no remaining findings.
