package com.timetask.pro.v2.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.timetask.pro.v2.data.local.db.entity.TaskEntity
import com.timetask.pro.v2.data.local.db.entity.TaskStatus
import com.timetask.pro.v2.data.local.db.entity.TaskTagCrossRef
import com.timetask.pro.v2.data.local.db.entity.TaskWithTags
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    // ============================================================
    // Queries (Flow-based — реактивные)
    // ============================================================

    @Query("SELECT * FROM tasks WHERE status != 'DELETED' AND parentId IS NULL ORDER BY isPinned DESC, `order` ASC, createdAt DESC")
    fun getAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getById(id: Long): Flow<TaskEntity?>

    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getByIdWithTags(id: Long): Flow<TaskWithTags?>

    @Query("SELECT * FROM tasks WHERE status != 'DELETED' AND folderId = :folderId AND parentId IS NULL ORDER BY isPinned DESC, `order` ASC, createdAt DESC")
    fun getByFolder(folderId: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status != 'DELETED' AND folderId IS NULL AND parentId IS NULL ORDER BY isPinned DESC, `order` ASC, createdAt DESC")
    fun getInbox(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status != 'DONE' AND status != 'WONT_DO' AND status != 'DELETED' AND parentId IS NULL ORDER BY isPinned DESC, `order` ASC")
    fun getActive(): Flow<List<TaskEntity>>

    // Subtasks
    @Query("SELECT * FROM tasks WHERE status != 'DELETED' AND parentId = :parentId ORDER BY `order` ASC, createdAt ASC")
    fun getSubtasks(parentId: Long): Flow<List<TaskEntity>>

    @Query("SELECT COUNT(*) FROM tasks WHERE parentId = :parentId AND status != 'DONE' AND status != 'WONT_DO' AND status != 'DELETED'")
    fun getActiveSubtaskCount(parentId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE status != 'DELETED' AND parentId = :parentId")
    fun getSubtaskCount(parentId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE status = 'DONE' AND parentId = :parentId")
    fun getCompletedSubtaskCount(parentId: Long): Flow<Int>

    @Query("SELECT * FROM tasks WHERE status = 'DONE' ORDER BY completedAt DESC")
    fun getCompleted(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status = 'WONT_DO' ORDER BY updatedAt DESC")
    fun getWontDo(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status = 'DELETED' ORDER BY updatedAt DESC")
    fun getDeleted(): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks 
        WHERE dueDate IS NOT NULL 
        AND dueDate BETWEEN :startOfDay AND :endOfDay 
        AND status != 'DONE' AND status != 'WONT_DO' AND status != 'DELETED'
        ORDER BY dueDate ASC
    """)
    fun getByDueDate(startOfDay: Long, endOfDay: Long): Flow<List<TaskEntity>>

    @Query("SELECT COUNT(*) FROM tasks WHERE folderId = :folderId AND status != 'DONE' AND status != 'WONT_DO' AND status != 'DELETED'")
    fun getActiveCountByFolder(folderId: Long): Flow<Int>

    // ============================================================
    // Mutations
    // ============================================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTaskTagCrossRefs(crossRefs: List<TaskTagCrossRef>)

    @Query("DELETE FROM task_tag_cross_ref WHERE taskId = :taskId")
    suspend fun deleteTaskTagCrossRefsByTaskId(taskId: Long)

    @Query("""
        SELECT t.* FROM tags t 
        INNER JOIN task_tag_cross_ref cr ON t.id = cr.tagId 
        WHERE cr.taskId = :taskId
    """)
    fun getTagsForTask(taskId: Long): Flow<List<com.timetask.pro.v2.data.local.db.entity.TagEntity>>

    @Update
    suspend fun update(task: TaskEntity)


    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE tasks SET status = :status, updatedAt = :updatedAt, completedAt = :completedAt WHERE id = :id")
    suspend fun updateStatus(
        id: Long,
        status: TaskStatus,
        updatedAt: Long = System.currentTimeMillis(),
        completedAt: Long? = null,
    )

    @Query("UPDATE tasks SET isPinned = :isPinned, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updatePinned(id: Long, isPinned: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE tasks SET `order` = :order WHERE id = :id")
    suspend fun updateOrder(id: Long, order: Int)

    @Query("UPDATE tasks SET parentId = :parentId, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateParentId(id: Long, parentId: Long?, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE tasks SET totalSpentTimeMs = totalSpentTimeMs + :addedTimeMs, updatedAt = :updatedAt WHERE id = :id")
    suspend fun addAccumulatedTime(id: Long, addedTimeMs: Long, updatedAt: Long = System.currentTimeMillis())
}
