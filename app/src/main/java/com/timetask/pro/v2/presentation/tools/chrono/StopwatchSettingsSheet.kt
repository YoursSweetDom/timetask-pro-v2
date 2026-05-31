package com.timetask.pro.v2.presentation.tools.chrono

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.timetask.pro.v2.domain.model.chrono.Stopwatch
import com.timetask.pro.v2.ui.theme.Spacing
import com.timetask.pro.v2.presentation.tasks.components.TaskPickerSheet
import com.timetask.pro.v2.data.local.db.entity.TagEntity
import com.timetask.pro.v2.presentation.components.tags.TagSelectionRow
import com.timetask.pro.v2.presentation.components.tags.TagSelectionSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopwatchSettingsSheet(
    showSheet: Boolean,
    stopwatch: Stopwatch?,
    onDismiss: () -> Unit,
    onSave: (name: String, category: String, tags: String, linkedTaskIds: List<Long>, showInNotifications: Boolean) -> Unit,
    onDelete: (String) -> Unit,
    availableTags: List<TagEntity> = emptyList(),
    onCreateTag: (String) -> Unit = {},
) {
    if (!showSheet) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    val isCreation = stopwatch == null
    var name by remember(stopwatch?.id) { mutableStateOf(stopwatch?.name ?: "") }
    var categoryText by remember(stopwatch?.id) { mutableStateOf(stopwatch?.categoryText ?: "") }
    var tagsText by remember(stopwatch?.id) { mutableStateOf(stopwatch?.tagsText ?: "") }

    val initialLinkedTaskIds = remember(stopwatch?.id) {
        try {
            val jsonArray = org.json.JSONArray(stopwatch?.linkedTaskIdsJson ?: "[]")
            List(jsonArray.length()) { jsonArray.getLong(it) }
        } catch (e: Exception) { emptyList() }
    }
    var linkedTaskIds by remember(stopwatch?.id) { mutableStateOf(initialLinkedTaskIds) }

    var showInNotifications by remember(stopwatch?.id) { mutableStateOf(stopwatch?.notification?.showInNotifications ?: true) }

    var showMeta by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.md)
                .navigationBarsPadding()
                .padding(bottom = Spacing.md),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (isCreation) "Новый секундомер" else "Настройки секундомера",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(
                    onClick = {
                        onSave(name.trim().ifBlank { "Секундомер" }, categoryText.trim(), tagsText.trim(), linkedTaskIds, showInNotifications)
                        onDismiss()
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Сохранить",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Имя") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(Spacing.md))
            HorizontalDivider()

            // Metadata Collapsible
            CollapsibleStopwatchSection(
                title = "Категория / Теги",
                expanded = showMeta,
                onToggle = { showMeta = !showMeta },
            ) {
                OutlinedTextField(
                    value = categoryText,
                    onValueChange = { categoryText = it },
                    label = { Text("📁 Категория") },
                    placeholder = { Text("Без категории") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(Spacing.sm))

                // Tag selection via TagSelectionRow
                var selectedStopwatchTags by remember(stopwatch?.id) { mutableStateOf(emptyList<TagEntity>()) }
                var showStopwatchTagSheet by remember { mutableStateOf(false) }
                TagSelectionRow(
                    selectedTags = selectedStopwatchTags,
                    onAddTagClick = { showStopwatchTagSheet = true }
                )
                if (showStopwatchTagSheet) {
                    TagSelectionSheet(
                        allTags = availableTags,
                        selectedTagIds = selectedStopwatchTags.map { it.id }.toSet(),
                        onTagToggled = { tag ->
                            selectedStopwatchTags = if (selectedStopwatchTags.any { it.id == tag.id }) {
                                selectedStopwatchTags.filter { it.id != tag.id }
                            } else {
                                selectedStopwatchTags + tag
                            }
                        },
                        onCreateNewTag = onCreateTag,
                        onDismissRequest = { showStopwatchTagSheet = false }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))
            HorizontalDivider()

            // Notifications switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🔔 Уведомления",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = "Показывать в шторке",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.width(Spacing.sm))
                Switch(
                    checked = showInNotifications,
                    onCheckedChange = {
                        showInNotifications = it
                        // Since `onSave` doesn't currently accept notification state, we can manually
                        // update it, or add it to `onSave` callback.
                    },
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))
            HorizontalDivider()

            // Linked Tasks Collapsible Alternative / Section
            var showTaskPicker by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTaskPicker = true }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Привязанные задачи",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                    )
                    if (linkedTaskIds.isNotEmpty()) {
                        Text(
                            text = "Выбрано: ${linkedTaskIds.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    } else {
                        Text(
                            text = "Нажмите, чтобы выбрать задачи",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Выбрать задачи",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (showTaskPicker) {
                TaskPickerSheet(
                    initialSelectedTaskIds = linkedTaskIds,
                    onTasksSelected = { selectedIds ->
                        linkedTaskIds = selectedIds
                    },
                    onDismiss = { showTaskPicker = false }
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            if (!isCreation) {
                Button(
                    onClick = {
                        stopwatch?.id?.let(onDelete)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text("Удалить секундомер")
                }
            }
        }
    }
}

@Composable
private fun CollapsibleStopwatchSection(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
        )
        Icon(
            imageVector = if (expanded) {
                Icons.Default.KeyboardArrowUp
            } else {
                Icons.Default.KeyboardArrowDown
            },
            contentDescription = if (expanded) "Свернуть" else "Развернуть",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    AnimatedVisibility(
        visible = expanded,
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        Column(modifier = Modifier.padding(bottom = Spacing.sm)) {
            content()
        }
    }
}
