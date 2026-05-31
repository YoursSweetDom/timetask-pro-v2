package com.timetask.pro.v2.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.timetask.pro.v2.data.local.db.TimeTaskDatabase
import com.timetask.pro.v2.data.repository.TimerRepository
import com.timetask.pro.v2.domain.timer.TimerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Ресивер для AlarmManager.
 * Срабатывает когда таймер достигает endTimeMs — даже если приложение убито/в Doze.
 *
 * Паттерн: AlarmManager → BroadcastReceiver → startForegroundService(TimerAlarmService)
 */
class TimerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_TIMER_EXPIRED) return

        val timerId = intent.getStringExtra(EXTRA_TIMER_ID) ?: return

        // goAsync() даёт ~30 секунд на выполнение (вместо стандартных ~10)
        val pendingResult = goAsync()

        // Scope создаётся локально — BroadcastReceiver пересоздаётся при каждом вызове
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val db = TimeTaskDatabase.getInstance(context)
                val repo = TimerRepository.getInstance(db)
                val manager = TimerManager.getInstance(context, repo)

                // Получить данные таймера ДО изменения состояния
                val timer = repo.getTimerById(timerId)

                // State Machine решает: Finish / Overtime / Auto-Repeat
                manager.onTimerExpired(timerId)

                // Запустить alarm через Foreground Service (ВСЕГДА работает из фона)
                if (timer != null) {
                    TimerAlarmService.start(
                        context = context,
                        timerId = timer.id,
                        timerName = timer.name,
                        soundUri = timer.notification.soundUri,
                        quickAddSec = timer.config.quickAddDurationSec,
                        ringingDurationSec = timer.notification.ringingDurationSec,
                    )
                }

                // Разбудить сервис для обновления уведомления
                TimerService.start(context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_TIMER_EXPIRED = "com.timetask.pro.v2.ACTION_TIMER_EXPIRED"
        const val EXTRA_TIMER_ID = "EXTRA_TIMER_ID"
    }
}
