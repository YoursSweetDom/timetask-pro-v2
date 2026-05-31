package com.timetask.pro.v2.data.local.entity

import kotlinx.serialization.Serializable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stopwatches",
    indices = [Index(value = ["isDeleted"])]
)
@Serializable
data class StopwatchEntity(
    @PrimaryKey val id: String,
    val name: String,
    val state: String, // IDLE, RUNNING, PAUSED
    val startTimeMs: Long,
    val accumulatedMs: Long,
    
    /** ID категории (связь с CategoryEntity) */
    val categoryId: Long? = null,
    
    /** Привязка к конкретным задачам для учета времени */
    val linkedTaskIdsJson: String = "[]",
    val categoryText: String = "",
    val tagsText: String = "",
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),

    @androidx.room.Embedded(prefix = "notif_")
    val notification: StopwatchNotification = StopwatchNotification()
)

@androidx.compose.runtime.Immutable
@Serializable
data class StopwatchNotification(
    /** Показывать этот секундомер в уведомлениях шторки */
    val showInNotifications: Boolean = true
)
