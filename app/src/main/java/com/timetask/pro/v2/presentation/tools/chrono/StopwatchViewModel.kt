package com.timetask.pro.v2.presentation.tools.chrono

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.timetask.pro.v2.domain.model.chrono.Stopwatch
import com.timetask.pro.v2.domain.model.chrono.Lap
import com.timetask.pro.v2.domain.usecase.chrono.StopwatchUseCases
import com.timetask.pro.v2.domain.usecase.chrono.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.timetask.pro.v2.data.local.db.TimeTaskDatabase
import com.timetask.pro.v2.data.repository.StopwatchRepositoryImpl
import com.timetask.pro.v2.data.repository.TaskRepository
import com.timetask.pro.v2.data.repository.TagRepository
import com.timetask.pro.v2.data.local.db.entity.TagEntity

class StopwatchViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val db = TimeTaskDatabase.getInstance(application)
    private val repository = StopwatchRepositoryImpl.getInstance(db)
    private val taskRepository = TaskRepository.getInstance(application)
    private val tagRepository = TagRepository.getInstance(application)

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
    
    private val stopwatchUseCases = StopwatchUseCases(
        getStopwatches = GetStopwatchesUseCase(repository),
        addStopwatch = AddStopwatchUseCase(repository),
        deleteStopwatch = DeleteStopwatchUseCase(repository),
        renameStopwatch = RenameStopwatchUseCase(repository),
        startStopwatch = StartStopwatchUseCase(repository),
        pauseStopwatch = PauseStopwatchUseCase(repository, taskRepository),
        resetStopwatch = ResetStopwatchUseCase(repository, taskRepository),
        addLap = AddLapUseCase(repository),
        editLap = EditLapUseCase(repository),
        softDeleteLap = SoftDeleteLapUseCase(repository),
        restoreStopwatch = RestoreStopwatchUseCase(repository),
        restoreLap = RestoreLapUseCase(repository),
        getDeletedStopwatches = GetDeletedStopwatchesUseCase(repository),
        getDeletedLaps = GetDeletedLapsUseCase(repository),
        hardDeleteStopwatch = HardDeleteStopwatchUseCase(repository),
        hardDeleteLap = HardDeleteLapUseCase(repository),
        updateStopwatchMetadata = UpdateStopwatchMetadataUseCase(repository),
        updateStopwatchNotification = UpdateStopwatchNotificationUseCase(repository)
    )

    private val _stopwatches = MutableStateFlow<List<Stopwatch>>(emptyList())
    val stopwatches: StateFlow<List<Stopwatch>> = _stopwatches.asStateFlow()

    val deletedStopwatches: StateFlow<List<Stopwatch>> = stopwatchUseCases.getDeletedStopwatches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deletedLaps: StateFlow<List<Lap>> = stopwatchUseCases.getDeletedLaps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val taskNames: StateFlow<Map<Long, String>> = taskRepository.getAllTasks()
        .map { list -> list.associate { it.id to it.title } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    private val _tickTrigger = MutableStateFlow(0L)
    val tickTrigger: StateFlow<Long> = _tickTrigger.asStateFlow()

    private var tickJob: Job? = null

    init {
        viewModelScope.launch {
            stopwatchUseCases.getStopwatches().collect { list ->
                _stopwatches.value = list

                if (list.any { it.state == StopwatchState.RUNNING }) {
                    ensureTicking()
                } else {
                    stopTicking()
                }
            }
        }
    }

    fun addStopwatch(name: String = "Секундомер", linkedTaskIds: List<Long> = emptyList()) {
        viewModelScope.launch {
            val linkedTaskIdsJson = org.json.JSONArray(linkedTaskIds).toString()
            stopwatchUseCases.addStopwatch(name, linkedTaskIdsJson)
        }
    }

    fun removeStopwatch(id: String) {
        viewModelScope.launch {
            stopwatchUseCases.deleteStopwatch(id)
        }
    }

    fun restoreStopwatch(id: String) {
        viewModelScope.launch {
            stopwatchUseCases.restoreStopwatch(id)
        }
    }

    fun renameStopwatch(id: String, newName: String) {
        viewModelScope.launch {
            stopwatchUseCases.renameStopwatch(id, newName)
        }
    }

    fun updateStopwatchMetadata(id: String, name: String, categoryText: String, tagsText: String, linkedTaskIds: List<Long>) {
        viewModelScope.launch {
            val linkedTaskIdsJson = org.json.JSONArray(linkedTaskIds).toString()
            stopwatchUseCases.updateStopwatchMetadata(id, name, categoryText, tagsText, linkedTaskIdsJson)
        }
    }

    fun toggleNotification(id: String, show: Boolean) {
        viewModelScope.launch {
            stopwatchUseCases.updateStopwatchNotification(id, show)
        }
    }

    fun startStopwatch(id: String) {
        val sw = _stopwatches.value.find { it.id == id } ?: return
        viewModelScope.launch {
            stopwatchUseCases.startStopwatch(sw, SystemClock.elapsedRealtime())
        }
        val intent = android.content.Intent(getApplication(), com.timetask.pro.v2.service.StopwatchService::class.java)
        getApplication<Application>().startService(intent)
    }

    fun pauseStopwatch(id: String) {
        val sw = _stopwatches.value.find { it.id == id } ?: return
        viewModelScope.launch {
            stopwatchUseCases.pauseStopwatch(sw, SystemClock.elapsedRealtime())
        }
    }

    fun resetStopwatch(id: String) {
        val sw = _stopwatches.value.find { it.id == id } ?: return
        viewModelScope.launch {
            stopwatchUseCases.resetStopwatch(sw, SystemClock.elapsedRealtime())
        }
    }

    fun toggleStartPause(id: String) {
        val sw = _stopwatches.value.find { it.id == id } ?: return
        when (sw.state) {
            StopwatchState.IDLE -> startStopwatch(id)
            StopwatchState.RUNNING -> pauseStopwatch(id)
            StopwatchState.PAUSED -> startStopwatch(id)
        }
    }

    fun addLap(id: String) {
        val sw = _stopwatches.value.find { it.id == id } ?: return
        viewModelScope.launch {
            stopwatchUseCases.addLap(sw, SystemClock.elapsedRealtime())
        }
    }

    fun editLap(lapId: String, newName: String, newColor: Int?) {
        viewModelScope.launch {
            stopwatchUseCases.editLap(lapId, newName, newColor)
        }
    }

    fun removeLap(lapId: String) {
        viewModelScope.launch {
            stopwatchUseCases.softDeleteLap(lapId)
        }
    }

    fun restoreLap(lapId: String) {
        viewModelScope.launch {
            stopwatchUseCases.restoreLap(lapId)
        }
    }

    fun hardDeleteStopwatch(id: String) {
        viewModelScope.launch {
            stopwatchUseCases.hardDeleteStopwatch(id)
        }
    }

    fun hardDeleteLap(lapId: String) {
        viewModelScope.launch {
            stopwatchUseCases.hardDeleteLap(lapId)
        }
    }

    private fun ensureTicking() {
        if (tickJob?.isActive == true) return
        tickJob = viewModelScope.launch {
            while (true) {
                delay(100L) // UI refresh rate: ~10 Hz (можно 60 Hz = 16L для большей плавности миллисекунд)
                _tickTrigger.value = SystemClock.elapsedRealtime()
            }
        }
    }

    private fun stopTicking() {
        tickJob?.cancel()
        tickJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopTicking()
    }
}
