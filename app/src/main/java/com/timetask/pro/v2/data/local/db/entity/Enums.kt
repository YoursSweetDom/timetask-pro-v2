package com.timetask.pro.v2.data.local.db.entity

import kotlinx.serialization.Serializable
import androidx.compose.runtime.Stable

/**
 * Статус задачи.
 */
@Stable
@Serializable
enum class TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE,
    WONT_DO,
    DELETED,
}

/**
 * Приоритет задачи.
 */
@Stable
@Serializable
enum class Priority {
    NONE,
    LOW,
    MEDIUM,
    HIGH,
}

/**
 * Тип повторения задачи.
 */
@Stable
@Serializable
enum class RecurrenceType {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY,
    CUSTOM,
}

/**
 * Цвет заметки.
 */
@Stable
@Serializable
enum class NoteColor {
    DEFAULT,
    BLUE,
    GREEN,
    YELLOW,
    PINK,
    PURPLE,
    GRAY,
}
