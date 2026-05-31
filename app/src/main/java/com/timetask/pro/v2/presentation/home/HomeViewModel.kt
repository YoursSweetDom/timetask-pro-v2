package com.timetask.pro.v2.presentation.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.timetask.pro.v2.data.local.db.entity.TaskEntity
import com.timetask.pro.v2.data.repository.TaskRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId

/**
 * ViewModel для Home Dashboard.
 *
 * Предоставляет:
 * - Задачи на сегодня (по dueDate)
 * - Просроченные задачи
 * - Активные задачи (всего)
 * - Выполненные задачи (всего)
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TaskRepository.getInstance(application)

    // ============================================================
    // Date helpers
    // ============================================================

    private val todayStartMs: Long
        get() {
            val start = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
            return start.toInstant().toEpochMilli()
        }

    private val todayEndMs: Long
        get() {
            val end = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault())
            return end.toInstant().toEpochMilli() - 1
        }

    // ============================================================
    // Flows
    // ============================================================

    /** All active (not done / wont_do) tasks */
    val activeTasks: StateFlow<ImmutableList<TaskEntity>> = repository.getActiveTasks()
        .map { it.toImmutableList() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, persistentListOf())

    /** Today's tasks (by dueDate) */
    val todayTasks: StateFlow<ImmutableList<TaskEntity>> = repository
        .getTasksByDueDate(todayStartMs, todayEndMs)
        .map { it.toImmutableList() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, persistentListOf())

    /** Overdue tasks (dueDate < today start, still active) */
    val overdueTasks: StateFlow<ImmutableList<TaskEntity>> = repository.getActiveTasks()
        .map { list ->
            val now = todayStartMs
            list.filter { it.dueDate != null && it.dueDate < now }.toImmutableList()
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, persistentListOf())

    /** Completed tasks count */
    val completedCount: StateFlow<Int> = repository.getCompletedTasks()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)
}
