package com.timetask.pro.v2.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.timetask.pro.v2.data.local.db.entity.FolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {

    @Query("SELECT * FROM folders ORDER BY isPinned DESC, `order` ASC, createdAt ASC")
    fun getAll(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE id = :id")
    fun getById(id: Long): Flow<FolderEntity?>

    @Query("SELECT * FROM folders WHERE isSystem = 1 ORDER BY `order` ASC")
    fun getSystemFolders(): Flow<List<FolderEntity>>

    /** Корневые папки (без родителя) */
    @Query("SELECT * FROM folders WHERE parentId IS NULL AND isSystem = 0 ORDER BY isPinned DESC, `order` ASC, createdAt ASC")
    fun getRootFolders(): Flow<List<FolderEntity>>

    /** Подпапки конкретной папки */
    @Query("SELECT * FROM folders WHERE parentId = :parentId ORDER BY isPinned DESC, `order` ASC, createdAt ASC")
    fun getByParentId(parentId: Long): Flow<List<FolderEntity>>

    /** Все папки-потомки (плоский список, для построения дерева) */
    @Query("SELECT * FROM folders WHERE isSystem = 0 ORDER BY isPinned DESC, `order` ASC, createdAt ASC")
    fun getUserFolders(): Flow<List<FolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(folder: FolderEntity): Long

    @Update
    suspend fun update(folder: FolderEntity)

    @Query("UPDATE folders SET `order` = :order WHERE id = :id")
    suspend fun updateOrder(id: Long, order: Int)

    @Transaction
    suspend fun updateOrders(updates: List<Pair<Long, Int>>) {
        updates.forEach { updateOrder(it.first, it.second) }
    }

    @Delete
    suspend fun delete(folder: FolderEntity)

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun deleteById(id: Long)
}
