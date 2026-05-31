package com.timetask.pro.v2.data.repository

import android.content.Context
import com.timetask.pro.v2.data.local.db.TimeTaskDatabase
import com.timetask.pro.v2.data.local.db.entity.FolderEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository для папок.
 * Обёртка над FolderDao с выполнением на Dispatchers.IO.
 */
class FolderRepository(context: Context) {

    private val folderDao = TimeTaskDatabase.getInstance(context).folderDao()

    // ============================================================
    // Queries
    // ============================================================

    fun getAllFolders(): Flow<List<FolderEntity>> = folderDao.getAll()

    fun getFolderById(id: Long): Flow<FolderEntity?> = folderDao.getById(id)

    fun getSystemFolders(): Flow<List<FolderEntity>> = folderDao.getSystemFolders()

    /** Все пользовательские папки (для построения дерева) */
    fun getUserFolders(): Flow<List<FolderEntity>> = folderDao.getUserFolders()

    /** Корневые папки (parentId == null) */
    fun getRootFolders(): Flow<List<FolderEntity>> = folderDao.getRootFolders()

    /** Подпапки конкретной папки */
    fun getByParentId(parentId: Long): Flow<List<FolderEntity>> = folderDao.getByParentId(parentId)

    // ============================================================
    // Mutations
    // ============================================================

    suspend fun addFolder(name: String, emoji: String? = null, color: String? = null): Long {
        return withContext(Dispatchers.IO) {
            folderDao.insert(
                FolderEntity(
                    name = name,
                    emoji = emoji,
                    color = color,
                )
            )
        }
    }

    /** Создать подпапку внутри родительской */
    suspend fun addSubfolder(name: String, parentId: Long, emoji: String? = null, color: String? = null): Long {
        return withContext(Dispatchers.IO) {
            folderDao.insert(
                FolderEntity(
                    name = name,
                    parentId = parentId,
                    emoji = emoji,
                    color = color,
                )
            )
        }
    }

    suspend fun updateFolder(folder: FolderEntity) {
        withContext(Dispatchers.IO) {
            folderDao.update(folder)
        }
    }

    suspend fun deleteFolder(folder: FolderEntity) {
        withContext(Dispatchers.IO) {
            folderDao.delete(folder)
        }
    }

    suspend fun deleteFolderById(id: Long) {
        withContext(Dispatchers.IO) {
            folderDao.deleteById(id)
        }
    }

    suspend fun updateFolderOrders(updates: List<Pair<Long, Int>>) {
        withContext(Dispatchers.IO) {
            folderDao.updateOrders(updates)
        }
    }

    companion object {
        @Volatile
        private var instance: FolderRepository? = null

        fun getInstance(context: Context): FolderRepository {
            return instance ?: synchronized(this) {
                instance ?: FolderRepository(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
