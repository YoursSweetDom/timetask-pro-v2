package com.timetask.pro.v2.service.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.timetask.pro.v2.data.local.db.entity.AlarmEntity
import com.timetask.pro.v2.domain.usecase.alarm.AlarmScheduler
import java.util.Calendar

class AndroidAlarmScheduler(
    private val context: Context
) : AlarmScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(alarmItem: AlarmEntity): AlarmEntity {
        // Android 14+ specific check. Without USE_EXACT_ALARM this can throw SecurityException for SCHEDULE_EXACT_ALARM.
        // But since we use USE_EXACT_ALARM, it's pre-granted. Just safe wrap.
        if (!alarmManager.canScheduleExactAlarms()) {
            Log.e("AlarmScheduler", "Cannot schedule exact alarms! Permission denied.")
            // Realistically we'd throw or notify the UI, but Google Play grants USE_EXACT_ALARM.
            return alarmItem.copy(nextTriggerTime = 0L, isEnabled = false)
        }

        val triggerTimeMs = calculateNextTriggerTime(alarmItem)
        
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarmItem.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmItem.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Used by UI to show the alarm icon in the System Status Bar and Lock Screen
        val info = AlarmManager.AlarmClockInfo(
            triggerTimeMs,
            pendingIntent // You can also pass a separate intent here to open the app if the user taps the upcoming alarm info
        )

        alarmManager.setAlarmClock(info, pendingIntent)
        
        Log.d("AlarmScheduler", "Scheduled alarm ${alarmItem.id} for $triggerTimeMs")
        return alarmItem.copy(nextTriggerTime = triggerTimeMs)
    }

    override fun cancel(alarmItem: AlarmEntity) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarmItem.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmItem.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        Log.d("AlarmScheduler", "Canceled alarm ${alarmItem.id}")
    }

    override fun snooze(alarmItem: AlarmEntity, overrideSnoozeDurationMinutes: Int?): AlarmEntity {
        if (!alarmManager.canScheduleExactAlarms()) {
            return alarmItem
        }

        val minutesToSnooze = overrideSnoozeDurationMinutes ?: alarmItem.snoozeDurationMinutes
        val triggerTimeMs = System.currentTimeMillis() + (minutesToSnooze * 60 * 1000L)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarmItem.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmItem.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val info = AlarmManager.AlarmClockInfo(triggerTimeMs, pendingIntent)
        alarmManager.setAlarmClock(info, pendingIntent)
        
        Log.d("AlarmScheduler", "Snoozed alarm ${alarmItem.id} until $triggerTimeMs")
        return alarmItem.copy(nextTriggerTime = triggerTimeMs)
    }

    companion object {
        const val EXTRA_ALARM_ID = "EXTRA_ALARM_ID"
        
        fun calculateNextTriggerTime(alarm: AlarmEntity): Long {
            val now = Calendar.getInstance()
            val next = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, alarm.hour)
                set(Calendar.MINUTE, alarm.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (alarm.repeatDaysMask == 0) {
                // One-time alarm
                if (next.before(now)) {
                    next.add(Calendar.DAY_OF_MONTH, 1)
                }
                return next.timeInMillis
            }

            // Repeating alarm: find the next matching day
            // repeatDaysMask: 1=Mon, 2=Tue, 4=Wed, 8=Thu, 16=Fri, 32=Sat, 64=Sun
            // Calendar.DAY_OF_WEEK: 1=Sun, 2=Mon... 7=Sat
            
            // If the time today has already passed, start checking from tomorrow
            if (next.before(now)) {
                next.add(Calendar.DAY_OF_MONTH, 1)
            }

            val maxLookaheadDays = 8
            for (i in 0 until maxLookaheadDays) {
                val calendarDayOfWeek = next.get(Calendar.DAY_OF_WEEK)
                val bitmaskDay = convertCalendarDayToBitmask(calendarDayOfWeek)
                
                if ((alarm.repeatDaysMask and bitmaskDay) != 0) {
                    return next.timeInMillis
                }
                next.add(Calendar.DAY_OF_MONTH, 1)
            }

            // Fallback (shouldn't really happen if mask is valid)
            return next.timeInMillis
        }

        private fun convertCalendarDayToBitmask(calendarDayOfWeek: Int): Int {
            return when (calendarDayOfWeek) {
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 4
                Calendar.THURSDAY -> 8
                Calendar.FRIDAY -> 16
                Calendar.SATURDAY -> 32
                Calendar.SUNDAY -> 64
                else -> 0
            }
        }
    }
}
