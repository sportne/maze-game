# WEB-07: Add Automated Browser Smoke Coverage

Status: complete

Depends on: WEB-05, WEB-06

## Goal

Catch failures that JVM unit tests and successful TeaVM compilation cannot detect.

## Scope

- Add a repeatable Java-based browser-test harness, such as Playwright for Java, that serves the
  release artifact over HTTP and remains covered by the existing Java formatting and static
  analysis conventions.
- Fail on page errors, console errors, missing assets, or a missing/non-rendering canvas.
- Automate navigation into Milestone 1, wall placement and clearing, starting the mouse, and reaching
  a result.
- Reload and verify that a saved best result remains available.
- Capture a failure screenshot and browser logs as test artifacts.
- Keep deterministic model assertions in JVM unit tests rather than duplicating them in UI tests.

## Acceptance Criteria

- The smoke suite runs noninteractively on Linux CI.
- Tests operate on the generated release artifact, not a mocked web shell.
- Failures retain enough evidence for diagnosis.
- Browser-test dependencies and downloads are pinned and cached safely.
- The harness is included in `qualityGate`; no unformatted or unlinted JavaScript/TypeScript helper
  code is introduced. If such code becomes necessary, its pinned formatter and linter are added to
  the same gate first.

## Verification

- `./gradlew spotlessApply`
- `./gradlew qualityGate`
- `./gradlew webBuild`
- Run the browser smoke suite twice from a clean browser profile.

## Completion Notes

Completed on 2026-08-03.

- Added a Java Playwright smoke-test module that installs a pinned Chromium headless shell, serves
  the optimized TeaVM artifact over an embedded loopback HTTP server, and runs inside the existing
  Java formatting and static-analysis conventions.
- Automated release-artifact startup, canvas rendering, Milestone 1 navigation, wall placement and
  clearing, mouse start, result persistence, reload, and saved-result availability without
  duplicating deterministic maze-model assertions.
- Made the suite fail on page or console errors, failed requests, HTTP errors, missing required
  assets, and absent or non-rendering canvas output.
- Captured a live-page screenshot and browser error log on failure, uploaded that evidence from
  failed CI runs, and verified the evidence path by intentionally withholding the generated script.
- Added a Playwright browser cache keyed by the pinned dependency catalog and a separate Linux
  system-dependency installation step for CI.
- Included the smoke module in `qualityGate` and added the `browserSmokeTest` convenience task.
- Passed `spotlessApply`, `qualityGate`, `webBuild`, PMD, CPD, Checkstyle, SpotBugs, and two forced
  smoke-suite executions using fresh browser contexts.
- Received approval from both general and simplicity-focused reviewers with no remaining findings.
