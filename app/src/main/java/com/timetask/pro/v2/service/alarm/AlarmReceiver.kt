package com.timetask.pro.v2.service.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.timetask.pro.v2.data.local.db.TimeTaskDatabase
import com.timetask.pro.v2.data.repository.OfflineFirstAlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getStringExtra(AndroidAlarmScheduler.EXTRA_ALARM_ID) ?: return
        Log.d("AlarmReceiver", "Received alarm trigger for ID: $alarmId")

        val pendingResult = goAsync()

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                AlarmRingingService.start(
                    context = context,
                    alarmId = alarmId
                )
            } finally {
                pendingResult.finish()
            }
        }
    }
}
