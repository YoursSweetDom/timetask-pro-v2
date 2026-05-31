package com.timetask.pro.v2.data.ai

import com.timetask.pro.v2.core.network.openai.OpenAiConfig
import com.timetask.pro.v2.core.network.openai.OpenAiInputMessage
import com.timetask.pro.v2.core.network.openai.OpenAiJson
import com.timetask.pro.v2.core.network.openai.OpenAiJsonSchemaFormat
import com.timetask.pro.v2.core.network.openai.OpenAiResponseResult
import com.timetask.pro.v2.core.network.openai.OpenAiResponsesClient
import com.timetask.pro.v2.core.network.openai.OpenAiResponsesRequest
import com.timetask.pro.v2.core.network.openai.OpenAiTextConfig
import com.timetask.pro.v2.domain.model.ai.AiTaskParseRequest
import com.timetask.pro.v2.domain.model.ai.AiTaskParseResult
import com.timetask.pro.v2.domain.repository.AiTaskParserRepository
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * OpenAI-реализация парсинга задачи через Responses API + Structured Outputs.
 */
class OpenAiTaskParserRepository(
    private val config: OpenAiConfig,
    private val client: OpenAiResponsesClient,
    private val json: Json = OpenAiJson.format,
) : AiTaskParserRepository {

    override suspend fun parseTask(request: AiTaskParseRequest): AiTaskParseResult {
        val response = client.createStructuredResponse(
            OpenAiResponsesRequest(
                model = config.model,
                maxOutputTokens = config.maxOutputTokens,
                input = listOf(
                    OpenAiInputMessage(
                        role = "system",
                        content = OpenAiTaskParserPrompt.SYSTEM.trim(),
                    ),
                    OpenAiInputMessage(
                        role = "user",
                        content = OpenAiTaskParserPrompt.user(request),
                    ),
                ),
                text = OpenAiTextConfig(
                    format = OpenAiJsonSchemaFormat(
                        name = OpenAiTaskParserSchema.NAME,
                        schema = OpenAiTaskParserSchema.schema,
                    ),
                ),
            )
        )

        return when (response) {
            is OpenAiResponseResult.Success -> decodeDraft(response.outputText)
            is OpenAiResponseResult.Refusal -> AiTaskParseResult.Refused(response.message)
            is OpenAiResponseResult.HttpError -> AiTaskParseResult.Unavailable(
                "OpenAI request failed with HTTP ${response.statusCode}."
            )
            is OpenAiResponseResult.NetworkError -> AiTaskParseResult.Unavailable(response.message)
            is OpenAiResponseResult.InvalidResponse -> AiTaskParseResult.InvalidOutput(
                rawOutput = response.rawResponse,
                reason = response.reason,
            )
        }
    }

    private fun decodeDraft(outputText: String): AiTaskParseResult {
        return try {
            val draft = json.decodeFromString<AiTaskDraftDto>(outputText).toDomain()
            if (draft.title.isBlank()) {
                AiTaskParseResult.InvalidOutput(outputText, "Parsed task title is blank.")
            } else {
                AiTaskParseResult.Success(draft)
            }
        } catch (error: SerializationException) {
            AiTaskParseResult.InvalidOutput(
                rawOutput = outputText,
                reason = error.message ?: "Cannot decode AI task draft.",
            )
        } catch (error: IllegalArgumentException) {
            AiTaskParseResult.InvalidOutput(
                rawOutput = outputText,
                reason = error.message ?: "Invalid AI task draft values.",
            )
        }
    }
}

