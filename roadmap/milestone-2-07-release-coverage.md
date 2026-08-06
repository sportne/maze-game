# M2-07: Add Multi-Level End-to-End and Release Coverage

Status: pending

Depends on: M2-01, M2-05, M2-06

## Goal

Extend the existing cross-platform verification so a release cannot silently break mobile controls,
progression, either authored level, or one of the two browser targets.

## Scope

- Cover selection, locked-state rejection, Level 1 completion, Level 2 unlock, Level 2 completion,
  independent persistence, retry, replay, and reload in end-to-end flows.
- Exercise representative portrait and toolbar-constrained landscape viewports with touch interaction,
  not render-only assertions.
- Keep the complete JavaScript and WebAssembly flows aligned and verify their production path and
  MIME behavior.
- Extend desktop smoke or debug-harness coverage through both levels without duplicating domain-level
  assertions.
- Extend live Safari validation to cover the responsive layout and multi-level progression.
- Add a bounded retry for SafariDriver session creation so transient runner startup failures do not
  invalidate an otherwise untested deployment; gameplay assertion failures must never be retried away.
- Retain actionable logs, screenshots, and browser evidence on failure.

## Acceptance Criteria

- CI fails if either production browser target cannot complete the required two-level progression.
- Portrait and constrained-landscape touch flows interact with every essential control and grid.
- The test proves that unlock and best-result state survive reload and remain isolated per level.
- JavaScript fallback behavior and the WebAssembly preview remain independently startable.
- Live Safari evidence identifies the browser version and records both level and orientation results.
- Safari startup retry is limited to session-creation failures and exhausts with the original evidence
  preserved.
- Test duration and duplication remain reasonable for every push.

## Verification

- Run the complete local browser suite for JavaScript and WebAssembly.
- Run desktop unit, architecture, rendering, and debug-harness checks.
- Deliberately exercise a locked card, storage failure, WasmGC fallback, and Safari session-start
  failure handling.
- Run `./gradlew spotlessApply qualityGate pagesBuild nativeImage` with GraalVM 21.
- Confirm the deployed Pages artifact and branded Safari gate pass before completing the task.
