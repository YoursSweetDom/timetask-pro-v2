package com.timetask.pro.v2.data.ai

import com.timetask.pro.v2.domain.model.ai.AiTaskDraft
import com.timetask.pro.v2.domain.model.ai.AiTaskPriority
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AiTaskDraftDto(
    val title: String,
    val description: String = "",
    @SerialName("due_date")
    val dueDate: String? = null,
    @SerialName("due_time")
    val dueTime: String? = null,
    val priority: String = "none",
    val tags: List<String> = emptyList(),
    @SerialName("folder_hint")
    val folderHint: String? = null,
    @SerialName("category_hint")
    val categoryHint: String? = null,
    @SerialName("estimated_minutes")
    val estimatedMinutes: Int? = null,
    val confidence: Double = 0.0,
    @SerialName("needs_review")
    val needsReview: Boolean = true,
    val warnings: List<String> = emptyList(),
) {
    fun toDomain(): AiTaskDraft {
        return AiTaskDraft(
            title = title.trim(),
            description = description.trim(),
            dueDateIso = dueDate,
            dueTime = dueTime,
            priority = priority.toAiPriority(),
            tags = tags.map { it.trim() }.filter { it.isNotBlank() }.distinct(),
            folderHint = folderHint?.trim()?.takeIf { it.isNotBlank() },
            categoryHint = categoryHint?.trim()?.takeIf { it.isNotBlank() },
            estimatedMinutes = estimatedMinutes?.takeIf { it > 0 },
            confidence = confidence.coerceIn(0.0, 1.0),
            needsReview = needsReview,
            warnings = warnings.map { it.trim() }.filter { it.isNotBlank() },
        )
    }

    private fun String.toAiPriority(): AiTaskPriority {
        return when (lowercase()) {
            "low" -> AiTaskPriority.LOW
            "medium" -> AiTaskPriority.MEDIUM
            "high" -> AiTaskPriority.HIGH
            "urgent" -> AiTaskPriority.URGENT
            else -> AiTaskPriority.NONE
        }
    }
}

