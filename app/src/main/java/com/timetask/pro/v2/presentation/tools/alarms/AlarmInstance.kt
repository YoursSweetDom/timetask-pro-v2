package com.timetask.pro.v2.presentation.tools.alarms

import androidx.compose.runtime.Immutable
import com.timetask.pro.v2.data.local.db.entity.AlarmEntity

/**
 * UI State representation of an Alarm.
 */
@Immutable
data class AlarmInstance(
    val id: String,
    val name: String,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean,
    val repeatDaysMask: Int,
    val snoozeMinutes: Int,
    val snoozeRepeatTimes: Int,
    val ringingDurationSec: Int,
    val autoSnoozeDurationSec: Int,
    val deleteAfterGoOff: Boolean,
    val colorARGB: Int?,
    val categoryText: String,
    val tagsText: String,
    val tagIds: List<Long> = emptyList(),
    val notesText: String,
    val nextTriggerTime: Long
)

/**
 * Short day names. Let's assume standard UI index 0=Mon, 1=Tue... 6=Sun
 */
val dayNames = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

fun AlarmEntity.toUiModel(): AlarmInstance {
    return AlarmInstance(
        id = this.id,
        name = this.label.ifBlank { "Будильник" },
        hour = this.hour,
        minute = this.minute,
        isEnabled = this.isEnabled,
        repeatDaysMask = this.repeatDaysMask,
        snoozeMinutes = this.snoozeDurationMinutes,
        snoozeRepeatTimes = this.snoozeRepeatTimes,
        ringingDurationSec = this.ringingDurationSec,
        autoSnoozeDurationSec = this.autoSnoozeDurationSec,
        deleteAfterGoOff = this.deleteAfterGoOff,
        colorARGB = this.colorARGB,
        categoryText = this.categoryText,
        tagsText = this.tagsText,
        tagIds = try { kotlinx.serialization.json.Json.decodeFromString(this.tagIdsJson) } catch (e: Exception) { emptyList() },
        notesText = this.notesText,
        nextTriggerTime = this.nextTriggerTime
    )
}

fun AlarmInstance.hasDay(dayIndex: Int): Boolean {
    // 0=Mon, 1=Tue... map to 1, 2, 4, 8, 16, 32, 64
    val bitForDay = 1 shl dayIndex
    return (this.repeatDaysMask and bitForDay) != 0
}
