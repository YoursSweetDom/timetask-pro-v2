package com.timetask.pro.v2.data.ai

import com.timetask.pro.v2.domain.model.ai.AiTaskParseRequest

/**
 * Prompt для structured parsing.
 * Держим отдельно, чтобы позже можно было тестировать версии prompt без правок клиента.
 */
object OpenAiTaskParserPrompt {
    const val SYSTEM = """
You parse personal productivity input into a task draft for an Android task manager.
Return only data that is supported by the provided JSON schema.
Do not invent dates, folders, categories, or tags when the input is ambiguous.
If a field is uncertain, set it to null or an empty array and add a short warning.
The user must review every result before it is saved.
"""

    fun user(request: AiTaskParseRequest): String {
        return buildString {
            appendLine("Locale: ${request.locale}")
            appendLine("Reference date: ${request.referenceDateIso}")
            appendLine("Time zone: ${request.timeZoneId}")
            appendLine("Known tags: ${request.knownTags.joinToString().ifBlank { "none" }}")
            appendLine("Known folders: ${request.knownFolders.joinToString().ifBlank { "none" }}")
            appendLine("Known categories: ${request.knownCategories.joinToString().ifBlank { "none" }}")
            appendLine()
            appendLine("Task input:")
            append(request.input)
        }
    }
}

