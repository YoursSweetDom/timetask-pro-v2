package com.timetask.pro.v2.data.repository

import android.content.Context
import com.timetask.pro.v2.data.local.db.TimeTaskDatabase
import com.timetask.pro.v2.data.local.db.entity.TemplateEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TemplateRepository private constructor(db: TimeTaskDatabase) {

    private val dao = db.templateDao()

    fun getAllTemplates(): Flow<List<TemplateEntity>> = dao.getAllTemplates()

    suspend fun getTemplateById(id: Long): TemplateEntity? = withContext(Dispatchers.IO) {
        dao.getTemplateById(id)
    }

    suspend fun createTemplate(
        name: String,
        icon: String?,
        taskConfigJson: String,
        folderId: Long? = null,
        isPinned: Boolean = false,
        description: String? = null
    ) = withContext(Dispatchers.IO) {
        val maxOrder = dao.getMaxOrder() ?: 0
        val template = TemplateEntity(
            name = name,
            icon = icon,
            order = maxOrder + 1,
            taskConfigJson = taskConfigJson,
            folderId = folderId,
            isPinned = isPinned,
            description = description
        )
        dao.insertTemplate(template)
    }

    suspend fun updateTemplate(template: TemplateEntity) = withContext(Dispatchers.IO) {
        dao.updateTemplate(template)
    }

    suspend fun deleteTemplate(template: TemplateEntity) = withContext(Dispatchers.IO) {
        dao.deleteTemplate(template)
    }

    suspend fun reorderTemplates(templates: List<TemplateEntity>) = withContext(Dispatchers.IO) {
        // Simple sequential update for reordering
        templates.forEachIndexed { index, template ->
            dao.updateTemplate(template.copy(order = index))
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: TemplateRepository? = null

        fun getInstance(context: Context): TemplateRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TemplateRepository(
                    TimeTaskDatabase.getInstance(context)
                ).also { INSTANCE = it }
            }
        }
    }
}
