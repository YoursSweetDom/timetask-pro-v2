package com.timetask.pro.v2.presentation.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timetask.pro.v2.data.local.db.entity.TemplateEntity
import com.timetask.pro.v2.ui.theme.Spacing
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxHeight
import com.timetask.pro.v2.presentation.tasks.components.MoveToFolderSheet
import com.timetask.pro.v2.presentation.tasks.components.CustomSwipeToReveal
import androidx.compose.material.icons.filled.Delete
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(
    viewModel: TemplatesViewModel,
    onBackClick: () -> Unit,
    onCreateTemplateClick: () -> Unit,
    onTemplateClick: (TemplateEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var selectedTemplateForEdit by remember { mutableStateOf<TemplateEntity?>(null) }
    var showMoveSheetForTemplate by remember { mutableStateOf<TemplateEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Шаблоны") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateTemplateClick) {
                Icon(Icons.Filled.Add, contentDescription = "Добавить шаблон")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        if (state.templates.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("У вас пока нет шаблонов", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            val hapticFeedback = LocalHapticFeedback.current
            val lazyListState = rememberLazyListState()
            
            var localTemplates by remember(state.templates) { mutableStateOf(state.templates) }
            var draggingTemplateId by remember { mutableStateOf<Long?>(null) }
            
            val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
                localTemplates = localTemplates.toMutableList().apply {
                    add(to.index, removeAt(from.index))
                }
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }

            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(localTemplates, { it.id }) { template ->
                    ReorderableItem(reorderableLazyListState, key = template.id) { isDragging ->
                        
                        androidx.compose.runtime.LaunchedEffect(isDragging) {
                            if (isDragging) {
                                draggingTemplateId = template.id
                            } else if (draggingTemplateId == template.id) {
                                draggingTemplateId = null
                                viewModel.onEvent(TemplatesEvent.ReorderTemplates(localTemplates))
                            }
                        }
                        
                        val elevation by animateDpAsState(
                            if (isDragging) 4.dp else 0.dp,
                            label = "templateDragElevation"
                        )
                        val folderName = state.folders.find { it.id == template.folderId }?.name

                        CustomSwipeToReveal(
                            modifier = Modifier.padding(bottom = 8.dp),
                            leftMenuWidth = 0.dp,
                            rightMenuWidth = 120.dp,
                            rightMenuContent = {
                                IconButton(
                                    onClick = { showMoveSheetForTemplate = template },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer).fillMaxHeight().width(60.dp)
                                ) {
                                    Icon(Icons.Default.Folder, contentDescription = "Переместить", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                                IconButton(
                                    onClick = { viewModel.onEvent(TemplatesEvent.DeleteTemplate(template)) },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.errorContainer).fillMaxHeight().width(60.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.onErrorContainer)
                                }
                            }
                        ) {
                            Surface(
                                shadowElevation = elevation,
                                color = Color.Transparent
                            ) {
                                TemplateItem(
                                    template = template,
                                    folderName = folderName,
                                    isDragging = isDragging,
                                    onTemplateClick = { selectedTemplateForEdit = template },
                                    onPinClick = { viewModel.onEvent(TemplatesEvent.PinTemplate(template)) },
                                    dragModifier = Modifier.longPressDraggableHandle(
                                        onDragStarted = {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        },
                                        onDragStopped = {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    selectedTemplateForEdit?.let { template ->
        EditTemplateSheet(
            template = template,
            viewModel = viewModel,
            onDismiss = { selectedTemplateForEdit = null },
            onShowFolderPicker = {
                showMoveSheetForTemplate = template
            }
        )
    }

    showMoveSheetForTemplate?.let { template ->
        MoveToFolderSheet(
            folders = state.folders.toImmutableList(),
            currentFolderId = template.folderId,
            onDismiss = { showMoveSheetForTemplate = null },
            onFolderSelected = { folderId ->
                viewModel.onEvent(TemplatesEvent.MoveToFolder(template, folderId))
                showMoveSheetForTemplate = null
            }
        )
    }
}

@Composable
fun TemplateItem(
    template: TemplateEntity,
    folderName: String?,
    isDragging: Boolean,
    onTemplateClick: () -> Unit,
    onPinClick: () -> Unit,
    dragModifier: Modifier = Modifier,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(if (isDragging) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface)
            .clickable(onClick = onTemplateClick)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm)
    ) {
        Spacer(modifier = Modifier.width(Spacing.sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = template.name, style = MaterialTheme.typography.bodyLarge)
            if (folderName != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Folder, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = folderName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (!template.description.isNullOrEmpty()) {
                Text(text = template.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        IconButton(onClick = onPinClick) {
            Icon(
                imageVector = if (template.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                contentDescription = if (template.isPinned) "Открепить" else "Закрепить",
                tint = if (template.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Filled.DragHandle,
            contentDescription = "Перетащить",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = dragModifier.padding(start = Spacing.sm)
        )
    }
}
