# M2-08: Playtest, Tune, and Release Milestone 2

Status: pending

Depends on: M2-01 through M2-07

## Goal

Validate the completed two-level progression with people and real devices, make evidence-based tuning
adjustments, and document a stable Milestone 2 release baseline.

## Scope

- Play Level 1 and Level 2 repeatedly on desktop and at least one real iPhone in portrait and
  landscape.
- Confirm that Level 1 teaches the objective and Level 2 increases difficulty without adding rules
  the interface has not explained.
- Observe wall editing, touch accuracy, text readability, completion rate, retry behavior, and time
  spent in each phase.
- Tune only authored parameters or responsive layout values supported by the observations; defer new
  mechanics and broad visual redesign.
- Re-run deterministic simulation fixtures after every level-parameter change.
- Update player-facing and developer documentation, support claims, screenshots, and known
  limitations.
- Record the accepted parameters and browser/device evidence as the baseline for Milestone 3
  planning.

## Acceptance Criteria

- Real-device play confirms that controls are comfortably usable in supported portrait and landscape
  layouts.
- Level 1 remains easy for a first-time player and Level 2 is noticeably harder but passable through
  deliberate maze construction.
- Deterministic replay and recorded best results remain stable after final tuning.
- No unresolved severity-high gameplay, progression, mobile, persistence, or release defects remain.
- Documentation accurately describes both levels, unlock behavior, supported platforms, and known
  constraints.
- CI, Pages deployment, JavaScript, WebAssembly, Safari validation, and native packaging are green for
  the accepted release commit.

## Verification

- Record the devices, browsers, orientations, level parameters, and qualitative playtest outcomes.
- Re-run representative passing and failing layouts against the final authored definition.
- Complete both levels from a clean profile and an existing Milestone 1 profile.
- Run the full quality and release gates after the last tuning change.
- Review the release evidence against every Milestone 2 definition-of-done item.
