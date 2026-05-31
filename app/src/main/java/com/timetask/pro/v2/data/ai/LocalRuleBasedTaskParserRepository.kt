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
        val normalized = request.input.trim()
        if (normalized.isBlank()) {
            return AiTaskParseResult.Unavailable("Пустой текст задачи.")
        }

        val tags = tagRegex.findAll(normalized)
            .map { it.groupValues[1].trim() }
            .filter { it.isNotBlank() }
            .distinct()
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
                warnings = listOf("Локальный fallback распознает только базовые поля."),
            )
        )
    }

    private fun detectPriority(input: String): AiTaskPriority {
        val lower = input.lowercase()
        return when {
            "срочно" in lower || "urgent" in lower || "важно" in lower -> AiTaskPriority.URGENT
            "high" in lower || "высок" in lower -> AiTaskPriority.HIGH
            "medium" in lower || "средн" in lower -> AiTaskPriority.MEDIUM
            "low" in lower || "низк" in lower -> AiTaskPriority.LOW
            else -> AiTaskPriority.NONE
        }
    }

    private companion object {
        val tagRegex = Regex("""#([\p{L}\p{N}_-]+)""")
        val timeRegex = Regex("""\b([01]?\d|2[0-3]):[0-5]\d\b""")
        val priorityRegex = Regex(
            pattern = """\b(urgent|high|medium|low|срочно|важно|высок\p{L}*|средн\p{L}*|низк\p{L}*)\b""",
            option = RegexOption.IGNORE_CASE,
        )
    }
}

