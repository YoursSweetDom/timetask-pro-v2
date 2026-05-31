package com.timetask.pro.v2.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.timetask.pro.v2.data.local.db.entity.FilterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FilterDao {

    @Query("SELECT * FROM filters ORDER BY isPinned DESC, `order` ASC, createdAt ASC")
    fun getAll(): Flow<List<FilterEntity>>

    @Query("SELECT * FROM filters WHERE id = :id")
    fun getById(id: Long): Flow<FilterEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(filter: FilterEntity): Long

    @Update
    suspend fun update(filter: FilterEntity)

    @Query("UPDATE filters SET `order` = :order WHERE id = :id")
    suspend fun updateOrder(id: Long, order: Int)

    @Transaction
    suspend fun updateOrders(updates: List<Pair<Long, Int>>) {
        updates.forEach { updateOrder(it.first, it.second) }
    }

    @Delete
    suspend fun delete(filter: FilterEntity)

    @Query("DELETE FROM filters WHERE id = :id")
    suspend fun deleteById(id: Long)
}
