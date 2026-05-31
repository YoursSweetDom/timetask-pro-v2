package com.timetask.pro.v2.data.local.db.entity

import kotlinx.serialization.Serializable
import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Заметка — вторая основная сущность приложения.
 * Стиль Google Keep: цветные карточки, закрепление, теги.
 */
@Immutable
@Entity(
    tableName = "notes",
    indices = [
        Index("isPinned"),
        Index("folderId"),
        Index("color"),
    ],
)
@Serializable
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String = "",

    val content: String = "",

    val color: NoteColor = NoteColor.DEFAULT,

    val isPinned: Boolean = false,

    /** ID папки (null = без папки) */
    val folderId: Long? = null,

    /** JSON array of tag IDs */
    val tagIds: String = "[]",

    /** Порядок сортировки */
    val order: Int = 0,

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis(),
)
