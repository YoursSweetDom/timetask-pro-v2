package com.timetask.pro.v2.domain.usecase.ai

import com.timetask.pro.v2.domain.model.ai.AiTaskParseRequest
import com.timetask.pro.v2.domain.model.ai.AiTaskParseResult
import com.timetask.pro.v2.domain.repository.AiTaskParserRepository

/**
 * Use case для будущего Quick Add AI.
 * Сейчас только валидирует вход и делегирует работу repository.
 */
class ParseTaskInputWithAiUseCase(
    private val repository: AiTaskParserRepository,
) {
    suspend operator fun invoke(request: AiTaskParseRequest): AiTaskParseResult {
        val normalizedInput = request.input.normalizeForAi()
        if (normalizedInput.isBlank()) {
            return AiTaskParseResult.Unavailable("Task input is empty.")
        }

        if (normalizedInput.length > MAX_INPUT_CHARS) {
            return AiTaskParseResult.Unavailable("Task input is too long for AI parsing.")
        }

        return repository.parseTask(
            request.copy(
                input = normalizedInput,
                knownTags = request.knownTags.sanitizedKnownLabels(),
                knownFolders = request.knownFolders.sanitizedKnownLabels(),
                knownCategories = request.knownCategories.sanitizedKnownLabels(),
            )
        )
    }

    private fun String.normalizeForAi(): String {
        return trim().replace(whitespaceRegex, " ")
    }

    private fun List<String>.sanitizedKnownLabels(): List<String> {
        return map { it.normalizeForAi() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .take(MAX_KNOWN_LABELS)
    }

    private companion object {
        const val MAX_INPUT_CHARS = 4_000
        const val MAX_KNOWN_LABELS = 50
        val whitespaceRegex = Regex("""\s+""")
    }
}
