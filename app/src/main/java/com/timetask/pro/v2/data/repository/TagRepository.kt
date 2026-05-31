package com.timetask.pro.v2.data.repository

import android.content.Context
import com.timetask.pro.v2.data.local.db.TimeTaskDatabase
import com.timetask.pro.v2.data.local.db.entity.TagEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TagRepository(context: Context) {

    private val tagDao = TimeTaskDatabase.getInstance(context).tagDao()

    fun getAllTags(): Flow<List<TagEntity>> = tagDao.getAll()
    fun getTagById(id: Long): Flow<TagEntity?> = tagDao.getById(id)

    suspend fun addTag(name: String, color: String? = null, emoji: String? = null): Long {
        return withContext(Dispatchers.IO) {
            tagDao.insert(TagEntity(name = name, color = color, emoji = emoji))
        }
    }

    suspend fun updateTag(tag: TagEntity) {
        withContext(Dispatchers.IO) { tagDao.update(tag) }
    }

    suspend fun deleteTag(tag: TagEntity) {
        withContext(Dispatchers.IO) { tagDao.delete(tag) }
    }

    suspend fun updateTagOrders(updates: List<Pair<Long, Int>>) {
        withContext(Dispatchers.IO) {
            tagDao.updateOrders(updates)
        }
    }

    companion object {
        @Volatile private var instance: TagRepository? = null
        fun getInstance(context: Context): TagRepository {
            return instance ?: synchronized(this) {
                instance ?: TagRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
