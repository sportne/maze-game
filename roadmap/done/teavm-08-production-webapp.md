# WEB-08: Produce a Release-Quality Static Site

Status: complete

Depends on: WEB-05, WEB-06

## Goal

Turn the generated TeaVM output into a polished, cache-safe, GitHub Pages-compatible site.

## Scope

- Add a maintained HTML/CSS shell with a responsive canvas container and accessible loading state.
- Add pinned HTML and CSS formatting/static-analysis tools, commit their lock file when applicable,
  and wire them into `qualityGate` before adding maintained shell files.
- Add a favicon and concise fallback message for unsupported browsers.
- Ensure every runtime URL is relative so the site works at `/maze-game/` as well as `/`.
- Add `.nojekyll` to the deployed artifact.
- Prevent source-only assets and development source maps from entering production output.
- Record compressed and uncompressed artifact sizes and define a regression budget.

## Acceptance Criteria

- `index.html` exists at the root of the Pages artifact.
- The site works from a local `/maze-game/` URL prefix with no 404s.
- Production output includes optimized JavaScript and no editable source art.
- Loading and failure states are understandable without opening developer tools.
- The artifact contains no symbolic links.
- Maintained HTML and CSS pass the repository-level formatter and static analysis when
  `qualityGate` runs.

## Verification

- `./gradlew spotlessApply`
- `./gradlew qualityGate`
- `./gradlew webBuild`
- Browser smoke tests against a `/maze-game/` path prefix.
- Inspect the final artifact manifest and sizes.

## Completion Notes

Completed on 2026-08-03.

- Added a maintained, responsive HTML/CSS shell with accessible loading, startup-failure,
  unsupported-browser, small-viewport, and no-JavaScript guidance plus a favicon.
- Added exact-version Prettier, html-validate, and Stylelint tooling with a committed npm lockfile;
  web formatting and validation now run through `spotlessApply` and `qualityGate`.
- Assembled the maintained shell after TeaVM generation for both `webBuild` and `webRun`, using only
  relative URLs and including `.nojekyll` for GitHub Pages.
- Kept loading visible through asset initialization until the first game frame, and verified that a
  simulated unavailable WebGL context reveals the failure message instead of a blank canvas.
- Pruned development metadata, source maps, classes, and source-art formats; verified that the
  artifact contains no symbolic links.
- Recorded a sorted artifact manifest with 4,828,502 uncompressed bytes and 3,938,845 aggregate
  gzip bytes, below the defined 6,500,000 and 4,500,000 byte regression budgets.
- Passed `spotlessApply`, `qualityGate`, `webBuild`, the `/maze-game/` browser smoke flow, and the
  development-server task-order check.
- Received approval from both general and simplicity-focused reviewers with no remaining findings.
