package com.timetask.pro.v2.presentation.tasks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.timetask.pro.v2.data.local.db.entity.Priority
import com.timetask.pro.v2.ui.theme.Spacing
import com.timetask.pro.v2.ui.theme.TitaniumError
import com.timetask.pro.v2.ui.theme.TitaniumPrimary
import com.timetask.pro.v2.ui.theme.TitaniumSuccess
import com.timetask.pro.v2.ui.theme.TitaniumWarning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskDetailScreen(
    viewModel: TaskDetailViewModel,
    onBack: () -> Unit,
) {
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val priority by viewModel.priority.collectAsState()
    val quadrant by viewModel.quadrant.collectAsState()
    val dueDate by viewModel.dueDate.collectAsState()
    val folderId by viewModel.folderId.collectAsState()
    val isPinned by viewModel.isPinned.collectAsState()
    val estimatedMinutes by viewModel.estimatedMinutes.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val isLoaded by viewModel.isLoaded.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale("ru")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Детали задачи") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                actions = {
                    // Pin toggle
                    IconButton(onClick = remember { { viewModel.togglePinned() } }) {
                        Icon(
                            Icons.Filled.PushPin,
                            contentDescription = "Закрепить",
                            tint = if (isPinned) TitaniumPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    // Save
                    IconButton(onClick = remember { { viewModel.save { onBack() } } }) {
                        Icon(
                            Icons.Filled.Save,
                            contentDescription = "Сохранить",
                            tint = TitaniumPrimary,
                        )
                    }
                    // Delete
                    IconButton(onClick = remember { { viewModel.deleteAndGoBack { onBack() } } }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Удалить",
                            tint = TitaniumError,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { innerPadding ->
        if (!isLoaded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Загрузка...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.md),
            ) {
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = remember<(String) -> Unit> { { viewModel.updateTitle(it) } },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )

                Spacer(Modifier.height(Spacing.md))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = remember<(String) -> Unit> { { viewModel.updateDescription(it) } },
                    label = { Text("Описание") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp),
                )

                Spacer(Modifier.height(Spacing.lg))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(Modifier.height(Spacing.md))

                // Priority
                Text(
                    text = "Приоритет",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(Spacing.sm))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    Priority.entries.forEach { p ->
                        val isSelected = priority == p
                        val chipColor = when (p) {
                            Priority.HIGH -> TitaniumError
                            Priority.MEDIUM -> TitaniumWarning
                            Priority.LOW -> TitaniumPrimary
                            Priority.NONE -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        val label = when (p) {
                            Priority.HIGH -> "Высокий"
                            Priority.MEDIUM -> "Средний"
                            Priority.LOW -> "Низкий"
                            Priority.NONE -> "Нет"
                        }

                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updatePriority(p) },
                            label = { Text(label) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = chipColor.copy(alpha = 0.15f),
                                selectedLabelColor = chipColor,
                            ),
                            border = if (isSelected) BorderStroke(1.dp, chipColor) else null,
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.lg))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(Modifier.height(Spacing.md))

                // Eisenhower Matrix
                Text(
                    text = "Матрица Эйзенхауэра",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(Spacing.sm))
                
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    EisenhowerOption(
                        label = "🔴 Срочно и Важно",
                        value = 1,
                        selectedValue = quadrant,
                        onSelect = { viewModel.updateQuadrant(it) }
                    )
                    EisenhowerOption(
                        label = "🟡 Важно, Не срочно",
                        value = 2,
                        selectedValue = quadrant,
                        onSelect = { viewModel.updateQuadrant(it) }
                    )
                    EisenhowerOption(
                        label = "🔵 Срочно, Неважно",
                        value = 3,
                        selectedValue = quadrant,
                        onSelect = { viewModel.updateQuadrant(it) }
                    )
                    EisenhowerOption(
                        label = "⚪ Не срочно, Неважно",
                        value = 4,
                        selectedValue = quadrant,
                        onSelect = { viewModel.updateQuadrant(it) }
                    )
                    if (quadrant != null) {
                        TextButton(onClick = { viewModel.updateQuadrant(null) }, modifier = Modifier.padding(top = Spacing.xs)) {
                            Text("Сбросить квадрант", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(Modifier.height(Spacing.lg))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(Modifier.height(Spacing.md))

                // Due date
                Text(
                    text = "Дедлайн",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(Spacing.sm))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AssistChip(
                        onClick = { showDatePicker = true },
                        label = {
                            Text(
                                if (dueDate != null) {
                                    dateFormat.format(Date(dueDate!!))
                                } else {
                                    "Выбрать дату"
                                }
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                    )

                    if (dueDate != null) {
                        Spacer(Modifier.width(Spacing.sm))
                        IconButton(
                            onClick = remember { { viewModel.updateDueDate(null) } },
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                Icons.Filled.Clear,
                                contentDescription = "Убрать дату",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(Spacing.lg))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(Modifier.height(Spacing.md))

                // Folder
                Text(
                    text = "Папка",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(Spacing.sm))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    // Inbox chip
                    FilterChip(
                        selected = folderId == null,
                        onClick = { viewModel.updateFolderId(null) },
                        label = { Text("📥 Inbox") },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TitaniumPrimary.copy(alpha = 0.15f),
                        ),
                    )

                    // Folder chips
                    folders.forEach { folder ->
                        FilterChip(
                            selected = folderId == folder.id,
                            onClick = { viewModel.updateFolderId(folder.id) },
                            label = {
                                Text("${folder.emoji ?: "📁"} ${folder.name}")
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TitaniumPrimary.copy(alpha = 0.15f),
                            ),
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.lg))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(Modifier.height(Spacing.md))

                // Estimated time
                Text(
                    text = "Оценка времени",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(Spacing.sm))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    val timeOptions = listOf(null, 5, 15, 30, 60, 120)
                    val timeLabels = listOf("—", "5 мин", "15 мин", "30 мин", "1 час", "2 часа")

                    timeOptions.forEachIndexed { index, minutes ->
                        FilterChip(
                            selected = estimatedMinutes == minutes,
                            onClick = { viewModel.updateEstimatedMinutes(minutes) },
                            label = { Text(timeLabels[index]) },
                            leadingIcon = if (index > 0) {
                                {
                                    Icon(
                                        Icons.Outlined.Schedule,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            } else null,
                            shape = RoundedCornerShape(12.dp),
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.xl))

                // Save button at bottom
                androidx.compose.material3.Button(
                    onClick = remember { { viewModel.save { onBack() } } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = TitaniumPrimary,
                    ),
                ) {
                    Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(Spacing.sm))
                    Text("Сохранить")
                }

                Spacer(Modifier.height(Spacing.lg))
            }
        }
    }

    // DatePicker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDate ?: System.currentTimeMillis(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateDueDate(datePickerState.selectedDateMillis)
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Отмена")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun EisenhowerOption(
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
