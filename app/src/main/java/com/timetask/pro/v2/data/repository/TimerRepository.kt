package com.timetask.pro.v2.data.repository

import com.timetask.pro.v2.data.local.db.TimeTaskDatabase
import com.timetask.pro.v2.data.local.db.entity.TimerConfig
import com.timetask.pro.v2.data.local.db.entity.TimerNotification
import com.timetask.pro.v2.data.local.db.entity.TimerEntity
import com.timetask.pro.v2.data.local.db.entity.TimerState
import com.timetask.pro.v2.data.local.db.entity.UserPresetEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Репозиторий таймеров — чистый CRUD.
 * Вся бизнес-логика (start/pause/stop/expire) находится в [com.timetask.pro.v2.domain.timer.TimerManager].
 */
class TimerRepository private constructor(db: TimeTaskDatabase) {

    private val dao = db.timerDao()
    private val presetDao = db.userPresetDao()

    // ========================================================================
    // Запросы
    // ========================================================================

    fun getAllTimers(): Flow<List<TimerEntity>> = dao.getAllTimers()

    suspend fun getTimerById(id: String): TimerEntity? = withContext(Dispatchers.IO) {
        dao.getTimerById(id)
    }

    suspend fun getRunningTimers(): List<TimerEntity> = withContext(Dispatchers.IO) {
        dao.getRunningTimers()
    }

    // ========================================================================
    // Мутации
    // ========================================================================

    suspend fun addTimer(
        name: String,
        durationMs: Long,
        config: TimerConfig = TimerConfig(),
        notification: TimerNotification = TimerNotification(),
        linkedTaskIdsJson: String = "[]",
        categoryId: String? = null,
        tagIdsJson: String = "[]",
    ) = withContext(Dispatchers.IO) {
        val timer = TimerEntity(
            name = name,
            totalDurationMs = durationMs,
            remainingMs = durationMs,
            state = TimerState.IDLE,
            config = config,
            notification = notification,
            linkedTaskIdsJson = linkedTaskIdsJson,
            categoryId = categoryId,
            tagIdsJson = tagIdsJson,
        )
        dao.insertTimer(timer)
    }


    suspend fun updateTimer(timer: TimerEntity) = withContext(Dispatchers.IO) {
        dao.updateTimer(timer)
    }

    suspend fun deleteTimer(timer: TimerEntity) = withContext(Dispatchers.IO) {
        dao.deleteTimer(timer)
    }

    suspend fun deleteTimerById(id: String) = withContext(Dispatchers.IO) {
        dao.deleteTimerById(id)
    }

    // ========================================================================
    // User Presets
    // ========================================================================

    fun getAllUserPresets(): Flow<List<UserPresetEntity>> = presetDao.getAll()

    suspend fun addUserPreset(preset: UserPresetEntity) = withContext(Dispatchers.IO) {
        presetDao.insert(preset)
    }

    suspend fun deleteUserPreset(id: String) = withContext(Dispatchers.IO) {
        presetDao.deleteById(id)
    }

    companion object {
        @Volatile
        private var INSTANCE: TimerRepository? = null

        fun getInstance(db: TimeTaskDatabase): TimerRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TimerRepository(db).also { INSTANCE = it }
            }
        }
    }
}
