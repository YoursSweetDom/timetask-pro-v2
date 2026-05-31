package com.timetask.pro.v2.presentation.tasks.components

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.timetask.pro.v2.data.local.db.entity.Priority
import com.timetask.pro.v2.data.local.db.entity.TagEntity
import com.timetask.pro.v2.presentation.components.tags.TagSelectionSheet
import com.timetask.pro.v2.presentation.util.bottomSheetFlingToClose
import com.timetask.pro.v2.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskSheet(
    initialTitle: String = "",
    initialDescription: String = "",
    initialQuadrant: Int? = null,
    initialPinMode: Int = 0,
    initialProgress: Int = 0,
    availableTags: List<TagEntity> = emptyList(),
    onCreateTag: (String) -> Unit = {},
    onShowTemplatePicker: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    onAdd: (title: String, description: String, quadrant: Int?, pinMode: Int, progress: Int, tags: List<TagEntity>) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()
    var showTagSheet by remember { mutableStateOf(false) }
    var selectedTags by remember { mutableStateOf(emptyList<TagEntity>()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        TaskFormContent(
            sheetTitle = "Новая задача",
            confirmButtonText = "Добавить",
            initialTitle = initialTitle,
            initialDescription = initialDescription,
            initialQuadrant = initialQuadrant,
            initialPinMode = initialPinMode,
            initialProgress = initialProgress,
            selectedTags = selectedTags,
            onAddTagClick = { showTagSheet = true },
            onTemplateClick = onShowTemplatePicker,
            onDismiss = onDismiss,
            onConfirm = { t, d, q, p, pr -> onAdd(t, d, q, p, pr, selectedTags) },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f) // Force tall content to enable Half-to-Fullscreen physics
                .bottomSheetFlingToClose(sheetState, scope, onDismiss)
                .padding(horizontal = Spacing.md)
                .navigationBarsPadding()
        )
    }

    if (showTagSheet) {
        TagSelectionSheet(
            allTags = availableTags,
            selectedTagIds = selectedTags.map { it.id }.toSet(),
            onTagToggled = { tag ->
                selectedTags = if (selectedTags.any { it.id == tag.id }) {
                    selectedTags.filter { it.id != tag.id }
                } else {
                    selectedTags + tag
                }
            },
            onCreateNewTag = onCreateTag,
            onDismissRequest = { showTagSheet = false }
        )
    }
}
