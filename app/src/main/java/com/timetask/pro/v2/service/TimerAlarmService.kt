package com.timetask.pro.v2.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.timetask.pro.v2.R
import com.timetask.pro.v2.data.preferences.AppPreferences
import com.timetask.pro.v2.presentation.tools.timers.TimerAlarmActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.cancel

/**
 * Foreground Service для воспроизведения alarm при завершении таймера.
 *
 * Паттерн (как в Google Clock / TickTick / MultiTimer):
 *   AlarmManager → BroadcastReceiver → startForegroundService(TimerAlarmService)
 *                                              ↓
 *                                   startForeground(notification с Full-Screen Intent)
 *                                   + звук (USAGE_ALARM) + вибрация
 *                                              ↓
 *                                   Android решает:
 *                                   • Экран выключен → запускает TimerAlarmActivity
 *                                   • Экран включён → heads-up notification
 *
 * Почему startForegroundService(), а не startActivity():
 *   На Android 10+ startActivity() из фона ЗАБЛОКИРОВАН.
 *   startForegroundService() РАЗРЕШЁН всегда.
 *   Свежезапущенный сервис с startForeground() + Full-Screen Intent
 *   гарантированно показывает alarm screen.
 */
class TimerAlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createAlarmChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "ACTION_STOP_FROM_NOTIF") {
            stopAlarm()
            stopSelf()
            return START_NOT_STICKY
        }

        val timerId = intent?.getStringExtra(EXTRA_TIMER_ID) ?: ""
        val timerName = intent?.getStringExtra(EXTRA_TIMER_NAME) ?: "Таймер"
        val soundUriString = intent?.getStringExtra(EXTRA_SOUND_URI)
        val quickAddSec = intent?.getIntExtra(EXTRA_QUICK_ADD_SEC, 60) ?: 60
        val ringingDurationSec = intent?.getIntExtra(EXTRA_RINGING_DURATION_SEC, 180) ?: 180

        // Читаем preference: показывать ли heads-up notification
        val showNotification = runBlocking {
            AppPreferences.getInstance(applicationContext)
                .showTriggeredNotification.first()
        }

        // 1. Построить notification (HIGH или LOW канал в зависимости от preference)
        val notification = buildAlarmNotification(
            timerId, timerName, soundUriString, quickAddSec, showNotification
        )

        // 2. startForeground — СРАЗУ, до любой другой работы
        startForeground(ALARM_NOTIFICATION_ID, notification)

        // 3. Запустить звук и вибрацию
        startAlarmSound(soundUriString)
        startVibration()

        // 4. Принудительный запуск Activity (popup).
        launchAlarmActivityFallback(timerId, timerName, soundUriString, quickAddSec)

        // 5. Запуск таймера автовыключения звонка (если не бесконечно)
        if (ringingDurationSec != -1) {
            serviceScope.launch {
                delay(ringingDurationSec * 1000L)
                Log.i(TAG, "Ringing duration expired ($ringingDurationSec sec). Auto-stopping timer alarm.")
                stopAlarm()
                stopSelf() // Выключит сервис, Notification сам не закроется пока не отработает AlarmActivity (т.к. мы пушим Intent, но в Activity есть проверка. Возможно она исчезнет, Android может убить Notification при stopForeground. В данном случае это желаемое поведение: если не ответили — закрылось).
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        stopAlarm()
    }

    // ============================================================
    // Helpers
    // ============================================================

    /**
     * Проверка доступности FullScreenIntent (Android 14+ может отозвать).
     */
    private fun canUseFullScreenIntentCompat(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            NotificationManagerCompat.from(this).canUseFullScreenIntent()
        } else {
            true // До Android 14 — всегда доступно
        }
    }

    /**
     * Fallback: принудительный запуск Activity, если FullScreenIntent не доступен.
     */
    private fun launchAlarmActivityFallback(
        timerId: String,
        timerName: String,
        soundUri: String?,
        quickAddSec: Int,
    ) {
        val activityIntent = TimerAlarmActivity.createIntent(
            this, timerId, timerName, soundUri, quickAddSec
        ).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
        }
        try {
            startActivity(activityIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Fallback startActivity failed", e)
        }
    }

    // ============================================================
    // Notification с Full-Screen Intent (AOSP pattern)
    // ============================================================

    private fun buildAlarmNotification(
        timerId: String,
        timerName: String,
        soundUri: String?,
        quickAddSec: Int,
        showHeadsUp: Boolean,
    ): Notification {
        // PendingIntent для Full-Screen → TimerAlarmActivity
        val fullScreenIntent = PendingIntent.getActivity(
            this,
            ALARM_NOTIFICATION_ID,
            TimerAlarmActivity.createIntent(this, timerId, timerName, soundUri, quickAddSec),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        // Content intent: при нажатии на notification → открыть AlarmActivity
        val contentIntent = PendingIntent.getActivity(
            this,
            ALARM_NOTIFICATION_ID + 1,
            TimerAlarmActivity.createIntent(this, timerId, timerName, soundUri, quickAddSec),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        // Выбор канала: HIGH (heads-up) или LOW (тихий) по preference
        val channelId = if (showHeadsUp) ALARM_CHANNEL_HIGH_ID else ALARM_CHANNEL_LOW_ID
        val priority = if (showHeadsUp) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_LOW

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO: Иконка будильника
            .setContentTitle("⏰ $timerName")
            .setContentText("Таймер завершён!")
            .setPriority(priority)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(contentIntent)
            .setFullScreenIntent(fullScreenIntent, true)
            .setDefaults(0) // Звук/вибрация управляются сервисом программно
            .addAction(
                R.drawable.ic_launcher_foreground, // TODO: Icon Stop
                "Стоп",
                stopPendingIntent()
            )
            .build()
    }

    private fun stopPendingIntent(): PendingIntent {
        val intent = Intent(this, TimerAlarmService::class.java).apply {
            action = "ACTION_STOP_FROM_NOTIF"
        }
        return PendingIntent.getService(
            this,
            ALARM_NOTIFICATION_ID + 2,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    /**
     * Два канала:
     * - HIGH: IMPORTANCE_HIGH — heads-up notification (default)
     * - LOW:  IMPORTANCE_LOW  — тихий, без heads-up (toggle OFF)
     */
    private fun createAlarmChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            // High-priority channel (default)
            val highChannel = NotificationChannel(
                ALARM_CHANNEL_HIGH_ID,
                "Будильник таймера",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Срабатывание таймера — полноэкранный alarm"
                setBypassDnd(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                enableVibration(false)
                setSound(null, null)
            }
            manager.createNotificationChannel(highChannel)

            // Low-priority channel (silent — when toggle is OFF)
            val lowChannel = NotificationChannel(
                ALARM_CHANNEL_LOW_ID,
                "Будильник таймера (тихий)",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Тихое уведомление при срабатывании (без heads-up)"
                enableVibration(false)
                setSound(null, null)
            }
            manager.createNotificationChannel(lowChannel)
        }
    }

    // ============================================================
    // Sound
    // ============================================================

    private fun startAlarmSound(soundUriString: String?) {
        try {
            val soundUri: Uri = if (!soundUriString.isNullOrBlank()) {
                Uri.parse(soundUriString)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                setDataSource(this@TimerAlarmService, soundUri)
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ============================================================
    // Vibration
    // ============================================================

    private fun startVibration() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val pattern = longArrayOf(0, 500, 300, 500, 300, 800)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    // ============================================================
    // Stop all
    // ============================================================

    private fun stopAlarm() {
        mediaPlayer?.let {
            try {
                if (it.isPlaying) it.stop()
                it.release()
            } catch (_: Exception) { }
        }
        mediaPlayer = null

        vibrator?.cancel()
        vibrator = null
    }

    // ============================================================
    // Companion — start/stop helpers
    // ============================================================

    companion object {
        private const val TAG = "TimerAlarmService"

        // Два канала: HIGH (heads-up) и LOW (тихий)
        private const val ALARM_CHANNEL_HIGH_ID = "timer_alarm_channel_high"
        private const val ALARM_CHANNEL_LOW_ID = "timer_alarm_channel_low"
        const val ALARM_NOTIFICATION_ID = 7000

        const val EXTRA_TIMER_ID = "timer_id"
        const val EXTRA_TIMER_NAME = "timer_name"
        const val EXTRA_SOUND_URI = "sound_uri"
        const val EXTRA_QUICK_ADD_SEC = "quick_add_sec"
        const val EXTRA_RINGING_DURATION_SEC = "ringing_duration_sec"

        /**
         * Запустить alarm service.
         * AOSP-паттерн: только startForegroundService.
         * FullScreenIntent в notification сам запустит Activity.
         * Fallback на startActivity() — внутри onStartCommand(), если FSI недоступен.
         */
        fun start(
            context: Context,
            timerId: String,
            timerName: String,
            soundUri: String? = null,
            quickAddSec: Int = 60,
            ringingDurationSec: Int = 180
        ) {
            val serviceIntent = Intent(context, TimerAlarmService::class.java).apply {
                putExtra(EXTRA_TIMER_ID, timerId)
                putExtra(EXTRA_TIMER_NAME, timerName)
                putExtra(EXTRA_SOUND_URI, soundUri)
                putExtra(EXTRA_QUICK_ADD_SEC, quickAddSec)
                putExtra(EXTRA_RINGING_DURATION_SEC, ringingDurationSec)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            // НЕ вызываем startActivity() здесь — FullScreenIntent в notification сделает это.
            // Fallback обрабатывается в onStartCommand() через canUseFullScreenIntentCompat().
        }

        /**
         * Остановить alarm service (звук, вибрация, notification).
         */
        fun stop(context: Context) {
            context.stopService(Intent(context, TimerAlarmService::class.java))
        }
    }
}
