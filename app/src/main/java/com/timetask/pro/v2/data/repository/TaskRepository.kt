package com.timetask.pro.v2.data.repository

import android.content.Context
import com.timetask.pro.v2.data.local.db.TimeTaskDatabase
import com.timetask.pro.v2.data.local.db.entity.TaskEntity
import com.timetask.pro.v2.data.local.db.entity.TaskStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository для задач.
 * Обёртка над TaskDao с выполнением на Dispatchers.IO.
 */
class TaskRepository(context: Context) {

    private val taskDao = TimeTaskDatabase.getInstance(context).taskDao()

    // ============================================================
    // Queries (Flow — реактивные, работают на main thread)
    // ============================================================

    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAll()

    fun getTaskById(id: Long): Flow<TaskEntity?> = taskDao.getById(id)

    fun getTasksByFolder(folderId: Long): Flow<List<TaskEntity>> = taskDao.getByFolder(folderId)

    fun getInboxTasks(): Flow<List<TaskEntity>> = taskDao.getInbox()

    fun getActiveTasks(): Flow<List<TaskEntity>> = taskDao.getActive()

    fun getCompletedTasks(): Flow<List<TaskEntity>> = taskDao.getCompleted()

    fun getWontDoTasks(): Flow<List<TaskEntity>> = taskDao.getWontDo()

    fun getDeletedTasks(): Flow<List<TaskEntity>> = taskDao.getDeleted()

    fun getActiveCountByFolder(folderId: Long): Flow<Int> = taskDao.getActiveCountByFolder(folderId)

    fun getTasksByDueDate(startOfDay: Long, endOfDay: Long): Flow<List<TaskEntity>> =
        taskDao.getByDueDate(startOfDay, endOfDay)

    // ============================================================
    // Mutations (suspend — выполняются на IO)
    // ============================================================

    suspend fun addTask(
        title: String,
        description: String = "",
        priority: com.timetask.pro.v2.data.local.db.entity.Priority = com.timetask.pro.v2.data.local.db.entity.Priority.NONE,
        folderId: Long? = null,
        quadrant: Int? = null,
        pinMode: Int = 0,
        progressPercent: Int = 0,
        tags: List<com.timetask.pro.v2.data.local.db.entity.TagEntity> = emptyList()
    ): Long {
        return withContext(Dispatchers.IO) {
            val taskId = taskDao.insert(
                TaskEntity(
                    title = title,
                    description = description,
                    priority = priority,
                    folderId = folderId,
                    quadrant = quadrant,
                    pinMode = pinMode,
                    progressPercent = progressPercent
                )
            )
            
            if (tags.isNotEmpty()) {
                val crossRefs = tags.map { tag ->
                    com.timetask.pro.v2.data.local.db.entity.TaskTagCrossRef(taskId, tag.id)
                }
                taskDao.insertTaskTagCrossRefs(crossRefs)
            }
            
            taskId
        }
    }

    suspend fun updateTask(task: TaskEntity, newTags: List<com.timetask.pro.v2.data.local.db.entity.TagEntity>? = null) {
        withContext(Dispatchers.IO) {
            // Serialize tag IDs into the tagIds JSON field for efficient list rendering
            val updatedTask = if (newTags != null) {
                val tagIdsJson = if (newTags.isEmpty()) "[]" else newTags.joinToString(prefix = "[", postfix = "]") { it.id.toString() }
                task.copy(tagIds = tagIdsJson, updatedAt = System.currentTimeMillis())
            } else {
                task.copy(updatedAt = System.currentTimeMillis())
            }
            taskDao.update(updatedTask)
            
            if (newTags != null) {
                taskDao.deleteTaskTagCrossRefsByTaskId(task.id)
                if (newTags.isNotEmpty()) {
                    val crossRefs = newTags.map { tag ->
                        com.timetask.pro.v2.data.local.db.entity.TaskTagCrossRef(task.id, tag.id)
                    }
                    taskDao.insertTaskTagCrossRefs(crossRefs)
                }
            }
        }
    }

    suspend fun updateTaskOrders(tasksWithNewOrders: List<Pair<Long, Int>>) {
        withContext(Dispatchers.IO) {
            tasksWithNewOrders.forEach { (id, order) ->
                taskDao.updateOrder(id, order)
            }
        }
    }

    suspend fun deleteTask(task: TaskEntity) {
        withContext(Dispatchers.IO) {
            taskDao.updateStatus(task.id, TaskStatus.DELETED)
        }
    }

    suspend fun permanentlyDeleteTask(task: TaskEntity) {
        withContext(Dispatchers.IO) {
            taskDao.delete(task)
        }
    }

    suspend fun deleteTaskById(id: Long) {
        withContext(Dispatchers.IO) {
            taskDao.updateStatus(id, TaskStatus.DELETED)
        }
    }

    suspend fun permanentlyDeleteTaskById(id: Long) {
        withContext(Dispatchers.IO) {
            taskDao.deleteById(id)
        }
    }

    suspend fun toggleTask(task: TaskEntity) {
        withContext(Dispatchers.IO) {
            val newStatus = if (task.status == TaskStatus.DONE) TaskStatus.TODO else TaskStatus.DONE
            val completedAt = if (newStatus == TaskStatus.DONE) System.currentTimeMillis() else null
            taskDao.updateStatus(
                id = task.id,
                status = newStatus,
                completedAt = completedAt,
            )
        }
    }

    suspend fun pinTask(id: Long, isPinned: Boolean) {
        withContext(Dispatchers.IO) {
            taskDao.updatePinned(id, isPinned)
        }
    }

    suspend fun addAccumulatedTime(id: Long, addedTimeMs: Long) {
        withContext(Dispatchers.IO) {
            taskDao.addAccumulatedTime(id, addedTimeMs)
        }
    }

    suspend fun setTaskParent(childId: Long, parentId: Long?) {
        withContext(Dispatchers.IO) {
            taskDao.updateParentId(childId, parentId)
        }
    }

    fun getSubtasks(parentId: Long): Flow<List<TaskEntity>> = taskDao.getSubtasks(parentId)

    fun getSubtaskCount(parentId: Long): Flow<Int> = taskDao.getSubtaskCount(parentId)
    fun getCompletedSubtaskCount(parentId: Long): Flow<Int> = taskDao.getCompletedSubtaskCount(parentId)

    fun getActiveSubtaskCount(parentId: Long): Flow<Int> = taskDao.getActiveSubtaskCount(parentId)

    fun getTagsForTask(taskId: Long): Flow<List<com.timetask.pro.v2.data.local.db.entity.TagEntity>> = taskDao.getTagsForTask(taskId)

    suspend fun addSubtask(title: String, parentId: Long): Long {
        return withContext(Dispatchers.IO) {
            taskDao.insert(
                TaskEntity(
                    title = title,
                    parentId = parentId,
                )
            )
        }
    }

    companion object {
        @Volatile
        private var instance: TaskRepository? = null

        fun getInstance(context: Context): TaskRepository {
            return instance ?: synchronized(this) {
                instance ?: TaskRepository(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
