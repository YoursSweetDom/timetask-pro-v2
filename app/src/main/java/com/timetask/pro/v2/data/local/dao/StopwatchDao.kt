package com.timetask.pro.v2.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.timetask.pro.v2.data.local.entity.StopwatchEntity
import com.timetask.pro.v2.data.local.entity.StopwatchLapEntity
import com.timetask.pro.v2.data.local.db.entity.StopwatchTagCrossRef
import com.timetask.pro.v2.data.local.relation.StopwatchWithLaps
import com.timetask.pro.v2.data.local.db.entity.StopwatchWithTags
import kotlinx.coroutines.flow.Flow

@Dao
interface StopwatchDao {

    @Transaction
    @Query("SELECT * FROM stopwatches WHERE isDeleted = 0 ORDER BY createdAt ASC")
    fun getAllStopwatchesWithLaps(): Flow<List<StopwatchWithLaps>>

    @Transaction
    @Query("SELECT * FROM stopwatches WHERE id = :stopwatchId")
    fun getStopwatchByIdWithTags(stopwatchId: String): Flow<StopwatchWithTags?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStopwatch(stopwatch: StopwatchEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStopwatchTagCrossRef(crossRef: StopwatchTagCrossRef)

    @Query("DELETE FROM stopwatch_tag_cross_ref WHERE stopwatchId = :stopwatchId")
    suspend fun deleteStopwatchTagCrossRefs(stopwatchId: String)

    @androidx.room.Update
    suspend fun update(stopwatch: StopwatchEntity)

    @Query("UPDATE stopwatches SET name = :newName WHERE id = :stopwatchId")
    suspend fun updateStopwatchName(stopwatchId: String, newName: String)

    @Query("UPDATE stopwatches SET name = :newName, categoryText = :categoryText, tagsText = :tagsText, linkedTaskIdsJson = :linkedTaskIdsJson WHERE id = :stopwatchId")
    suspend fun updateStopwatchMetadata(stopwatchId: String, newName: String, categoryText: String, tagsText: String, linkedTaskIdsJson: String)

    @Query("UPDATE stopwatches SET state = :state, startTimeMs = :startTimeMs, accumulatedMs = :accumulatedMs WHERE id = :stopwatchId")
    suspend fun updateStopwatchState(stopwatchId: String, state: String, startTimeMs: Long, accumulatedMs: Long)

    @Query("UPDATE stopwatches SET notif_showInNotifications = :showInNotifications WHERE id = :stopwatchId")
    suspend fun updateStopwatchNotification(stopwatchId: String, showInNotifications: Boolean)

    @Query("SELECT COALESCE(MAX(lapNumber), 0) FROM stopwatch_laps WHERE stopwatchId = :stopwatchId")
    suspend fun getMaxLapNumber(stopwatchId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLap(lap: StopwatchLapEntity)

    @Query("UPDATE stopwatches SET isDeleted = 1, deletedAt = :deletedAtMs WHERE id = :stopwatchId")
    suspend fun softDeleteStopwatchById(stopwatchId: String, deletedAtMs: Long = System.currentTimeMillis())

    @Query("UPDATE stopwatch_laps SET isDeleted = 1, deletedAt = :deletedAtMs WHERE stopwatchId = :stopwatchId")
    suspend fun softDeleteAllLapsForStopwatch(stopwatchId: String, deletedAtMs: Long = System.currentTimeMillis())

    @Query("UPDATE stopwatch_laps SET isDeleted = 1, deletedAt = :deletedAtMs WHERE id = :lapId")
    suspend fun softDeleteLapById(lapId: String, deletedAtMs: Long = System.currentTimeMillis())

    // --- RECYCLE BIN (TRASH) QUERIES ---

    @Transaction
    @Query("SELECT * FROM stopwatches WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getDeletedStopwatchesWithLaps(): Flow<List<StopwatchWithLaps>>

    @Query("SELECT * FROM stopwatch_laps WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getDeletedLaps(): Flow<List<StopwatchLapEntity>>

    @Query("UPDATE stopwatches SET isDeleted = 0, deletedAt = null WHERE id = :stopwatchId")
    suspend fun restoreStopwatchById(stopwatchId: String)

    @Query("UPDATE stopwatch_laps SET isDeleted = 0, deletedAt = null WHERE stopwatchId = :stopwatchId AND isDeleted = 1")
    suspend fun restoreAllLapsForStopwatch(stopwatchId: String)

    @Query("UPDATE stopwatch_laps SET isDeleted = 0, deletedAt = null WHERE id = :lapId")
    suspend fun restoreLapById(lapId: String)

    @Query("UPDATE stopwatch_laps SET title = :title, colorARGB = :color WHERE id = :lapId")
    suspend fun updateLapColorAndTitle(lapId: String, title: String?, color: Int?)

    @Query("DELETE FROM stopwatches WHERE id = :stopwatchId")
    suspend fun hardDeleteStopwatchById(stopwatchId: String)

    @Query("DELETE FROM stopwatch_laps WHERE id = :lapId")
    suspend fun hardDeleteLapById(lapId: String)
}
