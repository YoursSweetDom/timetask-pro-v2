package com.timetask.pro.v2.data.local.db

import androidx.room.TypeConverter
import com.timetask.pro.v2.data.local.db.entity.NoteColor
import com.timetask.pro.v2.data.local.db.entity.Priority
import com.timetask.pro.v2.data.local.db.entity.TaskStatus
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * TypeConverters для Room — конвертация сложных типов в строки/числа.
 */
class Converters {

    companion object {
        private val json = Json { ignoreUnknownKeys = true }
    }

    // ============================================================
    // TaskStatus
    // ============================================================

    @TypeConverter
    fun fromTaskStatus(status: TaskStatus): String = status.name

    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus = TaskStatus.valueOf(value)

    // ============================================================
    // Priority
    // ============================================================

    @TypeConverter
    fun fromPriority(priority: Priority): String = priority.name

    @TypeConverter
    fun toPriority(value: String): Priority = Priority.valueOf(value)

    // ============================================================
    // NoteColor
    // ============================================================

    @TypeConverter
    fun fromNoteColor(color: NoteColor): String = color.name

    @TypeConverter
    fun toNoteColor(value: String): NoteColor = NoteColor.valueOf(value)

    // ============================================================
    // TimerState
    // ============================================================

    @TypeConverter
    fun fromTimerState(state: com.timetask.pro.v2.data.local.db.entity.TimerState): String = state.name

    @TypeConverter
    fun toTimerState(value: String): com.timetask.pro.v2.data.local.db.entity.TimerState =
        com.timetask.pro.v2.data.local.db.entity.TimerState.valueOf(value)

    // ============================================================
    // ListType
    // ============================================================

    @TypeConverter
    fun fromListType(type: com.timetask.pro.v2.data.local.db.entity.ListType): String = type.name

    @TypeConverter
    fun toListType(value: String): com.timetask.pro.v2.data.local.db.entity.ListType =
        com.timetask.pro.v2.data.local.db.entity.ListType.valueOf(value)

    // ============================================================
    // RecurrenceType
    // ============================================================

    @TypeConverter
    fun fromRecurrenceType(type: com.timetask.pro.v2.data.local.db.entity.RecurrenceType): String = type.name

    @TypeConverter
    fun toRecurrenceType(value: String): com.timetask.pro.v2.data.local.db.entity.RecurrenceType =
        com.timetask.pro.v2.data.local.db.entity.RecurrenceType.valueOf(value)

    // ============================================================
    // List<String> ↔ JSON
    // ============================================================

    @TypeConverter
    fun fromStringList(value: List<String>): String = json.encodeToString(value)

    @TypeConverter
    fun toStringList(value: String): List<String> = try {
        json.decodeFromString(value)
    } catch (_: Exception) {
        emptyList()
    }

    // ============================================================
    // List<Long> ↔ JSON
    // ============================================================

    @TypeConverter
    fun fromLongList(value: List<Long>): String = json.encodeToString(value)

    @TypeConverter
    fun toLongList(value: String): List<Long> = try {
        json.decodeFromString(value)
    } catch (_: Exception) {
        emptyList()
    }
}
