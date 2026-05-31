package com.timetask.pro.v2.data.ai

import com.timetask.pro.v2.domain.model.ai.AiTaskDraft
import com.timetask.pro.v2.domain.model.ai.AiTaskParseRequest
import com.timetask.pro.v2.domain.model.ai.AiTaskParseResult
import com.timetask.pro.v2.domain.model.ai.AiTaskPriority
import com.timetask.pro.v2.domain.repository.AiTaskParserRepository

/**
 * Локальный fallback без сети.
 * Нужен, чтобы будущий UI мог иметь предсказуемое поведение при отсутствии API-ключа.
 */
class LocalRuleBasedTaskParserRepository : AiTaskParserRepository {
    override suspend fun parseTask(request: AiTaskParseRequest): AiTaskParseResult {
        val normalized = request.input.normalizeTaskText()
        if (normalized.isBlank()) {
            return AiTaskParseResult.Unavailable("Task input is empty.")
        }

        val tags = tagRegex.findAll(normalized)
            .map { it.groupValues[1].trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .toList()

        val title = normalized
            .replace(tagRegex, "")
            .replace(priorityRegex, "")
            .trim()
            .ifBlank { normalized }

        return AiTaskParseResult.Success(
            AiTaskDraft(
                title = title,
                description = "",
                dueDateIso = null,
                dueTime = timeRegex.find(normalized)?.value,
                priority = detectPriority(normalized),
                tags = tags,
                folderHint = null,
                categoryHint = null,
                estimatedMinutes = null,
                confidence = 0.35,
                needsReview = true,
                warnings = listOf("Local fallback recognizes only basic task fields."),
            )
        )
    }

    private fun detectPriority(input: String): AiTaskPriority {
        val lower = input.lowercase()
        return when {
            "urgent" in lower || "important" in lower || "терміново" in lower || "важливо" in lower -> AiTaskPriority.URGENT
            "high" in lower || "висок" in lower -> AiTaskPriority.HIGH
            "medium" in lower || "середн" in lower -> AiTaskPriority.MEDIUM
            "low" in lower || "низьк" in lower || "низк" in lower -> AiTaskPriority.LOW
            else -> AiTaskPriority.NONE
        }
    }

    private fun String.normalizeTaskText(): String {
        return trim().replace(whitespaceRegex, " ")
    }

    private companion object {
        val whitespaceRegex = Regex("""\s+""")
        val tagRegex = Regex("""#([\p{L}\p{N}_-]+)""")
        val timeRegex = Regex("""\b([01]?\d|2[0-3]):[0-5]\d\b""")
        val priorityRegex = Regex(
            pattern = """(?<![\p{L}\p{N}_-])(urgent|important|high|medium|low|терміново|важливо|висок\p{L}*|середн\p{L}*|низьк\p{L}*|низк\p{L}*)(?![\p{L}\p{N}_-])""",
            option = RegexOption.IGNORE_CASE,
        )
    }
}
