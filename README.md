# TimeTask Pro V2

TimeTask Pro V2 is an offline-first Android productivity app built with Kotlin and Jetpack Compose. It combines task management, notes, calendar planning, timers, stopwatches, alarms, reminders, templates, and productivity views in a single local-first workspace.

The long-term goal is to explore privacy-preserving AI workflows for personal productivity: natural-language task capture, day planning, and weekly review summaries where the user stays in control of what context is shared and what actions are written back to the local database.

> Status: active alpha. Core Android modules are implemented, and the OpenAI-backed assistant layer now has initial domain contracts, network client scaffolding, structured-output schema, and local fallback parsing. UI integration is still in progress.

## Ecosystem Direction

TimeTask Pro V2 is planned as the time-management layer of a broader local-first AI productivity ecosystem. A future optional companion integration with RefrAIct AI Android may connect task planning with a personal AI workspace, memory-oriented workflows, model orchestration, and long-running reasoning sessions.

This repository remains standalone: it does not include RefrAIct source code, does not depend on RefrAIct at runtime, and does not silently share task data. See [docs/ECOSYSTEM.md](docs/ECOSYSTEM.md) for the roadmap direction and privacy boundaries.

## Features

- Task management with Inbox, All, Today, Tomorrow, Next 7 Days, folder, tag, category, and custom-filter navigation.
- Task detail editing with title, description, priority, due date, folder selection, pinning, deletion, and estimated time.
- Sidebar workspace with folders, tags, categories, custom filters, pinned quick access, reorder support, and footer sections for completed, canceled, trash, and templates.
- Notes module with color-coded note cards and creation flow.
- Calendar month view with task indicators and day task lists.
- Multi-timer, stopwatch, alarm, reminder, and statistics tools.
- Eisenhower Matrix view mapped from task priority.
- Template management for reusable task structures.
- Local persistence with Room, schema export, migrations, repositories, and app-level preferences through DataStore.
- AI task parsing foundation with OpenAI Responses API contracts, Structured Outputs schema, and local fallback parser.
- Material 3 UI with Jetpack Compose, edge-to-edge layout, adaptive window sizing, splash screen, and debug StrictMode.

## AI Roadmap

TimeTask Pro V2 is designed to become an AI-assisted productivity app without turning private task history into an uncontrolled remote data stream.

Planned AI work:

- Natural-language task parsing: turn text such as "prepare report tomorrow at 15:00 #work" into structured task fields.
- Day planning: suggest a schedule from existing tasks, due dates, priorities, and estimated effort.
- Weekly summaries: generate productivity reviews from user-approved local task history.
- Action confirmation: AI suggestions should produce structured actions that the user can review before they are saved.
- Minimal context sharing: send only the task context needed for the selected AI action.

The repository will keep API keys out of source control. OpenAI-backed implementations should be added behind clean interfaces with local fallback behavior and tests for structured output contracts.

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose with type-safe routes
- Coroutines and Flow
- Room
- DataStore Preferences
- Ktor Client
- kotlinx.serialization
- WorkManager
- Coil
- Gradle Kotlin DSL
- Android Gradle Plugin 9.x
- Kotlin 2.x

## Architecture

The project follows a layered Android architecture:

```text
app/src/main/java/com/timetask/pro/v2/
+-- data/
|   +-- backup/
|   +-- local/
|   +-- preferences/
|   +-- repository/
+-- domain/
|   +-- model/
|   +-- repository/
|   +-- timer/
|   +-- usecase/
+-- presentation/
|   +-- calendar/
|   +-- home/
|   +-- matrix/
|   +-- navigation/
|   +-- notes/
|   +-- planner/
|   +-- settings/
|   +-- sidebar/
|   +-- tasks/
|   +-- templates/
|   +-- tools/
+-- service/
+-- ui/theme/
```

Key ideas:

- Presentation is built with Compose screens and ViewModels.
- Data access is routed through repositories over Room DAOs.
- Domain models and use cases hold app logic that should stay independent from UI details.
- Long-running timer, stopwatch, and alarm behavior is separated into service-oriented components.
- AI features should enter through domain-level contracts before touching UI or persistence.

## Current Roadmap

- [x] Native Android foundation with Kotlin and Jetpack Compose.
- [x] Room database with exported schemas and incremental migrations.
- [x] Task, note, folder, tag, category, filter, timer, stopwatch, alarm, and template data models.
- [x] Core navigation, bottom tabs, sidebar, and Material 3 theme.
- [x] Task list, task detail, notes, calendar, tools, matrix, settings, and template screens.
- [x] Timer, stopwatch, and alarm service infrastructure.
- [x] AI task parsing contracts, OpenAI Responses API client, Structured Outputs schema, and local fallback parser.
- [x] Public GitHub documentation, contributor guide, and issue templates.
- [x] GitHub Actions Android build workflow.
- [ ] Unit tests for repositories, filters, migrations, and AI contracts.
- [ ] Connect OpenAI-backed natural-language task parser to Quick Add UI.
- [ ] AI day planner with user confirmation before writes.
- [ ] Weekly productivity summaries with privacy controls.
- [ ] First public alpha release.

## Getting Started

### Prerequisites

- Android Studio with a recent Android SDK.
- JDK 21 through `JAVA_HOME`, Android Studio JBR, or the active shell environment.
- Android SDK Platform 36.1 and Build Tools 36.1.0.
- An Android emulator or device running Android 8.0 or newer.

### Build

Clone the repository and run:

```powershell
.\gradlew.bat assembleDebug
```

For a faster compile-only check:

```powershell
.\gradlew.bat :app:compileDebugKotlin --no-daemon
```

Run unit tests when test coverage is added or updated:

```powershell
.\gradlew.bat test
```

Install a debug build on a connected device:

```powershell
.\gradlew.bat installDebug
```

### Local configuration

Do not commit local machine files or secrets. The root `.gitignore` excludes `local.properties`, build outputs, APK files, keystores, env files, and local planning workspace files.

If OpenAI API support is added, provide keys through a local-only configuration path such as environment variables, local Gradle properties, or encrypted Android storage. Never hard-code API keys in source files.

## Contributing

Contributions are welcome after the public repository is prepared. Good first areas include:

- documentation cleanup;
- reproducible build setup;
- Room migration tests;
- filter and tag behavior tests;
- AI contract design;
- accessibility and localization improvements;
- issue triage and small UI fixes.

Before opening a pull request, please keep changes focused, avoid unrelated refactors, and make sure the project builds locally.

## Privacy Principles

TimeTask Pro V2 is local-first by default. The AI roadmap is built around these rules:

- User data should remain on-device unless the user explicitly starts an AI action.
- AI requests should use the minimum useful context.
- AI output should be structured and reviewable.
- The app should not silently create, edit, delete, or upload tasks through AI.
- API credentials should never be stored in the public repository.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
