package com.timetask.pro.v2.presentation.tools.timers


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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timetask.pro.v2.ui.theme.Spacing
import com.timetask.pro.v2.ui.theme.TitaniumPrimary

/**
 * Экран мульти-таймеров.
 *
 * - LazyVerticalGrid (2 колонки) с карточками.
 * - FAB «+ Add» для создания нового таймера.
 * - Диалог создания с пресетами.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimersScreen(
    viewModel: TimerViewModel,
    modifier: Modifier = Modifier,
) {
    val timers by viewModel.timers.collectAsStateWithLifecycle()
    val taskNames by viewModel.taskNames.collectAsStateWithLifecycle()
    val userPresets by viewModel.userPresets.collectAsStateWithLifecycle()
    val allTags by viewModel.tags.collectAsStateWithLifecycle()
    var showCreateSheet by remember { mutableStateOf(false) }
    var showRenameDialogFor by remember { mutableStateOf<String?>(null) }
    var showSettingsFor by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    var showOverlayPermissionDialog by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (!Settings.canDrawOverlays(context)) {
            showOverlayPermissionDialog = true
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (timers.isEmpty()) {
            // Empty state
            Text(
                text = "Нажми + чтобы создать таймер",
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
                    bottom = 80.dp, // space for FAB
                ),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                items(
                    items = timers,
                    // Use 'id' from TimerEntity
                    key = { it.id },
                ) { timer ->
                    val timerTags = remember(timer.tagIdsJson, allTags) {
                        try {
                            val jsonArray = org.json.JSONArray(timer.tagIdsJson)
                            val ids = List(jsonArray.length()) { jsonArray.getLong(it) }
                            allTags.filter { it.id in ids }
                        } catch (_: Exception) { emptyList() }
                    }
                    TimerCard(
                        timer = timer,
                        tags = timerTags,
                        taskNames = taskNames,
                        onStartPause = { viewModel.toggleStartPause(timer.id) },
                        onStop = { viewModel.stopTimer(timer.id) },
                        onAdjust = { delta -> viewModel.adjustTime(timer.id, delta) },
                        onDelete = { viewModel.removeTimer(timer.id) },
                        onRename = { showRenameDialogFor = timer.id },
                        onSettings = { showSettingsFor = timer.id },
                    )

                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { showCreateSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Spacing.md),
            containerColor = TitaniumPrimary,
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Добавить таймер")
        }
    }

    // ============================================================
    // Create Timer Sheet
    // ============================================================
    if (showCreateSheet) {
        val tags by viewModel.tags.collectAsStateWithLifecycle()
        CreateTimerSheet(
            onDismiss = { showCreateSheet = false },
            onAdd = { state ->
                viewModel.addTimer(state)
                showCreateSheet = false
            },
            availableTags = tags,
            onCreateTag = { viewModel.addTag(it) },
            userPresets = userPresets,
            onAddPreset = { name, emoji, durationMs ->
                viewModel.addUserPreset(name, emoji, durationMs)
            },
            onDeletePreset = { viewModel.deleteUserPreset(it) },
        )
    }

    // ============================================================
    // Rename Dialog
    // ============================================================
    showRenameDialogFor?.let { timerId ->
        val currentName = timers.find { it.id == timerId }?.name ?: ""
        var newName by remember(timerId) { mutableStateOf(currentName) }
        
        AlertDialog(
            onDismissRequest = { showRenameDialogFor = null },
            title = { Text("Переименовать") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Имя") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.renameTimer(timerId, newName.trim())
                    showRenameDialogFor = null
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialogFor = null }) {
                    Text("Отмена")
                }
            },
        )
    }

    // ============================================================
    // Edit Timer Sheet
    // ============================================================
    showSettingsFor?.let { timerId ->
        val timer = timers.find { it.id == timerId }
        if (timer != null) {
            val tags by viewModel.tags.collectAsStateWithLifecycle()
            EditTimerSheet(
                timer = timer,
                onDismiss = { showSettingsFor = null },
                onSave = { state ->
                    viewModel.updateFullTimer(timerId, state)
                },
                availableTags = tags,
                onCreateTag = { viewModel.addTag(it) },
                userPresets = userPresets,
                onAddPreset = { name, emoji, durationMs ->
                    viewModel.addUserPreset(name, emoji, durationMs)
                },
                onDeletePreset = { viewModel.deleteUserPreset(it) },
            )
        }
    }

    
    // ============================================================
    // Overlay Permission Dialog (Поверх других окон)
    // ============================================================
    if (showOverlayPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showOverlayPermissionDialog = false },
            title = { Text("Разрешение поверх окон") },
            text = { Text("Чтобы таймер громко звонил и открывался на весь экран (поверх браузера, игр и других приложений), необходимо выдать разрешение «Поверх других окон».\n\nБез него система Android заблокирует окно завершения таймера и вы увидите только маленькое уведомление.") },
            confirmButton = {
                TextButton(onClick = {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    context.startActivity(intent)
                    showOverlayPermissionDialog = false
                }) {
                    Text("Выдать")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOverlayPermissionDialog = false }) {
                    Text("Позже")
                }
            }
        )
    }
}

