package com.timetask.pro.v2.domain.usecase.alarm

import com.timetask.pro.v2.data.local.db.entity.AlarmEntity
import com.timetask.pro.v2.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class AlarmUseCases(
    private val repository: AlarmRepository,
    private val scheduler: AlarmScheduler
) {

    fun getAllActiveAlarms(): Flow<List<AlarmEntity>> = repository.getAllActiveAlarms()

    suspend fun addAlarm(
        hour: Int,
        minute: Int,
        repeatDaysMask: Int = 0,
        label: String = "",
        soundUri: String? = null,
        vibrationPattern: String? = null,
        volume: Float? = null,
        snoozeDurationMinutes: Int = 5,
        snoozeRepeatTimes: Int = 3,
        deleteAfterGoOff: Boolean = false,
        colorARGB: Int? = null,
        categoryText: String = "",
        tagsText: String = "",
        notesText: String = "",
        ringingDurationSec: Int = 300,
        autoSnoozeDurationSec: Int = 300,
        tagIdsJson: String = "[]",
    ) {
        val alarm = AlarmEntity(
            id = UUID.randomUUID().toString(),
            hour = hour,
            minute = minute,
            repeatDaysMask = repeatDaysMask,
            isEnabled = true,
            label = label,
            soundUri = soundUri,
            vibrationPattern = vibrationPattern,
            volume = volume,
            snoozeDurationMinutes = snoozeDurationMinutes,
            snoozeRepeatTimes = snoozeRepeatTimes,
            deleteAfterGoOff = deleteAfterGoOff,
            colorARGB = colorARGB,
            categoryText = categoryText,
            tagsText = tagsText,
            notesText = notesText,
            ringingDurationSec = ringingDurationSec,
            autoSnoozeDurationSec = autoSnoozeDurationSec,
            tagIdsJson = tagIdsJson,
            nextTriggerTime = 0L // Will be calculated by Scheduler
        )
        
        val scheduledAlarm = scheduler.schedule(alarm)
        repository.addAlarm(scheduledAlarm)
    }

    suspend fun toggleAlarm(alarm: AlarmEntity, isEnabled: Boolean) {
        val updated = alarm.copy(isEnabled = isEnabled)
        val finalAlarm = if (isEnabled) {
            scheduler.schedule(updated)
        } else {
            scheduler.cancel(updated)
            updated.copy(nextTriggerTime = 0L)
        }
        repository.updateAlarm(finalAlarm)
    }
    
    suspend fun updateAlarm(alarm: AlarmEntity) {
        val updatedAlarm = if (alarm.isEnabled) {
            scheduler.schedule(alarm)
        } else {
            scheduler.cancel(alarm)
            alarm.copy(nextTriggerTime = 0L)
        }
        repository.updateAlarm(updatedAlarm)
    }

    suspend fun deleteAlarm(alarm: AlarmEntity) {
        scheduler.cancel(alarm)
        repository.softDeleteAlarm(alarm.id)
    }

    suspend fun snoozeAlarm(alarmId: String, overrideSnoozeDurationMinutes: Int? = null) {
        val alarm = repository.getAlarmById(alarmId) ?: return
        if (!alarm.isEnabled || alarm.isDeleted) return

        val snoozedAlarm = scheduler.snooze(alarm, overrideSnoozeDurationMinutes)
        repository.updateAlarm(snoozedAlarm)
    }
}
