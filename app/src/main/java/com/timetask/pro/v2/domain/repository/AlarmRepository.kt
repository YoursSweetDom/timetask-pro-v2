package com.timetask.pro.v2.domain.repository

import com.timetask.pro.v2.data.local.db.entity.AlarmEntity
import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    fun getAllActiveAlarms(): Flow<List<AlarmEntity>>
    suspend fun getEnabledAlarms(): List<AlarmEntity>
    suspend fun getAlarmById(id: String): AlarmEntity?
    suspend fun addAlarm(alarm: AlarmEntity)
    suspend fun updateAlarm(alarm: AlarmEntity)
    suspend fun softDeleteAlarm(id: String)
    suspend fun restoreAlarm(id: String)
}
