package com.timetask.pro.v2.domain.repository

import com.timetask.pro.v2.domain.model.ai.AiTaskParseRequest
import com.timetask.pro.v2.domain.model.ai.AiTaskParseResult

/**
 * Абстракция парсинга задачи.
 * Реализация может быть OpenAI, локальной rule-based или гибридной.
 */
interface AiTaskParserRepository {
    suspend fun parseTask(request: AiTaskParseRequest): AiTaskParseResult
}

