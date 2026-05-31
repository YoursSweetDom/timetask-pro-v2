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

data class TemplatesState(
    val templates: List<TemplateEntity> = emptyList(),
    val folders: List<FolderEntity> = emptyList(),
    val templatesByFolder: Map<Long?, List<TemplateEntity>> = emptyMap()
)

class TemplatesViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val templateRepository = TemplateRepository.getInstance(application)
    private val folderRepository = FolderRepository(application)

    private val _state = MutableStateFlow(TemplatesState())
    val state = combine(
        templateRepository.getAllTemplates(),
        folderRepository.getAllFolders()
    ) { templates, folders ->
        TemplatesState(
            templates = templates,
            folders = folders,
            templatesByFolder = templates.groupBy { it.folderId }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TemplatesState()
    )

    fun onEvent(event: TemplatesEvent) {
        when (event) {
            is TemplatesEvent.ReorderTemplates -> {
                viewModelScope.launch {
                    templateRepository.reorderTemplates(event.templates)
                }
            }
            is TemplatesEvent.DeleteTemplate -> {
                viewModelScope.launch {
                    templateRepository.deleteTemplate(event.template)
                }
            }
            is TemplatesEvent.PinTemplate -> {
                viewModelScope.launch {
                    templateRepository.updateTemplate(event.template.copy(isPinned = !event.template.isPinned))
                }
            }
            is TemplatesEvent.MoveToFolder -> {
                viewModelScope.launch {
                    templateRepository.updateTemplate(event.template.copy(folderId = event.folderId))
                }
            }
            is TemplatesEvent.UpdateTemplate -> {
                viewModelScope.launch {
                    templateRepository.updateTemplate(event.template)
                }
            }
        }
    }
}

sealed class TemplatesEvent {
    data class ReorderTemplates(val templates: List<TemplateEntity>) : TemplatesEvent()
    data class DeleteTemplate(val template: TemplateEntity) : TemplatesEvent()
    data class PinTemplate(val template: TemplateEntity) : TemplatesEvent()
    data class MoveToFolder(val template: TemplateEntity, val folderId: Long?) : TemplatesEvent()
    data class UpdateTemplate(val template: TemplateEntity) : TemplatesEvent()
}
