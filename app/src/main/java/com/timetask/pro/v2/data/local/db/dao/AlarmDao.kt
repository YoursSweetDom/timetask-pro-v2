package com.timetask.pro.v2.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.timetask.pro.v2.data.local.db.entity.AlarmEntity
import com.timetask.pro.v2.data.local.db.entity.AlarmTagCrossRef
import com.timetask.pro.v2.data.local.db.entity.AlarmWithTags
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {

    @Query("SELECT * FROM alarms WHERE isDeleted = 0 ORDER BY hour ASC, minute ASC")
    fun getAllActiveAlarms(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarms WHERE isEnabled = 1 AND isDeleted = 0")
    suspend fun getEnabledAlarms(): List<AlarmEntity>

    @Query("SELECT * FROM alarms WHERE id = :id AND isDeleted = 0")
    suspend fun getAlarmById(id: String): AlarmEntity?

    @Transaction
    @Query("SELECT * FROM alarms WHERE id = :id AND isDeleted = 0")
    fun getAlarmByIdWithTags(id: String): Flow<AlarmWithTags?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: AlarmEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAlarmTagCrossRef(crossRef: AlarmTagCrossRef)

    @Query("DELETE FROM alarm_tag_cross_ref WHERE alarmId = :alarmId")
    suspend fun deleteAlarmTagCrossRefs(alarmId: String)

    @Update
    suspend fun updateAlarm(alarm: AlarmEntity)

    @Query("UPDATE alarms SET isDeleted = 1, deletedAt = :timestamp WHERE id = :id")
    suspend fun softDeleteAlarm(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE alarms SET isDeleted = 0, deletedAt = NULL WHERE id = :id")
    suspend fun restoreAlarm(id: String)
}
