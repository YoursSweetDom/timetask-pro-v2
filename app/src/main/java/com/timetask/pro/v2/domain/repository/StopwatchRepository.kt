package com.timetask.pro.v2.domain.repository

import com.timetask.pro.v2.domain.model.chrono.Stopwatch
import com.timetask.pro.v2.domain.model.chrono.Lap
import com.timetask.pro.v2.presentation.tools.chrono.StopwatchState
import kotlinx.coroutines.flow.Flow

interface StopwatchRepository {
    
    /** Returns a reactive stream of all stopwatches (with laps mapped automatically) **/
    fun getAllStopwatches(): Flow<List<Stopwatch>>

    suspend fun createStopwatch(name: String, linkedTaskIdsJson: String = "[]", categoryId: Long? = null)

    suspend fun deleteStopwatch(id: String)

    suspend fun renameStopwatch(id: String, newName: String)

    suspend fun updateStopwatchMetadata(id: String, newName: String, categoryText: String, tagsText: String, linkedTaskIdsJson: String)

    suspend fun updateStopwatchState(id: String, newState: StopwatchState, startTimeMs: Long, accumulatedMs: Long)

    suspend fun updateStopwatchNotification(id: String, showInNotifications: Boolean)

    suspend fun addLap(stopwatchId: String, lapTimeMs: Long, totalTimeMs: Long, title: String? = null)

    suspend fun resetStopwatch(id: String)

    // --- RECYCLE BIN (TRASH) ---
    fun getDeletedStopwatches(): Flow<List<Stopwatch>>
    fun getDeletedLaps(): Flow<List<Lap>>
    
    suspend fun restoreStopwatch(id: String)
    suspend fun restoreLap(id: String)
    
    suspend fun updateLapColorAndTitle(lapId: String, title: String?, color: Int?)

    suspend fun softDeleteLap(lapId: String)
    suspend fun hardDeleteStopwatch(id: String)
    suspend fun hardDeleteLap(lapId: String)
}
