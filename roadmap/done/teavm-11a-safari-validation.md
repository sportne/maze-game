# WEB-11A: Validate the JavaScript Release in Safari

Status: complete

Depends on: WEB-11

## Goal

Replace provisional WebKit compatibility evidence with a release check in current branded Safari
on macOS before any WebAssembly production rollout.

## Scope

- Run the live JavaScript release in current Safari on macOS.
- Verify direct and cache-refreshed navigation, relative assets, audio after interaction, the full
  mouse game loop, local persistence, and reload.
- Exercise touch input in real mobile Safari when an iPhone or iPad test environment is available.
- Update the JavaScript release guide with the Safari version, date, results, and any limitations.

## Acceptance Criteria

- Current macOS Safari completes the supported release checklist without browser or request errors.
- Any Safari-specific limitation is documented and either accepted or assigned a focused fix.
- The JavaScript release guide no longer relies on WebKit automation as its only Safari evidence.

## Verification

- Repeat the live release checklist in `docs/javascript-release.md` on macOS Safari.
- Record whether real mobile Safari was exercised or remains an explicit rollout constraint.
- Re-run `./gradlew qualityGate webBuild` if validation requires a source change.

## Completion Notes

- Added a branded Safari release test driven by Apple's `safaridriver` on GitHub's `macos-15`
  runner after the Pages deployment completes.
- Covered cache-busted live navigation, required assets, Web Audio resume after interaction, the
  complete game loop, local result persistence, and refresh.
- Published the Safari version, platform, saved result, and screenshots as workflow evidence.
- Verified required assets through explicit no-cache requests with successful status and expected
  MIME types. SafariDriver does not expose complete console history, so the check combines the
  page's startup failure state with post-initialization error listeners.
- Kept real iPhone and iPad Safari validation as an explicit rollout constraint because no physical
  mobile device is attached to the runner.
