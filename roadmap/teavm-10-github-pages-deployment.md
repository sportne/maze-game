# WEB-10: Deploy the JavaScript Site to GitHub Pages

Status: pending

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
