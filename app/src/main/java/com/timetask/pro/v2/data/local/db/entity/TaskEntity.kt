package com.timetask.pro.v2.data.local.db.entity

import kotlinx.serialization.Serializable
import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Задача — основная сущность приложения.
 * Маппинг из TypeScript Task → Room Entity.
 */
@Immutable
@Entity(
    tableName = "tasks",
    indices = [
        Index("status"),
        Index("folderId"),
        Index("dueDate"),
        Index("isPinned"),
        Index("parentId"),
    ],
)
@Serializable
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String,

    val description: String = "",

    val status: TaskStatus = TaskStatus.TODO,

    val priority: Priority = Priority.NONE,

    /** Дата дедлайна в миллисекундах (null = без дедлайна) */
    val dueDate: Long? = null,

    /** ID папки/списка (null = Inbox) */
    val folderId: Long? = null,

    /** JSON array of tag IDs */
    val tagIds: String = "[]",

    /** Порядок сортировки внутри папки */
    val order: Int = 0,

    /** Закреплена ли задача */
    val isPinned: Boolean = false,

    /** ID родительской задачи (для подзадач) */
    val parentId: Long? = null,

    /** Предполагаемая длительность в минутах */
    val estimatedMinutes: Int? = null,

    /** Квадрант матрицы Эйзенхауэра (1-4, null = не назначен) */
    val quadrant: Int? = null,

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis(),

    /** Когда задача была завершена */
    val completedAt: Long? = null,

    /** Накопленное время (сумма всех привязанных таймеров и секундомеров) в миллисекундах */
    val totalSpentTimeMs: Long = 0L,

    /** Процент выполнения задачи (0-100) */
    val progressPercent: Int = 0,

    /** ID категории (связь с CategoryEntity) */
    val categoryId: Long? = null,

    /** Уровень закрепления: 0 - Не закреплена, 1 - Локально (в папке), 2 - Глобально (самый верх) */
    val pinMode: Int = 0,
)

/**
 * Перехват обратной совместимости для старого isPinned.
 */
fun TaskEntity.isEffectivelyPinned(): Boolean = isPinned || pinMode > 0
