package com.timetask.pro.v2.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StopwatchDismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == StopwatchConstants.ACTION_STOPWATCH_DISMISS) {
            // Instant Respawn architecture for Android 14+
            // If the user dismissed the notification but the stopwatch is still active,
            // we immediately restart the service foreground to bring it back.
            val serviceIntent = Intent(context, StopwatchService::class.java)
            context.startService(serviceIntent) // Restarts sticky service
        }
    }
}
