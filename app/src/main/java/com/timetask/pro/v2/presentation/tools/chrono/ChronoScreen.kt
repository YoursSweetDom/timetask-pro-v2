package com.timetask.pro.v2.presentation.tools.chrono

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timetask.pro.v2.ui.theme.Spacing
import com.timetask.pro.v2.ui.theme.TitaniumPrimary
import com.timetask.pro.v2.presentation.util.LocalTopBarState
import androidx.compose.runtime.DisposableEffect
import kotlinx.coroutines.launch

/**
 * Экран мульти-секундомеров.
 *
 * - LazyVerticalGrid (2 колонки) с карточками.
 * - FAB «+» для создания нового секундомера.
 */
@Composable
fun ChronoScreen(
    viewModel: StopwatchViewModel,
    modifier: Modifier = Modifier,
) {
    val stopwatches by viewModel.stopwatches.collectAsStateWithLifecycle()
    val tickTrigger by viewModel.tickTrigger.collectAsStateWithLifecycle()
    var showSettingsSheet by remember { mutableStateOf(false) }
    var settingsStopwatchId by remember { mutableStateOf<String?>(null) }
    var showLapsFor by remember { mutableStateOf<String?>(null) }
    var showTrashBin by remember { mutableStateOf(false) }

    val taskNames by viewModel.taskNames.collectAsStateWithLifecycle()

    val deletedStopwatches by viewModel.deletedStopwatches.collectAsStateWithLifecycle()
    val deletedLaps by viewModel.deletedLaps.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Box(modifier = modifier.fillMaxSize()) {
        if (stopwatches.isEmpty()) {
            Text(
                text = "Нажми + чтобы создать секундомер",
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
                    items = stopwatches,
                    key = { it.id },
                ) { sw ->
                    StopwatchCard(
                        stopwatch = sw,
                        nowMs = tickTrigger,
                        taskNames = taskNames,
                        onStartPause = { viewModel.toggleStartPause(sw.id) },
                        onReset = { viewModel.resetStopwatch(sw.id) },
                        onLap = { viewModel.addLap(sw.id) },
                        onDelete = { 
                            showSettingsSheet = false
                            viewModel.removeStopwatch(sw.id)
                            
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Секундомер удален",
                                    actionLabel = "Отмена",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.restoreStopwatch(sw.id)
                                }
                            }
                        },
                        onSettingsClick = { 
                            settingsStopwatchId = sw.id
                            showSettingsSheet = true
                        },
                        onLapsClick = { showLapsFor = sw.id },
                        onToggleNotification = { show -> viewModel.toggleNotification(sw.id, show) }
                    )
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { 
                settingsStopwatchId = null
                showSettingsSheet = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Spacing.md),
            containerColor = TitaniumPrimary,
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Добавить секундомер")
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp) // Avoid FAB & NavBar overlapping
        )
    }

    val topBarState = LocalTopBarState.current
    val hasDeletedItems = deletedStopwatches.isNotEmpty() || deletedLaps.isNotEmpty()

    DisposableEffect(hasDeletedItems) {
        if (hasDeletedItems) {
            topBarState.actions = {
                androidx.compose.material3.IconButton(onClick = { showTrashBin = true }) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Корзина")
                }
            }
        } else {
            topBarState.actions = {}
        }
        onDispose {}
    }

    TrashBinBottomSheet(
        show = showTrashBin,
        stopwatches = deletedStopwatches,
        laps = deletedLaps,
        onDismiss = { showTrashBin = false },
        onRestoreStopwatch = viewModel::restoreStopwatch,
        onHardDeleteStopwatch = viewModel::hardDeleteStopwatch,
        onRestoreLap = viewModel::restoreLap,
        onHardDeleteLap = viewModel::hardDeleteLap
    )

    // Stopwatch Settings (Combined Create/Edit)
    val swForSettings = stopwatches.find { it.id == settingsStopwatchId }
    val tags by viewModel.tags.collectAsStateWithLifecycle()
    StopwatchSettingsSheet(
        showSheet = showSettingsSheet,
        stopwatch = swForSettings,
        onDismiss = { showSettingsSheet = false },
        onSave = { name, category, tagsStr, linkedTaskIds, showInNotifications ->
            if (swForSettings == null) {
                viewModel.addStopwatch(
                    name = name.ifBlank { "Секундомер" },
                    linkedTaskIds = linkedTaskIds
                )
                // We cannot pass showInNotifications instantly on create unless we modify the use case.
                // It defaults to true, which is fine for creations.
            } else {
                viewModel.updateStopwatchMetadata(
                    id = swForSettings.id,
                    name = name.ifBlank { "Секундомер" },
                    categoryText = category,
                    tagsText = tagsStr,
                    linkedTaskIds = linkedTaskIds
                )
                viewModel.toggleNotification(swForSettings.id, showInNotifications)
            }
        },
        onDelete = { id ->
            showSettingsSheet = false
            viewModel.removeStopwatch(id)
            
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "Секундомер удален",
                    actionLabel = "Отмена",
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.restoreStopwatch(id)
                }
            }
        },
        availableTags = tags,
        onCreateTag = { viewModel.addTag(it) },
    )

    // Laps Details & Swipe Actions Sheet
    val swForLaps = stopwatches.find { it.id == showLapsFor }
    LapsBottomSheet(
        stopwatch = swForLaps,
        onDismiss = { showLapsFor = null },
        onEditLap = { lapId, name, color -> 
            viewModel.editLap(lapId, name, color)
        },
        onDeleteLap = { lapId -> 
            viewModel.removeLap(lapId)
            
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "Круг удален",
                    actionLabel = "Отмена",
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.restoreLap(lapId)
                }
            }
        },
        onRestoreLap = viewModel::restoreLap
    )
}
