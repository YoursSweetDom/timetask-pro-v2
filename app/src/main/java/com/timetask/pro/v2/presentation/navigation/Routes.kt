package com.timetask.pro.v2.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes.
 * Используют @Serializable для Navigation Compose 2.8+.
 */

// ============================================================
// Основные маршруты (Bottom Navigation — 7 табов)
// ============================================================

@Serializable data object HomeRoute
@Serializable data object TasksRoute
@Serializable data object NotesRoute
@Serializable data object CalendarRoute
@Serializable data object PlannerRoute
@Serializable data class ToolsRoute(val initialTab: Int = -1)
@Serializable data object MatrixRoute

// ============================================================
// Дополнительные маршруты
// ============================================================

@Serializable data object SettingsRoute
@Serializable data class TaskDetailRoute(val taskId: Long)
@Serializable data object TemplatesRoute
@Serializable data object TrashRoute
@Serializable data object CompletedRoute
@Serializable data object WontDoRoute
