package com.timetask.pro.v2.data.local.db.entity

import kotlinx.serialization.Serializable
import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

/**
 * Шаблон для быстрого создания задач.
 */
@Immutable
@Entity(tableName = "templates")
@Serializable
data class TemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    /** Иконка для отображения в футере или меню */
    val icon: String? = null,
    
    val emoji: String? = null,

    val order: Int = 0,

    /** Сохраненная конфигурация задачи в виде JSON */
    val taskConfigJson: String = "{}",

    val folderId: Long? = null,
    @ColumnInfo(defaultValue = "'[]'")
    val tagIdsJson: String = "[]",
    @ColumnInfo(defaultValue = "0")
    val isPinned: Boolean = false,
    val description: String? = null,

    val createdAt: Long = System.currentTimeMillis(),
)
