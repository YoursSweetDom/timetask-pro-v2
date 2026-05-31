package com.timetask.pro.v2.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.timetask.pro.v2.data.local.db.entity.UserPresetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPresetDao {

    @Query("SELECT * FROM user_presets ORDER BY createdAt ASC")
    fun getAll(): Flow<List<UserPresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(preset: UserPresetEntity)

    @Query("DELETE FROM user_presets WHERE id = :id")
    suspend fun deleteById(id: String)
}
