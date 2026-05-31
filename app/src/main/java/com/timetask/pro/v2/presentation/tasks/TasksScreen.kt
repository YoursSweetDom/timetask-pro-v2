package com.timetask.pro.v2.presentation.tasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timetask.pro.v2.data.local.db.entity.FolderEntity
import com.timetask.pro.v2.data.local.db.entity.Priority
import com.timetask.pro.v2.data.local.db.entity.TagEntity
import com.timetask.pro.v2.data.local.db.entity.TaskEntity
import com.timetask.pro.v2.presentation.tasks.components.AddTaskSheet
import com.timetask.pro.v2.presentation.tasks.components.CreateToolChoiceSheet
import com.timetask.pro.v2.presentation.tasks.components.DateActionSheet
import com.timetask.pro.v2.presentation.tasks.components.MoveToFolderSheet
import com.timetask.pro.v2.presentation.tasks.components.QuickAddBar
import com.timetask.pro.v2.presentation.tasks.components.TaskItem
import com.timetask.pro.v2.presentation.templates.TemplatePickerSheet
import com.timetask.pro.v2.presentation.templates.TemplatesViewModel
import com.timetask.pro.v2.presentation.templates.TaskTemplateConfig
import com.timetask.pro.v2.data.local.db.entity.TemplateEntity
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timetask.pro.v2.presentation.tools.timers.CreateTimerSheet
import com.timetask.pro.v2.presentation.tasks.components.SubtaskRow
import com.timetask.pro.v2.presentation.tasks.components.TaskItem
import com.timetask.pro.v2.presentation.tasks.components.TaskListItem
import com.timetask.pro.v2.ui.theme.Spacing
import com.timetask.pro.v2.ui.theme.TitaniumPrimary
import com.timetask.pro.v2.domain.model.tasks.TaskGroup
import com.timetask.pro.v2.domain.model.tasks.TasksGroupingOrder
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import com.timetask.pro.v2.presentation.tasks.components.SwipeManager
import androidx.compose.material.icons.filled.MoreVert
import com.timetask.pro.v2.presentation.util.LocalTopBarState
import androidx.compose.runtime.DisposableEffect
import com.timetask.pro.v2.presentation.tasks.components.TasksMoreMenuSheet
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun TasksScreen(
    viewModel: TasksViewModel,
    folders: ImmutableList<FolderEntity> = persistentListOf(),
    onTaskClick: (Long) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val taskGroups by viewModel.taskGroups.collectAsStateWithLifecycle()
    val showAddSheet by viewModel.showAddSheet.collectAsStateWithLifecycle()
    val showCompleted by viewModel.showCompleted.collectAsStateWithLifecycle()
    val viewPreferences by viewModel.tasksViewPreferences.collectAsStateWithLifecycle()
    val tags by viewModel.tags.collectAsStateWithLifecycle()
    val snackbarHostState = com.timetask.pro.v2.LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()

    var showMoreMenuSheet by remember { androidx.compose.runtime.mutableStateOf(false) }
    var isMultiSelectMode by remember { androidx.compose.runtime.mutableStateOf(false) }
    var selectedTaskIds by remember { androidx.compose.runtime.mutableStateOf(emptySet<Long>()) }

    val currentFilterTitle by viewModel.currentFilterTitle.collectAsStateWithLifecycle()

    val topBarState = LocalTopBarState.current
    DisposableEffect(isMultiSelectMode, selectedTaskIds.size) {
        if (isMultiSelectMode) {
            topBarState.title = { Text("Выбрано: ${selectedTaskIds.size}", style = MaterialTheme.typography.titleMedium) }
            topBarState.navigationIcon = {
                androidx.compose.material3.IconButton(onClick = { 
                    isMultiSelectMode = false
                    selectedTaskIds = emptySet()
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Отмена")
                }
            }
            topBarState.actions = {
                androidx.compose.material3.IconButton(onClick = {
                    val allIds = taskGroups.flatMap { group -> group.activeTasks.map { it.id } + group.completedTasks.map { it.id } }.toSet()
                    if (selectedTaskIds.size == allIds.size) {
                        selectedTaskIds = emptySet()
                    } else {
                        selectedTaskIds = allIds
                    }
                }) {
                    Icon(Icons.Default.Checklist, contentDescription = "Выбрать все")
                }
            }
        } else {
            topBarState.title = null
            topBarState.navigationIcon = null
            topBarState.actions = {
                androidx.compose.material3.IconButton(onClick = { showMoreMenuSheet = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Ещё")
                }
            }
        }
        onDispose {}
    }

    // Flatten TaskGroups into a single list for LazyColumn and Reordering
    val flattenedItems = remember(taskGroups, showCompleted, viewPreferences, tags) {
        val items = mutableListOf<TaskListItem>()
        val tagMap = tags.associateBy { it.id }
        
        taskGroups.forEachIndexed { groupIndex, group ->
            if (groupIndex > 0) items.add(TaskListItem.Spacing())
            items.add(TaskListItem.Header(group))
            
            val hideComp = viewPreferences.hideCompleted
            val activeCount = group.activeTasks.size
            val hasCompleted = !hideComp && group.completedTasks.isNotEmpty()
            
            group.activeTasks.forEachIndexed { index, task ->
                val isLast = index == activeCount - 1 && !hasCompleted
                val isFirst = index == 0
                val taskTags = if (task.tagIds != "[]" && task.tagIds.isNotBlank()) {
                    task.tagIds.trim('[', ']').split(",").mapNotNull { it.trim().toLongOrNull() }.mapNotNull { tagMap[it] }
                } else emptyList()
                items.add(TaskListItem.ActiveTask(task, group, taskTags, isFirst, isLast))
            }
            
            if (hasCompleted) {
                val isHeaderLast = !showCompleted
                items.add(TaskListItem.CompletedHeader(group, isHeaderLast))
                if (showCompleted) {
                    val compCount = group.completedTasks.size
                    group.completedTasks.forEachIndexed { index, task ->
                        val isLast = index == compCount - 1
                        val taskTags = if (task.tagIds != "[]" && task.tagIds.isNotBlank()) {
                            task.tagIds.trim('[', ']').split(",").mapNotNull { it.trim().toLongOrNull() }.mapNotNull { tagMap[it] }
                        } else emptyList()
                        items.add(TaskListItem.CompletedTask(task, group, taskTags, isLast))
                    }
                }
            }
        }
        items
    }

    var reorderedItems by remember(flattenedItems) { 
        androidx.compose.runtime.mutableStateOf(flattenedItems.toList()) 
    }
    
    // Template states
    var showTemplatePicker by remember { androidx.compose.runtime.mutableStateOf(false) }
    var selectedTemplateConfig by remember { androidx.compose.runtime.mutableStateOf<TaskTemplateConfig?>(null) }

    // Expand/collapse state for subtasks
    var expandedTaskIds by remember { androidx.compose.runtime.mutableStateOf(emptySet<Long>()) }

    val lazyListState = rememberLazyListState()
    val hapticFeedback = androidx.compose.ui.platform.LocalHapticFeedback.current
    val reorderableLazyListState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            reorderedItems = reorderedItems.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
            hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
        }
    )

    var pendingSubtaskParentId by remember { androidx.compose.runtime.mutableStateOf<Long?>(null) }
    var draggingTaskId by remember { androidx.compose.runtime.mutableStateOf<Long?>(null) }
    val draggingIndex = draggingTaskId?.let { id -> reorderedItems.indexOfFirst { it is TaskListItem.ActiveTask && it.task.id == id } }?.takeIf { it >= 0 }

    androidx.compose.runtime.LaunchedEffect(draggingIndex) {
        if (draggingIndex != null) {
            pendingSubtaskParentId = null
            if (draggingIndex > 0) {
                kotlinx.coroutines.delay(800)
                val parentItem = reorderedItems.getOrNull(draggingIndex - 1) as? TaskListItem.ActiveTask
                if (parentItem != null) {
                    pendingSubtaskParentId = parentItem.task.id
                    hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                }
            }
        }
    }

    // Memoized callbacks
    val onQuickAdd = remember<(String) -> Unit> { { viewModel.quickAddTask(it) } }
    val onShowAddSheet = remember { { viewModel.showAddSheet() } }
    val onHideAddSheet = remember { { viewModel.hideAddSheet() } }
    val onToggleShowCompleted = remember { { viewModel.toggleShowCompleted() } }
    val onAddTask = remember<(String, String, Int?, Int, Int, List<TagEntity>) -> Unit> {
        { title, desc, quadrant, pinMode, progress, taskTags -> 
            viewModel.addTask(title, desc, quadrant, pinMode, progress, taskTags) 
        }
    }

    // State for opening Tool Choice Sheet from Task
    var showToolChoiceSheetForTask by remember { androidx.compose.runtime.mutableStateOf<TaskEntity?>(null) }
    var showTimerSheetForTask by remember { androidx.compose.runtime.mutableStateOf<TaskEntity?>(null) }
    var showDateSheetForTask by remember { androidx.compose.runtime.mutableStateOf<TaskEntity?>(null) }
    var showMoveSheetForTask by remember { androidx.compose.runtime.mutableStateOf<TaskEntity?>(null) }
    var showEditSheetForTask by remember { androidx.compose.runtime.mutableStateOf<TaskEntity?>(null) }
    
    // User presets for timer sheet (if TasksViewModel can provide them, else empty list for now)
    // We'll pass an empty list for now and let the user add standard timers.

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (isMultiSelectMode) {
                androidx.compose.material3.BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
                ) {
                    androidx.compose.material3.IconButton(onClick = { 
                        // TODO: Implement batch move
                    }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.DriveFileMove, "Переместить")
                    }
                    androidx.compose.material3.IconButton(onClick = { 
                        // TODO: Implement batch date change
                    }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.CalendarMonth, "Дата")
                    }
                    androidx.compose.material3.IconButton(onClick = { 
                        val tasksToDelete = taskGroups.flatMap { group -> group.activeTasks + group.completedTasks }.filter { selectedTaskIds.contains(it.id) }
                        val count = tasksToDelete.size
                        tasksToDelete.forEach { viewModel.softDeleteTaskWithUndo(it) }
                        isMultiSelectMode = false
                        selectedTaskIds = emptySet()
                        
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "Удалено задач: $count",
                                actionLabel = "Отмена",
                                duration = androidx.compose.material3.SnackbarDuration.Short
                            )
                            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                                tasksToDelete.forEach { viewModel.undoDeleteTask(it.id) }
                            }
                        }
                    }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Delete, "Удалить", tint = com.timetask.pro.v2.ui.theme.TitaniumError)
                    }
                }
            }
        },
        floatingActionButton = {
            if (!isMultiSelectMode) {
                FloatingActionButton(
                    onClick = onShowAddSheet,
                    containerColor = TitaniumPrimary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(Icons.Filled.Add, "Добавить задачу")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) {
                    detectTapGestures {
                        // Закрываем все свайпы при тапе в любое место
                        SwipeManager.closeAll()
                    }
                },
        ) {
            // Quick add bar
            QuickAddBar(
                onAdd = onQuickAdd,
            )

            if (taskGroups.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Нет задач",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = Spacing.sm),
                ) {
                    itemsIndexed(
                        items = reorderedItems,
                        key = { _, item -> item.key }
                    ) { index, item ->
                        when(item) {
                            is TaskListItem.Spacing -> {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            is TaskListItem.Header -> {
                                androidx.compose.foundation.layout.Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (item.group.groupIcon != null) {
                                        Text(text = item.group.groupIcon ?: "", modifier = Modifier.padding(end = 8.dp))
                                    }
                                    Text(
                                        text = item.group.title, 
                                        style = MaterialTheme.typography.titleMedium,
                                        color = item.group.groupColor?.let { Color(android.graphics.Color.parseColor(it)) } ?: MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.weight(1f))
                                    Text(
                                        text = "${item.group.activeTasks.size}", 
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            is TaskListItem.CompletedHeader -> {
                                val isBottomRounded = item.isLast
                                TextButton(
                                    onClick = onToggleShowCompleted,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(
                                            if (isBottomRounded) RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                                            else RectangleShape
                                        )
                                        .background(MaterialTheme.colorScheme.surface)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Text(
                                        text = " Выполнено (${item.group.completedTasks.size})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(start = Spacing.xs),
                                    )
                                }
                            }
                            is TaskListItem.CompletedTask -> {
                                val onToggle = remember(item.task.id) { { viewModel.toggleTask(item.task) } }
                                val onPin = remember(item.task.id) { { viewModel.pinTask(item.task) } }
                                val onDelete = remember<() -> Unit>(item.task.id) { 
                                    { 
                                        viewModel.softDeleteTaskWithUndo(item.task)
                                        scope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = "Задача удалена",
                                                actionLabel = "Отмена",
                                                duration = androidx.compose.material3.SnackbarDuration.Short
                                            )
                                            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                                                viewModel.undoDeleteTask(item.task.id)
                                            }
                                        }
                                        Unit
                                    } 
                                }
                                
                                AnimatedVisibility(visible = true, enter = expandVertically(), exit = shrinkVertically()) {
                                    Box(modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(
                                            if (item.isLast) RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp) 
                                            else RectangleShape
                                        )
                                        .background(MaterialTheme.colorScheme.surface)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                            TaskItem(
                                                task = item.task,
                                                onToggle = onToggle,
                                                onDelete = onDelete,
                                                onPin = onPin,
                                                onMove = { showMoveSheetForTask = item.task },
                                                onDateClick = { showDateSheetForTask = item.task },
                                                onToolsClick = { showToolChoiceSheetForTask = item.task },
                                                onClick = {
                                                    if (isMultiSelectMode) {
                                                        if (selectedTaskIds.contains(item.task.id)) selectedTaskIds -= item.task.id else selectedTaskIds += item.task.id
                                                    } else {
                                                        showEditSheetForTask = item.task
                                                    }
                                                },
                                                groupName = item.group.title,
                                                groupColor = item.group.groupColor,
                                                tags = item.tags,
                                                checkboxColorMode = viewPreferences.checkboxColorMode,
                                                isMultiSelectMode = isMultiSelectMode,
                                                isSelected = selectedTaskIds.contains(item.task.id)
                                            )
                                        }
                                    }
                                }
                            is TaskListItem.ActiveTask -> {
                                ReorderableItem(
                                    state = reorderableLazyListState,
                                    key = item.key
                                ) { isDragging ->
                                    val task = item.task
                                    val onToggle = remember(task.id) { { viewModel.toggleTask(task) } }
                                    val onDelete = remember<() -> Unit>(task.id) { 
                                        { 
                                            viewModel.softDeleteTaskWithUndo(task)
                                            scope.launch {
                                                val result = snackbarHostState.showSnackbar(
                                                    message = "Задача удалена",
                                                    actionLabel = "Отмена",
                                                    duration = androidx.compose.material3.SnackbarDuration.Short
                                                )
                                                if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                                                    viewModel.undoDeleteTask(task.id)
                                                }
                                            }
                                            Unit
                                        } 
                                    }
                                    val onPin = remember(task.id) { { viewModel.pinTask(task) } }
                                    
                                    val subtaskCount by viewModel.getSubtaskCount(task.id).collectAsStateWithLifecycle(initialValue = 0)
                                    val completedSubtaskCount by viewModel.getCompletedSubtaskCount(task.id).collectAsStateWithLifecycle(initialValue = 0)
                                    val isExpanded = expandedTaskIds.contains(task.id)

                                    val elevation by androidx.compose.animation.core.animateDpAsState(if (isDragging) 8.dp else 0.dp, label = "dragLevel")
                                    
                                    LaunchedEffect(isDragging) {
                                        if (isDragging) {
                                            draggingTaskId = task.id
                                        } else {
                                            if (draggingTaskId == task.id) draggingTaskId = null
                                            
                                            // Extract active tasks for reordering update
                                            val currentGroupTasks = reorderedItems.filterIsInstance<TaskListItem.ActiveTask>().map { it.task }
                                            
                                            if (pendingSubtaskParentId != null) {
                                                val parentId = pendingSubtaskParentId
                                                pendingSubtaskParentId = null
                                                viewModel.reorderTasks(currentGroupTasks)
                                                val parentTask = currentGroupTasks.find { it.id == parentId }
                                                if (parentTask != null) viewModel.makeSubtask(task, parentTask)
                                            } else {
                                                // Handle Cross-Group Drop
                                                val currentIndex = reorderedItems.indexOfFirst { it is TaskListItem.ActiveTask && it.task.id == task.id }
                                                if (currentIndex >= 0) {
                                                    // Find which group we are inside now by looking upwards for a Header
                                                    var newGroup: TaskGroup? = null
                                                    for (i in currentIndex downTo 0) {
                                                        val overItem = reorderedItems[i]
                                                        if (overItem is TaskListItem.Header) {
                                                            newGroup = overItem.group
                                                            break
                                                        } else if (overItem is TaskListItem.ActiveTask && overItem.task.id != task.id) {
                                                            newGroup = overItem.group
                                                            break
                                                        }
                                                    }
                                                    
                                                    if (newGroup != null && newGroup.id != item.group.id) {
                                                        // Item was moved to a new group! Apply properties
                                                        when (viewPreferences.groupingOrder) {
                                                            TasksGroupingOrder.LIST -> viewModel.moveTaskToFolder(task, if (newGroup.id == "inbox") null else newGroup.id.toLongOrNull())
                                                            TasksGroupingOrder.PRIORITY -> {
                                                                val priority = when (newGroup.id) {
                                                                    "high" -> Priority.HIGH
                                                                    "medium" -> Priority.MEDIUM
                                                                    "low" -> Priority.LOW
                                                                    else -> Priority.NONE
                                                                }
                                                                viewModel.updateTaskPriority(task, priority)
                                                            }
                                                            TasksGroupingOrder.DATE -> {
                                                                val newDate = when (newGroup.id) {
                                                                    "today" -> com.timetask.pro.v2.util.DateUtils.startOfToday() + 86400000L - 1 // End of Today
                                                                    "tomorrow" -> com.timetask.pro.v2.util.DateUtils.startOfTomorrow() + 86400000L - 1 // End of Tomorrow
                                                                    "next7d" -> com.timetask.pro.v2.util.DateUtils.startOfToday() + (7 * 86400000L) - 1
                                                                    "later" -> com.timetask.pro.v2.util.DateUtils.startOfToday() + (30L * 86400000L) - 1
                                                                    "no_date" -> null
                                                                    else -> task.dueDate // ignore dropping into overdue
                                                                }
                                                                // Don't modify time if user drops into overdue
                                                                if (newGroup.id != "overdue") viewModel.updateTaskDate(task, newDate)
                                                            }
                                                            TasksGroupingOrder.TAG -> {
                                                                val oldTagId = if (item.group.id == "no_tag") null else item.group.id.toLongOrNull()
                                                                val newTagId = if (newGroup.id == "no_tag") null else newGroup.id.toLongOrNull()
                                                                viewModel.updateTaskTag(task, oldTagId, newTagId)
                                                            }
                                                            else -> {}
                                                        }
                                                    }
                                                }
                                                viewModel.reorderTasks(currentGroupTasks)
                                            }
                                        }
                                    }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(
                                                if (item.isLast && !isDragging) RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp) 
                                                else RectangleShape
                                            )
                                            .background(MaterialTheme.colorScheme.surface)
                                            .padding(horizontal = 8.dp)
                                    ) {
                                        androidx.compose.material3.Surface(
                                            modifier = Modifier.longPressDraggableHandle(
                                                onDragStarted = { hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress) },
                                                onDragStopped = { hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove) },
                                            ),
                                            shadowElevation = elevation,
                                            color = Color.Transparent
                                        ) {
                                            TaskItem(
                                                task = task,
                                                onToggle = onToggle,
                                                onDelete = onDelete,
                                                onPin = onPin,
                                                onMove = { showMoveSheetForTask = task },
                                                onDateClick = { showDateSheetForTask = task },
                                                onToolsClick = { showToolChoiceSheetForTask = task },
                                                onClick = {
                                                    if (isMultiSelectMode) {
                                                        if (selectedTaskIds.contains(task.id)) selectedTaskIds = selectedTaskIds - task.id else selectedTaskIds = selectedTaskIds + task.id
                                                    } else {
                                                        showEditSheetForTask = task
                                                    }
                                                },
                                                isDragging = isDragging,
                                                subtaskCount = subtaskCount,
                                                completedSubtaskCount = completedSubtaskCount,
                                                isExpanded = isExpanded,
                                                isDropTarget = pendingSubtaskParentId == task.id,
                                                onExpandToggle = {
                                                    expandedTaskIds = if (isExpanded) expandedTaskIds - task.id else expandedTaskIds + task.id
                                                },
                                                hideSubtasks = viewPreferences.hideSubtasks,
                                                groupName = item.group.title,
                                                groupColor = item.group.groupColor,
                                                tags = item.tags,
                                                checkboxColorMode = viewPreferences.checkboxColorMode,
                                                isMultiSelectMode = isMultiSelectMode,
                                                isSelected = selectedTaskIds.contains(task.id)
                                            )
                                        }

                                        if (!viewPreferences.hideSubtasks && isExpanded && subtaskCount > 0) {
                                            val subtasks by viewModel.getSubtasks(task.id).collectAsStateWithLifecycle(initialValue = emptyList())
                                            subtasks.forEach { subtask ->
                                                SubtaskRow(subtask = subtask, viewModel = viewModel, onEditClick = { showEditSheetForTask = subtask })
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Bottom Sheet: Tool Choice (Timer or Stopwatch)
    showToolChoiceSheetForTask?.let { task ->
        CreateToolChoiceSheet(
            task = task,
            onDismiss = { showToolChoiceSheetForTask = null },
            onCreateTimer = {
                showToolChoiceSheetForTask = null
                showTimerSheetForTask = task
            },
            onCreateStopwatch = {
                viewModel.createStopwatchFromTask(task)
                showToolChoiceSheetForTask = null
            }
        )
    }

    // Bottom Sheet: Add Task
    if (showAddSheet) {
        AddTaskSheet(
            initialTitle = selectedTemplateConfig?.title ?: "",
            initialDescription = selectedTemplateConfig?.description ?: "",
            initialQuadrant = selectedTemplateConfig?.quadrant,
            initialPinMode = selectedTemplateConfig?.pinMode ?: 0,
            initialProgress = selectedTemplateConfig?.progress ?: 0,
            availableTags = tags,
            onCreateTag = { viewModel.addTag(it, color = "#8A8A8E") },
            onShowTemplatePicker = { showTemplatePicker = true },
            onDismiss = { 
                onHideAddSheet()
                selectedTemplateConfig = null 
            },
            onAdd = { title, description, quadrant, pinMode, progress, taskTags ->
                onAddTask(title, description, quadrant, pinMode, progress, taskTags)
                selectedTemplateConfig = null
            },
        )
    }

    // Bottom Sheet: Template Picker
    if (showTemplatePicker) {
        val templatesViewModel: TemplatesViewModel = viewModel()
        TemplatePickerSheet(
            viewModel = templatesViewModel,
            onTemplateSelected = { template ->
                try {
                    selectedTemplateConfig = Json.decodeFromString<TaskTemplateConfig>(template.taskConfigJson)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            onDismiss = { showTemplatePicker = false }
        )
    }

    // Bottom Sheet: Edit Task
    showEditSheetForTask?.let { task ->
        com.timetask.pro.v2.presentation.tasks.components.EditTaskSheet(
            task = task,
            viewModel = viewModel,
            availableTags = tags,
            onCreateTag = { viewModel.addTag(it, color = "#8A8A8E") },
            onDismiss = { showEditSheetForTask = null },
            onSave = { title, description, quadrant, pinMode, progress, taskTags ->
                viewModel.updateTask(
                    task = task.copy(
                        title = title,
                        description = description,
                        quadrant = quadrant,
                        pinMode = pinMode,
                        progressPercent = progress
                    ),
                    newTags = taskTags
                )
                showEditSheetForTask = null
            }
        )
    }

    // Bottom Sheet: Create Timer for Task
    showTimerSheetForTask?.let { task ->
        CreateTimerSheet(
            initialName = task.title,
            onDismiss = { showTimerSheetForTask = null },
            onAdd = { timerState ->
                viewModel.createTimerFromTask(task, timerState)
                showTimerSheetForTask = null
            },
        )
    }

    // Bottom Sheet: Date Action (Сегодня/Завтра/Понедельник/Выбрать/Очистить)
    showDateSheetForTask?.let { task ->
        DateActionSheet(
            onDismiss = { showDateSheetForTask = null },
            onDateSelected = { newDate ->
                viewModel.updateTaskDate(task, newDate)
                showDateSheetForTask = null
            },
            onPickCustomDate = {
                // TODO: Full DatePicker integration (Phase 3.7)
                showDateSheetForTask = null
            },
        )
    }

    // Bottom Sheet: Move to Folder
    showMoveSheetForTask?.let { task ->
        MoveToFolderSheet(
            folders = folders,
            currentFolderId = task.folderId,
            onDismiss = { showMoveSheetForTask = null },
            onFolderSelected = { folderId ->
                viewModel.moveTaskToFolder(task, folderId)
                showMoveSheetForTask = null
            },
        )
    }

    // Bottom Sheet: Tasks More Menu (Three Dots)
    if (showMoreMenuSheet) {
        TasksMoreMenuSheet(
            preferences = viewPreferences,
            onDismiss = { showMoreMenuSheet = false },
            onUpdatePreferences = viewModel::updateTasksViewPreferences,
            onSelectModeClick = {
                showMoreMenuSheet = false
                isMultiSelectMode = true
                selectedTaskIds = emptySet()
            }
        )
    }
}
