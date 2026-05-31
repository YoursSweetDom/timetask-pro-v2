package com.timetask.pro.v2.presentation.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.timetask.pro.v2.presentation.calendar.components.CalendarGrid
import com.timetask.pro.v2.presentation.calendar.components.DayTaskItem
import com.timetask.pro.v2.ui.theme.Spacing
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(viewModel: CalendarViewModel) {

    val currentMonth by viewModel.currentMonth.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val daysWithTasks by viewModel.daysWithTasks.collectAsState()
    val tasksForDay by viewModel.tasksForSelectedDate.collectAsState()

    var showEditSheetForTask by remember { androidx.compose.runtime.mutableStateOf<com.timetask.pro.v2.data.local.db.entity.TaskEntity?>(null) }

    val selectedDateTitle = remember(selectedDate) {
        val dayOfWeek = selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("ru"))
            .replaceFirstChar { it.uppercaseChar() }
        "$dayOfWeek, ${selectedDate.dayOfMonth} ${selectedDate.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))}"
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Calendar grid
        CalendarGrid(
            currentMonth = currentMonth,
            selectedDate = selectedDate,
            daysWithTasks = daysWithTasks,
            onDateSelected = remember { { date: java.time.LocalDate -> viewModel.selectDate(date) } },
            onPreviousMonth = remember { { viewModel.previousMonth() } },
            onNextMonth = remember { { viewModel.nextMonth() } },
            onToday = remember { { viewModel.goToToday() } },
            modifier = Modifier.padding(horizontal = Spacing.sm),
        )

        Spacer(Modifier.height(Spacing.sm))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        Spacer(Modifier.height(Spacing.sm))

        // Selected date header
        Text(
            text = selectedDateTitle,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = Spacing.md),
        )

        Spacer(Modifier.height(Spacing.sm))

        // Tasks for selected date
        if (tasksForDay.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.EventBusy,
                        contentDescription = null,
                        modifier = Modifier.size(Spacing.xl),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    )
                    Spacer(Modifier.height(Spacing.sm))
                    Text(
                        text = "Нет задач на этот день",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.xs),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                items(items = tasksForDay, key = { it.id }) { task ->
                    val onToggle = remember(task.id) { { viewModel.toggleTask(task) } }
                    DayTaskItem(
                        task = task,
                        onToggle = onToggle,
                        onClick = { showEditSheetForTask = task }
                    )
                }
            }
        }
    }

    // Bottom Sheet: Edit Task
    showEditSheetForTask?.let { task ->
        com.timetask.pro.v2.presentation.tasks.components.EditTaskSheet(
            task = task,
            onDismiss = { showEditSheetForTask = null },
            onSave = { title, description, quadrant, pinMode, progress, _ ->
                viewModel.updateTask(
                    task.copy(
                        title = title,
                        description = description,
                        quadrant = quadrant,
                        pinMode = pinMode,
                        progressPercent = progress
                    )
                )
                showEditSheetForTask = null
            }
        )
    }
}
