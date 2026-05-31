package com.timetask.pro.v2.domain.model.ai

/**
 * Черновик задачи, который AI предлагает пользователю.
 * Он не сохраняется в БД автоматически: UI должен сначала показать результат на проверку.
 */
data class AiTaskDraft(
    val title: String,
    val description: String,
    val dueDateIso: String?,
    val dueTime: String?,
    val priority: AiTaskPriority,
    val tags: List<String>,
    val folderHint: String?,
    val categoryHint: String?,
    val estimatedMinutes: Int?,
    val confidence: Double,
    val needsReview: Boolean,
    val warnings: List<String>,
)

