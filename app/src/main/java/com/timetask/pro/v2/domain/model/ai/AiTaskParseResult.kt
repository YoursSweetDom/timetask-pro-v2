package com.timetask.pro.v2.domain.model.ai

/**
 * Результат AI-парсинга без исключений наружу.
 * Так UI сможет спокойно показать ошибку, отказ модели или черновик.
 */
sealed interface AiTaskParseResult {
    data class Success(val draft: AiTaskDraft) : AiTaskParseResult
    data class Unavailable(val reason: String) : AiTaskParseResult
    data class Refused(val message: String) : AiTaskParseResult
    data class InvalidOutput(val rawOutput: String, val reason: String) : AiTaskParseResult
}

