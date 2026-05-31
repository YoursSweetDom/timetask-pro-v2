package com.timetask.pro.v2.service.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.timetask.pro.v2.data.repository.OfflineFirstAlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    // Usually, DI should inject these, but as this is a receiver, we manually get the instance
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.d("BootReceiver", "Boot completed, rescheduling all alarms.")
            
            val repository = OfflineFirstAlarmRepository.getInstance(context)
            val scheduler = AndroidAlarmScheduler(context)

            scope.launch {
                val enabledAlarms = repository.getEnabledAlarms()
                enabledAlarms.forEach { alarm ->
                    if (!alarm.isDeleted) {
                        Log.d("BootReceiver", "Rescheduling alarm ${alarm.id}")
                        scheduler.schedule(alarm)
                    }
                }
            }
        }
    }
}
