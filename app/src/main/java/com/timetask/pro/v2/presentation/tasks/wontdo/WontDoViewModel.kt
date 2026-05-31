package com.timetask.pro.v2.presentation.tasks.wontdo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.timetask.pro.v2.data.local.db.entity.TaskEntity
import com.timetask.pro.v2.data.local.db.entity.TaskStatus
import com.timetask.pro.v2.data.repository.TaskRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WontDoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TaskRepository.getInstance(application)

    val wontDoTasks: StateFlow<ImmutableList<TaskEntity>> = repository.getWontDoTasks()
        .map { it.toImmutableList() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = persistentListOf(),
        )

    fun restoreTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task.copy(status = TaskStatus.TODO, updatedAt = System.currentTimeMillis()))
        }
    }

    fun moveToTrash(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task.copy(status = TaskStatus.DELETED, updatedAt = System.currentTimeMillis()))
        }
    }
}
