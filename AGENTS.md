# AGENTS.md

These instructions apply to the entire repository.

## Project Overview

TimeTask Pro V2 is an offline-first Android productivity app built with Kotlin, Jetpack Compose, Room, DataStore, Coroutines/Flow, Ktor, and Clean Architecture/MVVM patterns.

The app is currently in active alpha. Core productivity modules exist, and the AI layer is being developed around privacy-preserving OpenAI-backed workflows for task parsing, day planning, and weekly summaries.

## Repository Map

- Android app module: `app/`
- Kotlin source: `app/src/main/java/com/timetask/pro/v2/`
- Room schemas: `app/schemas/`
- Public documentation: `docs/`
- GitHub templates and workflows: `.github/`
- Local-only planning workspace: `ai_workspace/` (ignored by Git; do not publish)

Important AI-related paths:

- OpenAI network client: `app/src/main/java/com/timetask/pro/v2/core/network/openai/`
- AI data adapters: `app/src/main/java/com/timetask/pro/v2/data/ai/`
- AI domain models: `app/src/main/java/com/timetask/pro/v2/domain/model/ai/`
- AI use cases: `app/src/main/java/com/timetask/pro/v2/domain/usecase/ai/`

## Build and Verification

Use the Gradle wrapper from the repository root.

On Windows:

```powershell
.\gradlew.bat :app:compileDebugKotlin --no-daemon
.\gradlew.bat test --no-daemon
```

On Linux/macOS:

```bash
./gradlew :app:compileDebugKotlin --no-daemon
./gradlew test --no-daemon
```

Notes:

- The project expects JDK 21 to be available through `JAVA_HOME` or the active shell.
- Gradle auto-increments `app/version.properties` during build configuration. If you only changed documentation, avoid running a full build unless verification is required.
- If Android SDK 36.1 is unavailable, install `platforms;android-36.1` and `build-tools;36.1.0`.

## Engineering Rules

- Keep changes focused on the requested task.
- Follow existing package names, architecture boundaries, and Compose style.
- Do not rewrite working screens, navigation, data models, or Gradle setup unless the task explicitly requires it.
- Do not modify Room entities without adding the required migrations and schema updates.
- Do not commit generated build outputs, APK/AAB files, `.gradle/`, `.idea/`, `local.properties`, secrets, or `ai_workspace/`.
- Kotlin identifiers, packages, classes, and functions must remain in English.
- User-facing Android UI strings should follow the existing app locale unless a localization task says otherwise.
- Code comments should stay concise and match the language already used in the surrounding file.

## AI and Privacy Rules

- Never hard-code OpenAI API keys or other credentials.
- Keep OpenAI-backed behavior behind domain-level interfaces and repository implementations.
- Prefer structured outputs for model responses that become app actions.
- AI suggestions must be reviewable before they write to the local Room database.
- Send the minimum useful user context for an AI action.
- Preserve local fallback behavior where practical so the app remains useful without network access.

## Pull Request Checklist

Before opening or updating a pull request:

- Confirm the diff is focused and does not include local/private files.
- Run the smallest relevant Gradle verification command.
- Update `README.md`, `CHANGELOG.md`, or `docs/` when public behavior, setup, or architecture changes.
- Add or update tests when changing behavior that can be tested safely.
- Mention any skipped verification and the reason.
