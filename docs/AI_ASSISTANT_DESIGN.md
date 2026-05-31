# AI Assistant Design

TimeTask Pro V2 is local-first. AI features must be added as reviewable task-draft workflows, not as hidden automatic writes to the local database.

## Current Foundation

The repository includes the first AI task parsing foundation:

- `domain/model/ai` contains clean task-draft models.
- `domain/repository/AiTaskParserRepository.kt` defines the parsing abstraction.
- `domain/usecase/ai/ParseTaskInputWithAiUseCase.kt` validates input and delegates parsing.
- `core/network/openai` contains a minimal OpenAI Responses API client.
- `data/ai/OpenAiTaskParserRepository.kt` maps OpenAI Structured Outputs into domain models.
- `data/ai/LocalRuleBasedTaskParserRepository.kt` provides a basic offline fallback.

This layer is intentionally not connected to UI yet.

## Design Rules

- No API keys in source control.
- No automatic task creation from model output.
- No silent upload of task history.
- Model output must be structured and parsed into domain models.
- Ambiguous fields must be nullable or marked with warnings.
- UI must require user confirmation before saving AI-generated changes.

## OpenAI Integration Direction

The preferred production path is:

1. User enters natural language in Quick Add.
2. UI builds an `AiTaskParseRequest` with explicit date, locale, timezone, and known labels.
3. `ParseTaskInputWithAiUseCase` calls `AiTaskParserRepository`.
4. The repository uses OpenAI Responses API with Structured Outputs.
5. The model returns a task draft matching the JSON schema.
6. UI displays the draft for review.
7. User confirms or edits.
8. Only then the app writes a `TaskEntity`.

## Why Structured Outputs

Task creation needs predictable fields: title, due date, time, priority, tags, folder hints, category hints, confidence, and warnings. Plain text or loose JSON would require fragile parsing. Structured Outputs let the app define a schema and handle refusals or invalid results explicitly.

## Secret Handling

OpenAI API keys must be supplied through a local-only or user-controlled mechanism. Future production options:

- user-owned API key stored through Android Keystore-backed encrypted storage;
- backend proxy with user authentication and abuse controls;
- build variant for local developer testing only.

Never hard-code API keys into Kotlin source, Gradle files, resources, or committed config.

## Next Steps

- Add settings UI for AI provider configuration.
- Add encrypted local storage for user-provided API keys.
- Add unit tests for schema decoding and local fallback parsing.
- Connect Quick Add to the use case behind a feature flag.
- Add a review sheet before saving AI-generated task drafts.

