package com.timetask.pro.v2.presentation.tools.chrono

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.timetask.pro.v2.domain.model.chrono.Stopwatch
import com.timetask.pro.v2.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LapsBottomSheet(
    stopwatch: Stopwatch?,
    onDismiss: () -> Unit,
    onEditLap: (lapId: String, newName: String, newColor: Int?) -> Unit,
    onDeleteLap: (lapId: String) -> Unit,
    onRestoreLap: (lapId: String) -> Unit
) {
    if (stopwatch == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var editingLapId by remember { mutableStateOf<String?>(null) }
    var swipedLapId by remember { mutableStateOf<String?>(null) }

    if (editingLapId != null) {
        val lapToEdit = stopwatch.laps.find { it.id == editingLapId }
        if (lapToEdit != null) {
            EditLapDialog(
                lap = lapToEdit,
                onDismiss = { editingLapId = null },
                onSave = { name, color ->
                    onEditLap(lapToEdit.id, name, color)
                    editingLapId = null
                }
            )
        } else {
            editingLapId = null
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f) // Take up most of the screen so swipes are easy
                .pointerInput(Unit) {
                    detectTapGestures {
                        swipedLapId = null
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Spacing.md)
                    .navigationBarsPadding(),
            ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Круги: ${stopwatch.name}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Закрыть",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(Spacing.sm))

            if (stopwatch.laps.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Нет сохраненных кругов", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(stopwatch.laps, key = { it.id }) { lap ->
                        LapSwipeableItem(
                            lap = lap,
                            isSwipedOpen = swipedLapId == lap.id,
                            onSwipeStateChanged = { isOpen ->
                                if (isOpen) swipedLapId = lap.id
                                else if (swipedLapId == lap.id) swipedLapId = null
                            },
                            onEdit = { 
                                swipedLapId = null
                                editingLapId = lap.id 
                            },
                            onDelete = { 
                                swipedLapId = null
                                onDeleteLap(lap.id)
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Круг удален",
                                        actionLabel = "Отмена",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        onRestoreLap(lap.id)
                                    }
                                }
                            }
                        )
                    }
                }
            } // end else
        } // Finish Column

        // Local SnackbarHost
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )
    } // Finish Box
} // Finish ModalBottomSheet
} // Finish LapsBottomSheet

