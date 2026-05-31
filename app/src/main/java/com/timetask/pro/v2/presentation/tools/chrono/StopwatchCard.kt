package com.timetask.pro.v2.presentation.tools.chrono

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timetask.pro.v2.ui.theme.Spacing
import com.timetask.pro.v2.ui.theme.TitaniumPrimary

import com.timetask.pro.v2.domain.model.chrono.Stopwatch

/**
 * Карточка одного секундомера (компактная, для grid 2 в ряд).
 *
 * Содержит:
 * - Имя
 * - Время HH:MM:SS.ms (крупно, Monospace)
 * - Кнопки: Start/Pause, Reset, Lap
 * - Список кругов (если есть)
 * - Контекстное меню ⋮
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StopwatchCard(
    stopwatch: Stopwatch,
    nowMs: Long,
    taskNames: Map<Long, String> = emptyMap(),
    onStartPause: () -> Unit,
    onReset: () -> Unit,
    onLap: () -> Unit,
    onDelete: () -> Unit,
    onSettingsClick: () -> Unit,
    onLapsClick: () -> Unit,
    onToggleNotification: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }

    val accentColor by animateColorAsState(
        targetValue = when (stopwatch.state) {
            StopwatchState.RUNNING -> Color(0xFF4CAF50)
            StopwatchState.PAUSED -> Color(0xFFFFC107)
            StopwatchState.IDLE -> TitaniumPrimary
        },
        animationSpec = tween(300),
        label = "swAccent",
    )

    val alpha = if (stopwatch.state == StopwatchState.PAUSED) {
        val infiniteTransition = rememberInfiniteTransition(label = "swBlink")
        val blinkAlpha by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(600),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "swBlinkAlpha",
        )
        blinkAlpha
    } else {
        1f
    }

    Card(
        modifier = modifier.defaultMinSize(minHeight = 220.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.sm),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "STOPWATCH",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stopwatch.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    // Linked tasks chips
                    val linkedTaskIds = remember(stopwatch.linkedTaskIdsJson) {
                        try {
                            val jsonArray = org.json.JSONArray(stopwatch.linkedTaskIdsJson)
                            List(jsonArray.length()) { jsonArray.getLong(it) }
                        } catch (e: Exception) { emptyList() }
                    }

                    if (linkedTaskIds.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            linkedTaskIds.forEach { taskId ->
                                val taskName = taskNames[taskId] ?: "Задача #$taskId"
                                AssistChip(
                                    onClick = { },
                                    label = { Text(taskName, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    border = null,
                                    modifier = Modifier.height(24.dp)
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        Icons.Outlined.MoreVert,
                        contentDescription = "Меню",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                    ) {
                        androidx.compose.material3.DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("В шторке", style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.width(Spacing.md))
                                    androidx.compose.material3.Switch(
                                        checked = stopwatch.notification.showInNotifications,
                                        onCheckedChange = {
                                            showMenu = false
                                            onToggleNotification(it)
                                        }
                                    )
                                }
                            },
                            onClick = {
                                // Click handled by the Switch itself, but we can also trigger toggle
                                onToggleNotification(!stopwatch.notification.showInNotifications)
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Edit, null, Modifier.size(18.dp))
                                    Text("  Редактировать")
                                }
                            },
                            onClick = {
                                showMenu = false
                                onSettingsClick()
                            },
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Outlined.Delete, null,
                                        Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.error,
                                    )
                                    Text(
                                        "  Удалить",
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }
                            },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xs))

            // Time display
            val currentElapsed = stopwatch.computeElapsedMs(nowMs)
            Text(
                text = formatStopwatch(currentElapsed),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                style = androidx.compose.ui.text.TextStyle(
                    fontFeatureSettings = "tnum",
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.sm)
                    .alpha(alpha),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Controls: Reset + Play/Pause + Lap
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Reset
                FilledIconButton(
                    onClick = onReset,
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    enabled = stopwatch.state != StopwatchState.IDLE,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Сброс", Modifier.size(18.dp))
                }

                // Play / Pause
                FilledIconButton(
                    onClick = onStartPause,
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = accentColor,
                        contentColor = Color.Black,
                    ),
                ) {
                    Icon(
                        imageVector = if (stopwatch.state == StopwatchState.RUNNING) {
                            Icons.Filled.Pause
                        } else {
                            Icons.Filled.PlayArrow
                        },
                        contentDescription = if (stopwatch.state == StopwatchState.RUNNING) "Пауза" else "Старт",
                        modifier = Modifier.size(24.dp),
                    )
                }

                // Lap
                FilledIconButton(
                    onClick = onLap,
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    enabled = stopwatch.state == StopwatchState.RUNNING,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Icon(Icons.Filled.Flag, contentDescription = "Круг", Modifier.size(18.dp))
                }
            }

            // Laps
            if (stopwatch.laps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.xs))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(vertical = Spacing.xs),
                )

                Box(modifier = Modifier.fillMaxWidth().clickable { onLapsClick() }) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 100.dp),
                        userScrollEnabled = false,
                    ) {
                    itemsIndexed(stopwatch.laps) { _, lap ->
                        val lapColor = when {
                            lap.colorARGB != null -> Color(lap.colorARGB)
                            lap.isBest -> Color(0xFF4CAF50) // Green for Best
                            lap.isWorst -> Color(0xFFF44336) // Red for Worst
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "#${lap.lapNumber} ${lap.title.orEmpty()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = lapColor,
                            )
                            Text(
                                text = formatStopwatch(lap.lapTimeMs),
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = lapColor,
                            )
                        }
                    }
                }
                }
            }
        }
    }
}

// ============================================================
// Formatting
// ============================================================

internal fun formatStopwatch(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    val centiseconds = (ms % 1000) / 10
    return "%02d:%02d:%02d.%02d".format(hours, minutes, seconds, centiseconds)
}
