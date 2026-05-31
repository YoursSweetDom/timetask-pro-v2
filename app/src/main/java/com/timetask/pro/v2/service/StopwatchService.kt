package com.timetask.pro.v2.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import com.timetask.pro.v2.domain.model.chrono.Stopwatch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import com.timetask.pro.v2.data.local.db.TimeTaskDatabase
import com.timetask.pro.v2.data.repository.StopwatchRepositoryImpl
import com.timetask.pro.v2.data.repository.TaskRepository
import com.timetask.pro.v2.domain.usecase.chrono.*
import com.timetask.pro.v2.presentation.tools.chrono.StopwatchState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay

class StopwatchService : Service() {

    private lateinit var stopwatchUseCases: StopwatchUseCases
    private lateinit var notificationHelper: StopwatchNotificationHelper

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var tickJob: Job? = null
    private var stopwatches: List<Stopwatch> = emptyList()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        
        // Manual DI
        val db = TimeTaskDatabase.getInstance(application)
        val repository = StopwatchRepositoryImpl.getInstance(db)
        val taskRepo = TaskRepository.getInstance(application)
        
        stopwatchUseCases = StopwatchUseCases(
            getStopwatches = GetStopwatchesUseCase(repository),
            addStopwatch = AddStopwatchUseCase(repository),
            deleteStopwatch = DeleteStopwatchUseCase(repository),
            renameStopwatch = RenameStopwatchUseCase(repository),
            startStopwatch = StartStopwatchUseCase(repository),
            pauseStopwatch = PauseStopwatchUseCase(repository, taskRepo),
            resetStopwatch = ResetStopwatchUseCase(repository, taskRepo),
            addLap = AddLapUseCase(repository),
            editLap = EditLapUseCase(repository),
            softDeleteLap = SoftDeleteLapUseCase(repository),
            restoreStopwatch = RestoreStopwatchUseCase(repository),
            restoreLap = RestoreLapUseCase(repository),
            getDeletedStopwatches = GetDeletedStopwatchesUseCase(repository),
            getDeletedLaps = GetDeletedLapsUseCase(repository),
            hardDeleteStopwatch = HardDeleteStopwatchUseCase(repository),
            hardDeleteLap = HardDeleteLapUseCase(repository),
            updateStopwatchMetadata = UpdateStopwatchMetadataUseCase(repository),
            updateStopwatchNotification = UpdateStopwatchNotificationUseCase(repository)
        )
        notificationHelper = StopwatchNotificationHelper(this)

        // Subscribe to DB updates
        serviceScope.launch {
            stopwatchUseCases.getStopwatches().collect { list ->
                stopwatches = list
                val hasRunning = list.any { it.state == StopwatchState.RUNNING }
                if (hasRunning) {
                    startTicking()
                } else {
                    stopTickingAndSelf()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val stopwatchId = intent?.getStringExtra(StopwatchConstants.EXTRA_STOPWATCH_ID)

        if (action != null && stopwatchId != null) {
            handleAction(action, stopwatchId)
        } else {
            // General start or respawn
            promoteToForeground()
        }

        return START_STICKY
    }

    private fun handleAction(action: String, stopwatchId: String) {
        serviceScope.launch {
            val stopwatch = stopwatches.find { it.id == stopwatchId } ?: return@launch
            val nowMs = SystemClock.elapsedRealtime()

            when (action) {
                StopwatchConstants.ACTION_STOPWATCH_START -> {
                    stopwatchUseCases.startStopwatch(stopwatch, nowMs)
                }
                StopwatchConstants.ACTION_STOPWATCH_PAUSE -> {
                    stopwatchUseCases.pauseStopwatch(stopwatch, nowMs)
                }
                StopwatchConstants.ACTION_STOPWATCH_LAP -> {
                    stopwatchUseCases.addLap(stopwatch, nowMs)
                }
            }
        }
    }

    private fun promoteToForeground() {
        // Build generic summary to satisfy foreground requirement immediately
        val summaryNotification = notificationHelper.buildSummaryNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                StopwatchConstants.STOPWATCH_NOTIFICATION_ID, // Summary ID
                summaryNotification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE // Or mediaPlayback depending on strictness
            )
        } else {
            startForeground(StopwatchConstants.STOPWATCH_NOTIFICATION_ID, summaryNotification)
        }
    }

    private fun startTicking() {
        if (tickJob?.isActive == true) return
        promoteToForeground() // ensure we are foreground

        tickJob = serviceScope.launch {
            while (true) {
                // We tick every 1000ms for accurate notification updates without killing battery
                // 1 Hz is enough for Android notifications
                val nowMs = SystemClock.elapsedRealtime()
                
                var activeCount = 0
                stopwatches.forEach { sw ->
                    if (sw.state == StopwatchState.RUNNING) {
                        activeCount++
                        if (sw.notification.showInNotifications) {
                            notificationHelper.updateNotification(sw, nowMs)
                        } else {
                            notificationHelper.cancelNotification(sw.id)
                        }
                    } else if (sw.state == StopwatchState.PAUSED) {
                        // Keep paused notifications visible but don't update them actively
                        if (sw.notification.showInNotifications) {
                            notificationHelper.updateNotification(sw, nowMs)
                        } else {
                            notificationHelper.cancelNotification(sw.id)
                        }
                    } else {
                        // Dismiss idle ones
                        notificationHelper.cancelNotification(sw.id)
                    }
                }

                if (activeCount == 0) {
                    stopTickingAndSelf()
                    break
                }
                
                delay(1000L)
            }
        }
    }

    private fun stopTickingAndSelf() {
        tickJob?.cancel()
        tickJob = null
        stopwatches.forEach { sw ->
            if (sw.state == StopwatchState.IDLE) {
                notificationHelper.cancelNotification(sw.id)
            }
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        tickJob?.cancel()
    }
}
