package com.timetask.pro.v2.data.local.db.entity

import kotlinx.serialization.Serializable
import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Serializable
enum class ListType {
    TASK, NOTE
}

/**
 * Папка / список задач (аналог Folder из TypeScript).
 */
@Immutable
@Entity(tableName = "folders")
@Serializable
data class FolderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val icon: String? = null,

    val emoji: String? = null,

    val color: String? = null,

    val order: Int = 0,

    /** Системный список (Inbox, All, Today) — нельзя удалить */
    val isSystem: Boolean = false,

    /** ID родительской папки (для вложенности) */
    val parentId: Long? = null,

    val isPinned: Boolean = false,

    val isArchived: Boolean = false,

    /** Не показывать задачи из этого списка в Умных списках (Все, Сегодня и т.д.) */
    val isExcludedFromSmartLists: Boolean = false,

    /** Скрыть сам список из бокового меню */
    val isHidden: Boolean = false,

    /** Тип списка: Задачи или Заметки */
    val listType: ListType = ListType.TASK,

    val createdAt: Long = System.currentTimeMillis(),
)
