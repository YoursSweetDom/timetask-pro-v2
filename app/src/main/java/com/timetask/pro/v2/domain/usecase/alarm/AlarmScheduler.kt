package com.timetask.pro.v2.domain.usecase.alarm

import com.timetask.pro.v2.data.local.db.entity.AlarmEntity

/**
 * Interface abstracting the Android AlarmManager logic.
 */
interface AlarmScheduler {
    
    /**
     * Calculates the next exact time the alarm should fire and schedules it in the OS.
     * Returns a copy of the AlarmEntity with updated `nextTriggerTime`.
     */
    fun schedule(alarmItem: AlarmEntity): AlarmEntity
    
    /**
     * Cancels the scheduled alarm in the OS.
     */
    fun cancel(alarmItem: AlarmEntity)

    /**
     * Reschedules the alarm immediately to current time + snoozeDurationMinutes.
     * Overrides the standard recurrence calculation for this specific trigger.
     */
    fun snooze(alarmItem: AlarmEntity, overrideSnoozeDurationMinutes: Int? = null): AlarmEntity
}
