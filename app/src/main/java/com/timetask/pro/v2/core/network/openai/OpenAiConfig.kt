package com.timetask.pro.v2.core.network.openai

/**
 * Конфигурация OpenAI-клиента.
 * apiKey приходит только извне и никогда не должен храниться в репозитории.
 */
data class OpenAiConfig(
    val apiKey: String,
    val model: String = OpenAiDefaults.DEFAULT_TASK_MODEL,
    val baseUrl: String = OpenAiDefaults.BASE_URL,
    val requestTimeoutMillis: Long = OpenAiDefaults.DEFAULT_REQUEST_TIMEOUT_MILLIS,
    val maxOutputTokens: Int = OpenAiDefaults.DEFAULT_MAX_OUTPUT_TOKENS,
)

