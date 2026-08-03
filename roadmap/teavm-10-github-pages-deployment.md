# WEB-10: Deploy the JavaScript Site to GitHub Pages

Status: complete

Depends on: WEB-09

## Goal

Publish successful `main` builds to the repository's GitHub Pages site using GitHub's supported
artifact deployment flow.

## Scope

- Add a Pages workflow or protected deploy job triggered by pushes to `main` and manual dispatches.
- Build from source rather than reusing artifacts from untrusted pull-request workflows.
- Use `actions/configure-pages`, `actions/upload-pages-artifact`, and `actions/deploy-pages` at pinned
  supported major versions.
- Grant only `contents: read`, `pages: write`, and `id-token: write` where required.
- Deploy through the `github-pages` environment with concurrency that does not cancel an active
  production deployment.
- Document the one-time repository setting that selects GitHub Actions as the Pages source.

## Acceptance Criteria

- A successful `main` workflow publishes `https://sportne.github.io/maze-game/`.
- Pull requests cannot deploy or obtain Pages write permissions.
- A failed build or smoke test cannot replace the current live site.
- The deployment exposes its final URL in the workflow environment.
- The published site loads all assets from the repository subpath.

## Verification

- Validate workflow syntax and least-privilege permissions.
- Perform a manual-dispatch deployment before enabling automatic `main` deployment.
- Run the production browser smoke suite against the published URL.

## Completion Notes

Completed on 2026-08-03.

- Added a source-built Pages workflow for trusted `main` pushes and manual dispatches, with the
  existing quality gate and browser smoke suite required before the deploy job can run.
- Used GitHub's supported `configure-pages@v5`, `upload-pages-artifact@v4`, and `deploy-pages@v4`
  flow with non-cancelling production concurrency and the `github-pages` environment URL.
- Kept the build job read-only and isolated `pages: write` plus `id-token: write` to the dependent
  deploy job; pull requests cannot invoke the workflow or receive deployment permissions.
- Documented the one-time Pages source setting and the distinction between the assembled site's
  `.nojekyll` compatibility marker and the Actions uploader's direct static deployment.
- Enabled GitHub Actions as the repository's Pages source, then completed the first trusted `main`
  deployment successfully at `https://sportne.github.io/maze-game/`.
- Confirmed the independent CI workflow passed its quality, web artifact, and Native Image build.
- Verified the live document, JavaScript, CSS, favicon, sprite, and audio URLs return successful
  responses with the expected content types from the `/maze-game/` repository path.
- Passed a live Chromium game-flow smoke test, including asset loading, canvas initialization,
  completing a run, persistence, and reload with no browser or request errors.
- Received approval from both general and simplicity-focused reviewers after resolving Pages API
  permission placement and documenting the official uploader's dotfile behavior.
