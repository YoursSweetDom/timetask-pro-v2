package com.timetask.pro.v2.presentation.tools.alarms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timetask.pro.v2.presentation.components.DurationOption
import com.timetask.pro.v2.presentation.components.DurationPickerDropdown
import com.timetask.pro.v2.ui.theme.Spacing
import com.timetask.pro.v2.data.local.db.entity.TagEntity
import com.timetask.pro.v2.presentation.components.tags.TagSelectionRow
import com.timetask.pro.v2.presentation.components.tags.TagSelectionSheet
import java.util.Calendar

// ============================================================
// State
// ============================================================

@Stable
data class CreateAlarmState(
    val hour: Int,
    val minute: Int,
    val repeatDaysMask: Int = 0,
    val snoozeMinutes: Int = 5,
    val snoozeRepeatTimes: Int = 3,
    val deleteAfterGoOff: Boolean = false,
    val name: String = "",
    val colorARGB: Int? = null,
    val categoryText: String = "",
    val tagsText: String = "",
    val notesText: String = "",
    val ringingDurationSec: Int = 300,
    val autoSnoozeDurationSec: Int = 300,
    val selectedTagIds: List<Long> = emptyList(),
) {
    companion object {
        fun fromInstance(alarm: AlarmInstance): CreateAlarmState {
            return CreateAlarmState(
                hour = alarm.hour,
                minute = alarm.minute,
                repeatDaysMask = alarm.repeatDaysMask,
                snoozeMinutes = alarm.snoozeMinutes,
                snoozeRepeatTimes = alarm.snoozeRepeatTimes,
                deleteAfterGoOff = alarm.deleteAfterGoOff,
                name = if (alarm.name == "Будильник") "" else alarm.name,
                colorARGB = alarm.colorARGB,
                categoryText = alarm.categoryText,
                tagsText = alarm.tagsText,
                notesText = alarm.notesText,
                ringingDurationSec = alarm.ringingDurationSec,
                autoSnoozeDurationSec = alarm.autoSnoozeDurationSec,
                selectedTagIds = alarm.tagIds,
            )
        }
        
        fun default(): CreateAlarmState {
            val cal = Calendar.getInstance()
            return CreateAlarmState(
                hour = cal.get(Calendar.HOUR_OF_DAY),
                minute = cal.get(Calendar.MINUTE)
            )
        }
    }
}

enum class AlarmFormMode { CREATE, EDIT }

private val CreateAlarmStateSaver = mapSaver(
    save = { state ->
        mapOf(
            "hour" to state.hour,
            "minute" to state.minute,
            "repeatDaysMask" to state.repeatDaysMask,
            "snoozeMinutes" to state.snoozeMinutes,
            "snoozeRepeatTimes" to state.snoozeRepeatTimes,
            "deleteAfterGoOff" to state.deleteAfterGoOff,
            "name" to state.name,
            "colorARGB" to state.colorARGB,
            "categoryText" to state.categoryText,
            "tagsText" to state.tagsText,
            "notesText" to state.notesText,
            "ringingDurationSec" to state.ringingDurationSec,
            "autoSnoozeDurationSec" to state.autoSnoozeDurationSec,
            "selectedTagIds" to state.selectedTagIds.joinToString(","),
        )
    },
    restore = { map ->
        CreateAlarmState(
            hour = map["hour"] as Int,
            minute = map["minute"] as Int,
            repeatDaysMask = map["repeatDaysMask"] as Int,
            snoozeMinutes = map["snoozeMinutes"] as Int,
            snoozeRepeatTimes = map["snoozeRepeatTimes"] as Int,
            deleteAfterGoOff = map["deleteAfterGoOff"] as Boolean,
            name = map["name"] as String,
            colorARGB = map["colorARGB"] as? Int,
            categoryText = map["categoryText"] as String,
            tagsText = map["tagsText"] as String,
            notesText = map["notesText"] as String,
            ringingDurationSec = map["ringingDurationSec"] as? Int ?: 300,
            autoSnoozeDurationSec = map["autoSnoozeDurationSec"] as? Int ?: 300,
            selectedTagIds = (map["selectedTagIds"] as? String)?.split(",")?.mapNotNull { it.toLongOrNull() } ?: emptyList(),
        )
    }
)

// ============================================================
// Create / Edit Sheets
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAlarmSheet(
    onDismiss: () -> Unit,
    onAdd: (CreateAlarmState) -> Unit,
    availableTags: List<TagEntity> = emptyList(),
    onCreateTag: (String) -> Unit = {},
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var state by rememberSaveable(stateSaver = CreateAlarmStateSaver) {
        mutableStateOf(CreateAlarmState.default())
    }
    val selectedTags = remember(availableTags, state.selectedTagIds) {
        availableTags.filter { it.id in state.selectedTagIds }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        AlarmFormContent(
            state = state,
            mode = AlarmFormMode.CREATE,
            onStateChange = { state = it },
            availableTags = availableTags,
            selectedTags = selectedTags,
            onCreateTag = onCreateTag,
            onTagToggled = { tag ->
                val newIds = if (tag.id in state.selectedTagIds) {
                    state.selectedTagIds.filter { it != tag.id }
                } else {
                    state.selectedTagIds + tag.id
                }
                state = state.copy(selectedTagIds = newIds)
            },
            onConfirm = {
                onAdd(state)
                onDismiss()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAlarmSheet(
    alarm: AlarmInstance,
    onDismiss: () -> Unit,
    onSave: (CreateAlarmState) -> Unit,
    availableTags: List<TagEntity> = emptyList(),
    onCreateTag: (String) -> Unit = {},
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var state by rememberSaveable(stateSaver = CreateAlarmStateSaver) {
        mutableStateOf(CreateAlarmState.fromInstance(alarm))
    }
    val selectedTags = remember(availableTags, state.selectedTagIds) {
        availableTags.filter { it.id in state.selectedTagIds }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        AlarmFormContent(
            state = state,
            mode = AlarmFormMode.EDIT,
            onStateChange = { state = it },
            availableTags = availableTags,
            selectedTags = selectedTags,
            onCreateTag = onCreateTag,
            onTagToggled = { tag ->
                val newIds = if (tag.id in state.selectedTagIds) {
                    state.selectedTagIds.filter { it != tag.id }
                } else {
                    state.selectedTagIds + tag.id
                }
                state = state.copy(selectedTagIds = newIds)
            },
            onConfirm = {
                onSave(state)
                onDismiss()
            }
        )
    }
}

// ============================================================
// Form Content
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AlarmFormContent(
    state: CreateAlarmState,
    mode: AlarmFormMode,
    onStateChange: (CreateAlarmState) -> Unit,
    onConfirm: () -> Unit,
    availableTags: List<TagEntity> = emptyList(),
    selectedTags: List<TagEntity> = emptyList(),
    onCreateTag: (String) -> Unit = {},
    onTagToggled: (TagEntity) -> Unit = {},
) {
    var showBehavior by rememberSaveable { mutableStateOf(false) }
    var showMeta by rememberSaveable { mutableStateOf(false) }

    // TimePicker
    val timePickerState = rememberTimePickerState(
        initialHour = state.hour,
        initialMinute = state.minute,
        is24Hour = true
    )

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
                text = when (mode) {
                    AlarmFormMode.CREATE -> "⏰ Новый будильник"
                    AlarmFormMode.EDIT -> "⏰ Редактирование"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            IconButton(onClick = { 
                onStateChange(state.copy(hour = timePickerState.hour, minute = timePickerState.minute))
                onConfirm() 
            }) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Сохранить",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            TimeInput(state = timePickerState) // TimeInput is often more compact than TimePicker
        }

        Spacer(modifier = Modifier.height(Spacing.md))
        
        OutlinedTextField(
            value = state.name,
            onValueChange = { onStateChange(state.copy(name = it)) },
            label = { Text("Название будильника") },
            placeholder = { Text("Например: Вставай на работу") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(Spacing.md))
        HorizontalDivider()

        // Behavior (Repeat, Snooze, Auto-delete)
        CollapsibleSection(
            title = "Поведение (Дни и Повторы)",
            expanded = showBehavior,
            onToggle = { showBehavior = !showBehavior }
        ) {
            AlarmBehaviorSection(state, onStateChange)
        }

        HorizontalDivider()

        // Meta (Color, Tags, Notes)
        CollapsibleSection(
            title = "Цвет / Категория / Заметки",
            expanded = showMeta,
            onToggle = { showMeta = !showMeta }
        ) {
            AlarmMetadataSection(
                state = state,
                onStateChange = onStateChange,
                availableTags = availableTags,
                selectedTags = selectedTags,
                onCreateTag = onCreateTag,
                onTagToggled = onTagToggled,
            )
        }

        Spacer(modifier = Modifier.height(Spacing.lg))

        Button(
            onClick = { 
                onStateChange(state.copy(hour = timePickerState.hour, minute = timePickerState.minute))
                onConfirm() 
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(Spacing.xs))
            Text(if (mode == AlarmFormMode.CREATE) "Создать будильник" else "Сохранить")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AlarmBehaviorSection(
    state: CreateAlarmState,
    onStateChange: (CreateAlarmState) -> Unit
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
        options = durationOptions,
        currentDurationSec = state.ringingDurationSec,
        onDurationSelected = { onStateChange(state.copy(ringingDurationSec = it)) },
    )
    
    Spacer(modifier = Modifier.height(Spacing.sm))
    
    // Авто-откладывание
    val autoSnoozeOptions = remember {
        listOf(
            DurationOption("Выключено", -1),
            DurationOption("1 мин", 60),
            DurationOption("3 мин", 180),
            DurationOption("5 мин", 300),
            DurationOption("10 мин", 600),
            DurationOption("15 мин", 900),
        )
    }
    
    DurationPickerDropdown(
        title = "🔁 Авто-откладывание",
        description = "Если не отвечаете на звонок",
        options = autoSnoozeOptions,
        allowOffOption = true,
        currentDurationSec = state.autoSnoozeDurationSec,
        onDurationSelected = { onStateChange(state.copy(autoSnoozeDurationSec = it)) },
    )
    
    Spacer(modifier = Modifier.height(Spacing.sm))
    HorizontalDivider()
    Spacer(modifier = Modifier.height(Spacing.sm))

    // Days of Week
    Text(
        text = "Дни повтора",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(Spacing.xs))
    
    val days = listOf("Пн" to 0, "Вт" to 1, "Ср" to 2, "Чт" to 3, "Пт" to 4, "Сб" to 5, "Вс" to 6)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEach { (name, index) ->
            val bit = 1 shl index
            val isSelected = (state.repeatDaysMask and bit) != 0
            
            FilterChip(
                selected = isSelected,
                onClick = {
                    val newMask = if (isSelected) {
                        state.repeatDaysMask and bit.inv()
                    } else {
                        state.repeatDaysMask or bit
                    }
                    onStateChange(state.copy(repeatDaysMask = newMask))
                },
                label = { Text(name, fontSize = 12.sp) }
            )
        }
    }

    Spacer(modifier = Modifier.height(Spacing.sm))

    // Snooze Interval
    Text(
        text = "Интервал повтора (Snooze)",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(Spacing.xs))
    
    val snoozeIntervals = listOf(3 to "3м", 5 to "5м", 10 to "10м", 15 to "15м", 30 to "30м")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        snoozeIntervals.forEach { (mins, label) ->
            FilterChip(
                selected = state.snoozeMinutes == mins,
                onClick = { onStateChange(state.copy(snoozeMinutes = mins)) },
                label = { Text(label) }
            )
        }
    }

    Spacer(modifier = Modifier.height(Spacing.sm))
    
    // Snooze Limit
    Text(
        text = "Лимит повторов",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(Spacing.xs))
    
    val snoozeLimits = listOf(1 to "1 раз", 3 to "3 раза", 5 to "5 раз", 10 to "10 раз")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        snoozeLimits.forEach { (times, label) ->
            FilterChip(
                selected = state.snoozeRepeatTimes == times,
                onClick = { onStateChange(state.copy(snoozeRepeatTimes = times)) },
                label = { Text(label) }
            )
        }
    }

    // Auto Delete
    SettingRow(
        title = "🗑 Авто-удаление",
        description = "Удалить после первого срабатывания",
        checked = state.deleteAfterGoOff,
        onCheckedChange = { onStateChange(state.copy(deleteAfterGoOff = it)) }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AlarmMetadataSection(
    state: CreateAlarmState,
    onStateChange: (CreateAlarmState) -> Unit,
    availableTags: List<TagEntity> = emptyList(),
    selectedTags: List<TagEntity> = emptyList(),
    onCreateTag: (String) -> Unit = {},
    onTagToggled: (TagEntity) -> Unit = {},
) {
    // Colors
    Text(
        text = "Цвет карточки",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(Spacing.xs))
    
    val colors = listOf(
        null, // Default
        android.graphics.Color.parseColor("#E53935"), // Red
        android.graphics.Color.parseColor("#8E24AA"), // Purple
        android.graphics.Color.parseColor("#3949AB"), // Indigo
        android.graphics.Color.parseColor("#039BE5"), // Light Blue
        android.graphics.Color.parseColor("#00897B"), // Teal
        android.graphics.Color.parseColor("#43A047"), // Green
        android.graphics.Color.parseColor("#F4511E"), // Deep Orange
    )
    
    FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        colors.forEach { colorArgb ->
            val color = if (colorArgb != null) Color(colorArgb) else MaterialTheme.colorScheme.surfaceVariant
            val isSelected = state.colorARGB == colorArgb
            
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onStateChange(state.copy(colorARGB = colorArgb)) }
            )
        }
    }
    
    Spacer(modifier = Modifier.height(Spacing.md))

    OutlinedTextField(
        value = state.categoryText,
        onValueChange = { onStateChange(state.copy(categoryText = it)) },
        label = { Text("📁 Категория") },
        placeholder = { Text("Например: Утро") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(Spacing.sm))

    // Tags via TagSelectionRow
    var showAlarmTagSheet by remember { mutableStateOf(false) }
    TagSelectionRow(
        selectedTags = selectedTags,
        onAddTagClick = { showAlarmTagSheet = true }
    )
    if (showAlarmTagSheet) {
        TagSelectionSheet(
            allTags = availableTags,
            selectedTagIds = selectedTags.map { it.id }.toSet(),
            onTagToggled = onTagToggled,
            onCreateNewTag = onCreateTag,
            onDismissRequest = { showAlarmTagSheet = false }
        )
    }

    Spacer(modifier = Modifier.height(Spacing.sm))
    
    OutlinedTextField(
        value = state.notesText,
        onValueChange = { onStateChange(state.copy(notesText = it)) },
        label = { Text("📝 Заметки") },
        placeholder = { Text("Что-то важное для этого будильника") },
        modifier = Modifier.fillMaxWidth(),
        maxLines = 3
    )
}

// Reused UI Components

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
