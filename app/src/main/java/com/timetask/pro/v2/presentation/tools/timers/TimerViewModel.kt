package com.timetask.pro.v2.presentation.tools.timers

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.timetask.pro.v2.data.local.db.TimeTaskDatabase
import com.timetask.pro.v2.data.local.db.entity.TimerConfig
import com.timetask.pro.v2.data.local.db.entity.TimerNotification
import com.timetask.pro.v2.data.local.db.entity.TimerEntity
import com.timetask.pro.v2.data.local.db.entity.TimerState
import com.timetask.pro.v2.data.local.db.entity.UserPresetEntity
import com.timetask.pro.v2.data.repository.TaskRepository
import com.timetask.pro.v2.data.repository.TimerRepository
import com.timetask.pro.v2.data.repository.TagRepository
import com.timetask.pro.v2.data.local.db.entity.TagEntity
import com.timetask.pro.v2.domain.timer.TimerManager
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel для таймеров.
 *
 * - Наблюдает за Flow из Room (реактивное состояние).
 * - Делегирует управление в [TimerManager].
 * - Локальный тик в UI не нужен — [TimerCard] считает время из endTimeMs.
 */
class TimerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = TimeTaskDatabase.getInstance(application)
    private val repository = TimerRepository.getInstance(db)
    private val taskRepository = TaskRepository.getInstance(application)
    private val tagRepository = TagRepository.getInstance(application)
    private val timerManager = TimerManager.getInstance(application, repository)

    // ============================================================
    // Состояние (из Room — реактивное)
    // ============================================================

    val taskNames: StateFlow<Map<Long, String>> = taskRepository.getAllTasks()
        .map { list -> list.associate { it.id to it.title } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    val timers: StateFlow<ImmutableList<TimerEntity>> = repository.getAllTimers()
        .map { it.toImmutableList() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = persistentListOf<TimerEntity>(),
        )

    val presets: List<TimerPreset> = defaultTimerPresets

    val userPresets: StateFlow<ImmutableList<UserPresetEntity>> = repository.getAllUserPresets()
        .map { it.toImmutableList() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = persistentListOf<UserPresetEntity>(),
        )

    val tags: StateFlow<List<TagEntity>> = tagRepository.getAllTags()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    fun addTag(name: String, color: String = "#8A8A8E") {
        if (name.isBlank()) return
        viewModelScope.launch {
            tagRepository.addTag(name = name.trim(), color = color)
        }
    }

    // ============================================================
    // Создание / Удаление
    // ============================================================

    fun addTimer(name: String = "Таймер", durationMs: Long = 5 * 60 * 1000L) {
        viewModelScope.launch {
            repository.addTimer(name, durationMs)
        }
    }

    fun addTimer(state: CreateTimerState) {
        val finalName = state.name.trim().ifBlank { "Таймер" }
        viewModelScope.launch {
            val linkedTaskIdsJson = org.json.JSONArray(state.linkedTaskIds).toString()
            val tagIdsJson = org.json.JSONArray(state.selectedTagIds).toString()
            repository.addTimer(
                name = finalName,
                durationMs = state.durationMs,
                config = state.config,
                notification = TimerNotification(
                    showInNotifications = state.showInNotifications,
                    ringingDurationSec = state.ringingDurationSec,
                ),
                linkedTaskIdsJson = linkedTaskIdsJson,
                tagIdsJson = tagIdsJson,
            )
        }
    }

    fun addTimerFromPreset(preset: TimerPreset) {
        addTimer(name = preset.name, durationMs = preset.durationMs)
    }

    fun removeTimer(id: String) {
        viewModelScope.launch {
            timerManager.stopTimer(id) // Гарантировать остановку
            repository.deleteTimerById(id)
        }
    }

    fun renameTimer(id: String, newName: String) {
        viewModelScope.launch {
            val timer = repository.getTimerById(id) ?: return@launch
            repository.updateTimer(timer.copy(name = newName))
        }
    }

    // ============================================================
    // Управление (делегируется в TimerManager)
    // ============================================================

    fun startTimer(id: String) = timerManager.startTimer(id)

    fun pauseTimer(id: String) = timerManager.pauseTimer(id)

    fun stopTimer(id: String) = timerManager.stopTimer(id)

    fun toggleStartPause(id: String) {
        val timer = timers.value.find { it.id == id } ?: return
        if (timer.state == TimerState.RUNNING) {
            pauseTimer(id)
        } else {
            startTimer(id)
        }
    }

    fun adjustTime(id: String, deltaMs: Long) = timerManager.adjustTime(id, deltaMs)

    fun updateConfig(id: String, config: TimerConfig) {
        viewModelScope.launch {
            val timer = repository.getTimerById(id) ?: return@launch
            repository.updateTimer(timer.copy(config = config))
        }
    }

    fun updateFullTimer(id: String, state: CreateTimerState) {
        viewModelScope.launch {
            val timer = repository.getTimerById(id) ?: return@launch
            val newName = state.name.trim().ifBlank { timer.name }
            val newDuration = state.durationMs
            val newNotification = timer.notification.copy(
                showInNotifications = state.showInNotifications,
                ringingDurationSec = state.ringingDurationSec,
            )
            val newLinkedTaskIdsJson = org.json.JSONArray(state.linkedTaskIds).toString()
            val newTagIdsJson = org.json.JSONArray(state.selectedTagIds).toString()

            // Если таймер IDLE — обновить и remaining. Если RUNNING/PAUSED — только total + config.
            val updatedTimer = if (timer.state == TimerState.IDLE) {
                timer.copy(
                    name = newName,
                    totalDurationMs = newDuration,
                    remainingMs = newDuration,
                    config = state.config,
                    notification = newNotification,
                    linkedTaskIdsJson = newLinkedTaskIdsJson,
                    tagIdsJson = newTagIdsJson,
                )
            } else {
                timer.copy(
                    name = newName,
                    totalDurationMs = newDuration,
                    config = state.config,
                    notification = newNotification,
                    linkedTaskIdsJson = newLinkedTaskIdsJson,
                    tagIdsJson = newTagIdsJson,
                )
            }
            repository.updateTimer(updatedTimer)
        }
    }

    // ============================================================
    // Пользовательские пресеты
    // ============================================================

    fun addUserPreset(name: String, emoji: String, durationMs: Long) {
        viewModelScope.launch {
            repository.addUserPreset(
                UserPresetEntity(
                    name = name,
                    emoji = emoji,
                    durationMs = durationMs,
                ),
            )
        }
    }

    fun deleteUserPreset(id: String) {
        viewModelScope.launch {
            repository.deleteUserPreset(id)
        }
    }
}
