package com.timetask.pro.v2.presentation.tasks.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardReturn
import androidx.compose.material.icons.filled.Delete
import com.timetask.pro.v2.data.local.db.entity.TaskEntity
import com.timetask.pro.v2.data.local.db.entity.TaskStatus
import com.timetask.pro.v2.presentation.tasks.TasksViewModel
import com.timetask.pro.v2.ui.theme.Spacing
import com.timetask.pro.v2.ui.theme.TitaniumSuccess
import kotlinx.coroutines.launch

/**
 * Compact subtask row rendered below expanded parent task.
 * Lighter background, indented, no swipe actions, no drag handle.
 */
@Composable
fun SubtaskRow(
    subtask: TaskEntity,
    viewModel: TasksViewModel,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDone = subtask.status == com.timetask.pro.v2.data.local.db.entity.TaskStatus.DONE
    val onToggle = remember(subtask.id) { { viewModel.toggleTask(subtask) } }
    val onOutdent = remember(subtask.id) { { viewModel.removeFromParent(subtask) } }
    
    val snackbarHostState = com.timetask.pro.v2.LocalSnackbarHostState.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    
    val onDelete = remember<() -> Unit>(subtask.id) { 
        { 
            viewModel.softDeleteTaskWithUndo(subtask) 
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "Подзадача удалена",
                    actionLabel = "Отмена",
                    duration = androidx.compose.material3.SnackbarDuration.Short
                )
                if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                    viewModel.undoDeleteTask(subtask.id)
                }
            }
            Unit
        } 
    }

    CustomSwipeToReveal(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, bottom = Spacing.xs), // indent + spacing
        leftMenuWidth = 60.dp, // Only one action (Outdent)
        rightMenuWidth = 60.dp, // Only one action (Delete)
        leftMenuContent = {
            androidx.compose.material3.IconButton(
                onClick = onOutdent,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                    .background(com.timetask.pro.v2.ui.theme.TitaniumPrimary.copy(alpha = 0.2f))
            ) {
                androidx.compose.material3.Icon(
                    androidx.compose.material.icons.Icons.Default.KeyboardReturn, 
                    "Отвязать", 
                    tint = com.timetask.pro.v2.ui.theme.TitaniumPrimary
                )
            }
        },
        rightMenuContent = {
            androidx.compose.material3.IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                    .background(com.timetask.pro.v2.ui.theme.TitaniumError.copy(alpha = 0.2f))
            ) {
                androidx.compose.material3.Icon(
                    androidx.compose.material.icons.Icons.Default.Delete, 
                    "Удалить", 
                    tint = com.timetask.pro.v2.ui.theme.TitaniumError
                )
            }
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .clickable(onClick = onEditClick)
                .padding(vertical = 4.dp, horizontal = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(Modifier.width(Spacing.xs))
            
            Checkbox(
                checked = isDone,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = com.timetask.pro.v2.ui.theme.TitaniumSuccess,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    checkmarkColor = MaterialTheme.colorScheme.surface,
                ),
                modifier = Modifier.size(36.dp)
            )

            Text(
                text = subtask.title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDone) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onBackground
                },
                textDecoration = if (isDone) TextDecoration.LineThrough else null,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
