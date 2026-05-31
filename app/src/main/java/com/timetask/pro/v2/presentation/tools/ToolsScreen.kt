package com.timetask.pro.v2.presentation.tools

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import com.timetask.pro.v2.presentation.tools.alarms.AlarmsScreen
import com.timetask.pro.v2.presentation.tools.chrono.ChronoScreen
import com.timetask.pro.v2.presentation.tools.reminders.RemindersScreen
import com.timetask.pro.v2.presentation.tools.stats.StatsScreen
import com.timetask.pro.v2.presentation.tools.timers.TimerViewModel
import com.timetask.pro.v2.presentation.tools.timers.TimersScreen
import com.timetask.pro.v2.ui.theme.Spacing

private data class ToolTab(
    val title: String,
    val icon: ImageVector,
)

private val toolTabs = listOf(
    ToolTab("Таймеры", Icons.Outlined.Timer),
    ToolTab("Хроно", Icons.Outlined.Watch),
    ToolTab("Будильники", Icons.Outlined.Alarm),
    ToolTab("Напоминания", Icons.Outlined.Notifications),
    ToolTab("Статистика", Icons.Outlined.BarChart),
)

/**
 * Контейнер инструментов с 5 подтабами.
 * ScrollableTabRow + переключение содержимого.
 *
 * @param initialTab если >= 0, переключиться на этот таб (Quick Access)
 */
@Composable
fun ToolsScreen(initialTab: Int = -1) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    // Quick Access: если пришёл конкретный таб — переключиться
    LaunchedEffect(initialTab) {
        if (initialTab in 0..4) selectedTab = initialTab
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = Spacing.md,
        ) {
            toolTabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(tab.title) },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                        )
                    },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Content area
        when (selectedTab) {
            0 -> {
                val timerViewModel: TimerViewModel = viewModel()
                TimersScreen(
                    viewModel = timerViewModel,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            1 -> {
                val stopwatchViewModel: com.timetask.pro.v2.presentation.tools.chrono.StopwatchViewModel = viewModel()
                ChronoScreen(
                    viewModel = stopwatchViewModel,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            2 -> {
                val alarmViewModel: com.timetask.pro.v2.presentation.tools.alarms.AlarmViewModel = viewModel()
                AlarmsScreen(
                    viewModel = alarmViewModel,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            3 -> RemindersScreen(modifier = Modifier.fillMaxSize())
            4 -> StatsScreen(modifier = Modifier.fillMaxSize())
        }
    }
}
