# Milestone 2 Release Baseline

Milestone 2 is the first two-level Maze Game release. It keeps the original random mouse and normal
walls, adds a harder 7x7 level, and makes the complete progression practical on desktop and phone
layouts. The player wins by delaying the mouse beyond the target time while preserving at least one
path to the cheese.

## Accepted Levels

| Level | Grid | Build time | Target | Maximum run | Seed | Intended role |
| --- | ---: | ---: | ---: | ---: | ---: | --- |
| Milestone 1 | 5x5 | 30 seconds | More than 5 seconds | 10 seconds | 1 | Easy introduction |
| Milestone 2 | 7x7 | 25 seconds | More than 6 seconds | 15 seconds | 38 | Harder deliberate detour building |

Passing Milestone 1 unlocks Milestone 2. Each level retains its own best passing time and move count
in the current browser profile. Retry starts a new build, replay repeats the completed seeded run,
and the next-level action appears only after a pass when another authored level exists.

During the build phase, tap or click an empty grid cell to place a wall. Tap or click the occupied
cell again to clear it. Desktop right-click clearing remains available as a shortcut.

No parameter tuning was needed during release review. The deterministic Level 2 fixtures still
separate the intended outcomes: the empty board reaches the cheese in 3.00 seconds and fails;
accepted nine-wall layouts reach it in 8.50 or 9.50 seconds and pass; and the boundary layout times
out at 15.00 seconds and passes. See the
[level-design record](milestone-2-level-design.md) for coordinates and diagrams.

## Playtest and Compatibility Evidence

Evidence accepted on 2026-08-08:

| Environment | Orientations or viewport | Outcome |
| --- | --- | --- |
| Desktop JVM debug harness | 1280x720 landscape | Completed both levels, including unlock, retry, replay, and next-level navigation |
| Chromium JavaScript release | Desktop plus 390x844 portrait, 844x286 toolbar-constrained landscape, and 756x286 safe-content landscape | Completed both levels with mouse and real touch events; persistence and reload passed |
| Chromium WebAssembly preview | Same desktop and touch viewports | Matched the JavaScript two-level flow, layout, persistence, and reload behavior |
| Physical iPhone Safari, JavaScript release | Portrait and landscape; model, iOS version, and exact viewport were not recorded | Product-owner playtest accepted both levels, control comfort, difficulty progression, and the revised tap-again-to-clear interaction |
| Branded Safari on macOS | Desktop plus scripted portrait and landscape resize | The Pages workflow validates the deployed two-level JavaScript and WebAssembly flows and records exact runner evidence after every release push |

The physical-iPhone playtest verifies the Safari presentation and the same responsive control system
used by both levels. It found that a separate place/clear mode was awkward on touch, so the accepted
release toggles a cell directly: the first tap places a wall and the second clears it. The playtest
accepted Level 1 as introductory and Level 2 as noticeably harder without requiring another rule.

The responsive contract keeps primary buttons at least 44x44 CSS pixels and editable cells at least
32x32 CSS pixels. Level 2 cells measure 51 CSS pixels in the portrait reference and 34 CSS pixels in
the constrained-landscape references. The retained browser screenshots cover the final Level 2
result in each layout.

## Known Constraints

- JavaScript remains the production default. WebAssembly is an opt-in preview, and real iPhone or
  iPad Safari has not yet been verified for that preview.
- Safari on iPhone can suppress game music while the phone is in Silent Mode even when the browser
  reports that Web Audio is running. Disable Silent Mode to hear the music; this is expected platform
  behavior and does not require a separate audio implementation.
- Best results use local browser storage. They do not synchronize between devices or profiles and
  may disappear when site data is cleared or evicted.
- Keyboard navigation and comprehensive screen-reader support are not release requirements for this
  milestone. Pointer and touch interaction are the supported game inputs.
- The game gives a short objective statement and immediate path-preservation feedback; an extended
  tutorial is intentionally deferred.

## Release Gates

The accepted local release passes formatting, static analysis, unit and integration tests,
coverage verification, architecture checks, JavaScript and WebAssembly production builds and browser
flows, Pages assembly, and GraalVM native-image packaging. Under the single-push execution plan, the
aggregate push for M2-04 through M2-08 supplies the final GitHub Actions, Pages deployment, and
branded-Safari evidence for the same commit series after the task commits are complete.
