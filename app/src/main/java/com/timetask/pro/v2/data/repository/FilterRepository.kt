package com.timetask.pro.v2.data.repository

import android.content.Context
import com.timetask.pro.v2.data.local.db.TimeTaskDatabase
import com.timetask.pro.v2.data.local.db.entity.FilterEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FilterRepository(context: Context) {

    private val filterDao = TimeTaskDatabase.getInstance(context).filterDao()

    fun getAllFilters(): Flow<List<FilterEntity>> = filterDao.getAll()
    fun getFilterById(id: Long): Flow<FilterEntity?> = filterDao.getById(id)

    suspend fun addFilter(name: String, icon: String? = null, emoji: String? = null, logicJson: String = "{}"): Long {
        return withContext(Dispatchers.IO) {
            filterDao.insert(FilterEntity(name = name, icon = icon, emoji = emoji, logicJson = logicJson))
        }
    }

    suspend fun updateFilter(filter: FilterEntity) {
        withContext(Dispatchers.IO) { filterDao.update(filter) }
    }

    suspend fun deleteFilter(filter: FilterEntity) {
        withContext(Dispatchers.IO) { filterDao.delete(filter) }
    }

    suspend fun updateFilterOrders(updates: List<Pair<Long, Int>>) {
        withContext(Dispatchers.IO) {
            filterDao.updateOrders(updates)
        }
    }

    companion object {
        @Volatile private var instance: FilterRepository? = null
        fun getInstance(context: Context): FilterRepository {
            return instance ?: synchronized(this) {
                instance ?: FilterRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
