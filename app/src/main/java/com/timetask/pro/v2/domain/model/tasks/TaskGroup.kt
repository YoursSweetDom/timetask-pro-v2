package com.timetask.pro.v2.domain.model.tasks

import androidx.compose.runtime.Immutable
import com.timetask.pro.v2.data.local.db.entity.TaskEntity

@Immutable
data class TaskGroup(
    val id: String, // E.g., folderId toString, or Date string, or Tag ID
    val title: String, // "Входящие", "Спорт", "Сегодня"
    val groupIcon: String? = null, // Emoji or icon name
    val groupColor: String? = null, // Hex color
    val activeTasks: List<TaskEntity>,
    val completedTasks: List<TaskEntity> // Completed tasks specific to this group
)
