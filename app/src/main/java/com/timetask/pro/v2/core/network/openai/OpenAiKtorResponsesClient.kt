package com.timetask.pro.v2.core.network.openai

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json

/**
 * Ktor-реализация OpenAI Responses API.
 * Класс не владеет API-ключом сам: ключ передается через OpenAiConfig.
 */
class OpenAiKtorResponsesClient(
    private val config: OpenAiConfig,
    private val httpClient: HttpClient = createDefaultHttpClient(config.requestTimeoutMillis),
    private val textExtractor: OpenAiResponseTextExtractor = OpenAiResponseTextExtractor(),
) : OpenAiResponsesClient {

    override suspend fun createStructuredResponse(request: OpenAiResponsesRequest): OpenAiResponseResult {
        if (config.apiKey.isBlank()) {
            return OpenAiResponseResult.NetworkError("OpenAI API key is empty.")
        }

        return try {
            val response = httpClient.post("${config.baseUrl.trimEnd('/')}/responses") {
                bearerAuth(config.apiKey)
                accept(ContentType.Application.Json)
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(request)
            }

            val rawBody = response.bodyAsText()
            if (response.status.value !in 200..299) {
                return OpenAiResponseResult.HttpError(
                    statusCode = response.status.value,
                    body = rawBody,
                )
            }

            val extracted = textExtractor.extract(rawBody)
            when {
                extracted.refusal != null -> OpenAiResponseResult.Refusal(
                    message = extracted.refusal,
                    rawResponse = rawBody,
                )
                extracted.outputText != null -> OpenAiResponseResult.Success(
                    outputText = extracted.outputText,
                    rawResponse = rawBody,
                )
                else -> OpenAiResponseResult.InvalidResponse(
                    rawResponse = rawBody,
                    reason = "No output_text content found in Responses API payload.",
                )
            }
        } catch (error: Exception) {
            OpenAiResponseResult.NetworkError(error.message ?: error::class.java.simpleName)
        }
    }

    companion object {
        fun createDefaultHttpClient(requestTimeoutMillis: Long): HttpClient {
            return HttpClient(Android) {
                install(ContentNegotiation) {
                    json(OpenAiJson.format)
                }
                install(HttpTimeout) {
                    this.requestTimeoutMillis = requestTimeoutMillis
                    connectTimeoutMillis = requestTimeoutMillis
                    socketTimeoutMillis = requestTimeoutMillis
                }
            }
        }
    }
}
