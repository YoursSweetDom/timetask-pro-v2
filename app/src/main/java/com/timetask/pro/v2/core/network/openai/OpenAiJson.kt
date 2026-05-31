package com.timetask.pro.v2.core.network.openai

import kotlinx.serialization.json.Json

/**
 * Единая JSON-конфигурация для OpenAI DTO.
 */
object OpenAiJson {
    val format: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
}
