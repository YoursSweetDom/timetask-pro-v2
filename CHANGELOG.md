# Changelog

All notable public changes to TimeTask Pro V2 will be documented in this file.

## Unreleased

### Added

- Added unit tests for OpenAI response text extraction, AI input normalization, local fallback parsing, and oversized-context rejection.
- Added GitHub Actions security scan for accidental API keys, tokens, and private-key blocks in tracked files.

### Changed

- Updated Android CI to run unit tests before compiling debug Kotlin.
- Improved AI task parsing input validation by normalizing whitespace, trimming known labels, deduplicating labels case-insensitively, and limiting oversized AI input.
- Improved local fallback parsing with normalized multiline input, case-insensitive tag deduplication, English/Ukrainian priority terms, and English fallback warnings.

## v0.1.0-alpha

First public alpha release.

### Added

- Added root `.gitignore` for Android, Gradle, local secrets, build outputs, APKs, and local planning files.
- Added MIT `LICENSE`.
- Added public `README.md` with project overview, current status, architecture, roadmap, build instructions, privacy principles, and AI roadmap.
- Added `CONTRIBUTING.md`, `CODE_OF_CONDUCT.md`, `SECURITY.md`, GitHub issue templates, and pull request template.
- Added AI task parsing foundation with domain contracts, OpenAI Responses API client, Structured Outputs schema, OpenAI repository, and local rule-based fallback.
- Added `docs/AI_ASSISTANT_DESIGN.md`.
- Added root `AGENTS.md` with Codex/agent contribution guidance, build commands, privacy rules, and repository boundaries.
- Added GitHub Actions Android CI workflow for `:app:compileDebugKotlin`.
- Removed committed machine-specific Gradle Java home fallback so public builds can use the active JDK 21 environment.
- Added `docs/ECOSYSTEM.md` to document the future optional TimeTask Pro V2 + RefrAIct AI Android companion direction and privacy boundaries.

### Notes

- The Android app is in active alpha.
- Core productivity modules are implemented in Kotlin and Jetpack Compose.
- OpenAI-backed assistant behavior is planned, but not yet implemented as a production feature.
- RefrAIct AI Android is a separate project and is not a runtime dependency of TimeTask Pro V2.
