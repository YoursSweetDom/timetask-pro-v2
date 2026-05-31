package com.timetask.pro.v2.data.local.db.entity

import kotlinx.serialization.Serializable
import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity(tableName = "categories")
@Serializable
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val icon: String? = null,
    val emoji: String? = null,
    val color: String? = null,
    val order: Int = 0,
    val parentId: Long? = null, // Support for nested categories
    val isHidden: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
