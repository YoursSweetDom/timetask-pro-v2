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
        if (request.input.isBlank()) {
            return AiTaskParseResult.Unavailable("Пустой текст задачи.")
        }

        return repository.parseTask(request)
    }
}

