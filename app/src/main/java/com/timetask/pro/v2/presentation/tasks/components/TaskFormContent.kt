package com.timetask.pro.v2.presentation.tasks.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.KeyboardReturn
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import com.timetask.pro.v2.data.local.db.entity.TaskEntity
import com.timetask.pro.v2.data.local.db.entity.TaskStatus
import com.timetask.pro.v2.data.local.db.entity.TagEntity
import com.timetask.pro.v2.presentation.components.tags.TagSelectionRow
import com.timetask.pro.v2.ui.theme.Spacing
import com.timetask.pro.v2.ui.theme.TitaniumError
import com.timetask.pro.v2.ui.theme.TitaniumPrimary
import com.timetask.pro.v2.ui.theme.TitaniumSuccess
import com.timetask.pro.v2.ui.theme.TitaniumWarning

@Composable
fun TaskFormContent(
    sheetTitle: String,
    confirmButtonText: String,
    initialTitle: String = "",
    initialDescription: String = "",
    initialQuadrant: Int? = null,
    initialPinMode: Int = 0,
    initialProgress: Int = 0,
    selectedTags: List<TagEntity> = emptyList(),
    onAddTagClick: () -> Unit = {},
    headerContent: @Composable () -> Unit = {},
    onTemplateClick: (() -> Unit)? = null,
    // Subtask support
    subtasks: List<TaskEntity> = emptyList(),
    parentTaskId: Long? = null,
    onOutdent: (() -> Unit)? = null,
    onAddSubtask: ((String) -> Unit)? = null,
    onPickerSubtaskClick: (() -> Unit)? = null,
    onToggleSubtask: ((TaskEntity) -> Unit)? = null,
    onDeleteSubtask: ((TaskEntity) -> Unit)? = null,
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, quadrant: Int?, pinMode: Int, progress: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember(initialTitle) { mutableStateOf(initialTitle) }
    var description by remember(initialDescription) { mutableStateOf(initialDescription) }
    var quadrant by remember(initialQuadrant) { mutableStateOf<Int?>(initialQuadrant) }
    var pinMode by remember(initialPinMode) { mutableIntStateOf(initialPinMode) }
    var progress by remember(initialProgress) { mutableStateOf(initialProgress.toFloat()) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = sheetTitle,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            
            if (parentTaskId != null && onOutdent != null) {
                IconButton(onClick = onOutdent) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.KeyboardReturn,
                        contentDescription = "Отвязать от родителя",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            if (onTemplateClick != null) {
                IconButton(onClick = onTemplateClick) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Выбрать шаблон",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(Spacing.md))

        // Main scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            // Header content (e.g. for Template specific fields)
            headerContent()

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Название") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TitaniumPrimary,
                    cursorColor = TitaniumPrimary,
                    focusedLabelColor = TitaniumPrimary,
                ),
            )

            Spacer(Modifier.height(Spacing.md))

            // Date Placeholder
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* TODO: Open DatePicker */ }
                    .padding(vertical = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Дата",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(Spacing.md))
                Text(
                    text = "Без даты", // Placeholder
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(Spacing.md))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание (опционально)") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TitaniumPrimary,
                    cursorColor = TitaniumPrimary,
                    focusedLabelColor = TitaniumPrimary,
                ),
            )

            Spacer(Modifier.height(Spacing.md))

            // Tags Section
            TagSelectionRow(
                selectedTags = selectedTags,
                onAddTagClick = onAddTagClick
            )

            Spacer(Modifier.height(Spacing.lg))

            // ═══════════════════════════════════════
            // Subtasks Section
            // ═══════════════════════════════════════
            if (onAddSubtask != null) {
                Text(
                    text = "Подзадачи",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(Spacing.sm))

                // Existing subtasks
                subtasks.forEach { subtask ->
                    val isDone = subtask.status == TaskStatus.DONE
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .padding(start = Spacing.xs, end = 0.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = isDone,
                            onCheckedChange = { onToggleSubtask?.invoke(subtask) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = TitaniumSuccess,
                                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                        Text(
                            text = subtask.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant
                                    else MaterialTheme.colorScheme.onBackground,
                            textDecoration = if (isDone) TextDecoration.LineThrough else null,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(
                            onClick = { onDeleteSubtask?.invoke(subtask) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Удалить подзадачу",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(Spacing.xs))
                }

                // Quick add subtask field
                var subtaskTitle by remember { mutableStateOf("") }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = subtaskTitle,
                        onValueChange = { subtaskTitle = it },
                        placeholder = { Text("Добавить подзадачу", style = MaterialTheme.typography.bodyMedium) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TitaniumPrimary,
                            cursorColor = TitaniumPrimary,
                        ),
                    )
                    Spacer(Modifier.width(Spacing.xs))
                    IconButton(
                        onClick = {
                            if (subtaskTitle.isNotBlank()) {
                                onAddSubtask(subtaskTitle.trim())
                                subtaskTitle = ""
                            }
                        },
                        enabled = subtaskTitle.isNotBlank()
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Добавить",
                            tint = if (subtaskTitle.isNotBlank()) TitaniumPrimary
                                   else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }
                }

                if (onPickerSubtaskClick != null) {
                    Spacer(Modifier.height(Spacing.xs))
                    TextButton(
                        onClick = onPickerSubtaskClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            androidx.compose.material.icons.Icons.Default.Menu,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(Spacing.xs))
                        Text("Выбрать из существующих")
                    }
                }

                Spacer(Modifier.height(Spacing.lg))
            }
            
            // Progress Slider
            Text(
                text = "Прогресс (${progress.toInt()}%)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(Spacing.xs))
            Slider(
                value = progress,
                onValueChange = { progress = it },
                valueRange = 0f..100f,
                steps = 19, // 0, 5, 10, 15... 100
                colors = SliderDefaults.colors(
                    thumbColor = TitaniumPrimary,
                    activeTrackColor = TitaniumPrimary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.xs)
            )

            Spacer(Modifier.height(Spacing.lg))

            // Eisenhower Matrix
            Text(
                text = "Матрица Эйзенхауэра",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(Spacing.sm))
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                EisenhowerOption(
                    label = "🔴 Срочно и Важно",
                    value = 1,
                    selectedValue = quadrant,
                    onSelect = { quadrant = it }
                )
                EisenhowerOption(
                    label = "🟡 Важно, Не срочно",
                    value = 2,
                    selectedValue = quadrant,
                    onSelect = { quadrant = it }
                )
                EisenhowerOption(
                    label = "🔵 Срочно, Неважно",
                    value = 3,
                    selectedValue = quadrant,
                    onSelect = { quadrant = it }
                )
                EisenhowerOption(
                    label = "⚪ Не срочно, Неважно",
                    value = 4,
                    selectedValue = quadrant,
                    onSelect = { quadrant = it }
                )
                if (quadrant != null) {
                    TextButton(onClick = { quadrant = null }, modifier = Modifier.padding(top = Spacing.xs)) {
                        Text("Сбросить квадрант", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(Modifier.height(Spacing.lg))

            // Pinning Mode
            Text(
                text = "Режим закрепления",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "Управляет тем, где задача закреплена",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Spacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                PinModeChip("Нет", 0, pinMode, Icons.Outlined.PushPin) { pinMode = it }
                PinModeChip("Локально", 1, pinMode, Icons.Default.PushPin) { pinMode = it }
                PinModeChip("Глобально", 2, pinMode, Icons.Default.PushPin) { pinMode = it }
            }

            Spacer(Modifier.height(Spacing.xl)) // padding bottom
        }

        // Buttons pinned at bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.sm),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
            Spacer(Modifier.width(Spacing.sm))
            Button(
                onClick = { onConfirm(title, description, quadrant, pinMode, progress.toInt()) },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TitaniumPrimary,
                ),
            ) {
                Text(confirmButtonText)
            }
        }
    }
}

@Composable
internal fun EisenhowerOption(
    label: String,
    value: Int,
    selectedValue: Int?,
    onSelect: (Int) -> Unit
) {
    val isSelected = value == selectedValue
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(value) }
            .padding(vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.RadioButton(
            selected = isSelected,
            onClick = null,
            colors = androidx.compose.material3.RadioButtonDefaults.colors(
                selectedColor = TitaniumPrimary
            )
        )
        Spacer(Modifier.width(Spacing.sm))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}



@Composable
internal fun PinModeChip(
    label: String,
    value: Int,
    selected: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onSelect: (Int) -> Unit,
) {
    FilterChip(
        selected = selected == value,
        onClick = { onSelect(value) },
        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
        leadingIcon = {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = TitaniumPrimary.copy(alpha = 0.2f),
            selectedLabelColor = TitaniumPrimary,
            selectedLeadingIconColor = TitaniumPrimary,
        ),
    )
}
