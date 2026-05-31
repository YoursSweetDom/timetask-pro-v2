package com.timetask.pro.v2.presentation.tasks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.timetask.pro.v2.data.local.db.entity.FolderEntity
import com.timetask.pro.v2.data.local.db.entity.Priority
import com.timetask.pro.v2.data.local.db.entity.TaskEntity
import com.timetask.pro.v2.data.repository.FolderRepository
import com.timetask.pro.v2.data.repository.TaskRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана деталей задачи.
 * Загружает задачу по ID и позволяет редактировать поля.
 */
class TaskDetailViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {

    private val taskRepository = TaskRepository.getInstance(application)
    private val folderRepository = FolderRepository.getInstance(application)

    private val taskId: Long = savedStateHandle["taskId"] ?: 0L

    // ============================================================
    // Task fields (editable)
    // ============================================================

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _priority = MutableStateFlow(Priority.NONE)
    val priority: StateFlow<Priority> = _priority.asStateFlow()

    private val _dueDate = MutableStateFlow<Long?>(null)
    val dueDate: StateFlow<Long?> = _dueDate.asStateFlow()

    private val _folderId = MutableStateFlow<Long?>(null)
    val folderId: StateFlow<Long?> = _folderId.asStateFlow()

    private val _isPinned = MutableStateFlow(false)
    val isPinned: StateFlow<Boolean> = _isPinned.asStateFlow()

    private val _quadrant = MutableStateFlow<Int?>(null)
    val quadrant: StateFlow<Int?> = _quadrant.asStateFlow()

    private val _estimatedMinutes = MutableStateFlow<Int?>(null)
    val estimatedMinutes: StateFlow<Int?> = _estimatedMinutes.asStateFlow()

    private val _isLoaded = MutableStateFlow(false)
    val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()

    private var currentTask: TaskEntity? = null

    // ============================================================
    // Available folders
    // ============================================================

    val folders: StateFlow<ImmutableList<FolderEntity>> = folderRepository.getAllFolders()
        .map { it.toImmutableList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), persistentListOf())

    // ============================================================
    // Init — load task
    // ============================================================

    init {
        viewModelScope.launch {
            taskRepository.getTaskById(taskId).collect { task ->
                if (task != null && !_isLoaded.value) {
                    currentTask = task
                    _title.value = task.title
                    _description.value = task.description
                    _priority.value = task.priority
                    _dueDate.value = task.dueDate
                    _folderId.value = task.folderId
                    _isPinned.value = task.isPinned
                    _quadrant.value = task.quadrant
                    _estimatedMinutes.value = task.estimatedMinutes
                    _isLoaded.value = true
                }
            }
        }
    }

    // ============================================================
    // Actions
    // ============================================================

    fun updateTitle(value: String) { _title.value = value }
    fun updateDescription(value: String) { _description.value = value }
    fun updatePriority(value: Priority) { _priority.value = value }
    fun updateDueDate(value: Long?) { _dueDate.value = value }
    fun updateFolderId(value: Long?) { _folderId.value = value }
    fun togglePinned() { _isPinned.value = !_isPinned.value }
    fun updateQuadrant(value: Int?) { _quadrant.value = value }
    fun updateEstimatedMinutes(value: Int?) { _estimatedMinutes.value = value }

    /**
     * Сохраняет все изменения в базу.
     */
    fun save(onSaved: () -> Unit = {}) {
        val task = currentTask ?: return
        if (_title.value.isBlank()) return

        viewModelScope.launch {
            taskRepository.updateTask(
                task.copy(
                    title = _title.value.trim(),
                    description = _description.value.trim(),
                    priority = _priority.value,
                    dueDate = _dueDate.value,
                    folderId = _folderId.value,
                    isPinned = _isPinned.value,
                    quadrant = _quadrant.value,
                    estimatedMinutes = _estimatedMinutes.value,
                )
            )
            onSaved()
        }
    }

    fun deleteAndGoBack(onDeleted: () -> Unit = {}) {
        val task = currentTask ?: return
        viewModelScope.launch {
            taskRepository.deleteTask(task)
            onDeleted()
        }
    }
}
