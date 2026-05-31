package com.timetask.pro.v2.core.network.openai

/**
 * Минимальный клиент Responses API.
 * Возвращает уже агрегированный текст structured output, а не заставляет data layer
 * знать внутреннюю форму OpenAI response.output.
 */
interface OpenAiResponsesClient {
    suspend fun createStructuredResponse(request: OpenAiResponsesRequest): OpenAiResponseResult
}

