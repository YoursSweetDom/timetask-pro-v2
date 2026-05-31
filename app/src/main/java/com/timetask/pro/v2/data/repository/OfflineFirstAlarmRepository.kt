package com.timetask.pro.v2.data.repository

import android.content.Context
import com.timetask.pro.v2.data.local.db.TimeTaskDatabase
import com.timetask.pro.v2.data.local.db.dao.AlarmDao
import com.timetask.pro.v2.data.local.db.entity.AlarmEntity
import com.timetask.pro.v2.domain.repository.AlarmRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class OfflineFirstAlarmRepository private constructor(
    private val alarmDao: AlarmDao
) : AlarmRepository {

    override fun getAllActiveAlarms(): Flow<List<AlarmEntity>> = alarmDao.getAllActiveAlarms()

    override suspend fun getEnabledAlarms(): List<AlarmEntity> = withContext(Dispatchers.IO) {
        alarmDao.getEnabledAlarms()
    }

    override suspend fun getAlarmById(id: String): AlarmEntity? = withContext(Dispatchers.IO) {
        alarmDao.getAlarmById(id)
    }

    override suspend fun addAlarm(alarm: AlarmEntity) = withContext(Dispatchers.IO) {
        alarmDao.insertAlarm(alarm)
    }

    override suspend fun updateAlarm(alarm: AlarmEntity) = withContext(Dispatchers.IO) {
        alarmDao.updateAlarm(alarm)
    }

    override suspend fun softDeleteAlarm(id: String) = withContext(Dispatchers.IO) {
        alarmDao.softDeleteAlarm(id)
    }

    override suspend fun restoreAlarm(id: String) = withContext(Dispatchers.IO) {
        alarmDao.restoreAlarm(id)
    }

    companion object {
        @Volatile
        private var instance: OfflineFirstAlarmRepository? = null

        fun getInstance(context: Context): OfflineFirstAlarmRepository {
            return instance ?: synchronized(this) {
                instance ?: OfflineFirstAlarmRepository(
                    TimeTaskDatabase.getInstance(context).alarmDao()
                ).also { instance = it }
            }
        }
    }
}
