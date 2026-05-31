package com.timetask.pro.v2.data.local.db.entity

import kotlinx.serialization.Serializable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Пользовательский пресет таймера.
 * Хранит имя, emoji и длительность для быстрого создания таймеров.
 */
@Entity(tableName = "user_presets")
@Serializable
data class UserPresetEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val emoji: String = "⏱️",
    val durationMs: Long,
    val createdAt: Long = System.currentTimeMillis(),
)
