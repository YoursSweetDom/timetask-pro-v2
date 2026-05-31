package com.timetask.pro.v2.domain.model

/**
 * Модель логики пользовательского фильтра (TickTick-стиль).
 * Сериализуется в JSON и хранится в FilterEntity.logicJson.
 */
data class FilterLogic(
    val mode: FilterMode = FilterMode.MATCH_ALL,
    val rules: List<FilterRule> = emptyList()
)

enum class FilterMode {
    /** Все условия должны совпасть (AND) */
    MATCH_ALL,
    /** Любое условие совпадёт (OR) */
    MATCH_ANY
}

data class FilterRule(
    val field: FilterField,
    val operator: FilterOperator,
    val values: List<String> = emptyList()
)

enum class FilterField {
    FOLDER,
    TAG,
    CATEGORY,
    PRIORITY,
    DUE_DATE,
    KEYWORD
}

enum class FilterOperator {
    IS,
    IS_NOT,
    CONTAINS,
    NOT_CONTAINS
}
