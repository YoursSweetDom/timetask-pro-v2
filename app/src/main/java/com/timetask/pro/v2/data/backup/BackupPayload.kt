package com.timetask.pro.v2.data.backup

import com.timetask.pro.v2.data.local.db.entity.AlarmEntity
import com.timetask.pro.v2.data.local.db.entity.CategoryEntity
import com.timetask.pro.v2.data.local.db.entity.FilterEntity
import com.timetask.pro.v2.data.local.db.entity.FolderEntity
import com.timetask.pro.v2.data.local.db.entity.NoteEntity
import com.timetask.pro.v2.data.local.db.entity.TagEntity
import com.timetask.pro.v2.data.local.db.entity.TaskEntity
import com.timetask.pro.v2.data.local.db.entity.TemplateEntity
import com.timetask.pro.v2.data.local.db.entity.TimerEntity
import com.timetask.pro.v2.data.local.db.entity.UserPresetEntity
import com.timetask.pro.v2.data.local.entity.StopwatchEntity
import com.timetask.pro.v2.data.local.entity.StopwatchLapEntity
import kotlinx.serialization.Serializable

/**
 * Оболочка для всех пользовательских данных, собираемых из локальной БД
 * и настроек приложения, чтобы сериализовать их в один JSON-файл для экспорта.
 */
@Serializable
data class BackupPayload(
    val version: Int = 1,
    val timestampMs: Long = System.currentTimeMillis(),
    
    // Preferences
    val appPreferencesJson: String = "{}",
    
    // Database Entities
    val tasks: List<TaskEntity> = emptyList(),
    val folders: List<FolderEntity> = emptyList(),
    val tags: List<TagEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val filters: List<FilterEntity> = emptyList(),
    val notes: List<NoteEntity> = emptyList(),
    val timers: List<TimerEntity> = emptyList(),
    val alarms: List<AlarmEntity> = emptyList(),
    val templates: List<TemplateEntity> = emptyList(),
    val userPresets: List<UserPresetEntity> = emptyList(),
    val stopwatches: List<StopwatchEntity> = emptyList(),
    val stopwatchLaps: List<StopwatchLapEntity> = emptyList()
)
