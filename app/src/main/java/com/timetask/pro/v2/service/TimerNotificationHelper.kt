package com.timetask.pro.v2.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.timetask.pro.v2.MainActivity
import com.timetask.pro.v2.R
import com.timetask.pro.v2.data.local.db.entity.TimerEntity
import com.timetask.pro.v2.data.local.db.entity.TimerNotification

/**
 * Хелпер для уведомлений о завершении таймера.
 *
 * Отдельный канал IMPORTANCE_HIGH для heads-up notification.
 * Поддерживает: звук (custom URI или default alarm), вибрацию, зацикливание.
 */
object TimerNotificationHelper {

    private const val CHANNEL_ID = "timer_finished_channel"
    private const val NOTIFICATION_ID_BASE = 5000 // offset от foreground service ID

    /** Extra key для указания куда навигировать по клику на уведомление */
    const val EXTRA_NAVIGATE_TO = "navigate_to_tab"
    /** Значение extra для открытия вкладки Таймеров */
    const val NAVIGATE_TIMERS = "timers"

    /**
     * Показать notification о завершении таймера (дополнительно к TimerAlarmService).
     * Это «тихое» уведомление в шторке — звук и alarm screen управляются сервисом.
     */
    /**
     * Показать notification о завершении таймера (дополнительно к TimerAlarmService).
     * Это «тихое» уведомление в шторке — звук и alarm screen управляются сервисом.
     *
     * @deprecated Используется TimerAlarmService
     */
    fun showFinished(context: Context, timer: TimerEntity) {
        // Логика перенесена в TimerAlarmService
        // Оставляем пустым или удаляем вызов
    }

    /**
     * Убрать уведомление о завершении (если пользователь сам остановил таймер).
     */
    fun cancelFinished(context: Context, timerId: String) {
        val notifId = NOTIFICATION_ID_BASE + timerId.hashCode()
        NotificationManagerCompat.from(context).cancel(notifId)
    }

    /**
     * Создать notification channel для завершённых таймеров.
     */
    private fun createFinishedChannel(context: Context, notifConfig: TimerNotification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Завершение таймера",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Уведомления о завершении таймеров"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)

                // Звук через alarm stream
                val soundUri = getSoundUri(context, notifConfig)
                if (soundUri != null && !notifConfig.isSilent) {
                    setSound(
                        soundUri,
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build(),
                    )
                }
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Получить URI звука: custom или default alarm.
     */
    private fun getSoundUri(context: Context, notification: TimerNotification): Uri? {
        return if (notification.soundUri != null) {
            try {
                Uri.parse(notification.soundUri)
            } catch (_: Exception) {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            }
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        }
    }

    /**
     * Парсить JSON паттерн вибрации (e.g. "[0, 500, 200, 500]").
     */
    private fun parseVibrationPattern(json: String?): LongArray? {
        if (json.isNullOrBlank()) return null
        return try {
            json.trim('[', ']')
                .split(",")
                .map { it.trim().toLong() }
                .toLongArray()
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Программная вибрация.
     */
    private fun triggerVibration(context: Context, notification: TimerNotification) {
        val pattern = parseVibrationPattern(notification.vibrationPatternJson)
            ?: longArrayOf(0, 500, 200, 500)

        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val repeat = if (notification.isLooping) 1 else -1
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, repeat))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, if (notification.isLooping) 1 else -1)
        }
    }
}
