package com.timetask.pro.v2.core.network.openai

/**
 * Значения по умолчанию для OpenAI-интеграции.
 * Модель должна оставаться настраиваемой: для точного планирования можно выбрать сильнее,
 * для быстрых структурированных задач — дешевле и быстрее.
 */
object OpenAiDefaults {
    const val BASE_URL = "https://api.openai.com/v1"
    const val DEFAULT_TASK_MODEL = "gpt-5-mini"
    const val DEFAULT_REQUEST_TIMEOUT_MILLIS = 30_000L
    const val DEFAULT_MAX_OUTPUT_TOKENS = 900
}

