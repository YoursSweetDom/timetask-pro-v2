package com.timetask.pro.v2.domain.model.ai

/**
 * Входные данные для AI-парсинга задачи.
 *
 * referenceDateIso и timeZoneId передаются явно, чтобы фразы вроде "завтра"
 * интерпретировались предсказуемо и тестируемо.
 */
data class AiTaskParseRequest(
    val input: String,
    val locale: String = "uk-UA",
    val referenceDateIso: String,
    val timeZoneId: String,
    val knownTags: List<String> = emptyList(),
    val knownFolders: List<String> = emptyList(),
    val knownCategories: List<String> = emptyList(),
)
