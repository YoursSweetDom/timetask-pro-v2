package com.timetask.pro.v2.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.content.pm.ServiceInfo
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.timetask.pro.v2.MainActivity
import com.timetask.pro.v2.R
import com.timetask.pro.v2.service.TimerNotificationHelper.EXTRA_NAVIGATE_TO
import com.timetask.pro.v2.service.TimerNotificationHelper.NAVIGATE_TIMERS
import com.timetask.pro.v2.data.local.db.TimeTaskDatabase
import com.timetask.pro.v2.data.local.db.entity.TimerEntity
import com.timetask.pro.v2.data.local.db.entity.TimerState
import com.timetask.pro.v2.data.repository.TimerRepository
import com.timetask.pro.v2.domain.timer.TimerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Foreground Service для отображения уведомления и проверки таймеров.
 *
 * Обязанности:
 * - Показывать уведомление с ближайшим таймером
 * - Каждую секунду проверять, не истёк ли таймер
 * - При отсутствии активных таймеров — останавливаться
 */
class TimerService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var tickJob: Job? = null
    private lateinit var repository: TimerRepository
    private lateinit var timerManager: TimerManager
    override fun onCreate() {
        super.onCreate()
        val db = TimeTaskDatabase.getInstance(applicationContext)
        repository = TimerRepository.getInstance(db)
        timerManager = TimerManager.getInstance(applicationContext, repository)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val timerId = intent?.getStringExtra(EXTRA_TIMER_ID)
        
        when (intent?.action) {
            ACTION_START -> startTicking()
            ACTION_STOP_SERVICE -> stopTicking()
            ACTION_STOP_TIMER -> timerId?.let { timerManager.stopTimer(it) }
            ACTION_RESTART -> timerId?.let { timerManager.restartTimer(it) }
            ACTION_OVERTIME -> timerId?.let { timerManager.startOvertime(it) }
            ACTION_ADD_TIME -> {
                if (timerId != null) {
                    val seconds = intent.getIntExtra(EXTRA_ADD_SECONDS, 60)
                    timerManager.adjustTime(timerId, seconds * 1000L)
                }
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        tickJob?.cancel()
    }

    private fun startTicking() {
        if (tickJob?.isActive == true) return

        startForeground(NOTIFICATION_ID, buildNotification(emptyList()))

        tickJob = serviceScope.launch {
            while (isActive) {
                val runningTimers = repository.getRunningTimers()

                if (runningTimers.isEmpty()) {
                    stopSelf()
                    break
                }

                // Обновить уведомление (только таймеры с включённым showInNotifications)
                val visibleTimers = runningTimers.filter { it.notification.showInNotifications }
                updateNotification(visibleTimers)

                // Проверить истёкшие таймеры (ВСЕ, не только видимые)
                val now = System.currentTimeMillis()
                runningTimers.forEach { timer ->
                    if (timer.endTimeMs <= now && timer.state == TimerState.RUNNING) {
                        // Делегируем State Machine в TimerManager
                        timerManager.onTimerExpired(timer.id)

                        // Запустить alarm через отдельный Foreground Service
                        // (startForegroundService разрешён всегда, startActivity — нет)
                        TimerAlarmService.start(
                            context = this@TimerService,
                            timerId = timer.id,
                            timerName = timer.name,
                            soundUri = timer.notification.soundUri,
                            quickAddSec = timer.config.quickAddDurationSec,
                            ringingDurationSec = timer.notification.ringingDurationSec,
                        )
                    }
                }

                delay(1000L)
            }
        }
    }

    private fun stopTicking() {
        tickJob?.cancel()
        stopSelf()
    }

    private fun updateNotification(timers: List<TimerEntity>) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, buildNotification(timers))
    }

    private fun buildNotification(timers: List<TimerEntity>): Notification {
        val title = if (timers.isEmpty()) "Таймеры" else "Активных таймеров: ${timers.size}"
        val content = if (timers.isNotEmpty()) {
            val nearest = timers.minByOrNull { it.endTimeMs }!!
            val now = System.currentTimeMillis()
            val remaining = nearest.endTimeMs - now

            // Форматирование (поддержка Overtime — отрицательного времени)
            val absRemaining = kotlin.math.abs(remaining)
            val sign = if (remaining < 0) "-" else ""

            val format = String.format(
                "%s%02d:%02d",
                sign,
                TimeUnit.MILLISECONDS.toMinutes(absRemaining),
                TimeUnit.MILLISECONDS.toSeconds(absRemaining) % 60,
            )
            "${nearest.name}: $format"
        } else {
            "Нет активных таймеров"
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(EXTRA_NAVIGATE_TO, NAVIGATE_TIMERS)
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        // Мгновенное возрождение (Instant Respawn)
        // Ловим свайп уведомления (если Android 14+ разрешил его смахнуть) и моментально показываем её заново
        val dismissIntent = Intent(this, TimerDismissReceiver::class.java).apply {
            action = TimerDismissReceiver.ACTION_DISMISS
        }
        val pendingDismissIntent = PendingIntent.getBroadcast(
            this, 0, dismissIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO: Заменить на иконку таймера
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setContentIntent(pendingIntent)
            .setDeleteIntent(pendingDismissIntent)
            .setOnlyAlertOnce(true)
            .setAutoCancel(false)
            .build()
            .apply {
                flags = flags or Notification.FLAG_NO_CLEAR or Notification.FLAG_ONGOING_EVENT
            }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Активные таймеры",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Отслеживание запущенных таймеров"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "timer_service_channel_v2"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE" // Renamed from ACTION_STOP to avoid confusion
        
        const val ACTION_STOP_TIMER = "ACTION_STOP_TIMER"
        const val ACTION_RESTART = "ACTION_RESTART"
        const val ACTION_OVERTIME = "ACTION_OVERTIME"
        const val ACTION_ADD_TIME = "ACTION_ADD_TIME"

        const val EXTRA_TIMER_ID = "timer_id"
        const val EXTRA_ADD_SECONDS = "add_seconds"

        fun start(context: Context) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.startService(intent)
        }

        // Helpers for actions
        fun stopTimer(context: Context, timerId: String) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_STOP_TIMER
                putExtra(EXTRA_TIMER_ID, timerId)
            }
            context.startService(intent)
        }

        fun restartTimer(context: Context, timerId: String) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_RESTART
                putExtra(EXTRA_TIMER_ID, timerId)
            }
            context.startService(intent)
        }

        fun startOvertime(context: Context, timerId: String) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_OVERTIME
                putExtra(EXTRA_TIMER_ID, timerId)
            }
            context.startService(intent)
        }

        fun addTime(context: Context, timerId: String, seconds: Int) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_ADD_TIME
                putExtra(EXTRA_TIMER_ID, timerId)
                putExtra(EXTRA_ADD_SECONDS, seconds)
            }
            context.startService(intent)
        }
    }
}
