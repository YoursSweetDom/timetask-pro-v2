package com.timetask.pro.v2.data.local.db.entity

import kotlinx.serialization.Serializable
import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Тег для задач и заметок.
 */
@Immutable
@Entity(tableName = "tags")
@Serializable
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val color: String? = null,

    val emoji: String? = null,

    val order: Int = 0,

    val isPinned: Boolean = false,

    /** ID родительского тега для вложенности */
    val parentId: Long? = null,

    val createdAt: Long = System.currentTimeMillis(),
)
