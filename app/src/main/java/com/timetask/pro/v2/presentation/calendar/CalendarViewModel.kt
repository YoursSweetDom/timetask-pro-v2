package com.timetask.pro.v2.presentation.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.timetask.pro.v2.data.local.db.entity.TaskEntity
import com.timetask.pro.v2.data.repository.TaskRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TaskRepository.getInstance(application)

    // ============================================================
    // Текущий месяц
    // ============================================================

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    // ============================================================
    // Выбранный день
    // ============================================================

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // ============================================================
    // Задачи на выбранный день
    // ============================================================

    @OptIn(ExperimentalCoroutinesApi::class)
    val tasksForSelectedDate: StateFlow<ImmutableList<TaskEntity>> = _selectedDate
        .flatMapLatest { date ->
            val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
            repository.getTasksByDueDate(startOfDay, endOfDay)
        }
        .map { it.toImmutableList() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, persistentListOf())

    // ============================================================
    // Все задачи текущего месяца (для отображения точек)
    // ============================================================

    @OptIn(ExperimentalCoroutinesApi::class)
    val tasksForMonth: StateFlow<ImmutableList<TaskEntity>> = _currentMonth
        .flatMapLatest { month ->
            val startOfMonth = month.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfMonth = month.plusMonths(1).atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
            repository.getTasksByDueDate(startOfMonth, endOfMonth)
        }
        .map { it.toImmutableList() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, persistentListOf())

    // ============================================================
    // Set дней с задачами (для быстрого lookup)
    // ============================================================

    val daysWithTasks: StateFlow<Set<Int>> = tasksForMonth
        .map { tasks ->
            tasks.mapNotNull { task ->
                task.dueDate?.let { millis ->
                    java.time.Instant.ofEpochMilli(millis)
                        .atZone(ZoneId.systemDefault())
                        .dayOfMonth
                }
            }.toSet()
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    // ============================================================
    // Actions
    // ============================================================

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun previousMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
    }

    fun goToToday() {
        _currentMonth.value = YearMonth.now()
        _selectedDate.value = LocalDate.now()
    }

    fun toggleTask(task: com.timetask.pro.v2.data.local.db.entity.TaskEntity) {
        viewModelScope.launch {
            repository.toggleTask(task)
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task.copy(updatedAt = System.currentTimeMillis()))
        }
    }
}
