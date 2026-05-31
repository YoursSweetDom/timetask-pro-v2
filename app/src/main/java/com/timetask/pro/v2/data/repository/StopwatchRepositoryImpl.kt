package com.timetask.pro.v2.data.repository

import com.timetask.pro.v2.data.local.dao.StopwatchDao
import com.timetask.pro.v2.data.local.entity.StopwatchEntity
import com.timetask.pro.v2.data.local.entity.StopwatchLapEntity
import com.timetask.pro.v2.data.local.relation.StopwatchWithLaps
import com.timetask.pro.v2.domain.model.chrono.Lap
import com.timetask.pro.v2.domain.model.chrono.Stopwatch
import com.timetask.pro.v2.domain.repository.StopwatchRepository
import com.timetask.pro.v2.presentation.tools.chrono.StopwatchState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

import com.timetask.pro.v2.data.local.db.TimeTaskDatabase

class StopwatchRepositoryImpl private constructor(db: TimeTaskDatabase) : StopwatchRepository {

    private val stopwatchDao = db.stopwatchDao()

    override fun getAllStopwatches(): Flow<List<Stopwatch>> {
        return stopwatchDao.getAllStopwatchesWithLaps().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun createStopwatch(name: String, linkedTaskIdsJson: String, categoryId: Long?) {
        val entity = StopwatchEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            state = StopwatchState.IDLE.name,
            startTimeMs = 0L,
            accumulatedMs = 0L,
            categoryId = categoryId,
            linkedTaskIdsJson = linkedTaskIdsJson,
            isDeleted = false,
            deletedAt = null
        )
        stopwatchDao.insertStopwatch(entity)
    }

    override suspend fun deleteStopwatch(id: String) {
        stopwatchDao.softDeleteStopwatchById(id)
    }

    override suspend fun renameStopwatch(id: String, newName: String) {
        stopwatchDao.updateStopwatchName(id, newName)
    }

    override suspend fun updateStopwatchMetadata(id: String, newName: String, categoryText: String, tagsText: String, linkedTaskIdsJson: String) {
        stopwatchDao.updateStopwatchMetadata(id, newName, categoryText, tagsText, linkedTaskIdsJson)
    }

    override suspend fun updateStopwatchState(
        id: String,
        newState: StopwatchState,
        startTimeMs: Long,
        accumulatedMs: Long
    ) {
        stopwatchDao.updateStopwatchState(id, newState.name, startTimeMs, accumulatedMs)
    }

    override suspend fun updateStopwatchNotification(id: String, showInNotifications: Boolean) {
        stopwatchDao.updateStopwatchNotification(id, showInNotifications)
    }

    override suspend fun addLap(stopwatchId: String, lapTimeMs: Long, totalTimeMs: Long, title: String?) {
        // Query current lap count to determine the new lap number. 
        // Since we don't have a direct query for max lap number right now, 
        // we can either add one to Dao or query it. Let's add it to Dao in next step, 
        // or just use 0 temporarily and fix it immediately.
        val lapNumber = stopwatchDao.getMaxLapNumber(stopwatchId) + 1
        
        val entity = StopwatchLapEntity(
            id = UUID.randomUUID().toString(),
            stopwatchId = stopwatchId,
            lapNumber = lapNumber,
            lapTimeMs = lapTimeMs,
            totalTimeMs = totalTimeMs,
            title = title,
            categoryText = "",
            tagsText = "",
            colorARGB = null,
            isDeleted = false,
            deletedAt = null
        )
        stopwatchDao.insertLap(entity)
    }

    override suspend fun resetStopwatch(id: String) {
        // Clear laps and set accumulated/start to 0
        stopwatchDao.softDeleteAllLapsForStopwatch(id)
    }

    // --- RECYCLE BIN (TRASH) ---

    override fun getDeletedStopwatches(): Flow<List<Stopwatch>> {
        return stopwatchDao.getDeletedStopwatchesWithLaps().map { list ->
            list.map { it.toDomain(includeDeletedLaps = true) }
        }
    }

    override fun getDeletedLaps(): Flow<List<Lap>> {
        return stopwatchDao.getDeletedLaps().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun restoreStopwatch(id: String) {
        stopwatchDao.restoreStopwatchById(id)
        stopwatchDao.restoreAllLapsForStopwatch(id)
    }

    override suspend fun restoreLap(id: String) {
        stopwatchDao.restoreLapById(id)
    }

    override suspend fun updateLapColorAndTitle(lapId: String, title: String?, color: Int?) {
        stopwatchDao.updateLapColorAndTitle(lapId, title, color)
    }

    override suspend fun softDeleteLap(lapId: String) {
        stopwatchDao.softDeleteLapById(lapId)
    }

    override suspend fun hardDeleteStopwatch(id: String) {
        stopwatchDao.hardDeleteStopwatchById(id)
    }

    override suspend fun hardDeleteLap(lapId: String) {
        stopwatchDao.hardDeleteLapById(lapId)
    }

    private fun StopwatchLapEntity.toDomain(): Lap {
        return Lap(
            id = this.id,
            lapNumber = this.lapNumber,
            lapTimeMs = this.lapTimeMs,
            totalTimeMs = this.totalTimeMs,
            title = this.title,
            isBest = false,
            isWorst = false,
            categoryText = this.categoryText,
            tagsText = this.tagsText,
            colorARGB = this.colorARGB,
            isDeleted = this.isDeleted,
            deletedAt = this.deletedAt
        )
    }

    private fun StopwatchWithLaps.toDomain(includeDeletedLaps: Boolean = false): Stopwatch {
        // Filter soft-deleted laps
        val activeLaps = if (includeDeletedLaps) this.laps else this.laps.filter { !it.isDeleted }

        // Sort laps and calculate best/worst dynamically
        val sortedLaps = activeLaps.sortedByDescending { it.lapNumber }
        
        // Find best and worst lap times ignoring the very first "0" lap if needed
        val lapTimes = sortedLaps.map { it.lapTimeMs }
        val bestLapTime = lapTimes.minOrNull() ?: -1L
        val worstLapTime = lapTimes.maxOrNull() ?: -1L
        val showBestWorst = sortedLaps.size >= 2
        
        return Stopwatch(
            id = this.stopwatch.id,
            name = this.stopwatch.name,
            state = try { 
                StopwatchState.valueOf(this.stopwatch.state) 
            } catch (e: Exception) { 
                StopwatchState.IDLE 
            },
            startTimeMs = this.stopwatch.startTimeMs,
            accumulatedMs = this.stopwatch.accumulatedMs,
            categoryId = this.stopwatch.categoryId,
            linkedTaskIdsJson = this.stopwatch.linkedTaskIdsJson,
            categoryText = this.stopwatch.categoryText,
            tagsText = this.stopwatch.tagsText,
            isDeleted = this.stopwatch.isDeleted,
            deletedAt = this.stopwatch.deletedAt,
            notification = this.stopwatch.notification,
            laps = sortedLaps.map { lapEnt ->
                Lap(
                    id = lapEnt.id,
                    lapNumber = lapEnt.lapNumber,
                    lapTimeMs = lapEnt.lapTimeMs,
                    totalTimeMs = lapEnt.totalTimeMs,
                    title = lapEnt.title,
                    isBest = showBestWorst && lapEnt.lapTimeMs == bestLapTime,
                    isWorst = showBestWorst && lapEnt.lapTimeMs == worstLapTime,
                    categoryText = lapEnt.categoryText,
                    tagsText = lapEnt.tagsText,
                    colorARGB = lapEnt.colorARGB,
                    isDeleted = lapEnt.isDeleted,
                    deletedAt = lapEnt.deletedAt
                )
            }
        )
    }

    companion object {
        @Volatile
        private var INSTANCE: StopwatchRepositoryImpl? = null

        fun getInstance(db: TimeTaskDatabase): StopwatchRepositoryImpl {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: StopwatchRepositoryImpl(db).also { INSTANCE = it }
            }
        }
    }
}
