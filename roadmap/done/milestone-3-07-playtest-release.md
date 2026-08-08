# M3-07: Playtest, Tune, and Release Milestone 3

Status: complete

Archived: yes

Depends on: M3-01 through M3-06

## Goal

Validate that people can discover Scout's priority, intentionally build against it, and find the
third level distinct and fair before accepting the Milestone 3 release baseline.

## Scope

- Complete all three levels repeatedly on desktop and at least one physical iPhone in portrait and
  landscape.
- Observe when players infer the left, straight, right, and back choices from repeated runs without
  being told the order.
- Evaluate wall editing, sprite recognition, text readability, completion rate, retries, and time in
  build and run phases.
- Tune only third-level geometry/timing or bounded responsive presentation values supported by
  observations; do not add another mouse or Milestone 4 building mechanics.
- Re-run deterministic direction and balancing fixtures after every authored-parameter change.
- Record accepted parameters, qualitative evidence, screenshots, support claims, and known
  limitations in a Milestone 3 release document.
- Confirm JavaScript remains the production default and the WebAssembly preview remains equivalent.

## Acceptance Criteria

- First-time players can infer and explain Scout's order through observation without initial UI text
  disclosing it.
- The third level feels meaningfully different from the random-mouse levels and is passable through
  deliberate construction rather than trial-only luck.
- Physical-device controls and Scout presentation remain comfortable in portrait and landscape.
- Deterministic replay, progression, and per-level best results remain stable after final tuning.
- No unresolved severity-high gameplay, mobile, persistence, asset, or release defect remains.
- Documentation accurately records the three levels, mouse behaviors, platform support, and known
  constraints.

## Verification

- Record devices, browsers, orientations, accepted level parameters, and playtest outcomes.
- Complete all levels from clean and existing two-level profiles.
- Re-run representative passing, failing, backtracking, and timeout fixtures.
- Run the full formatting, analysis, coverage, test, browser, Pages, Safari, and native-image gates.
- Review every Milestone 3 definition-of-done item before commit, then validate the deployed release
  under the approved push workflow.

## Completion Notes

- The product owner exercised the JavaScript release on a physical iPhone in portrait and landscape
  and accepted the final controls and presentation after a local-network retest.
- Device feedback simplified every level card to its level name and locked/best state, removed the
  pre-run Scout behavior note, and added a build-screen Back action that abandons the attempt and
  returns to level selection.
- The reviewer already knew Scout's authored rule, so the first-time independent-discovery criterion
  could not be tested blindly. Product-owner release approval accepts that limitation without
  representing it as successful discovery evidence.
- Accepted parameters, automated cross-platform evidence, physical-device evidence, and known
  constraints are recorded in
  [`docs/milestone-3-release.md`](../../docs/milestone-3-release.md).
