# WEB-09: Add Continuous-Integration Web Verification

Status: complete

Depends on: WEB-07, WEB-08

## Goal

Require the web build and browser smoke suite on pull requests without granting deployment
permissions to untrusted builds.

## Scope

- Extend CI to build the optimized JavaScript webapp after the normal quality gate.
- Run the automated browser smoke suite.
- Upload failure diagnostics and the built site as ordinary workflow artifacts.
- Cache Gradle and browser runtime downloads without caching generated production output.
- Keep the verification job read-only with `contents: read` permissions.

## Acceptance Criteria

- Pull requests cannot pass CI when TeaVM compilation or the browser smoke suite fails.
- The built site can be downloaded from successful CI runs for review.
- CI still runs the existing quality gate and Native Image build.
- The verification job cannot deploy Pages.

## Verification

- Validate the workflow syntax locally where tooling permits.
- Exercise both a successful run and an intentional browser-test failure on a temporary branch.
- Confirm uploaded artifacts contain no credentials or machine-local paths.

## Completion Notes

Completed on 2026-08-03.

- Extended the existing read-only CI job to run `qualityGate`, `webBuild`, and `nativeImage` in one
  Gradle invocation, preserving all existing verification while making TeaVM failures blocking.
- Uploaded the verified static site as an ordinary workflow artifact and explicitly retained the
  hidden `.nojekyll` file without granting Pages, deployment, or token-write permissions.
- Kept the existing Gradle and Playwright caches scoped to dependencies rather than generated
  production output, and retained conditional browser failure evidence uploads.
- Validated the workflow with a YAML parser and actionlint 1.7.9.
- Passed `qualityGate webBuild`; passed `nativeImage` with the SDKMAN GraalVM 21 installation.
- Exercised an intentional missing-`app.js` browser failure locally and confirmed that it produced
  both the browser log and failure screenshot before restoring the artifact.
- Scanned the assembled site for credentials, machine-local paths, and symbolic links with no
  findings.
- Received approval from both general and simplicity-focused reviewers with no remaining findings.
