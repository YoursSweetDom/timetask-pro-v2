package com.timetask.pro.v2.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.timetask.pro.v2.MainActivity
import com.timetask.pro.v2.R
import com.timetask.pro.v2.domain.model.chrono.Stopwatch
class StopwatchNotificationHelper(
    private val context: Context
) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createChannel()
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            StopwatchConstants.STOPWATCH_CHANNEL_ID,
            StopwatchConstants.STOPWATCH_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT // Using default so it shows in the main area and not silent
        ).apply {
            setSound(null, null)
            enableVibration(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Builds a notification for a single stopwatch.
     */
    fun buildNotificationForStopwatch(
        stopwatch: Stopwatch,
        nowMs: Long
    ): Notification {
        val elapsedMs = stopwatch.computeElapsedMs(nowMs)
        val formattedTime = formatElapsedForNotification(elapsedMs)

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            stopwatch.id.hashCode(),
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Toggle action depending on state
        val isRunning = stopwatch.state == com.timetask.pro.v2.presentation.tools.chrono.StopwatchState.RUNNING
        val toggleIntent = Intent(context, StopwatchService::class.java).apply {
            action = if (isRunning) {
                StopwatchConstants.ACTION_STOPWATCH_PAUSE
            } else {
                StopwatchConstants.ACTION_STOPWATCH_START
            }
            putExtra(StopwatchConstants.EXTRA_STOPWATCH_ID, stopwatch.id)
        }
        val togglePendingIntent = PendingIntent.getService(
            context,
            stopwatch.id.hashCode() + 1,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val toggleIcon = if (isRunning) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val toggleTitle = if (isRunning) "Пауза" else "Старт"

        // Lap Action
        val lapIntent = Intent(context, StopwatchService::class.java).apply {
            action = StopwatchConstants.ACTION_STOPWATCH_LAP
            putExtra(StopwatchConstants.EXTRA_STOPWATCH_ID, stopwatch.id)
        }
        val lapPendingIntent = PendingIntent.getService(
            context,
            stopwatch.id.hashCode() + 2,
            lapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Dismiss intent for Instant Respawn (101% Architecture)
        val dismissIntent = Intent(context, StopwatchDismissReceiver::class.java).apply {
            action = StopwatchConstants.ACTION_STOPWATCH_DISMISS
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            stopwatch.id.hashCode() + 3,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, StopwatchConstants.STOPWATCH_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with valid icon when available
            .setContentTitle("${stopwatch.name} • $formattedTime")
            .setContentText(if (isRunning) "Работает" else "На паузе")
            .setOngoing(true) // Always keep it pinned if showInNotifications is true (until dismissed in app)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setGroup("stopwatch_group")
            .setContentIntent(mainPendingIntent)
            .setDeleteIntent(dismissPendingIntent)
            .addAction(toggleIcon, toggleTitle, togglePendingIntent)

        // Only show Lap if running
        if (isRunning) {
            builder.addAction(android.R.drawable.ic_menu_edit, "Круг", lapPendingIntent)
        }

        return builder.build()
    }

    fun buildSummaryNotification(): Notification {
        return NotificationCompat.Builder(context, StopwatchConstants.STOPWATCH_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Секундомеры активны")
            .setGroup("stopwatch_group")
            .setGroupSummary(true)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()
    }

    fun updateNotification(stopwatch: Stopwatch, nowMs: Long) {
        val notification = buildNotificationForStopwatch(stopwatch, nowMs)
        // Ensure notification IDs don't collide. We calculate HashCode of ID, but make sure it's strictly positive
        val notifId = (stopwatch.id.hashCode() and 0x7FFFFFFF) + StopwatchConstants.STOPWATCH_NOTIFICATION_ID
        notificationManager.notify(notifId, notification)
    }

    fun cancelNotification(stopwatchId: String) {
        val notifId = (stopwatchId.hashCode() and 0x7FFFFFFF) + StopwatchConstants.STOPWATCH_NOTIFICATION_ID
        notificationManager.cancel(notifId)
    }

    private fun formatElapsedForNotification(ms: Long): String {
        val totalSecs = ms / 1000
        val h = totalSecs / 3600
        val m = (totalSecs % 3600) / 60
        val s = totalSecs % 60

        return if (h > 0) {
            String.format("%02d:%02d:%02d", h, m, s)
        } else {
            String.format("%02d:%02d", m, s)
        }
    }
}
