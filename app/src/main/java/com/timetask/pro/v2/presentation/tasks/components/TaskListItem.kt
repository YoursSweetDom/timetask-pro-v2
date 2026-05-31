package com.timetask.pro.v2.presentation.tasks.components

import com.timetask.pro.v2.data.local.db.entity.TagEntity
import com.timetask.pro.v2.data.local.db.entity.TaskEntity
import com.timetask.pro.v2.domain.model.tasks.TaskGroup

sealed class TaskListItem(open val key: String) {
    data class Spacing(val id: String = java.util.UUID.randomUUID().toString()) : TaskListItem("space_$id")
    data class Header(val group: TaskGroup) : TaskListItem("header_${group.id}")
    data class ActiveTask(val task: TaskEntity, val group: TaskGroup, val tags: List<TagEntity>, val isFirst: Boolean, val isLast: Boolean) : TaskListItem("active_${task.id}")
    data class CompletedHeader(val group: TaskGroup, val isLast: Boolean) : TaskListItem("comp_header_${group.id}")
    data class CompletedTask(val task: TaskEntity, val group: TaskGroup, val tags: List<TagEntity>, val isLast: Boolean) : TaskListItem("comp_${task.id}")
}
