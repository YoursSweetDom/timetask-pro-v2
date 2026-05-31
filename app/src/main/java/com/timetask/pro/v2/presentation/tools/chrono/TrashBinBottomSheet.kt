package com.timetask.pro.v2.presentation.tools.chrono

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.timetask.pro.v2.domain.model.chrono.Lap
import com.timetask.pro.v2.domain.model.chrono.Stopwatch
import com.timetask.pro.v2.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashBinBottomSheet(
    show: Boolean,
    stopwatches: List<Stopwatch>,
    laps: List<Lap>,
    onDismiss: () -> Unit,
    onRestoreStopwatch: (String) -> Unit,
    onHardDeleteStopwatch: (String) -> Unit,
    onRestoreLap: (String) -> Unit,
    onHardDeleteLap: (String) -> Unit
) {
    if (!show) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Секундомеры (${stopwatches.size})", "Круги (${laps.size})")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f) // 80% screen height
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
                    text = "Корзина",
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

            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(Spacing.sm))

            if (selectedTabIndex == 0) {
                if (stopwatches.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Корзина секундомеров пуста")
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(stopwatches, key = { it.id }) { sw ->
                            TrashItemRow(
                                title = sw.name.ifBlank { "Без имени" },
                                subtitle = "Добавлен в корзину: " + (sw.deletedAt?.let { formatTime(it) } ?: "Неизвестно"),
                                onRestore = { onRestoreStopwatch(sw.id) },
                                onHardDelete = { onHardDeleteStopwatch(sw.id) }
                            )
                        }
                    }
                }
            } else {
                if (laps.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Корзина кругов пуста")
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(laps, key = { it.id }) { lap ->
                            TrashItemRow(
                                title = "Круг #${lap.lapNumber} ${lap.title.orEmpty()}",
                                subtitle = "Удален: " + (lap.deletedAt?.let { formatTime(it) } ?: "Неизвестно"),
                                onRestore = { onRestoreLap(lap.id) },
                                onHardDelete = { onHardDeleteLap(lap.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrashItemRow(
    title: String,
    subtitle: String,
    onRestore: () -> Unit,
    onHardDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row {
            IconButton(onClick = onRestore) {
                Icon(Icons.Default.Restore, contentDescription = "Восстановить", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onHardDelete) {
                Icon(Icons.Default.DeleteForever, contentDescription = "Удалить навсегда", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val date = java.util.Date(ms)
    val formatter = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
    return formatter.format(date)
}
