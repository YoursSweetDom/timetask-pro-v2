package com.timetask.pro.v2.data.local.entity

import kotlinx.serialization.Serializable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(
    tableName = "stopwatch_laps",
    foreignKeys = [
        ForeignKey(
            entity = StopwatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["stopwatchId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["stopwatchId"]),
        Index(value = ["isDeleted"])
    ]
)
@Serializable
data class StopwatchLapEntity(
    @PrimaryKey val id: String,
    val stopwatchId: String,
    val lapNumber: Int,
    val lapTimeMs: Long,
    val totalTimeMs: Long,
    val title: String? = null,
    @ColumnInfo(defaultValue = "''")
    val categoryText: String = "",
    @ColumnInfo(defaultValue = "''")
    val tagsText: String = "",
    val colorARGB: Int? = null,
    @ColumnInfo(defaultValue = "0")
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
