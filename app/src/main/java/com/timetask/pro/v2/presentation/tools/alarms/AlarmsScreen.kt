package com.timetask.pro.v2.presentation.tools.alarms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timetask.pro.v2.ui.theme.Spacing
import com.timetask.pro.v2.ui.theme.TitaniumPrimary

/**
 * Экран мульти-будильников.
 *
 * - LazyVerticalGrid (2 колонки) с карточками.
 * - FAB «+» для создания нового будильника.
 */
@Composable
fun AlarmsScreen(
    viewModel: AlarmViewModel,
    modifier: Modifier = Modifier,
) {
    val alarms by viewModel.alarms.collectAsStateWithLifecycle()
    val allTags by viewModel.tags.collectAsStateWithLifecycle()
    var showAddSheet by remember { mutableStateOf(false) }
    var showEditSheetFor by remember { mutableStateOf<String?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        if (alarms.isEmpty()) {
            Text(
                text = "Нажми + чтобы создать будильник",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Center),
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    start = Spacing.sm,
                    end = Spacing.sm,
                    top = Spacing.sm,
                    bottom = 80.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                items(
                    items = alarms,
                    key = { it.id },
                ) { alarm ->
                    val alarmTags = remember(alarm.tagIds, allTags) {
                        allTags.filter { it.id in alarm.tagIds }
                    }
                    AlarmCard(
                        alarm = alarm,
                        tags = alarmTags,
                        onToggle = { viewModel.toggleAlarm(alarm.id) },
                        onDelete = { viewModel.removeAlarm(alarm.id) },
                        onEdit = { showEditSheetFor = alarm.id },
                    )

                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { showAddSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Spacing.md),
            containerColor = TitaniumPrimary,
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Добавить будильник")
        }
    }

    // Add Sheet
    if (showAddSheet) {
        val tags by viewModel.tags.collectAsStateWithLifecycle()
        CreateAlarmSheet(
            onDismiss = { showAddSheet = false },
            onAdd = { state ->
                viewModel.addAlarmState(state)
                showAddSheet = false
            },
            availableTags = tags,
            onCreateTag = { viewModel.addTag(it) },
        )
    }

    // Edit Sheet
    showEditSheetFor?.let { alarmId ->
        val alarm = alarms.find { it.id == alarmId }
        if (alarm != null) {
            val tags by viewModel.tags.collectAsStateWithLifecycle()
            EditAlarmSheet(
                alarm = alarm,
                onDismiss = { showEditSheetFor = null },
                onSave = { state ->
                    viewModel.updateAlarmState(alarmId, state)
                    showEditSheetFor = null
                },
                availableTags = tags,
                onCreateTag = { viewModel.addTag(it) },
            )
        }
    }

}
