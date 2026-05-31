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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timetask.pro.v2.data.local.db.entity.TaskEntity
import com.timetask.pro.v2.presentation.tasks.TasksViewModel
import com.timetask.pro.v2.presentation.util.bottomSheetFlingToClose
import com.timetask.pro.v2.ui.theme.Spacing
import com.timetask.pro.v2.presentation.components.tags.TagSelectionSheet
import com.timetask.pro.v2.data.local.db.entity.TagEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskSheet(
    task: TaskEntity,
    viewModel: TasksViewModel? = null,
    availableTags: List<TagEntity> = emptyList(),
    onCreateTag: (String) -> Unit = {},
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, quadrant: Int?, pinMode: Int, progress: Int, tags: List<TagEntity>) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()
    val snackbarHostState = com.timetask.pro.v2.LocalSnackbarHostState.current

    // Collect subtasks reactively (only if viewModel provided)
    val subtasks by (viewModel?.getSubtasks(task.id)
        ?.collectAsStateWithLifecycle(initialValue = emptyList())
        ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(emptyList()) })

    val allTasks by (viewModel?.taskGroups?.collectAsStateWithLifecycle()
        ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(kotlinx.collections.immutable.persistentListOf()) })
    var showPicker by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    
    // Load existing tags for this task from the DB
    val existingTags by (viewModel?.getTagsForTask(task.id)
        ?.collectAsStateWithLifecycle(initialValue = emptyList())
        ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(emptyList()) })
    
    var showTagSheet by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    // Track user overrides: null means "use DB tags", non-null means user changed them
    var userSelectedTags by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<List<TagEntity>?>(null) }
    val selectedTags = userSelectedTags ?: existingTags

    val availableTasksForPicker = androidx.compose.runtime.remember(allTasks, subtasks, task.id) {
        val flatActiveTasks = allTasks.flatMap { it.activeTasks }
        flatActiveTasks.filter { it.id != task.id && !subtasks.any { sub -> sub.id == it.id } }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        TaskFormContent(
            sheetTitle = "Редактировать задачу",
            confirmButtonText = "Сохранить",
            initialTitle = task.title,
            initialDescription = task.description,
            initialQuadrant = task.quadrant,
            initialPinMode = task.pinMode,
            initialProgress = task.progressPercent,
            selectedTags = selectedTags,
            onAddTagClick = { showTagSheet = true },
            subtasks = subtasks,
            parentTaskId = task.parentId,
            onOutdent = viewModel?.let { vm -> {
                vm.removeFromParent(task)
                onDismiss()
            } },
            onAddSubtask = viewModel?.let { vm -> { title: String -> vm.addSubtask(title, task.id) } },
            onPickerSubtaskClick = viewModel?.let { { showPicker = true } },
            onToggleSubtask = viewModel?.let { vm -> { subtask: TaskEntity -> vm.toggleTask(subtask) } },
            onDeleteSubtask = viewModel?.let { vm -> { subtask: TaskEntity -> 
                vm.softDeleteTaskWithUndo(subtask)
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = "Подзадача удалена",
                        actionLabel = "Отмена",
                        duration = androidx.compose.material3.SnackbarDuration.Short
                    )
                    if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                        vm.undoDeleteTask(subtask.id)
                    }
                }
            } },
            onDismiss = onDismiss,
            onConfirm = { t, d, q, p, pr -> onSave(t, d, q, p, pr, selectedTags) },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .bottomSheetFlingToClose(sheetState, scope, onDismiss)
                .padding(horizontal = Spacing.md)
                .navigationBarsPadding()
        )
    }

    if (showPicker) {
        TaskPickerSheet(
            tasks = availableTasksForPicker,
            onDismiss = { showPicker = false },
            onTaskSelected = { selectedTask ->
                viewModel?.makeSubtask(selectedTask, task)
            }
        )
    }

    if (showTagSheet) {
        TagSelectionSheet(
            allTags = availableTags,
            selectedTagIds = selectedTags.map { it.id }.toSet(),
            onTagToggled = { tag ->
                val current = selectedTags
                userSelectedTags = if (current.any { it.id == tag.id }) {
                    current.filter { it.id != tag.id }
                } else {
                    current + tag
                }
            },
            onCreateNewTag = onCreateTag,
            onDismissRequest = { showTagSheet = false }
        )
    }
}
