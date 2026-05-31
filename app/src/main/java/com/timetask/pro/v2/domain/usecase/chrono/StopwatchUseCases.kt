package com.timetask.pro.v2.domain.usecase.chrono

import com.timetask.pro.v2.domain.model.chrono.Stopwatch
import com.timetask.pro.v2.domain.model.chrono.Lap
import com.timetask.pro.v2.domain.repository.StopwatchRepository
import com.timetask.pro.v2.data.repository.TaskRepository
import com.timetask.pro.v2.presentation.tools.chrono.StopwatchState
import kotlinx.coroutines.flow.Flow

class GetStopwatchesUseCase(
    private val repository: StopwatchRepository
) {
    operator fun invoke(): Flow<List<Stopwatch>> {
        return repository.getAllStopwatches()
    }
}

class AddStopwatchUseCase(
    private val repository: StopwatchRepository
) {
    suspend operator fun invoke(name: String = "Секундомер", linkedTaskIdsJson: String = "[]", categoryId: Long? = null) {
        repository.createStopwatch(name, linkedTaskIdsJson, categoryId)
    }
}

class DeleteStopwatchUseCase(
    private val repository: StopwatchRepository
) {
    suspend operator fun invoke(id: String) {
        repository.deleteStopwatch(id)
    }
}

class RenameStopwatchUseCase(
    private val repository: StopwatchRepository
) {
    suspend operator fun invoke(id: String, newName: String) {
        repository.renameStopwatch(id, newName)
    }
}

class UpdateStopwatchMetadataUseCase(
    private val repository: StopwatchRepository
) {
    suspend operator fun invoke(id: String, newName: String, categoryText: String, tagsText: String, linkedTaskIdsJson: String) {
        repository.updateStopwatchMetadata(id, newName, categoryText, tagsText, linkedTaskIdsJson)
    }
}

class StartStopwatchUseCase(
    private val repository: StopwatchRepository
) {
    suspend operator fun invoke(stopwatch: Stopwatch, nowMs: Long) {
        if (stopwatch.state != StopwatchState.RUNNING) {
            repository.updateStopwatchState(
                id = stopwatch.id,
                newState = StopwatchState.RUNNING,
                startTimeMs = nowMs,
                accumulatedMs = stopwatch.accumulatedMs
            )
        }
    }
}

class PauseStopwatchUseCase(
    private val repository: StopwatchRepository,
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(stopwatch: Stopwatch, nowMs: Long) {
        if (stopwatch.state == StopwatchState.RUNNING) {
            val elapsed = nowMs - stopwatch.startTimeMs + stopwatch.accumulatedMs
            val sessionElapsed = nowMs - stopwatch.startTimeMs // Time spent only in this run
            
            repository.updateStopwatchState(
                id = stopwatch.id,
                newState = StopwatchState.PAUSED,
                startTimeMs = 0L,
                accumulatedMs = elapsed
            )
            
            if (sessionElapsed > 0 && stopwatch.linkedTaskIdsJson.isNotBlank() && stopwatch.linkedTaskIdsJson != "[]") {
                try {
                    val jsonArray = org.json.JSONArray(stopwatch.linkedTaskIdsJson)
                    for (i in 0 until jsonArray.length()) {
                        taskRepository.addAccumulatedTime(jsonArray.getLong(i), sessionElapsed)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}

class ResetStopwatchUseCase(
    private val repository: StopwatchRepository,
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(stopwatch: Stopwatch, nowMs: Long) {
        if (stopwatch.state == StopwatchState.RUNNING) {
            val sessionElapsed = nowMs - stopwatch.startTimeMs
            if (sessionElapsed > 0 && stopwatch.linkedTaskIdsJson.isNotBlank() && stopwatch.linkedTaskIdsJson != "[]") {
                try {
                    val jsonArray = org.json.JSONArray(stopwatch.linkedTaskIdsJson)
                    for (i in 0 until jsonArray.length()) {
                        taskRepository.addAccumulatedTime(jsonArray.getLong(i), sessionElapsed)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        repository.updateStopwatchState(
            id = stopwatch.id,
            newState = StopwatchState.IDLE,
            startTimeMs = 0L,
            accumulatedMs = 0L
        )
        repository.resetStopwatch(stopwatch.id)
    }
}

class AddLapUseCase(
    private val repository: StopwatchRepository
) {
    suspend operator fun invoke(stopwatch: Stopwatch, nowMs: Long, title: String? = null) {
        if (stopwatch.state == StopwatchState.RUNNING) {
            val totalTimeMs = stopwatch.computeElapsedMs(nowMs)
            val previousLapTotal = stopwatch.laps.firstOrNull()?.totalTimeMs ?: 0L
            val lapTimeMs = totalTimeMs - previousLapTotal

            repository.addLap(
                stopwatchId = stopwatch.id,
                lapTimeMs = lapTimeMs,
                totalTimeMs = totalTimeMs,
                title = title
            )
        }
    }
}

class SoftDeleteLapUseCase(
    private val repository: StopwatchRepository
) {
    suspend operator fun invoke(lapId: String) {
        repository.softDeleteLap(lapId)
    }
}

class EditLapUseCase(
    private val repository: StopwatchRepository
) {
    suspend operator fun invoke(lapId: String, title: String?, color: Int?) {
        repository.updateLapColorAndTitle(lapId, title, color)
    }
}

class GetDeletedStopwatchesUseCase(private val repository: StopwatchRepository) {
    operator fun invoke(): Flow<List<Stopwatch>> = repository.getDeletedStopwatches()
}

class GetDeletedLapsUseCase(private val repository: StopwatchRepository) {
    operator fun invoke(): Flow<List<Lap>> = repository.getDeletedLaps()
}

class HardDeleteStopwatchUseCase(private val repository: StopwatchRepository) {
    suspend operator fun invoke(id: String) {
        repository.hardDeleteStopwatch(id)
    }
}

class HardDeleteLapUseCase(private val repository: StopwatchRepository) {
    suspend operator fun invoke(id: String) {
        repository.hardDeleteLap(id)
    }
}

class RestoreStopwatchUseCase(
    private val repository: StopwatchRepository
) {
    suspend operator fun invoke(id: String) {
        repository.restoreStopwatch(id)
    }
}

class RestoreLapUseCase(
    private val repository: StopwatchRepository
) {
    suspend operator fun invoke(id: String) {
        repository.restoreLap(id)
    }
}

class UpdateStopwatchNotificationUseCase(
    private val repository: StopwatchRepository
) {
    suspend operator fun invoke(id: String, showInNotifications: Boolean) {
        repository.updateStopwatchNotification(id, showInNotifications)
    }
}

data class StopwatchUseCases(
    val getStopwatches: GetStopwatchesUseCase,
    val addStopwatch: AddStopwatchUseCase,
    val deleteStopwatch: DeleteStopwatchUseCase,
    val renameStopwatch: RenameStopwatchUseCase,
    val updateStopwatchMetadata: UpdateStopwatchMetadataUseCase,
    val startStopwatch: StartStopwatchUseCase,
    val pauseStopwatch: PauseStopwatchUseCase,
    val resetStopwatch: ResetStopwatchUseCase,
    val addLap: AddLapUseCase,
    val editLap: EditLapUseCase,
    val softDeleteLap: SoftDeleteLapUseCase,
    val restoreStopwatch: RestoreStopwatchUseCase,
    val restoreLap: RestoreLapUseCase,
    val getDeletedStopwatches: GetDeletedStopwatchesUseCase,
    val getDeletedLaps: GetDeletedLapsUseCase,
    val hardDeleteStopwatch: HardDeleteStopwatchUseCase,
    val hardDeleteLap: HardDeleteLapUseCase,
    val updateStopwatchNotification: UpdateStopwatchNotificationUseCase
)
