package com.timetask.pro.v2.core.network.openai

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Responses API может вернуть несколько output items.
 * Поэтому не читаем output[0], а собираем все content.type == output_text.
 */
class OpenAiResponseTextExtractor(
    private val json: Json = OpenAiJson.format,
) {
    fun extract(rawResponse: String): ExtractedOpenAiText {
        val root = json.parseToJsonElement(rawResponse).jsonObject

        root["output_text"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }?.let {
            return ExtractedOpenAiText(outputText = it)
        }

        val textParts = mutableListOf<String>()
        val refusalParts = mutableListOf<String>()

        root["output"]?.jsonArrayOrNull()?.forEach { outputItem ->
            val outputObject = outputItem as? JsonObject ?: return@forEach
            outputObject["content"]?.jsonArrayOrNull()?.forEach { contentItem ->
                val contentObject = contentItem as? JsonObject ?: return@forEach
                when (contentObject["type"]?.jsonPrimitive?.contentOrNull) {
                    "output_text" -> contentObject["text"]?.jsonPrimitive?.contentOrNull?.let(textParts::add)
                    "refusal" -> contentObject["refusal"]?.jsonPrimitive?.contentOrNull?.let(refusalParts::add)
                }
            }
        }

        return ExtractedOpenAiText(
            outputText = textParts.joinToString(separator = "\n").takeIf { it.isNotBlank() },
            refusal = refusalParts.joinToString(separator = "\n").takeIf { it.isNotBlank() },
        )
    }

    private fun kotlinx.serialization.json.JsonElement.jsonArrayOrNull(): JsonArray? {
        return runCatching { jsonArray }.getOrNull()
    }
}

data class ExtractedOpenAiText(
    val outputText: String? = null,
    val refusal: String? = null,
)

