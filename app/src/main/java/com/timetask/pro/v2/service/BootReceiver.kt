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
 * Ресивер для восстановления таймеров после перезагрузки устройства.
 *
 * При BOOT_COMPLETED:
 * 1. Получает список таймеров, которые были RUNNING до перезагрузки
 * 2. Перепланирует alarm'ы для каждого (если endTimeMs ещё в будущем)
 * 3. Запускает ForegroundService для уведомлений
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val db = TimeTaskDatabase.getInstance(context)
                val repository = TimerRepository.getInstance(db)
                val timerManager = TimerManager.getInstance(context, repository)

                val runningTimers = repository.getRunningTimers()
                if (runningTimers.isEmpty()) return@launch

                val now = System.currentTimeMillis()

                runningTimers.forEach { timer ->
                    if (timer.endTimeMs > now) {
                        // Таймер ещё не истёк — перепланировать alarm
                        timerManager.rescheduleAlarm(timer.id, timer.endTimeMs)
                    } else {
                        // Таймер уже должен был завершиться — обработать истечение
                        timerManager.onTimerExpired(timer.id)
                    }
                }

                // Запустить сервис для отображения уведомления
                TimerService.start(context)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
