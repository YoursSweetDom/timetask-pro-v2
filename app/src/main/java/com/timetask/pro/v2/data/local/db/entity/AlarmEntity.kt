package com.timetask.pro.v2.data.local.db.entity

import kotlinx.serialization.Serializable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "alarms")
@Serializable
data class AlarmEntity(
    @PrimaryKey val id: String,
    val hour: Int,
    val minute: Int,
    val repeatDaysMask: Int = 0, // Bitmap. 1=Mon, 2=Tue, 4=Wed, 8=Thu, 16=Fri, 32=Sat, 64=Sun.
    val isEnabled: Boolean = true,
    val nextTriggerTime: Long = 0L,
    val label: String = "",
    val soundUri: String? = null,
    val vibrationPattern: String? = null,
    val volume: Float? = null,
    val snoozeDurationMinutes: Int = 5,
    @ColumnInfo(defaultValue = "3")
    val snoozeRepeatTimes: Int = 3, // Amount of times it can auto-snooze
    
    @ColumnInfo(defaultValue = "300")
    val ringingDurationSec: Int = 300, // Duration of ringing (in sec). -1 = infinite
    
    @ColumnInfo(defaultValue = "300")
    val autoSnoozeDurationSec: Int = 300, // Delay applied automatically if ringing finishes. -1 = off
    
    val deleteAfterGoOff: Boolean = false,
    
    // UI and Metadata
    val colorARGB: Int? = null,
    @ColumnInfo(defaultValue = "''")
    val categoryText: String = "",
    @ColumnInfo(defaultValue = "''")
    val tagsText: String = "",
    @ColumnInfo(defaultValue = "'[]'")
    val tagIdsJson: String = "[]",
    @ColumnInfo(defaultValue = "''")
    val notesText: String = "",
    
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
