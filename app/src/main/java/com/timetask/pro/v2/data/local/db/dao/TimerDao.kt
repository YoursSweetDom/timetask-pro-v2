package com.timetask.pro.v2.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.timetask.pro.v2.data.local.db.entity.TimerEntity
import com.timetask.pro.v2.data.local.db.entity.TimerState
import com.timetask.pro.v2.data.local.db.entity.TimerTagCrossRef
import com.timetask.pro.v2.data.local.db.entity.TimerWithTags
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerDao {

    @Query("SELECT * FROM timers ORDER BY createdAt DESC")
    fun getAllTimers(): Flow<List<TimerEntity>>

    @Query("SELECT * FROM timers WHERE id = :id")
    suspend fun getTimerById(id: String): TimerEntity?

    @Transaction
    @Query("SELECT * FROM timers WHERE id = :id")
    fun getTimerByIdWithTags(id: String): Flow<TimerWithTags?>

    @Query("SELECT * FROM timers WHERE state = 'RUNNING'")
    suspend fun getRunningTimers(): List<TimerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimer(timer: TimerEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTimerTagCrossRef(crossRef: TimerTagCrossRef)

    @Query("DELETE FROM timer_tag_cross_ref WHERE timerId = :timerId")
    suspend fun deleteTimerTagCrossRefs(timerId: String)

    @Update
    suspend fun updateTimer(timer: TimerEntity)

    @Delete
    suspend fun deleteTimer(timer: TimerEntity)

    @Query("DELETE FROM timers WHERE id = :id")
    suspend fun deleteTimerById(id: String)
}
