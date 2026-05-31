package com.timetask.pro.v2.presentation.tools.timers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import com.timetask.pro.v2.presentation.components.DurationOption
import com.timetask.pro.v2.presentation.components.DurationPickerDropdown
import com.timetask.pro.v2.data.local.db.entity.TimerConfig
import com.timetask.pro.v2.data.local.db.entity.TimerEntity
import com.timetask.pro.v2.data.local.db.entity.TimerNotification
import com.timetask.pro.v2.data.local.db.entity.UserPresetEntity
import com.timetask.pro.v2.ui.theme.Spacing
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import com.timetask.pro.v2.presentation.tasks.components.TaskPickerSheet
import com.timetask.pro.v2.data.local.db.entity.TagEntity
import com.timetask.pro.v2.presentation.components.tags.TagSelectionRow
import com.timetask.pro.v2.presentation.components.tags.TagSelectionSheet

// ============================================================
// State
// ============================================================

@Stable
data class CreateTimerState(
    val name: String = "",
    val hours: Int = 0,
    val minutes: Int = 5,
    val seconds: Int = 0,
    val config: TimerConfig = TimerConfig(),
    val showInNotifications: Boolean = true,
    val ringingDurationSec: Int = 180,
    val linkedTaskIds: List<Long> = emptyList(),
    val selectedTagIds: List<Long> = emptyList(),
) {
    val durationMs: Long
        get() = (hours * 3600L + minutes * 60L + seconds) * 1000L

    companion object {
        /** Создаёт state из существующего таймера (для режима EDIT) */
        fun fromEntity(timer: TimerEntity): CreateTimerState {
            val totalSec = timer.totalDurationMs / 1000
            val linkedTaskIds = try {
                val jsonArray = org.json.JSONArray(timer.linkedTaskIdsJson)
                List(jsonArray.length()) { jsonArray.getLong(it) }
            } catch (e: Exception) { emptyList() }
            val tagIds = try {
                val jsonArray = org.json.JSONArray(timer.tagIdsJson)
                List(jsonArray.length()) { jsonArray.getLong(it) }
            } catch (e: Exception) { emptyList() }

            return CreateTimerState(
                name = timer.name,
                hours = (totalSec / 3600).toInt(),
                minutes = ((totalSec % 3600) / 60).toInt(),
                seconds = (totalSec % 60).toInt(),
                config = timer.config,
                showInNotifications = timer.notification.showInNotifications,
                ringingDurationSec = timer.notification.ringingDurationSec,
                linkedTaskIds = linkedTaskIds,
                selectedTagIds = tagIds,
            )
        }
    }
}

/** Режим формы: создание или редактирование */
enum class TimerFormMode { CREATE, EDIT }

private val CreateTimerStateSaver = mapSaver(
    save = { state ->
        mapOf(
            "name" to state.name,
            "hours" to state.hours,
            "minutes" to state.minutes,
            "seconds" to state.seconds,
            "adjustStepSec" to state.config.adjustStepSec,
            "enableOvertime" to state.config.enableOvertime,
            "autoRepeat" to state.config.autoRepeat,
            "autoReset" to state.config.autoReset,
            "quickAddDurationSec" to state.config.quickAddDurationSec,
            "showInNotifications" to state.showInNotifications,
            "ringingDurationSec" to state.ringingDurationSec,
            "linkedTaskIds" to state.linkedTaskIds.joinToString(","),
            "selectedTagIds" to state.selectedTagIds.joinToString(","),
        )
    },
    restore = { map ->
        CreateTimerState(
            name = map["name"] as String,
            hours = map["hours"] as Int,
            minutes = map["minutes"] as Int,
            seconds = map["seconds"] as Int,
            config = TimerConfig(
                adjustStepSec = map["adjustStepSec"] as Int,
                enableOvertime = map["enableOvertime"] as Boolean,
                autoRepeat = map["autoRepeat"] as Boolean,
                autoReset = map["autoReset"] as Boolean,
                quickAddDurationSec = map["quickAddDurationSec"] as? Int ?: 60,
            ),
            showInNotifications = map["showInNotifications"] as? Boolean ?: true,
            ringingDurationSec = map["ringingDurationSec"] as? Int ?: 180,
            linkedTaskIds = (map["linkedTaskIds"] as? String)?.split(",")?.mapNotNull { it.toLongOrNull() } ?: emptyList(),
            selectedTagIds = (map["selectedTagIds"] as? String)?.split(",")?.mapNotNull { it.toLongOrNull() } ?: emptyList(),
        )
    },
)

// ============================================================
// CreateTimerSheet
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTimerSheet(
    onDismiss: () -> Unit,
    onAdd: (CreateTimerState) -> Unit,
    initialName: String = "",
    availableTags: List<TagEntity> = emptyList(),
    onCreateTag: (String) -> Unit = {},
    userPresets: ImmutableList<UserPresetEntity> = persistentListOf(),
    onAddPreset: (name: String, emoji: String, durationMs: Long) -> Unit = { _, _, _ -> },
    onDeletePreset: (id: String) -> Unit = {},
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var state by rememberSaveable(stateSaver = CreateTimerStateSaver) {
        mutableStateOf(CreateTimerState(name = initialName))
    }
    val selectedTags = remember(availableTags, state.selectedTagIds) {
        availableTags.filter { it.id in state.selectedTagIds }
    }

    val canCreate by remember { derivedStateOf { state.durationMs > 0 } }

    val doCreate = {
        if (canCreate) {
            onAdd(state)
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        TimerFormContent(
            state = state,
            canConfirm = canCreate,
            mode = TimerFormMode.CREATE,
            onStateChange = { state = it },
            onConfirm = doCreate,
            availableTags = availableTags,
            selectedTags = selectedTags,
            onAddTagClick = {},
            onCreateTag = onCreateTag,
            onTagToggled = { tag ->
                val newIds = if (tag.id in state.selectedTagIds) {
                    state.selectedTagIds.filter { it != tag.id }
                } else {
                    state.selectedTagIds + tag.id
                }
                state = state.copy(selectedTagIds = newIds)
            },
            userPresets = userPresets,
            onAddPreset = onAddPreset,
            onDeletePreset = onDeletePreset,
        )
    }
}

// ============================================================
// EditTimerSheet — редактирование существующего таймера
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTimerSheet(
    timer: TimerEntity,
    onDismiss: () -> Unit,
    onSave: (CreateTimerState) -> Unit,
    availableTags: List<TagEntity> = emptyList(),
    onCreateTag: (String) -> Unit = {},
    userPresets: ImmutableList<UserPresetEntity> = persistentListOf(),
    onAddPreset: (name: String, emoji: String, durationMs: Long) -> Unit = { _, _, _ -> },
    onDeletePreset: (id: String) -> Unit = {},
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var state by rememberSaveable(stateSaver = CreateTimerStateSaver) {
        mutableStateOf(CreateTimerState.fromEntity(timer))
    }
    val selectedTags = remember(availableTags, state.selectedTagIds) {
        availableTags.filter { it.id in state.selectedTagIds }
    }

    val canSave by remember { derivedStateOf { state.durationMs > 0 } }

    val doSave = {
        if (canSave) {
            onSave(state)
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        TimerFormContent(
            state = state,
            canConfirm = canSave,
            mode = TimerFormMode.EDIT,
            onStateChange = { state = it },
            onConfirm = doSave,
            availableTags = availableTags,
            selectedTags = selectedTags,
            onAddTagClick = {},
            onCreateTag = onCreateTag,
            onTagToggled = { tag ->
                val newIds = if (tag.id in state.selectedTagIds) {
                    state.selectedTagIds.filter { it != tag.id }
                } else {
                    state.selectedTagIds + tag.id
                }
                state = state.copy(selectedTagIds = newIds)
            },
            userPresets = userPresets,
            onAddPreset = onAddPreset,
            onDeletePreset = onDeletePreset,
        )
    }
}

// ============================================================
// TimerFormContent — переиспользуемый для create + edit
// ============================================================

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
internal fun TimerFormContent(
    state: CreateTimerState,
    canConfirm: Boolean,
    mode: TimerFormMode,
    onStateChange: (CreateTimerState) -> Unit,
    onConfirm: () -> Unit,
    availableTags: List<TagEntity> = emptyList(),
    selectedTags: List<TagEntity> = emptyList(),
    onAddTagClick: () -> Unit = {},
    onCreateTag: (String) -> Unit = {},
    onTagToggled: (TagEntity) -> Unit = {},
    userPresets: ImmutableList<UserPresetEntity> = persistentListOf(),
    onAddPreset: (name: String, emoji: String, durationMs: Long) -> Unit = { _, _, _ -> },
    onDeletePreset: (id: String) -> Unit = {},
) {
    var showBehavior by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.md)
            .navigationBarsPadding()
            .padding(bottom = Spacing.md),
    ) {
        // ============================================================
        // 1. Заголовок + кнопка ✓
        // ============================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = when (mode) {
                    TimerFormMode.CREATE -> "⏱ Новый таймер"
                    TimerFormMode.EDIT -> "⏱ Редактирование"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            IconButton(
                onClick = onConfirm,
                enabled = canConfirm,
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = when (mode) {
                        TimerFormMode.CREATE -> "Создать"
                        TimerFormMode.EDIT -> "Сохранить"
                    },
                    tint = if (canConfirm) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.sm))

        // ============================================================
        // 2. Имя
        // ============================================================
        OutlinedTextField(
            value = state.name,
            onValueChange = { onStateChange(state.copy(name = it)) },
            label = { Text("Имя (необязательно)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        // Tags Section
        var showTagSheet by rememberSaveable { mutableStateOf(false) }
        TagSelectionRow(
            selectedTags = selectedTags,
            onAddTagClick = { showTagSheet = true }
        )
        if (showTagSheet) {
            TagSelectionSheet(
                allTags = availableTags,
                selectedTagIds = selectedTags.map { it.id }.toSet(),
                onTagToggled = onTagToggled,
                onCreateNewTag = onCreateTag,
                onDismissRequest = { showTagSheet = false }
            )
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        // ============================================================
        // 3. Time Picker
        // ============================================================
        TimeScrollPicker(
            hours = state.hours,
            minutes = state.minutes,
            seconds = state.seconds,
            onTimeChange = { h, m, s ->
                onStateChange(state.copy(hours = h, minutes = m, seconds = s))
            },
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        // ============================================================
        // 4. Пресеты (компактный единый FlowRow)
        // ============================================================
        var showAddPresetDialog by rememberSaveable { mutableStateOf(false) }
        var showDeletePresetId by rememberSaveable { mutableStateOf<String?>(null) }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // Стандартные пресеты длительности
            standardDurationPresets.forEach { preset ->
                val isSelected = state.durationMs == preset.durationMs
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        val totalSec = preset.durationMs / 1000
                        onStateChange(
                            state.copy(
                                hours = (totalSec / 3600).toInt(),
                                minutes = ((totalSec % 3600) / 60).toInt(),
                                seconds = (totalSec % 60).toInt(),
                            ),
                        )
                    },
                    label = { Text(preset.label, fontSize = 12.sp) },
                    modifier = Modifier.height(28.dp),
                )
            }

            // Именованные пресеты
            defaultTimerPresets.forEach { preset ->
                val isSelected = state.durationMs == preset.durationMs
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        val totalSec = preset.durationMs / 1000
                        onStateChange(
                            state.copy(
                                name = if (state.name.isBlank()) preset.name else state.name,
                                hours = (totalSec / 3600).toInt(),
                                minutes = ((totalSec % 3600) / 60).toInt(),
                                seconds = (totalSec % 60).toInt(),
                            ),
                        )
                    },
                    label = { Text("${preset.emoji} ${preset.name}", fontSize = 12.sp) },
                    modifier = Modifier.height(28.dp),
                )
            }

            // Пользовательские пресеты
            userPresets.forEach { preset ->
                val isSelected = state.durationMs == preset.durationMs
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        val totalSec = preset.durationMs / 1000
                        onStateChange(
                            state.copy(
                                name = if (state.name.isBlank()) preset.name else state.name,
                                hours = (totalSec / 3600).toInt(),
                                minutes = ((totalSec % 3600) / 60).toInt(),
                                seconds = (totalSec % 60).toInt(),
                            ),
                        )
                    },
                    label = {
                        Text(
                            text = "${preset.emoji} ${preset.name}",
                            fontSize = 12.sp,
                            modifier = Modifier.combinedClickable(
                                onClick = {},
                                onLongClick = { showDeletePresetId = preset.id },
                            ),
                        )
                    },
                    modifier = Modifier.height(28.dp),
                )
            }

            // Кнопка [+ Свой]
            FilterChip(
                selected = false,
                onClick = { showAddPresetDialog = true },
                label = { Text("+ Свой", fontSize = 12.sp) },
                modifier = Modifier.height(28.dp),
            )
        }

        // AlertDialog: добавить пресет
        if (showAddPresetDialog) {
            AddPresetDialog(
                initialHours = state.hours,
                initialMinutes = state.minutes,
                initialSeconds = state.seconds,
                onDismiss = { showAddPresetDialog = false },
                onSave = { name, emoji, durationMs ->
                    onAddPreset(name, emoji, durationMs)
                    showAddPresetDialog = false
                },
            )
        }

        // AlertDialog: удалить пресет
        showDeletePresetId?.let { presetId ->
            AlertDialog(
                onDismissRequest = { showDeletePresetId = null },
                title = { Text("Удалить пресет?") },
                confirmButton = {
                    TextButton(onClick = {
                        onDeletePreset(presetId)
                        showDeletePresetId = null
                    }) { Text("Удалить") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeletePresetId = null }) { Text("Отмена") }
                },
            )
        }

        Spacer(modifier = Modifier.height(Spacing.md))
        HorizontalDivider()

        // ============================================================
        // 5. ▸ Поведение (складываемая)
        // ============================================================
        CollapsibleSection(
            title = "Поведение",
            expanded = showBehavior,
            onToggle = { showBehavior = !showBehavior },
        ) {
            BehaviorSection(
                config = state.config,
                onConfigChange = { onStateChange(state.copy(config = it)) },
                showInNotifications = state.showInNotifications,
                onShowInNotificationsChange = { onStateChange(state.copy(showInNotifications = it)) },
                ringingDurationSec = state.ringingDurationSec,
                onRingingDurationChange = { onStateChange(state.copy(ringingDurationSec = it)) },
            )
        }

        Spacer(modifier = Modifier.height(Spacing.md))
        HorizontalDivider()

        // ============================================================
        // ▸ Привязка задач
        // ============================================================
        var showTaskPicker by rememberSaveable { mutableStateOf(false) }

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
                if (state.linkedTaskIds.isNotEmpty()) {
                    Text(
                        text = "Выбрано: ${state.linkedTaskIds.size}",
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
                initialSelectedTaskIds = state.linkedTaskIds,
                onTasksSelected = { selectedIds ->
                    onStateChange(state.copy(linkedTaskIds = selectedIds))
                },
                onDismiss = { showTaskPicker = false }
            )
        }

        Spacer(modifier = Modifier.height(Spacing.lg))

        // ============================================================
        // 7. Кнопка «Создать»
        // ============================================================
        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth(),
            enabled = canConfirm,
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(Spacing.xs))
            Text(
                when (mode) {
                    TimerFormMode.CREATE -> "Создать таймер"
                    TimerFormMode.EDIT -> "Сохранить"
                },
            )
        }
    }
}

// ============================================================
// Складываемая секция
// ============================================================

@Composable
private fun CollapsibleSection(
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

// ============================================================
// SettingRow — строка настройки со Switch'ом
// ============================================================

@Composable
internal fun SettingRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.width(Spacing.sm))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

// ============================================================
// Секция «Поведение»
// ============================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BehaviorSection(
    config: TimerConfig,
    onConfigChange: (TimerConfig) -> Unit,
    showInNotifications: Boolean,
    onShowInNotificationsChange: (Boolean) -> Unit,
    ringingDurationSec: Int,
    onRingingDurationChange: (Int) -> Unit,
) {
    // Длительность звонка
    val durationOptions = remember {
        listOf(
            DurationOption("1 мин", 60),
            DurationOption("3 мин", 180),
            DurationOption("5 мин", 300),
            DurationOption("10 мин", 600),
            DurationOption("15 мин", 900),
            DurationOption("Бесконечно", -1)
        )
    }
    
    DurationPickerDropdown(
        title = "⏳ Длительность звонка",
        description = "Как долго звучит сигнал",
        options = durationOptions,
        currentDurationSec = ringingDurationSec,
        onDurationSelected = onRingingDurationChange,
    )
    
    Spacer(modifier = Modifier.height(Spacing.sm))

    // Уведомления
    SettingRow(
        title = "🔔 Уведомления",
        description = "Показывать в шторке уведомлений",
        checked = showInNotifications,
        onCheckedChange = onShowInNotificationsChange,
    )

    // Overtime
    SettingRow(
        title = "⏳ Overtime",
        description = "Продолжить отсчёт после завершения",
        checked = config.enableOvertime,
        onCheckedChange = {
            onConfigChange(
                config.copy(
                    enableOvertime = it,
                    autoRepeat = if (it) false else config.autoRepeat,
                    autoReset = if (it) false else config.autoReset,
                ),
            )
        },
    )

    // Auto-Repeat
    SettingRow(
        title = "🔁 Авто-повтор",
        description = "Перезапустить таймер автоматически",
        checked = config.autoRepeat,
        onCheckedChange = {
            onConfigChange(
                config.copy(
                    autoRepeat = it,
                    enableOvertime = if (it) false else config.enableOvertime,
                    autoReset = if (it) false else config.autoReset,
                ),
            )
        },
    )

    // Auto-Reset
    SettingRow(
        title = "↩ Авто-сброс",
        description = "Сбросить в исходное после завершения",
        checked = config.autoReset,
        onCheckedChange = {
            onConfigChange(
                config.copy(
                    autoReset = it,
                    enableOvertime = if (it) false else config.enableOvertime,
                    autoRepeat = if (it) false else config.autoRepeat,
                ),
            )
        },
    )

    Spacer(modifier = Modifier.height(Spacing.sm))

    // Adjust Step
    Text(
        text = "Шаг корректировки",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Spacer(modifier = Modifier.height(Spacing.xs))

    val stepOptions = listOf(15 to "15s", 30 to "30s", 60 to "1m", 300 to "5m")

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        stepOptions.forEach { (seconds, label) ->
            FilterChip(
                selected = config.adjustStepSec == seconds,
                onClick = { onConfigChange(config.copy(adjustStepSec = seconds)) },
                label = { Text(label) },
            )
        }
    }

    Spacer(modifier = Modifier.height(Spacing.sm))

    // Quick Add Step
    Text(
        text = "Быстрое добавление (в будильнике)",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Spacer(modifier = Modifier.height(Spacing.xs))

    val quickAddOptions = listOf(15 to "+15s", 30 to "+30s", 60 to "+1m", 120 to "+2m", 300 to "+5m")

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        quickAddOptions.forEach { (seconds, label) ->
            FilterChip(
                selected = config.quickAddDurationSec == seconds,
                onClick = { onConfigChange(config.copy(quickAddDurationSec = seconds)) },
                label = { Text(label) },
            )
        }
    }
}

// ============================================================
// Секция «Категория / Теги / Папка» (plain text пока нет Dao)
// ============================================================

@Composable
private fun MetadataSection(
    categoryText: String,
    tagsText: String,
    folderText: String,
    onCategoryChange: (String) -> Unit,
    onTagsChange: (String) -> Unit,
    onFolderChange: (String) -> Unit,
) {
    // Категория
    OutlinedTextField(
        value = categoryText,
        onValueChange = onCategoryChange,
        label = { Text("📁 Категория") },
        placeholder = { Text("Без категории") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(Spacing.sm))

    // Теги
    OutlinedTextField(
        value = tagsText,
        onValueChange = onTagsChange,
        label = { Text("🏷 Теги") },
        placeholder = { Text("Через запятую") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(Spacing.sm))

    // Папка
    OutlinedTextField(
        value = folderText,
        onValueChange = onFolderChange,
        label = { Text("📂 Папка") },
        placeholder = { Text("Корневая") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

// ============================================================
// Диалог добавления пользовательского пресета
// ============================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddPresetDialog(
    initialHours: Int = 0,
    initialMinutes: Int = 0,
    initialSeconds: Int = 0,
    onDismiss: () -> Unit,
    onSave: (name: String, emoji: String, durationMs: Long) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var emoji by rememberSaveable { mutableStateOf("⏱️") }
    var hoursText by rememberSaveable { mutableStateOf(if (initialHours > 0) initialHours.toString() else "") }
    var minutesText by rememberSaveable { mutableStateOf(if (initialMinutes > 0) initialMinutes.toString() else "") }
    var secondsText by rememberSaveable { mutableStateOf(if (initialSeconds > 0) initialSeconds.toString() else "") }

    val h = hoursText.toIntOrNull() ?: 0
    val m = minutesText.toIntOrNull() ?: 0
    val s = secondsText.toIntOrNull() ?: 0
    val durationMs = (h * 3600L + m * 60L + s) * 1000L

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Сохранить пресет") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Имя пресета") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(Spacing.sm))

                // Поля H:M:S
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedTextField(
                        value = hoursText,
                        onValueChange = { hoursText = it.filter { c -> c.isDigit() }.take(2) },
                        label = { Text("ч") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = minutesText,
                        onValueChange = {
                            val digits = it.filter { c -> c.isDigit() }.take(2)
                            minutesText = if ((digits.toIntOrNull() ?: 0) <= 59) digits else "59"
                        },
                        label = { Text("м") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = secondsText,
                        onValueChange = {
                            val digits = it.filter { c -> c.isDigit() }.take(2)
                            secondsText = if ((digits.toIntOrNull() ?: 0) <= 59) digits else "59"
                        },
                        label = { Text("с") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.sm))

                // Быстрый выбор emoji
                val emojiOptions = listOf("⏱️", "🍅", "☕", "🧘", "💪", "📚", "🎮", "🏃")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    emojiOptions.forEach { option ->
                        FilterChip(
                            selected = emoji == option,
                            onClick = { emoji = option },
                            label = { Text(option) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name.trim(), emoji, durationMs) },
                enabled = name.isNotBlank() && durationMs > 0,
            ) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        },
    )
}

