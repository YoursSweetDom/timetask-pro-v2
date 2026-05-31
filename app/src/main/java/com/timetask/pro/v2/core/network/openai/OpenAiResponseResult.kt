package com.timetask.pro.v2.core.network.openai

sealed interface OpenAiResponseResult {
    data class Success(
        val outputText: String,
        val rawResponse: String,
    ) : OpenAiResponseResult

    data class Refusal(
        val message: String,
        val rawResponse: String,
    ) : OpenAiResponseResult

    data class HttpError(
        val statusCode: Int,
        val body: String,
    ) : OpenAiResponseResult

    data class NetworkError(
        val message: String,
    ) : OpenAiResponseResult

    data class InvalidResponse(
        val rawResponse: String,
        val reason: String,
    ) : OpenAiResponseResult
}

