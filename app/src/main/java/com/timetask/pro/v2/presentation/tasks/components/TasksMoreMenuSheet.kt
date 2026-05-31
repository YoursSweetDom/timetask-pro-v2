package com.timetask.pro.v2.presentation.tasks.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.timetask.pro.v2.domain.model.tasks.TasksGroupingOrder
import com.timetask.pro.v2.domain.model.tasks.TasksSortOrder
import com.timetask.pro.v2.domain.model.tasks.TasksViewMode
import com.timetask.pro.v2.domain.model.tasks.TasksViewPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksMoreMenuSheet(
    preferences: TasksViewPreferences,
    onDismiss: () -> Unit,
    onUpdatePreferences: (TasksViewPreferences) -> Unit,
    onSelectModeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showSortMenu by remember { mutableStateOf(false) }
    var showGroupMenu by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Вид",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // View Mode
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ViewModeButton(
                    icon = Icons.Default.Subject,
                    label = "Список",
                    isSelected = preferences.viewMode == TasksViewMode.LIST,
                    onClick = { onUpdatePreferences(preferences.copy(viewMode = TasksViewMode.LIST)) },
                    modifier = Modifier.weight(1f)
                )
                ViewModeButton(
                    icon = Icons.Default.ViewAgenda,
                    label = "Канбан",
                    isSelected = preferences.viewMode == TasksViewMode.KANBAN,
                    onClick = { onUpdatePreferences(preferences.copy(viewMode = TasksViewMode.KANBAN)) },
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Toggles
            MenuToggleRow(
                icon = Icons.Default.Subject,
                label = "Показать детали",
                checked = !preferences.hideDetails,
                onCheckedChange = { onUpdatePreferences(preferences.copy(hideDetails = !it)) }
            )
            MenuToggleRow(
                icon = Icons.Outlined.CheckCircle,
                label = "Скрыть выполненные",
                checked = preferences.hideCompleted,
                onCheckedChange = { onUpdatePreferences(preferences.copy(hideCompleted = it)) }
            )
            MenuToggleRow(
                icon = Icons.Default.VisibilityOff,
                label = "Скрыть подзадачи",
                checked = preferences.hideSubtasks,
                onCheckedChange = { onUpdatePreferences(preferences.copy(hideSubtasks = it)) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Actions
            Box {
                MenuActionRow(
                    icon = Icons.Default.Sort,
                    label = "Сортировка",
                    value = translateSortOrder(preferences.sortOrder),
                    onClick = { showSortMenu = true }
                )
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    TasksSortOrder.values().forEach { order ->
                        DropdownMenuItem(
                            text = { Text(translateSortOrder(order)) },
                            onClick = { 
                                onUpdatePreferences(preferences.copy(sortOrder = order))
                                showSortMenu = false
                            }
                        )
                    }
                }
            }

            Box {
                MenuActionRow(
                    icon = Icons.Default.Dashboard,
                    label = "Группировка",
                    value = translateGroupingOrder(preferences.groupingOrder),
                    onClick = { showGroupMenu = true }
                )
                DropdownMenu(
                    expanded = showGroupMenu,
                    onDismissRequest = { showGroupMenu = false }
                ) {
                    TasksGroupingOrder.values().forEach { order ->
                        if (order != TasksGroupingOrder.NONE) { // Hide 'NONE' as it's meant for simple list view and we offer LIST
                            DropdownMenuItem(
                                text = { Text(translateGroupingOrder(order)) },
                                onClick = { 
                                    onUpdatePreferences(preferences.copy(groupingOrder = order))
                                    showGroupMenu = false
                                }
                            )
                        }
                    }
                }
            }



            MenuActionRow(
                icon = Icons.Default.Image,
                label = "Фон",
                onClick = { /* TODO: open background selector */ }
            )
            MenuActionRow(
                icon = Icons.Default.Checklist,
                label = "Выбрать",
                onClick = onSelectModeClick
            )
        }
    }
}

@Composable
private fun ViewModeButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    
    androidx.compose.material3.Surface(
        color = bgColor,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
            .padding(4.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(4.dp))
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = color)
        }
    }
}

@Composable
private fun MenuToggleRow(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = label,
            modifier = Modifier.weight(1f).padding(start = 16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun MenuActionRow(
    icon: ImageVector,
    label: String,
    value: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = label,
            modifier = Modifier.weight(1f).padding(start = 16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun translateSortOrder(order: TasksSortOrder): String = when(order) {
    TasksSortOrder.DATE -> "По дате"
    TasksSortOrder.NAME -> "По названию"
    TasksSortOrder.TAG -> "По меткам"
    TasksSortOrder.PRIORITY -> "По приоритету"
}

private fun translateGroupingOrder(order: TasksGroupingOrder): String = when(order) {
    TasksGroupingOrder.LIST -> "Списки"
    TasksGroupingOrder.DATE -> "Даты"
    TasksGroupingOrder.TAG -> "Метки"
    TasksGroupingOrder.PRIORITY -> "Приоритет"
    TasksGroupingOrder.NONE -> "Без группы"
}
