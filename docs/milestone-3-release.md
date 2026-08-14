# Milestone 3 Release Baseline

Milestone 3 is the three-level Maze Game release. The player must delay each mouse beyond the
target time while preserving at least one path to the goal. The third level introduces Scout, a
visually distinct deterministic mouse whose search behavior is learned by watching its runs rather
than by reading an explanation before play.

## Accepted Levels

| Level | Grid | Build time | Target | Maximum run | Mouse | Seed | Intended role |
| --- | ---: | ---: | ---: | ---: | --- | ---: | --- |
| Milestone 1 | 5x5 | 30 seconds | More than 5 seconds | 10 seconds | Random | 1 | Easy introduction |
| Milestone 2 | 7x7 | 25 seconds | More than 6 seconds | 15 seconds | Random | 38 | Harder deliberate detour building |
| Milestone 3 | 7x7 | 25 seconds | More than 6 seconds | 8 seconds | Scout | 53 | Deterministic observation challenge |

Passing a level unlocks the next catalog entry. Each level retains its own best passing time and
move count in the current browser profile. Retry starts a new build, replay repeats the completed
run, and next-level navigation appears only when another authored level exists.

Scout starts facing north and uses one deterministic relative-direction priority internally. The
production UI deliberately does not state that order. Scout uses the basic squirrel and an acorn;
Random uses the classic mouse and cheese. Their silhouettes distinguish the behaviors without
relying on color alone or visually revealing Scout's rule.
The complete engineering rule, comparison evidence, and accepted traces are in the
[Scout design record](milestone-3-mouse-design.md).

The accepted Scout fixtures use the unchanged normal-wall interaction. Four walls produce a
6.50-second, 26-move pass; five produce a 7.50-second, 30-move pass; and a six-wall viable-path
layout reaches the 8-second timeout after 32 moves. These outcomes are deterministic and unchanged
by splitting elapsed time across update calls.

## Accepted Interface Tuning

Physical-device review simplified every selection card to the same two-line structure: level name
and locked or best-result state. Scout's former pre-run search-pattern sentence was removed from
both selection and build presentation, leaving the common objective and Scout's visual/name identity.

The build screen now includes **Back**, which returns to level selection and discards the abandoned
attempt, alongside **Start**. Tap or click an empty grid cell to place a wall and tap or click that
same occupied cell again to clear it. Desktop right-click clearing remains an optional shortcut.

## Playtest and Compatibility Evidence

Evidence accepted on 2026-08-08:

| Environment | Orientations or viewport | Outcome |
| --- | --- | --- |
| Desktop JVM debug harness | 1280x720 landscape | Completed all three levels, including unlock, retry, replay, next-level, and independent result behavior |
| Chromium JavaScript release | Desktop plus 390x844 portrait, 844x286 toolbar-constrained landscape, and 756x286 safe-content landscape | Completed all three levels with mouse and real touch events; Scout identity, build Back, persistence, migration, and reload passed |
| Chromium WebAssembly preview | Same desktop and touch viewports | Matched the JavaScript three-level flow, layout, Scout presentation, persistence, migration, and reload behavior |
| Physical iPhone Safari, JavaScript release | Portrait and landscape; model, iOS version, and exact viewport were not recorded | Product-owner interface playtest and retest accepted card density, controls, Scout presentation, removal of the behavior note, and build-screen Back navigation |
| Branded Safari on macOS | Desktop plus scripted portrait and landscape resize | The Pages workflow validates deployed three-level JavaScript and WebAssembly flows and retains runner evidence after every release push |

The physical-iPhone reviewer defined Scout's rule before the playtest, so this session could not be
a blinded first-time discovery study. Product-owner acceptance allows the release to proceed with
that qualitative criterion explicitly unproven; whether a new player independently infers and can
explain the order remains a future observation rather than a claimed result. Deterministic fixtures
do prove that Scout is meaningfully different from the seeded random mouse and that deliberate,
path-preserving layouts can pass the third level.

## Known Constraints

- JavaScript remains the production default. WebAssembly is an opt-in preview, and real iPhone or
  iPad Safari has not yet been verified for that preview.
- Safari on iPhone can suppress game music while the phone is in Silent Mode even when Web Audio is
  running. Disable Silent Mode to hear the music; this is expected platform behavior.
- Best results use local browser storage. They do not synchronize between devices or profiles and
  may disappear when site data is cleared or evicted.
- Keyboard navigation and comprehensive screen-reader support are not release requirements. Pointer
  and touch interaction are the supported game inputs.
- The exact physical-iPhone hardware, iOS version, Safari version, and viewport were not recorded.

## Release Gates

The accepted local release passes formatting, static analysis, unit and integration tests, coverage
verification, architecture checks, JavaScript and WebAssembly production builds and browser flows,
Pages assembly, and GraalVM native-image packaging. The single release push supplies the final
GitHub Actions, Pages deployment, and branded-Safari evidence for the same commit series.
