package com.timetask.pro.v2.domain.model

/**
 * Фильтр для отображения задач.
 * Заменяет простой `Long?` (folderId) полноценной навигацией по умным спискам,
 * тегам, категориям и пользовательским фильтрам.
 */
sealed class TaskFilter {

    /** Входящие — задачи без папки */
    data object Inbox : TaskFilter()

    /** Все активные задачи */
    data object All : TaskFilter()

    /** Задачи с dueDate = сегодня */
    data object Today : TaskFilter()

    /** Задачи с dueDate = завтра */
    data object Tomorrow : TaskFilter()

    /** Задачи с dueDate в ближайшие 7 дней */
    data object Next7Days : TaskFilter()

    /** Задачи из конкретной папки */
    data class Folder(val id: Long) : TaskFilter()

    /** Задачи с конкретным тегом */
    data class Tag(val id: Long) : TaskFilter()

    /** Задачи конкретной категории */
    data class Category(val id: Long) : TaskFilter()

    /** Пользовательский фильтр (по logicJson из FilterEntity) */
    data class Custom(val id: Long) : TaskFilter()
}
