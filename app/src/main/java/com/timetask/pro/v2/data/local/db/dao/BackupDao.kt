package com.timetask.pro.v2.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.timetask.pro.v2.data.local.db.entity.AlarmEntity
import com.timetask.pro.v2.data.local.db.entity.CategoryEntity
import com.timetask.pro.v2.data.local.db.entity.FilterEntity
import com.timetask.pro.v2.data.local.db.entity.FolderEntity
import com.timetask.pro.v2.data.local.db.entity.NoteEntity
import com.timetask.pro.v2.data.local.db.entity.TagEntity
import com.timetask.pro.v2.data.local.db.entity.TaskEntity
import com.timetask.pro.v2.data.local.db.entity.TemplateEntity
import com.timetask.pro.v2.data.local.db.entity.TimerEntity
import com.timetask.pro.v2.data.local.db.entity.UserPresetEntity
import com.timetask.pro.v2.data.local.entity.StopwatchEntity
import com.timetask.pro.v2.data.local.entity.StopwatchLapEntity

@Dao
interface BackupDao {

    // --- READ ALL ---
    @Query("SELECT * FROM tasks")
    suspend fun getAllTasks(): List<TaskEntity>

    @Query("SELECT * FROM folders")
    suspend fun getAllFolders(): List<FolderEntity>

    @Query("SELECT * FROM tags")
    suspend fun getAllTags(): List<TagEntity>

    @Query("SELECT * FROM categories")
    suspend fun getAllCategories(): List<CategoryEntity>

    @Query("SELECT * FROM filters")
    suspend fun getAllFilters(): List<FilterEntity>
    
    @Query("SELECT * FROM notes")
    suspend fun getAllNotes(): List<NoteEntity>

    @Query("SELECT * FROM timers")
    suspend fun getAllTimers(): List<TimerEntity>

    @Query("SELECT * FROM alarms")
    suspend fun getAllAlarms(): List<AlarmEntity>

    @Query("SELECT * FROM templates")
    suspend fun getAllTemplates(): List<TemplateEntity>

    @Query("SELECT * FROM user_presets")
    suspend fun getAllUserPresets(): List<UserPresetEntity>

    @Query("SELECT * FROM stopwatches")
    suspend fun getAllStopwatches(): List<StopwatchEntity>

    @Query("SELECT * FROM stopwatch_laps")
    suspend fun getAllStopwatchLaps(): List<StopwatchLapEntity>


    // --- INSERT ALL ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(items: List<TaskEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolders(items: List<FolderEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(items: List<TagEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(items: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFilters(items: List<FilterEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(items: List<NoteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimers(items: List<TimerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarms(items: List<AlarmEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplates(items: List<TemplateEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPresets(items: List<UserPresetEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStopwatches(items: List<StopwatchEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStopwatchLaps(items: List<StopwatchLapEntity>)


    // --- DELETE ALL ---
    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()

    @Query("DELETE FROM folders")
    suspend fun deleteAllFolders()

    @Query("DELETE FROM tags")
    suspend fun deleteAllTags()

    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()

    @Query("DELETE FROM filters")
    suspend fun deleteAllFilters()

    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()

    @Query("DELETE FROM timers")
    suspend fun deleteAllTimers()

    @Query("DELETE FROM alarms")
    suspend fun deleteAllAlarms()

    @Query("DELETE FROM templates")
    suspend fun deleteAllTemplates()

    @Query("DELETE FROM user_presets")
    suspend fun deleteAllUserPresets()

    @Query("DELETE FROM stopwatches")
    suspend fun deleteAllStopwatches()

    @Query("DELETE FROM stopwatch_laps")
    suspend fun deleteAllStopwatchLaps()
}
