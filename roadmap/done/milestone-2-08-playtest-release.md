# M2-08: Playtest, Tune, and Release Milestone 2

Status: complete

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

## Completion Evidence

- Accepted the authored parameters without further tuning. Deterministic empty, passing, and timeout
  fixtures retain their documented outcomes, and the debug harness completes both levels.
- JavaScript and WebAssembly release flows complete both levels from isolated clean profiles, then
  verify independent results and unlock state after reload. Portrait, toolbar-constrained landscape,
  and safe-content landscape touch evidence uses the same production artifacts.
- Physical iPhone Safari playtesting accepted both levels in portrait and landscape after the old
  minimum-window gate was removed. Exact device and browser versions were not recorded.
- The initial playtest found the separate place/clear mode awkward. The accepted interaction toggles
  the tapped cell directly, removes the redundant mode control, and retains desktop right-click
  clearing. Physical-iPhone retesting accepted the change and the remaining mobile presentation.
- The iPhone audio observation was traced to Silent Mode and is documented as expected platform
  behavior; no alternate audio path was added.
- [`docs/milestone-2-release.md`](../../docs/milestone-2-release.md) records the accepted parameters,
  playtest matrix, progression behavior, input sizes, known constraints, and release gates.
- Local formatting, static analysis, coverage, tests, browser builds and flows, Pages assembly, and
  native packaging passed. The user-mandated single aggregate push produces the post-commit CI,
  deployment, and branded-Safari evidence required to close the execution cycle.

## Post-Commit Aggregate Gate

M2-08 is committed before deployment because this task cycle permits only one push after all five
task commits. The aggregate push must leave CI, Pages deployment, and branded Safari green. If it
does not, fixes are tested and staged for owner review without another commit or push.
