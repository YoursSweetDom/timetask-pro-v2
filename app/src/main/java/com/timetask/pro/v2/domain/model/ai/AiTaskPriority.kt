package com.timetask.pro.v2.domain.model.ai

/**
 * Приоритет черновика, возвращаемого AI.
 * Это domain-модель, она не привязана напрямую к Room enum.
 */
enum class AiTaskPriority {
    NONE,
    LOW,
    MEDIUM,
    HIGH,
    URGENT,
}

