package com.timetask.pro.v2.presentation.sidebar.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.timetask.pro.v2.domain.model.TaskFilter

@Composable
fun SidebarSmartLists(
    quickListItems: List<String>,
    currentFilter: TaskFilter?,
    onFilterSelected: (TaskFilter) -> Unit,
    onItemLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        quickListItems.forEach { item ->
            when (item) {
                "ALL" -> SidebarItem(
                    label = "Все",
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    selected = currentFilter is TaskFilter.All,
                    onClick = { onFilterSelected(TaskFilter.All) },
                    onLongPress = onItemLongClick
                )
                "TODAY" -> SidebarItem(
                    label = "Сегодня",
                    icon = { Icon(Icons.Default.WbSunny, contentDescription = null) },
                    selected = currentFilter is TaskFilter.Today,
                    onClick = { onFilterSelected(TaskFilter.Today) },
                    onLongPress = onItemLongClick
                )
                "TOMORROW" -> SidebarItem(
                    label = "Завтра",
                    icon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    selected = currentFilter is TaskFilter.Tomorrow,
                    onClick = { onFilterSelected(TaskFilter.Tomorrow) },
                    onLongPress = onItemLongClick
                )
                "NEXT_7_DAYS" -> SidebarItem(
                    label = "Следующие 7 дней",
                    icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    selected = currentFilter is TaskFilter.Next7Days,
                    onClick = { onFilterSelected(TaskFilter.Next7Days) },
                    onLongPress = onItemLongClick
                )
                "INBOX" -> SidebarItem(
                    label = "Входящие",
                    icon = { Icon(Icons.Default.Inbox, contentDescription = null) },
                    selected = currentFilter is TaskFilter.Inbox,
                    onClick = { onFilterSelected(TaskFilter.Inbox) },
                    onLongPress = onItemLongClick
                )
            }
        }
    }
}
