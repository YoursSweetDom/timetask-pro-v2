package com.timetask.pro.v2.presentation.templates


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timetask.pro.v2.data.local.db.entity.TemplateEntity
import com.timetask.pro.v2.ui.theme.Spacing
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatePickerSheet(
    viewModel: TemplatesViewModel,
    onTemplateSelected: (TemplateEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .padding(horizontal = Spacing.md)
                .navigationBarsPadding()
        ) {
            Text(
                text = "Выберите шаблон",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(Spacing.md))

            if (state.templates.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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

                LazyColumn(state = lazyListState, modifier = Modifier.fillMaxWidth()) {
                    items(localTemplates, key = { it.id }) { template ->
                        ReorderableItem(reorderableLazyListState, key = template.id) { isDragging ->
                            
                            androidx.compose.runtime.LaunchedEffect(isDragging) {
                                if (isDragging) {
                                    draggingTemplateId = template.id
                                } else if (draggingTemplateId == template.id) {
                                    draggingTemplateId = null
                                    viewModel.onEvent(TemplatesEvent.ReorderTemplates(localTemplates))
                                }
                            }
                            
                            val folderName = state.folders.find { it.id == template.folderId }?.name
                            TemplatePickerItem(
                                template = template,
                                folderName = folderName,
                                isDragging = isDragging,
                                dragModifier = Modifier.longPressDraggableHandle(
                                    onDragStarted = { hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) },
                                    onDragStopped = { hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove) }
                                ),
                                onClick = {
                                    onTemplateSelected(template)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TemplatePickerItem(
    template: TemplateEntity,
    folderName: String?,
    isDragging: Boolean = false,
    dragModifier: Modifier = Modifier,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(if (isDragging) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm)
    ) {
        Icon(
            imageVector = Icons.Default.Description,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(Spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = template.name, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
            if (folderName != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Folder, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = folderName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Icon(
            imageVector = Icons.Filled.DragHandle,
            contentDescription = "Сортировка",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = dragModifier.padding(start = Spacing.sm)
        )
    }
}
