package com.timetask.pro.v2.data.local.db.entity

import kotlinx.serialization.Serializable
import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Пользовательский фильтр / Смарт-список.
 */
@Immutable
@Entity(tableName = "filters")
@Serializable
data class FilterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val icon: String? = null,
    val emoji: String? = null,
    val color: String? = null,

    val order: Int = 0,

    val isPinned: Boolean = false,

    /** JSON-строка, хранящая сериализованную логику (Match All / Match Any и список Rules) */
    val logicJson: String = "{}",

    val createdAt: Long = System.currentTimeMillis(),
)
