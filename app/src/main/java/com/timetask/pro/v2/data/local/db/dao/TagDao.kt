package com.timetask.pro.v2.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.timetask.pro.v2.data.local.db.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Query("SELECT * FROM tags ORDER BY isPinned DESC, `order` ASC, createdAt ASC")
    fun getAll(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE id = :id")
    fun getById(id: Long): Flow<TagEntity?>

    @Query("SELECT * FROM tags WHERE parentId IS NULL ORDER BY `order` ASC")
    fun getRootTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE parentId = :parentId ORDER BY `order` ASC")
    fun getByParentId(parentId: Long): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: TagEntity): Long

    @Update
    suspend fun update(tag: TagEntity)

    @Query("UPDATE tags SET `order` = :order WHERE id = :id")
    suspend fun updateOrder(id: Long, order: Int)

    @Transaction
    suspend fun updateOrders(updates: List<Pair<Long, Int>>) {
        updates.forEach { updateOrder(it.first, it.second) }
    }

    @Delete
    suspend fun delete(tag: TagEntity)

    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun deleteById(id: Long)
}
