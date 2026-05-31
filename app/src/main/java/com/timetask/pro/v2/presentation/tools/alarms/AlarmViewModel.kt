package com.timetask.pro.v2.presentation.tools.alarms

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.timetask.pro.v2.data.repository.OfflineFirstAlarmRepository
import com.timetask.pro.v2.data.repository.TagRepository
import com.timetask.pro.v2.data.local.db.entity.TagEntity
import com.timetask.pro.v2.domain.usecase.alarm.AlarmUseCases
import com.timetask.pro.v2.service.alarm.AndroidAlarmScheduler
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val alarmUseCases: AlarmUseCases

    init {
        val repository = OfflineFirstAlarmRepository.getInstance(application)
        val scheduler = AndroidAlarmScheduler(application)
        alarmUseCases = AlarmUseCases(repository, scheduler)
    }

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

    val alarms: StateFlow<ImmutableList<AlarmInstance>> = alarmUseCases.getAllActiveAlarms()
        .map { entities -> 
            entities.map { it.toUiModel() }.toImmutableList() 
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = persistentListOf()
        )

    fun addAlarm(hour: Int, minute: Int, name: String = "Будильник") {
        viewModelScope.launch {
            alarmUseCases.addAlarm(
                hour = hour,
                minute = minute,
                label = name
            )
        }
    }

    fun removeAlarm(id: String) {
        viewModelScope.launch {
            val entity = OfflineFirstAlarmRepository.getInstance(getApplication()).getAlarmById(id)
            if (entity != null) {
                alarmUseCases.deleteAlarm(entity)
            }
        }
    }

    fun toggleAlarm(id: String) {
        viewModelScope.launch {
            val entity = OfflineFirstAlarmRepository.getInstance(getApplication()).getAlarmById(id)
            if (entity != null) {
                alarmUseCases.toggleAlarm(entity, !entity.isEnabled)
            }
        }
    }

    fun toggleRepeatDay(id: String, dayIndex: Int) {
        viewModelScope.launch {
            val entity = OfflineFirstAlarmRepository.getInstance(getApplication()).getAlarmById(id)
            if (entity != null) {
                val bitForDay = 1 shl dayIndex
                val currentMask = entity.repeatDaysMask
                val newMask = if ((currentMask and bitForDay) != 0) {
                    currentMask and bitForDay.inv() // clear bit
                } else {
                    currentMask or bitForDay // set bit
                }
                
                // When modifying repeating days, it recalculates the trigger entirely
                // Usually we toggle it only if it's currently enabled.
                val updated = entity.copy(repeatDaysMask = newMask)
                alarmUseCases.updateAlarm(updated)
            }
        }
    }

    fun renameAlarm(id: String, newName: String) {
        viewModelScope.launch {
            val entity = OfflineFirstAlarmRepository.getInstance(getApplication()).getAlarmById(id)
            if (entity != null) {
                alarmUseCases.updateAlarm(entity.copy(label = newName))
            }
        }
    }

    fun updateAlarmTime(id: String, hour: Int, minute: Int) {
        viewModelScope.launch {
            val entity = OfflineFirstAlarmRepository.getInstance(getApplication()).getAlarmById(id)
            if (entity != null) {
                alarmUseCases.updateAlarm(entity.copy(hour = hour, minute = minute))
            }
        }
    }

    fun addAlarmState(state: CreateAlarmState) {
        viewModelScope.launch {
            val tagIdsJson = org.json.JSONArray(state.selectedTagIds).toString()
            alarmUseCases.addAlarm(
                hour = state.hour,
                minute = state.minute,
                label = state.name.ifBlank { "Будильник" },
                repeatDaysMask = state.repeatDaysMask,
                snoozeDurationMinutes = state.snoozeMinutes,
                snoozeRepeatTimes = state.snoozeRepeatTimes,
                deleteAfterGoOff = state.deleteAfterGoOff,
                colorARGB = state.colorARGB,
                categoryText = state.categoryText,
                tagsText = state.tagsText,
                notesText = state.notesText,
                ringingDurationSec = state.ringingDurationSec,
                autoSnoozeDurationSec = state.autoSnoozeDurationSec,
                tagIdsJson = tagIdsJson,
            )
        }
    }

    fun updateAlarmState(id: String, state: CreateAlarmState) {
        viewModelScope.launch {
            val entity = OfflineFirstAlarmRepository.getInstance(getApplication()).getAlarmById(id)
            if (entity != null) {
                val tagIdsJson = org.json.JSONArray(state.selectedTagIds).toString()
                val updated = entity.copy(
                    hour = state.hour,
                    minute = state.minute,
                    label = state.name.ifBlank { "Будильник" },
                    repeatDaysMask = state.repeatDaysMask,
                    snoozeDurationMinutes = state.snoozeMinutes,
                    snoozeRepeatTimes = state.snoozeRepeatTimes,
                    deleteAfterGoOff = state.deleteAfterGoOff,
                    colorARGB = state.colorARGB,
                    categoryText = state.categoryText,
                    tagsText = state.tagsText,
                    notesText = state.notesText,
                    ringingDurationSec = state.ringingDurationSec,
                    autoSnoozeDurationSec = state.autoSnoozeDurationSec,
                    tagIdsJson = tagIdsJson,
                )
                alarmUseCases.updateAlarm(updated)
            }
        }
    }
}
