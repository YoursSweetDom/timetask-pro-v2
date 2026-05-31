package com.timetask.pro.v2.service.alarm

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
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.timetask.pro.v2.R
import com.timetask.pro.v2.data.local.db.entity.AlarmEntity
import com.timetask.pro.v2.data.preferences.AppPreferences
import com.timetask.pro.v2.data.repository.OfflineFirstAlarmRepository
import com.timetask.pro.v2.domain.usecase.alarm.AlarmUseCases
import com.timetask.pro.v2.presentation.tools.alarms.AlarmRingingActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class AlarmRingingService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var alarmUseCases: AlarmUseCases
    
    // We will store the current ringing alarm ID so we know what to dismiss/snooze
    private var currentAlarmId: String? = null

    override fun onCreate() {
        super.onCreate()
        val repository = OfflineFirstAlarmRepository.getInstance(applicationContext)
        val scheduler = AndroidAlarmScheduler(applicationContext)
        alarmUseCases = AlarmUseCases(repository, scheduler)

        createNotificationChannel()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        // Handling quick actions from the notification
        if (action == ACTION_DISMISS_QUICK) {
            val aId = intent.getStringExtra(EXTRA_ALARM_ID) ?: return START_NOT_STICKY
            serviceScope.launch { handleDismiss(aId) }
            return START_NOT_STICKY
        }
        if (action == ACTION_SNOOZE_QUICK) {
            val aId = intent.getStringExtra(EXTRA_ALARM_ID) ?: return START_NOT_STICKY
            serviceScope.launch { handleSnooze(aId) }
            return START_NOT_STICKY
        }
        
        // Activity callback actions
        if (action == ACTION_DISMISS) {
            currentAlarmId?.let { serviceScope.launch { handleDismiss(it) } }
            return START_NOT_STICKY
        }
        if (action == ACTION_SNOOZE) {
            currentAlarmId?.let { serviceScope.launch { handleSnooze(it) } }
            return START_NOT_STICKY
        }

        val alarmId = intent?.getStringExtra(EXTRA_ALARM_ID) ?: return START_NOT_STICKY
        currentAlarmId = alarmId

        // Читаем preference: показывать ли heads-up notification
        val showNotification = runBlocking {
            AppPreferences.getInstance(applicationContext)
                .showTriggeredNotification.first()
        }

        // Ring!
        startForeground(NOTIFICATION_ID, createNotification(alarmId, showNotification))
        
        serviceScope.launch { 
            val alarm = OfflineFirstAlarmRepository.getInstance(applicationContext).getAlarmById(alarmId)
            playRingtoneAndVibrate(alarm?.soundUri, alarm?.volume)

            // Принудительный запуск Activity (popup).
            launchAlarmActivityFallback(alarmId, alarm?.label ?: "Будильник", alarm?.notesText ?: "")

            if (alarm != null && alarm.ringingDurationSec != -1) {
                delay(alarm.ringingDurationSec * 1000L)
                Log.i(TAG, "Ringing duration expired (${alarm.ringingDurationSec} sec). Triggering auto-action.")
                if (alarm.autoSnoozeDurationSec != -1) {
                    Log.i(TAG, "Auto-snoozing alarm for ${alarm.autoSnoozeDurationSec} sec.")
                    handleSnooze(alarmId, overrideSnoozeDurationSec = alarm.autoSnoozeDurationSec)
                } else {
                    Log.i(TAG, "Auto-snooze disabled. Dismissing alarm.")
                    handleDismiss(alarmId)
                }
            }
        }

        return START_STICKY
    }

    /**
     * Проверка доступности FullScreenIntent (Android 14+ может отозвать).
     */
    private fun canUseFullScreenIntentCompat(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            NotificationManagerCompat.from(this).canUseFullScreenIntent()
        } else {
            true
        }
    }

    /**
     * Fallback: принудительный запуск Activity, если FullScreenIntent недоступен.
     */
    private fun launchAlarmActivityFallback(alarmId: String, alarmName: String, alarmNotes: String) {
        val activityIntent = Intent(this, AlarmRingingActivity::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_ALARM_NAME, alarmName)
            putExtra(EXTRA_ALARM_NOTES, alarmNotes)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                     Intent.FLAG_ACTIVITY_CLEAR_TOP or
                     Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        try {
            startActivity(activityIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Fallback startActivity failed", e)
        }
    }

    private suspend fun handleDismiss(alarmId: String) {
        val alarm = OfflineFirstAlarmRepository.getInstance(applicationContext).getAlarmById(alarmId)
        if (alarm != null) {
            if (alarm.deleteAfterGoOff) {
                alarmUseCases.deleteAlarm(alarm)
            } else if (alarm.repeatDaysMask == 0) {
                // One-time alarm, disable it
                alarmUseCases.toggleAlarm(alarm, false)
            } else {
                // Repeating alarm, schedule for next time
                alarmUseCases.updateAlarm(alarm)
            }
        }
        stopRingingAndFinish()
    }

    private suspend fun handleSnooze(alarmId: String, overrideSnoozeDurationSec: Int? = null) {
        if (overrideSnoozeDurationSec != null) {
            // If it's an auto-snooze, we pass the custom duration in minutes
            alarmUseCases.snoozeAlarm(alarmId, overrideSnoozeDurationSec / 60)
        } else {
            alarmUseCases.snoozeAlarm(alarmId)
        }
        stopRingingAndFinish()
    }

    private fun playRingtoneAndVibrate(uriString: String?, volume: Float?) {
        try {
            val uri = if (uriString.isNullOrEmpty()) {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            } else {
                Uri.parse(uriString)
            }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, uri)
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION) // Using SONIFICATION for proper volume matching like Timers
                    .build()
                setAudioAttributes(audioAttributes)
                
                volume?.let { setVolume(it, it) }
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e("AlarmRingingService", "Failed to play ringtone", e)
        }

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        val pattern = longArrayOf(0, 1000, 1000)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun stopRingingAndFinish() {
        mediaPlayer?.let {
            try {
                if (it.isPlaying) it.stop()
                it.release()
            } catch (_: Exception) { }
        }
        mediaPlayer = null

        vibrator?.cancel()
        vibrator = null

        releaseWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        
        // Tell the activity to close if it's currently open
        sendBroadcast(Intent(AlarmRingingActivity.ACTION_FINISH_ACTIVITY))
    }

    private fun createNotification(alarmId: String, showHeadsUp: Boolean = true): Notification {
        val fullScreenIntent = Intent(this, AlarmRingingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ALARM_ID, alarmId)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            alarmId.hashCode(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(this, AlarmRingingService::class.java).apply {
            action = ACTION_DISMISS_QUICK
            putExtra(EXTRA_ALARM_ID, alarmId)
        }
        val dismissPendingIntent = PendingIntent.getService(this, 1, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val snoozeIntent = Intent(this, AlarmRingingService::class.java).apply {
            action = ACTION_SNOOZE_QUICK
            putExtra(EXTRA_ALARM_ID, alarmId)
        }
        val snoozePendingIntent = PendingIntent.getService(this, 2, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Выбор канала: HIGH (heads-up) или LOW (тихий) по preference
        val channelId = if (showHeadsUp) CHANNEL_HIGH_ID else CHANNEL_LOW_ID
        val priority = if (showHeadsUp) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_LOW

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Будильник звонит!")
            .setContentText("Просыпайтесь")
            .setPriority(priority)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setOngoing(true)
            .setAutoCancel(false)
            .setDefaults(0)
            .addAction(0, "Отключить", dismissPendingIntent)
            .addAction(0, "Отложить", snoozePendingIntent)
            .build()
    }

    /**
     * Два канала:
     * - HIGH: IMPORTANCE_HIGH — heads-up notification (default)
     * - LOW:  IMPORTANCE_LOW  — тихий, без heads-up (toggle OFF)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val highChannel = NotificationChannel(
                CHANNEL_HIGH_ID,
                "Alarm Service",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for Alarm Ringing"
                setSound(null, null)
                enableVibration(false)
                setBypassDnd(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(highChannel)

            val lowChannel = NotificationChannel(
                CHANNEL_LOW_ID,
                "Alarm Service (тихий)",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Тихое уведомление при срабатывании (без heads-up)"
                setSound(null, null)
                enableVibration(false)
            }
            manager.createNotificationChannel(lowChannel)
        }
    }

    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "TimeTaskPro:AlarmRingingWakeLock"
            )
            wakeLock?.acquire(10 * 60 * 1000L) // 10 minutes max
        }
    }

    private fun releaseWakeLock() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRingingAndFinish()
    }

    override fun onBind(intent: Intent?): IBinder? = null
    
    companion object {
        private const val TAG = "AlarmRingingService"
        
        const val ACTION_DISMISS = "com.timetask.pro.v2.ACTION_DISMISS_ALARM"
        const val ACTION_SNOOZE = "com.timetask.pro.v2.ACTION_SNOOZE_ALARM"
        const val ACTION_DISMISS_QUICK = "com.timetask.pro.v2.ACTION_DISMISS_QUICK"
        const val ACTION_SNOOZE_QUICK = "com.timetask.pro.v2.ACTION_SNOOZE_QUICK"
        
        const val EXTRA_ALARM_ID = "EXTRA_ALARM_ID"
        const val EXTRA_ALARM_NAME = "EXTRA_ALARM_NAME"
        const val EXTRA_ALARM_NOTES = "EXTRA_ALARM_NOTES"
        const val NOTIFICATION_ID = 8888
        private const val CHANNEL_HIGH_ID = "alarm_channel_high"
        private const val CHANNEL_LOW_ID = "alarm_channel_low"
        
        fun start(context: Context, alarmId: String) {
            val serviceIntent = Intent(context, AlarmRingingService::class.java).apply {
                putExtra(EXTRA_ALARM_ID, alarmId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            // НЕ вызываем startActivity() — FullScreenIntent в notification сделает это.
            // Fallback обрабатывается в onStartCommand().
        }
        
        fun stop(context: Context) {
            val serviceIntent = Intent(context, AlarmRingingService::class.java)
            context.stopService(serviceIntent)
        }
    }
}
