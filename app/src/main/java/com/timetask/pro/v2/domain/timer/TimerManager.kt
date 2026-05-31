package com.timetask.pro.v2.domain.timer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.timetask.pro.v2.data.local.db.entity.TimerConfig
import com.timetask.pro.v2.data.local.db.entity.TimerState
import com.timetask.pro.v2.data.repository.TaskRepository
import com.timetask.pro.v2.data.repository.TimerRepository
import com.timetask.pro.v2.service.TimerAlarmReceiver
import com.timetask.pro.v2.service.TimerNotificationHelper
import com.timetask.pro.v2.service.TimerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Единая точка управления таймерами (Domain Layer).
 *
 * Обязанности:
 * - Start / Pause / Stop / Adjust — жизненный цикл таймера
 * - onTimerExpired — State Machine (Finish / Overtime / Auto-Repeat)
 * - scheduleAlarm / cancelAlarm — взаимодействие с AlarmManager
 * - Управление ForegroundService
 */
class TimerManager private constructor(
    private val context: Context,
    private val repository: TimerRepository,
    private val taskRepository: TaskRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ========================================================================
    // Управление таймерами
    // ========================================================================

    fun startTimer(id: String) {
        scope.launch {
            val timer = repository.getTimerById(id) ?: return@launch
            if (timer.state == TimerState.RUNNING) return@launch

            val now = System.currentTimeMillis()

            // Если таймер был FINISHED (и не сброшен) — рестарт с полной длительности.
            // Если PAUSED — продолжаем с оставшегося времени.
            val durationToUse = if (timer.state == TimerState.FINISHED && timer.remainingMs <= 0) {
                timer.totalDurationMs
            } else {
                timer.remainingMs
            }

            val endTime = now + durationToUse

            // Elapsed: при первом старте из IDLE/FINISHED — инициализация.
            //          при resume из PAUSED — накопление паузного времени.
            val isResume = timer.state == TimerState.PAUSED && timer.startedAtMs > 0L
            val newStartedAt = if (isResume) timer.startedAtMs else now
            val newAccPause = if (isResume) {
                timer.accumulatedPauseMs + (now - timer.pausedAtMs)
            } else {
                0L
            }

            val updatedTimer = timer.copy(
                state = TimerState.RUNNING,
                remainingMs = durationToUse,
                endTimeMs = endTime,
                startedAtMs = newStartedAt,
                pausedAtMs = 0L,
                accumulatedPauseMs = newAccPause,
            )

            // 1. Обновить БД (Single Source of Truth)
            repository.updateTimer(updatedTimer)

            // 2. Завести системный будильник (надёжность — сработает даже если процесс убит)
            scheduleAlarm(id, endTime)

            // 3. Запустить Foreground Service (видимость + keep-alive)
            TimerService.start(context)
        }
    }

    fun pauseTimer(id: String) {
        scope.launch {
            val timer = repository.getTimerById(id) ?: return@launch
            if (timer.state != TimerState.RUNNING) return@launch

            val now = System.currentTimeMillis()
            val remaining = timer.endTimeMs - now

            // Для Overtime оставляем отрицательное значение, для обычного — clamp к 0
            val storedRemaining = if (remaining < 0 && !timer.config.enableOvertime) 0L else remaining

            // Time Accumulation
            val sessionElapsed = now - timer.startedAtMs
            if (sessionElapsed > 0 && timer.linkedTaskIdsJson.isNotBlank() && timer.linkedTaskIdsJson != "[]") {
                try {
                    val jsonArray = org.json.JSONArray(timer.linkedTaskIdsJson)
                    for (i in 0 until jsonArray.length()) {
                        taskRepository.addAccumulatedTime(jsonArray.getLong(i), sessionElapsed)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val updatedTimer = timer.copy(
                state = TimerState.PAUSED,
                remainingMs = storedRemaining,
                endTimeMs = 0L,
                pausedAtMs = now,
            )

            repository.updateTimer(updatedTimer)
            cancelAlarm(timer.id)
            checkServiceStop()
        }
    }

    fun stopTimer(id: String) {
        scope.launch {
            val timer = repository.getTimerById(id) ?: return@launch

            val updatedTimer = timer.copy(
                state = TimerState.IDLE,
                remainingMs = timer.totalDurationMs,
                endTimeMs = 0L,
                startedAtMs = 0L,
                pausedAtMs = 0L,
                accumulatedPauseMs = 0L,
            )

            // Time Accumulation if it was running or paused
            val now = System.currentTimeMillis()
            val sessionElapsed = if (timer.state == TimerState.RUNNING) {
                now - timer.startedAtMs
            } else 0L
            if (sessionElapsed > 0 && timer.linkedTaskIdsJson.isNotBlank() && timer.linkedTaskIdsJson != "[]") {
                try {
                    val jsonArray = org.json.JSONArray(timer.linkedTaskIdsJson)
                    for (i in 0 until jsonArray.length()) {
                        taskRepository.addAccumulatedTime(jsonArray.getLong(i), sessionElapsed)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            repository.updateTimer(updatedTimer)
            cancelAlarm(timer.id)
            TimerNotificationHelper.cancelFinished(context, timer.id)
            checkServiceStop()
        }
    }

    fun adjustTime(id: String, deltaMs: Long) {
        scope.launch {
            val timer = repository.getTimerById(id) ?: return@launch
            val now = System.currentTimeMillis()

            if (timer.state == TimerState.FINISHED) {
                // Если таймер завершён, добавление времени запускает его на это время
                val newEndTime = now + deltaMs
                val updatedTimer = timer.copy(
                    state = TimerState.RUNNING,
                    endTimeMs = newEndTime,
                    remainingMs = deltaMs,
                    startedAtMs = now,
                )
                repository.updateTimer(updatedTimer)
                scheduleAlarm(timer.id, newEndTime)
                TimerNotificationHelper.cancelFinished(context, timer.id)
                TimerService.start(context)
            } else if (timer.state == TimerState.RUNNING) {
                val newEndTime = timer.endTimeMs + deltaMs
                val newRemaining = newEndTime - now

                val updatedTimer = timer.copy(
                    endTimeMs = newEndTime,
                    remainingMs = newRemaining,
                )
                repository.updateTimer(updatedTimer)
                scheduleAlarm(timer.id, newEndTime)
            } else {
                val newRemaining = (timer.remainingMs + deltaMs).coerceAtLeast(0L)
                val updatedTimer = timer.copy(remainingMs = newRemaining)
                repository.updateTimer(updatedTimer)
            }
        }
    }

    fun restartTimer(id: String) {
        scope.launch {
            val timer = repository.getTimerById(id) ?: return@launch
            // Сброс в IDLE и сразу Start
            stopTimer(id)
            // Небольшая задержка, чтобы stop успел пройти (хотя здесь всё в одном скоупе, но stopTimer запускает корутину)
            // Лучше переиспользовать логику stop внутри или просто вызвать startTimer,
            // но startTimer проверяет state.
            // Проще вручную сбросить и запустить.
            
            val now = System.currentTimeMillis()
            val endTime = now + timer.totalDurationMs
            
            val restartedTimer = timer.copy(
                state = TimerState.RUNNING,
                remainingMs = timer.totalDurationMs,
                endTimeMs = endTime,
                startedAtMs = now,
                pausedAtMs = 0L,
                accumulatedPauseMs = 0L,
            )
            
            repository.updateTimer(restartedTimer)
            scheduleAlarm(id, endTime)
            TimerNotificationHelper.cancelFinished(context, id)
            TimerService.start(context)
        }
    }

    fun startOvertime(id: String) {
        scope.launch {
            val timer = repository.getTimerById(id) ?: return@launch
            if (timer.state != TimerState.FINISHED) return@launch

            val now = System.currentTimeMillis()
            // Overtime начинается с 0 (или с момента завершения, если бы мы его хранили).
            // Считаем с сейчас.
            val updatedTimer = timer.copy(
                state = TimerState.RUNNING,
                endTimeMs = now, // remaining = 0 -> потом будет отрицательным
                startedAtMs = now,
                pausedAtMs = 0L,
                accumulatedPauseMs = 0L,
            )
            repository.updateTimer(updatedTimer)
            TimerNotificationHelper.cancelFinished(context, id)
            TimerService.start(context)
        }
    }

    // ========================================================================
    // State Machine — логика истечения таймера
    // ========================================================================

    /**
     * Вызывается когда таймер достигает endTimeMs.
     * Решает: завершить / перейти в Overtime / Auto-Repeat / Auto-Reset.
     *
     * Может быть вызван из:
     * - [TimerAlarmReceiver] (AlarmManager сработал)
     * - [TimerService] (tick loop обнаружил истечение)
     * - [BootReceiver] (таймер истёк во время перезагрузки)
     */
    suspend fun onTimerExpired(id: String) {
        val timer = repository.getTimerById(id) ?: return
        val now = System.currentTimeMillis()

        // Защита от race condition — проверяем, действительно ли таймер истёк
        if (timer.state != TimerState.RUNNING || timer.endTimeMs > now + 1000) return

        // Time Accumulation
        val sessionElapsed = now - timer.startedAtMs
        if (sessionElapsed > 0 && timer.linkedTaskIdsJson.isNotBlank() && timer.linkedTaskIdsJson != "[]") {
            try {
                val jsonArray = org.json.JSONArray(timer.linkedTaskIdsJson)
                for (i in 0 until jsonArray.length()) {
                    taskRepository.addAccumulatedTime(jsonArray.getLong(i), sessionElapsed)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 1. Auto-Repeat: перезапуск с полной длительности
        if (timer.config.autoRepeat) {
            val nextEndTime = now + timer.totalDurationMs
            val restartedTimer = timer.copy(
                endTimeMs = nextEndTime,
                startedAtMs = now,
                pausedAtMs = 0L,
                accumulatedPauseMs = 0L,
            )
            repository.updateTimer(restartedTimer)
            scheduleAlarm(id, nextEndTime)
            return
        }

        // 2. Overtime: таймер продолжает работать, endTimeMs в прошлом → отрицательное время
        if (timer.config.enableOvertime) {
            // State остаётся RUNNING, UI считает endTimeMs - now (будет отрицательным)
            return
        }

        // 3. Auto-Reset: сброс в исходное состояние без перезапуска
        if (timer.config.autoReset) {
            val resetTimer = timer.copy(
                state = TimerState.IDLE,
                remainingMs = timer.totalDurationMs,
                endTimeMs = 0L,
                startedAtMs = 0L,
                pausedAtMs = 0L,
                accumulatedPauseMs = 0L,
            )
            repository.updateTimer(resetTimer)
            return
        }

        // 4. Стандартное завершение — ожидание действия пользователя
        val finishedTimer = timer.copy(
            state = TimerState.FINISHED,
            remainingMs = 0L,
            endTimeMs = 0L,
            pausedAtMs = 0L,
            accumulatedPauseMs = 0L,
        )
        repository.updateTimer(finishedTimer)
    }

    // ========================================================================
    // Обновление конфигурации
    // ========================================================================

    fun updateTimerConfig(id: String, config: TimerConfig) {
        scope.launch {
            val timer = repository.getTimerById(id) ?: return@launch
            repository.updateTimer(timer.copy(config = config))
        }
    }

    // ========================================================================
    // Alarm Helpers (приватные)
    // ========================================================================

    /**
     * Публичный метод для перепланирования alarm'а (используется [BootReceiver]).
     */
    fun rescheduleAlarm(timerId: String, triggerAtMs: Long) {
        scheduleAlarm(timerId, triggerAtMs)
    }

    internal fun scheduleAlarm(timerId: String, triggerAtMs: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TimerAlarmReceiver::class.java).apply {
            action = TimerAlarmReceiver.ACTION_TIMER_EXPIRED
            putExtra(TimerAlarmReceiver.EXTRA_TIMER_ID, timerId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            timerId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // setAlarmClock — МАКСИМАЛЬНЫЙ приоритет:
        // • Гарантированно срабатывает в Doze
        // • Показывает иконку ⏰ в статус-баре
        // • Не требует SCHEDULE_EXACT_ALARM на API 31+
        // • Используется Google Clock, TickTick, MultiTimer
        val showIntent = PendingIntent.getActivity(
            context,
            timerId.hashCode() + 1,
            Intent(context, com.timetask.pro.v2.MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerAtMs, showIntent),
            pendingIntent,
        )
    }

    private fun cancelAlarm(timerId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TimerAlarmReceiver::class.java).apply {
            action = TimerAlarmReceiver.ACTION_TIMER_EXPIRED
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            timerId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    // ========================================================================
    // Service Helpers
    // ========================================================================

    private suspend fun checkServiceStop() {
        val runningTimers = repository.getRunningTimers()
        if (runningTimers.isEmpty()) {
            TimerService.stop(context)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: TimerManager? = null

        fun getInstance(context: Context, repository: TimerRepository): TimerManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TimerManager(
                    context.applicationContext, 
                    repository,
                    TaskRepository.getInstance(context)
                ).also {
                    INSTANCE = it
                }
            }
        }
    }
}
