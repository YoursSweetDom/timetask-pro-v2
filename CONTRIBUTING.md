# Contributing to TimeTask Pro V2

Thank you for considering a contribution. TimeTask Pro V2 is currently in active alpha, so the best contributions are focused, easy to review, and aligned with the local-first Android architecture already in the repository.

## Project Priorities

Good first contribution areas:

- documentation cleanup;
- reproducible Android build setup;
- Room migration tests;
- task filtering and sidebar behavior tests;
- accessibility improvements;
- localization cleanup;
- small UI fixes;
- AI contract design that does not require committing API keys.

Please avoid broad rewrites unless an issue discusses the change first.

## Development Setup

Prerequisites:

- Android Studio with a recent Android SDK.
- JDK 21.
- An Android emulator or Android 8.0+ device.

Build the debug app:

```powershell
.\gradlew.bat assembleDebug
```

Run tests:

```powershell
.\gradlew.bat test
```

Install on a connected device:

```powershell
.\gradlew.bat installDebug
```

Note: the current Gradle setup may update `app/version.properties` during builds. Check your diff before opening a pull request.

## Branches and Commits

Use small, descriptive branches:

```text
fix/task-filter-empty-state
docs/update-ai-roadmap
test/room-migration-coverage
```

Commit messages should describe the change plainly:

```text
Add Room migration test scaffolding
Fix completed task filter count
Document AI task parsing contract
```

## Pull Request Checklist

Before opening a pull request:

- Keep the change focused on one topic.
- Confirm the project builds, or explain why it could not be run.
- Add or update tests when behavior changes.
- Update documentation when public behavior, setup, or architecture changes.
- Do not commit API keys, keystores, local paths, APKs, or build outputs.
- Do not commit `ai_workspace/`; it is a local planning workspace.

## AI Contributions

AI-related contributions should follow the privacy-first direction of the project:

- API keys must stay out of source control.
- Model calls should be hidden behind clear interfaces.
- AI output should be structured and reviewable.
- User confirmation should be required before AI writes changes to local data.
- Requests should send the minimum useful context.

## Code Style

- Follow the existing Kotlin and Jetpack Compose style.
- Prefer existing repository and ViewModel patterns before adding new abstractions.
- Keep comments useful and brief.
- Avoid unrelated formatting-only churn.

## Reporting Issues

When reporting a bug, include:

- app version or commit;
- Android version and device/emulator;
- steps to reproduce;
- expected behavior;
- actual behavior;
- logs or screenshots if useful.

