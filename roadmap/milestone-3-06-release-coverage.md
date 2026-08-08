# M3-06: Extend Cross-Platform Release Coverage

Status: pending

Depends on: M3-04, M3-05

## Goal

Make CI fail if Scout's behavior, visual identity, third-level progression, or any supported runtime
regresses.

## Scope

- Extend desktop debug or smoke coverage through all three levels without copying domain-level
  direction assertions into UI harnesses.
- Extend JavaScript and WebAssembly production flows through Milestone 2 unlock, Scout selection,
  representative maze editing, completion, independent persistence, retry, replay, and reload.
- Exercise the third level with real touch events at portrait and toolbar-constrained landscape sizes.
- Assert that the selected mouse name and unique rendered appearance change with the level.
- Extend live branded-Safari validation to both browser targets and the three-level progression.
- Retain viewport-specific screenshots, logs, runtime errors, required-asset status/MIME evidence, and
  failure diagnostics.
- Keep runtime reasonable by reusing one shared scenario and leaving exhaustive movement rules in core
  tests.

## Acceptance Criteria

- CI cannot pass if either TeaVM target uses the random mouse for Milestone 3 or Scout for an earlier
  level.
- The release flow proves third-level unlock and per-level results survive reload without contaminating
  Milestone 1 or 2 data.
- Portrait and constrained-landscape touch flows edit and complete the Scout level with usable
  controls and readable behavior text.
- Live Safari evidence names the browser/platform and records all three level and orientation results.
- JavaScript fallback and the WebAssembly preview remain independently startable and atomically
  deployed.

## Verification

- Run complete JavaScript and WebAssembly browser suites against production artifacts.
- Run the desktop debug harness through the three-level sequence.
- Exercise clean storage, existing two-level storage, locked selection, retry, replay, and reload.
- Inspect retained desktop and mobile evidence for the correct Scout sprite and non-spoiler labels.
- Run formatting, static analysis, tests, coverage, architecture checks, Pages assembly, branded
  Safari validation where available, and native-image packaging before commit.
