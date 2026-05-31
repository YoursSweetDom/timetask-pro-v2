package com.timetask.pro.v2.data.repository

import android.content.Context
import com.timetask.pro.v2.data.local.db.TimeTaskDatabase
import com.timetask.pro.v2.data.local.db.entity.CategoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CategoryRepository(context: Context) {

    private val categoryDao = TimeTaskDatabase.getInstance(context).categoryDao()

    fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAll()
    fun getCategoryById(id: Long): Flow<CategoryEntity?> = categoryDao.getById(id)

    suspend fun addCategory(name: String, icon: String? = null, color: String? = null): Long {
        return withContext(Dispatchers.IO) {
            categoryDao.insert(CategoryEntity(name = name, icon = icon, color = color))
        }
    }

    suspend fun updateCategory(category: CategoryEntity) {
        withContext(Dispatchers.IO) { categoryDao.update(category) }
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        withContext(Dispatchers.IO) { categoryDao.delete(category) }
    }

    suspend fun updateCategoryOrders(updates: List<Pair<Long, Int>>) {
        withContext(Dispatchers.IO) {
            categoryDao.updateOrders(updates)
        }
    }

    companion object {
        @Volatile private var instance: CategoryRepository? = null
        fun getInstance(context: Context): CategoryRepository {
            return instance ?: synchronized(this) {
                instance ?: CategoryRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
