package com.timetask.pro.v2.presentation.tasks.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.timetask.pro.v2.data.local.db.entity.TaskEntity
import com.timetask.pro.v2.presentation.tasks.TasksViewModel
import com.timetask.pro.v2.ui.theme.Spacing

/**
 * BottomSheet для выбора одной или нескольких задач.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskPickerSheet(
    tasks: List<TaskEntity>? = null,
    initialSelectedTaskIds: List<Long> = emptyList(),
    onDismiss: () -> Unit,
    onTaskSelected: ((TaskEntity) -> Unit)? = null,
    onTasksSelected: ((List<Long>) -> Unit)? = null,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    // Resolve tasks from ViewModel if not provided explicitly
    val resolvedTasks = tasks ?: run {
        val vm: TasksViewModel = viewModel()
        val groups by vm.taskGroups.collectAsStateWithLifecycle()
        groups.flatMap { it.activeTasks }
    }

    var searchQuery by remember { mutableStateOf("") }
    
    // Multi-select state
    val isMultiSelect = onTasksSelected != null
    var selectedIds by remember(initialSelectedTaskIds) { mutableStateOf(initialSelectedTaskIds.toSet()) }

    val filteredTasks = remember(resolvedTasks, searchQuery) {
        if (searchQuery.isBlank()) {
            resolvedTasks
        } else {
            resolvedTasks.filter { it.title.contains(searchQuery, ignoreCase = true) }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f) // Занимаем 80% экрана
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isMultiSelect) "Выбор задач" else "Выбрать задачу",
                    style = MaterialTheme.typography.titleMedium,
                )
                if (isMultiSelect) {
                    TextButton(onClick = {
                        onTasksSelected?.invoke(selectedIds.toList())
                        onDismiss()
                    }) {
                        Text("Готово")
                    }
                }
            }

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Поиск задач...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Поиск") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Очистить")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                ),
                shape = MaterialTheme.shapes.medium
            )

            Spacer(Modifier.height(Spacing.sm))

            if (filteredTasks.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (searchQuery.isBlank()) "Нет доступных задач" else "Ничего не найдено",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        val isSelected = selectedIds.contains(task.id)
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = task.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            supportingContent = if (task.description.isNotBlank()) {
                                {
                                    Text(
                                        text = task.description,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            } else null,
                            leadingContent = {
                                Icon(
                                    Icons.AutoMirrored.Filled.Assignment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingContent = if (isMultiSelect) {
                                {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = null // handled by row click
                                    )
                                }
                            } else null,
                            modifier = Modifier.clickable {
                                if (isMultiSelect) {
                                    selectedIds = if (isSelected) {
                                        selectedIds - task.id
                                    } else {
                                        selectedIds + task.id
                                    }
                                } else {
                                    onTaskSelected?.invoke(task)
                                    onDismiss()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
