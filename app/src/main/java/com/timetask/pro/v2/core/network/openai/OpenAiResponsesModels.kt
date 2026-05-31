package com.timetask.pro.v2.core.network.openai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class OpenAiResponsesRequest(
    val model: String,
    val input: List<OpenAiInputMessage>,
    val text: OpenAiTextConfig,
    @SerialName("max_output_tokens")
    val maxOutputTokens: Int,
)

@Serializable
data class OpenAiInputMessage(
    val role: String,
    val content: String,
)

@Serializable
data class OpenAiTextConfig(
    val format: OpenAiJsonSchemaFormat,
)

@Serializable
data class OpenAiJsonSchemaFormat(
    val type: String = "json_schema",
    val name: String,
    val strict: Boolean = true,
    val schema: JsonObject,
)

