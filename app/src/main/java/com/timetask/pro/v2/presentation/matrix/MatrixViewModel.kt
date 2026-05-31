package com.timetask.pro.v2.presentation.matrix

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.timetask.pro.v2.data.local.db.entity.Priority
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

/**
 * Квадрант матрицы Эйзенхауэра.
 */
data class Quadrant(
    val title: String,
    val subtitle: String,
    val emoji: String,
    val color: Color,
    val tasks: ImmutableList<TaskEntity>,
)

/**
 * ViewModel для матрицы Эйзенхауэра.
 * Маппинг: Priority.HIGH → Q1, MEDIUM → Q2, LOW → Q3, NONE → Q4
 */
class MatrixViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TaskRepository.getInstance(application)

    val quadrants: StateFlow<List<Quadrant>> = repository.getActiveTasks()
        .map { tasks ->
            val byPriority = tasks.groupBy { it.priority }
            listOf(
                Quadrant(
                    title = "Делай",
                    subtitle = "Срочно и важно",
                    emoji = "🔴",
                    color = Color(0xFFE53E3E),
                    tasks = (byPriority[Priority.HIGH] ?: emptyList()).toImmutableList(),
                ),
                Quadrant(
                    title = "Запланируй",
                    subtitle = "Важно, не срочно",
                    emoji = "🟡",
                    color = Color(0xFFD69E2E),
                    tasks = (byPriority[Priority.MEDIUM] ?: emptyList()).toImmutableList(),
                ),
                Quadrant(
                    title = "Делегируй",
                    subtitle = "Срочно, не важно",
                    emoji = "🔵",
                    color = Color(0xFF3182CE),
                    tasks = (byPriority[Priority.LOW] ?: emptyList()).toImmutableList(),
                ),
                Quadrant(
                    title = "Удали",
                    subtitle = "Не срочно, не важно",
                    emoji = "⚪",
                    color = Color(0xFF718096),
                    tasks = (byPriority[Priority.NONE] ?: emptyList()).toImmutableList(),
                ),
            )
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTask(task)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }
}
