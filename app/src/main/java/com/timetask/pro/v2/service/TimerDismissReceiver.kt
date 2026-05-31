package com.timetask.pro.v2.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class TimerDismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == ACTION_DISMISS) {
            Log.d(TAG, "Notification dismissed by user, immediately respawning...")
            // The service is running, so telling it to start will just re-trigger onStartCommand
            // which will subsequently update/re-issue the notification via startTicking() or updateNotification()
            TimerService.start(context)
        }
    }

    companion object {
        const val ACTION_DISMISS = "com.timetask.pro.v2.ACTION_TIMER_NOTIFICATION_DISMISS"
        private const val TAG = "TimerDismissReceiver"
    }
}
