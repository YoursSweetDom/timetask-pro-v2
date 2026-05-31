package com.timetask.pro.v2.domain.model.tasks

enum class TasksViewMode {
    LIST, KANBAN
}

enum class TasksSortOrder {
    DATE, NAME, TAG, PRIORITY
}

enum class TasksGroupingOrder {
    LIST, DATE, TAG, PRIORITY, NONE
}

enum class TaskCheckboxColorMode {
    DEFAULT, PRIORITY, FOLDER
}

data class TasksViewPreferences(
    val viewMode: TasksViewMode = TasksViewMode.LIST,
    val hideCompleted: Boolean = false,
    val hideSubtasks: Boolean = false,
    val hideDetails: Boolean = false,
    val sortOrder: TasksSortOrder = TasksSortOrder.DATE,
    val groupingOrder: TasksGroupingOrder = TasksGroupingOrder.LIST,
    val checkboxColorMode: TaskCheckboxColorMode = TaskCheckboxColorMode.DEFAULT
)
