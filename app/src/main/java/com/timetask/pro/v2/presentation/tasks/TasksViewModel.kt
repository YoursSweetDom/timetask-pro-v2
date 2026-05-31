package com.timetask.pro.v2.presentation.tasks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.timetask.pro.v2.data.local.db.TimeTaskDatabase
import com.timetask.pro.v2.data.local.db.entity.Priority
import com.timetask.pro.v2.data.local.db.entity.TaskEntity
import com.timetask.pro.v2.data.local.db.entity.TaskStatus
import com.timetask.pro.v2.data.repository.StopwatchRepositoryImpl
import com.timetask.pro.v2.data.repository.TaskRepository
import com.timetask.pro.v2.data.repository.TimerRepository
import com.timetask.pro.v2.data.repository.FolderRepository
import com.timetask.pro.v2.data.repository.TagRepository
import com.timetask.pro.v2.data.repository.CategoryRepository
import com.timetask.pro.v2.data.repository.FilterRepository
import com.timetask.pro.v2.domain.model.TaskFilter
import com.timetask.pro.v2.domain.model.tasks.TasksViewPreferences
import com.timetask.pro.v2.data.preferences.AppPreferences
import com.timetask.pro.v2.domain.model.tasks.TaskGroup
import com.timetask.pro.v2.presentation.tools.timers.CreateTimerState
import com.timetask.pro.v2.util.DateUtils
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.timetask.pro.v2.domain.model.tasks.TasksSortOrder

class TasksViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TaskRepository.getInstance(application)
    private val folderRepository = FolderRepository.getInstance(application)
    private val tagRepository = TagRepository.getInstance(application)
    private val categoryRepository = CategoryRepository.getInstance(application)
    private val filterRepository = FilterRepository.getInstance(application)
    private val appPreferences = AppPreferences.getInstance(application)

    val tasksViewPreferences: StateFlow<TasksViewPreferences> = appPreferences.tasksViewPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = TasksViewPreferences()
        )

    val tags = tagRepository.getAllTags()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    fun updateTasksViewPreferences(prefs: TasksViewPreferences) {
        viewModelScope.launch {
            appPreferences.updateTasksViewPreferences(
                viewMode = prefs.viewMode,
                hideCompleted = prefs.hideCompleted,
                hideSubtasks = prefs.hideSubtasks,
                hideDetails = prefs.hideDetails,
                sortOrder = prefs.sortOrder,
                groupingOrder = prefs.groupingOrder,
                checkboxColorMode = prefs.checkboxColorMode
            )
        }
    }

    // ============================================================
    // Фильтр (заменяет старый _selectedFolderId)
    // ============================================================

    private val _currentFilter = MutableStateFlow<TaskFilter>(TaskFilter.Inbox)
    val currentFilter: StateFlow<TaskFilter> = _currentFilter.asStateFlow()

    fun setFilter(filter: TaskFilter) {
        _currentFilter.value = filter
    }

    /** Обратная совместимость: установить фильтр по folderId (null = Inbox) */
    fun setFolderFilter(folderId: Long?) {
        _currentFilter.value = if (folderId == null) TaskFilter.Inbox else TaskFilter.Folder(folderId)
    }

    // ============================================================
    // Tasks — реагируют на смену фильтра и настройки вида
    // ============================================================

    /** Выполненные задачи (с учетом фильтра) */
    @OptIn(ExperimentalCoroutinesApi::class)
    val completedTasks: StateFlow<ImmutableList<TaskEntity>> = _currentFilter
        .flatMapLatest { filter ->
            when (filter) {
                is TaskFilter.Folder -> repository.getTasksByFolder(filter.id).map { list ->
                    list.filter { it.status == TaskStatus.DONE }
                }
                else -> repository.getCompletedTasks()
            }
        }
        .map { it.toImmutableList() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = persistentListOf(),
        )

    val currentFilterTitle: StateFlow<String> = combine(
        _currentFilter,
        folderRepository.getAllFolders(),
        tagRepository.getAllTags(),
        categoryRepository.getAllCategories(),
        filterRepository.getAllFilters()
    ) { filter, folders, tags, categories, filters ->
        when (filter) {
            is TaskFilter.Inbox -> "📥 Входящие"
            is TaskFilter.All -> "📄 Все"
            is TaskFilter.Today -> "📅 Сегодня"
            is TaskFilter.Tomorrow -> "☀️ Завтра"
            is TaskFilter.Next7Days -> "🗓 След. 7 дней"
            is TaskFilter.Folder -> {
                val f = folders.find { it.id == filter.id }
                val icon = f?.emoji ?: f?.icon?.let { "" } ?: "📁"
                "$icon ${f?.name ?: "Список"}"
            }
            is TaskFilter.Tag -> {
                val t = tags.find { it.id == filter.id }
                val icon = t?.emoji ?: "🏷️"
                "$icon ${t?.name ?: "Метка"}"
            }
            is TaskFilter.Category -> {
                val c = categories.find { it.id == filter.id }
                val icon = c?.icon ?: "📦"
                "$icon ${c?.name ?: "Категория"}"
            }
            is TaskFilter.Custom -> {
                val f = filters.find { it.id == filter.id }
                val icon = f?.icon ?: "🌪"
                "$icon ${f?.name ?: "Фильтр"}"
            }
            else -> "Задачи"
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = "📥 Входящие"
    )

    private val _pendingDeletedTaskIds = MutableStateFlow<Set<Long>>(emptySet())

    /** Главный StateFlow сГруппированными задачами (TaskGroup) */
    @OptIn(ExperimentalCoroutinesApi::class)
    val taskGroups: StateFlow<ImmutableList<TaskGroup>> = combine(
        combine(
            _currentFilter.flatMapLatest { filter ->
                when (filter) {
                    is TaskFilter.Inbox -> repository.getInboxTasks()
                    is TaskFilter.All -> repository.getActiveTasks()
                    is TaskFilter.Today -> repository.getTasksByDueDate(
                        startOfDay = DateUtils.startOfToday(),
                        endOfDay = DateUtils.endOfToday()
                    )
                    is TaskFilter.Tomorrow -> repository.getTasksByDueDate(
                        startOfDay = DateUtils.startOfTomorrow(),
                        endOfDay = DateUtils.endOfTomorrow()
                    )
                    is TaskFilter.Next7Days -> repository.getTasksByDueDate(
                        startOfDay = DateUtils.startOfToday(),
                        endOfDay = DateUtils.endOfDaysFromNow(6)
                    )
                    is TaskFilter.Folder -> repository.getTasksByFolder(filter.id).map { list ->
                        list.filter { it.status != TaskStatus.DONE && it.status != TaskStatus.WONT_DO }
                    }
                    is TaskFilter.Tag -> repository.getActiveTasks().map { list ->
                        list.filter { task ->
                            task.tagIds.contains("\"${filter.id}\"") || task.tagIds.contains("[${filter.id}]") || task.tagIds.contains(",${filter.id},") || task.tagIds.contains("[${filter.id},") || task.tagIds.contains(",${filter.id}]")
                        }
                    }
                    is TaskFilter.Category -> repository.getActiveTasks().map { list ->
                        list.filter { it.categoryId == filter.id }
                    }
                    is TaskFilter.Custom -> repository.getActiveTasks()
                }
            },
            completedTasks,
            tasksViewPreferences,
            ::Triple
        ),
        folderRepository.getAllFolders(),
        tagRepository.getAllTags(),
        _pendingDeletedTaskIds
    ) { baseData, folders, tags, pendingDeletedIds ->
        val rawActive = baseData.first
        val rawCompleted = baseData.second
        val prefs = baseData.third
        
        // Filter out pending deleted tasks
        val filteredActive = if (pendingDeletedIds.isNotEmpty()) rawActive.filter { it.id !in pendingDeletedIds } else rawActive
        val filteredCompleted = if (pendingDeletedIds.isNotEmpty()) rawCompleted.filter { it.id !in pendingDeletedIds } else rawCompleted

        // 1. Sort active tasks FIRST according to SortOrder
        val sortedActive = when (prefs.sortOrder) {
            TasksSortOrder.DATE -> filteredActive.sortedWith(compareBy<TaskEntity> { it.dueDate == null }.thenBy { it.dueDate })
            TasksSortOrder.NAME -> filteredActive.sortedBy { it.title.lowercase() }
            TasksSortOrder.PRIORITY -> filteredActive.sortedByDescending { it.priority.ordinal }
            TasksSortOrder.TAG -> filteredActive.sortedBy { it.tagIds } // Basic sort 
        }

        val sortedCompleted = filteredCompleted // Should we sort completed too? Usually sorted by completion date, but we can keep it as is.
        
        val folderMap = folders.associateBy { it.id }
        val tagMap = tags.associateBy { it.id }

        // 2. Mutable lists inside maps for grouping
        data class MutableGroup(
            val id: String, val title: String, val icon: String? = null, val color: String? = null,
            val active: MutableList<TaskEntity> = mutableListOf(),
            val completed: MutableList<TaskEntity> = mutableListOf()
        )
        
        val groupedMap = mutableMapOf<String, MutableGroup>()
        
        fun getGroup(id: String, title: String, icon: String? = null, color: String? = null): MutableGroup {
            return groupedMap.getOrPut(id) { MutableGroup(id, title, icon, color) }
        }

        // 3. Helper to assign a task to grouping(s)
        fun assignTask(task: TaskEntity, isActive: Boolean) {
            when (prefs.groupingOrder) {
                com.timetask.pro.v2.domain.model.tasks.TasksGroupingOrder.NONE -> {
                    val group = getGroup("all", "Все задачи")
                    if (isActive) group.active.add(task) else group.completed.add(task)
                }
                com.timetask.pro.v2.domain.model.tasks.TasksGroupingOrder.LIST -> {
                    val folderId = task.folderId
                    val group = if (folderId == null) {
                        getGroup("inbox", "Входящие", "📥")
                    } else {
                        val folder = folderMap[folderId]
                        getGroup(folderId.toString(), folder?.name ?: "Список", folder?.emoji, folder?.color)
                    }
                    if (isActive) group.active.add(task) else group.completed.add(task)
                }
                com.timetask.pro.v2.domain.model.tasks.TasksGroupingOrder.PRIORITY -> {
                    val group = when (task.priority) {
                        Priority.HIGH -> getGroup("high", "Высокий приоритет", "🔴", "#F44336")
                        Priority.MEDIUM -> getGroup("medium", "Средний приоритет", "🟡", "#FFEB3B")
                        Priority.LOW -> getGroup("low", "Низкий приоритет", "🔵", "#2196F3")
                        Priority.NONE -> getGroup("none", "Без приоритета", "⚪", "#9E9E9E")
                    }
                    if (isActive) group.active.add(task) else group.completed.add(task)
                }
                com.timetask.pro.v2.domain.model.tasks.TasksGroupingOrder.DATE -> {
                    val now = DateUtils.startOfToday()
                    val due = task.dueDate
                    val group = when {
                        due == null -> getGroup("no_date", "Без даты", "📅")
                        due < now -> getGroup("overdue", "Просрочено", "⚠", "#F44336")
                        due in now until DateUtils.startOfTomorrow() -> getGroup("today", "Сегодня", "⭐")
                        due in DateUtils.startOfTomorrow() until DateUtils.startOfTomorrow() + 86400000L -> getGroup("tomorrow", "Завтра", "☀️")
                        due in DateUtils.startOfToday() until DateUtils.endOfDaysFromNow(6) -> getGroup("next7d", "Следующие 7 дней", "📆")
                        else -> getGroup("later", "Позже", "🔮")
                    }
                    if (isActive) group.active.add(task) else group.completed.add(task)
                }
                com.timetask.pro.v2.domain.model.tasks.TasksGroupingOrder.TAG -> {
                    // Extract tag IDs from JSON array string "[1, 2]"
                    val tagIdsStr = task.tagIds.trim('[', ']').split(",").mapNotNull { it.trim().toLongOrNull() }
                    if (tagIdsStr.isEmpty()) {
                        val group = getGroup("no_tag", "Без меток", "🏷")
                        if (isActive) group.active.add(task) else group.completed.add(task)
                    } else {
                        for (tid in tagIdsStr) {
                            val tag = tagMap[tid]
                            val group = getGroup(tid.toString(), tag?.name ?: "Метка", tag?.emoji, tag?.color)
                            if (isActive) group.active.add(task) else group.completed.add(task)
                        }
                    }
                }
            }
        }

        sortedActive.forEach { assignTask(it, true) }
        sortedCompleted.forEach { assignTask(it, false) }

        // 4. Map back to TaskGroup and order the groups themselves logically
        val finalGroups = groupedMap.values.map { 
            TaskGroup(it.id, it.title, it.icon, it.color, it.active, it.completed)
        }
        
        // Optionally sort groups based on standard logical order
        val sortedFinalGroups = when (prefs.groupingOrder) {
            com.timetask.pro.v2.domain.model.tasks.TasksGroupingOrder.PRIORITY -> finalGroups.sortedBy { 
                when(it.id) { "high" -> 1; "medium" -> 2; "low" -> 3; else -> 4 }
            }
            com.timetask.pro.v2.domain.model.tasks.TasksGroupingOrder.DATE -> finalGroups.sortedBy {
                when(it.id) { "overdue" -> 1; "today" -> 2; "tomorrow" -> 3; "next7d" -> 4; "later" -> 5; else -> 6 }
            }
            else -> finalGroups
        }

        sortedFinalGroups.toImmutableList()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = persistentListOf(),
    )



    /** Показать/скрыть лист добавления задачи */
    private val _showAddSheet = MutableStateFlow(false)
    val showAddSheet: StateFlow<Boolean> = _showAddSheet.asStateFlow()

    /** Показать/скрыть выполненные задачи */
    private val _showCompleted = MutableStateFlow(false)
    val showCompleted: StateFlow<Boolean> = _showCompleted.asStateFlow()

    // ============================================================
    // Actions
    // ============================================================

    fun quickAddTask(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val folderId = (_currentFilter.value as? TaskFilter.Folder)?.id
            repository.addTask(
                title = title.trim(),
                folderId = folderId,
            )
        }
    }

    fun addTask(
        title: String, 
        description: String, 
        quadrant: Int? = null, 
        pinMode: Int = 0, 
        progress: Int = 0, 
        tags: List<com.timetask.pro.v2.data.local.db.entity.TagEntity> = emptyList()
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val folderId = (_currentFilter.value as? TaskFilter.Folder)?.id
            repository.addTask(
                title = title.trim(),
                description = description.trim(),
                priority = Priority.NONE, // obsolete
                folderId = folderId,
                quadrant = quadrant,
                pinMode = pinMode,
                progressPercent = progress,
                tags = tags
            )
            _showAddSheet.value = false
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTask(task)
        }
    }

    fun updateTask(task: TaskEntity, newTags: List<com.timetask.pro.v2.data.local.db.entity.TagEntity>? = null) {
        viewModelScope.launch {
            repository.updateTask(task.copy(updatedAt = System.currentTimeMillis()), newTags)
        }
    }

    /**
     * Мягкое удаление задачи с возможностью отмены (Undo).
     * Сразу скрывает задачу из UI, ждет 3.5 секунды,
     * и если не было отмены — пишет в БД статус DELETED.
     */
    fun softDeleteTaskWithUndo(task: TaskEntity) {
        _pendingDeletedTaskIds.value += task.id
        
        viewModelScope.launch {
            kotlinx.coroutines.delay(3500)
            if (_pendingDeletedTaskIds.value.contains(task.id)) {
                _pendingDeletedTaskIds.value -= task.id
                repository.deleteTask(task)
            }
        }
    }

    /** Отменяет удаление задачи (Undo) */
    fun undoDeleteTask(taskId: Long) {
        _pendingDeletedTaskIds.value -= taskId
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun reorderTasks(activeTasks: List<TaskEntity>) {
        viewModelScope.launch {
            // Map the tasks to their new order based on list index
            val updates = activeTasks.mapIndexed { index, task ->
                Pair(task.id, index)
            }
            repository.updateTaskOrders(updates)
        }
    }

    /** Делает childTask подзадачей parentTask */
    fun makeSubtask(childTask: TaskEntity, parentTask: TaskEntity) {
        viewModelScope.launch {
            repository.setTaskParent(childTask.id, parentTask.id)
        }
    }

    /** Быстрое добавление подзадачи */
    fun addSubtask(title: String, parentId: Long) {
        viewModelScope.launch {
            repository.addSubtask(title, parentId)
        }
    }

    /** Отвязать подзадачу от родителя (сделать самостоятельной) */
    fun removeFromParent(task: TaskEntity) {
        viewModelScope.launch {
            repository.setTaskParent(task.id, null)
        }
    }

    /** Получить подзадачи для конкретной задачи (Flow) */
    fun getSubtasks(parentId: Long) = repository.getSubtasks(parentId)

    /** Получить количество подзадач */
    fun getSubtaskCount(parentId: Long) = repository.getSubtaskCount(parentId)
    fun getCompletedSubtaskCount(parentId: Long) = repository.getCompletedSubtaskCount(parentId)

    /** Получить теги задачи (Flow) */
    fun getTagsForTask(taskId: Long) = repository.getTagsForTask(taskId)

    fun pinTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.pinTask(task.id, !task.isPinned)
        }
    }

    /** Создает новую метку (при вводе имени, если её еще нет) */
    fun addTag(name: String, color: String = "#8A8A8E") {
        if (name.isBlank()) return
        viewModelScope.launch {
            tagRepository.addTag(name = name.trim(), color = color)
        }
    }

    fun showAddSheet() {
        _showAddSheet.value = true
    }

    fun hideAddSheet() {
        _showAddSheet.value = false
    }

    fun toggleShowCompleted() {
        _showCompleted.value = !_showCompleted.value
    }

    /** Обновить дату задачи (из свайп-действия «Календарь») */
    fun updateTaskDate(task: TaskEntity, newDate: Long?) {
        viewModelScope.launch {
            repository.updateTask(task.copy(dueDate = newDate, updatedAt = System.currentTimeMillis()))
        }
    }

    /** Переместить задачу в другую папку (из свайп-действия «Переместить») */
    fun moveTaskToFolder(task: TaskEntity, folderId: Long?) {
        viewModelScope.launch {
            repository.updateTask(task.copy(folderId = folderId, updatedAt = System.currentTimeMillis()))
        }
    }

    /** Обновить приоритет задачи (при Drag & Drop между группами приоритетов) */
    fun updateTaskPriority(task: TaskEntity, priority: Priority) {
        viewModelScope.launch {
            repository.updateTask(task.copy(priority = priority, updatedAt = System.currentTimeMillis()))
        }
    }

    /** Обновить метку задачи (при Drag & Drop между группами меток) */
    fun updateTaskTag(task: TaskEntity, oldTagId: Long?, newTagId: Long?) {
        viewModelScope.launch {
            // Parse existing tag list representing JSON like "[1, 2]" or "[]"
            val currentTagsStr = task.tagIds.trim('[', ']').split(",").mapNotNull { it.trim().toLongOrNull() }
            val currentTags = currentTagsStr.toMutableList()

            // Remove the tag we dragged OUT of (if valid)
            if (oldTagId != null) {
                currentTags.remove(oldTagId)
            }

            // Add the tag we dragged INTO (if valid and not already present)
            if (newTagId != null && !currentTags.contains(newTagId)) {
                currentTags.add(newTagId)
            }

            // Serialize back to JSON string logic
            val newTagsJson = if (currentTags.isEmpty()) "[]" else currentTags.joinToString(prefix = "[", postfix = "]")

            repository.updateTask(task.copy(tagIds = newTagsJson, updatedAt = System.currentTimeMillis()))
        }
    }

    // ============================================================
    // Tools Integration (Timers / Stopwatches)
    // ============================================================

    fun createTimerFromTask(task: TaskEntity, state: CreateTimerState) {
        viewModelScope.launch {
            val db = TimeTaskDatabase.getInstance(getApplication())
            val timerRepo = TimerRepository.getInstance(db)
            timerRepo.addTimer(
                name = state.name.ifBlank { task.title },
                durationMs = state.durationMs,
                config = state.config,
                notification = com.timetask.pro.v2.data.local.db.entity.TimerNotification(
                    showInNotifications = state.showInNotifications
                ),
                linkedTaskIdsJson = "[${task.id}]",
            )
        }
    }

    fun createStopwatchFromTask(task: TaskEntity) {
        viewModelScope.launch {
            val db = TimeTaskDatabase.getInstance(getApplication())
            val stopwatchRepo = StopwatchRepositoryImpl.getInstance(db)
            stopwatchRepo.createStopwatch(
                name = task.title,
                linkedTaskIdsJson = "[${task.id}]",
                categoryId = task.folderId
            )
        }
    }
}
