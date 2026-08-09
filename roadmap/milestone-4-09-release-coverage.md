# M4-09: Extend Cross-Platform Release Coverage

Status: pending

Depends on: M4-08

## Goal

Make CI fail when inventory, palette interaction, drag cancellation, fourth-level progression, or
either TeaVM target regresses.

## Scope

- Extend the shared JavaScript and WebAssembly production scenario through all four levels.
- Exercise finite and infinite display, selection placement, palette drag, replacement, tap-again
  removal, existing-item movement, invalid drop, cancellation, timer lock, and independent result
  persistence without copying exhaustive domain assertions into browser tests.
- Use real touch events at 390x844 portrait, 844x286 constrained landscape, and the safe-landscape
  contract; retain viewport-specific screenshots and failure evidence.
- Extend live branded-Safari validation to the new level and both browser targets.
- Verify required assets, MIME types, runtime errors, reload, existing-profile migration, JavaScript
  fallback, artifact budgets, and safe-area layout.
- Keep runtime bounded by one shared scenario and deterministic debug hooks.

## Acceptance Criteria

- CI cannot pass if drag and click placement diverge, inventory changes during a move, a cancelled
  gesture commits, or either runtime omits Slow Floor behavior.
- JavaScript and WebAssembly preserve all four per-level results across reload and migration.
- Desktop and every supported touch viewport complete the fourth level with readable, usable controls.
- Safari evidence records browser/platform, four level results, palette interactions, and orientations.
- JavaScript remains the production root and WebAssembly remains an atomically deployed preview.

## Verification

- Run production JS/Wasm browser flows from isolated clean and existing three-level profiles.
- Retain semantic evidence plus success and viewport-specific failure screenshots/logs.
- Run the desktop debug harness, complete quality gate, Pages assembly, fallback test, artifact budgets,
  branded Safari where available, and native-image packaging before review and commit.
