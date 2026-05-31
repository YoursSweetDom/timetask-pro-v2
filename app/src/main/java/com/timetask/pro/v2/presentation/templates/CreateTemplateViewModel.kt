package com.timetask.pro.v2.presentation.templates

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.timetask.pro.v2.data.local.db.entity.FolderEntity
import com.timetask.pro.v2.data.local.db.entity.TemplateEntity
import com.timetask.pro.v2.data.repository.FolderRepository
import com.timetask.pro.v2.data.repository.TemplateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

@Serializable
data class TaskTemplateConfig(
    val title: String = "",
    val description: String = "",
    val quadrant: Int? = null,
    val progress: Int = 0,
    val pinMode: Int = 0
)

data class CreateTemplateState(
    val templateName: String = "",
    val templateDescription: String = "",
    val selectedFolderId: Long? = null,
    val isPinned: Boolean = false,
    val folders: List<FolderEntity> = emptyList()
)

class CreateTemplateViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val templateRepository = TemplateRepository.getInstance(application)
    private val folderRepository = FolderRepository(application)
    
    private val _state = MutableStateFlow(CreateTemplateState())
    val state = combine(_state, folderRepository.getAllFolders()) { currentState, folders ->
        currentState.copy(folders = folders)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CreateTemplateState()
    )

    fun onEvent(event: CreateTemplateEvent) {
        when (event) {
            is CreateTemplateEvent.TemplateNameChanged -> {
                _state.value = _state.value.copy(templateName = event.name)
            }
            is CreateTemplateEvent.TemplateDescriptionChanged -> {
                _state.value = _state.value.copy(templateDescription = event.description)
            }
            is CreateTemplateEvent.FolderSelected -> {
                _state.value = _state.value.copy(selectedFolderId = event.folderId)
            }
            is CreateTemplateEvent.IsPinnedChanged -> {
                _state.value = _state.value.copy(isPinned = event.isPinned)
            }
            is CreateTemplateEvent.SaveTemplate -> {
                viewModelScope.launch {
                    val config = TaskTemplateConfig(
                        title = event.taskTitle,
                        description = event.taskDescription,
                        quadrant = event.quadrant,
                        progress = event.progress,
                        pinMode = event.pinMode
                    )
                    val jsonString = Json.encodeToString(config)
                    
                    templateRepository.createTemplate(
                        name = state.value.templateName.takeIf { it.isNotBlank() } ?: "Новый шаблон",
                        icon = null, // TODO: add icon selection if needed
                        taskConfigJson = jsonString,
                        folderId = state.value.selectedFolderId,
                        isPinned = state.value.isPinned,
                        description = state.value.templateDescription
                    )
                }
            }
        }
    }
}

sealed class CreateTemplateEvent {
    data class TemplateNameChanged(val name: String) : CreateTemplateEvent()
    data class TemplateDescriptionChanged(val description: String) : CreateTemplateEvent()
    data class FolderSelected(val folderId: Long?) : CreateTemplateEvent()
    data class IsPinnedChanged(val isPinned: Boolean) : CreateTemplateEvent()
    data class SaveTemplate(
        val taskTitle: String,
        val taskDescription: String,
        val quadrant: Int?,
        val pinMode: Int,
        val progress: Int
    ) : CreateTemplateEvent()
}
