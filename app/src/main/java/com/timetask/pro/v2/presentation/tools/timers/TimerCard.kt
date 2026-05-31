package com.timetask.pro.v2.presentation.tools.timers

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import com.timetask.pro.v2.data.local.db.entity.TimerEntity
import com.timetask.pro.v2.data.local.db.entity.TimerState
import com.timetask.pro.v2.ui.theme.Spacing
import com.timetask.pro.v2.ui.theme.TitaniumPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Карточка одного таймера (компактная, для grid 2 в ряд).
 *
 * Содержит:
 * - Метка «TIMER» + имя
 * - Время MM:SS (крупно, Monospace)
 * - Кнопки -15s / +15s
 * - Прогресс / Ends: HH:MM:SS
 * - Reset (✕) + Play/Pause
 * - Контекстное меню ⋮ (удалить, переименовать)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimerCard(
    timer: TimerEntity,
    tags: List<com.timetask.pro.v2.data.local.db.entity.TagEntity> = emptyList(),
    taskNames: Map<Long, String> = emptyMap(),
    onStartPause: () -> Unit,
    onStop: () -> Unit,
    onAdjust: (Long) -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }

    // ============================================================
    // Независимый тик-счётчик
    //
    // Корутина ключевана ТОЛЬКО на (id, state) → Play/Pause/Stop.
    // Adjust НИКОГДА не рестартует корутину.
    // endTimeMs передаётся через SideEffect → ref → корутина читает.
    // ============================================================
    val endTimeRef = remember { mutableLongStateOf(timer.endTimeMs) }
    val remainingRef = remember { mutableLongStateOf(timer.remainingMs) }
    val overtimeRef = remember { mutableStateOf(timer.config.enableOvertime) }

    // SideEffect — синхронизирует ref'ы после КАЖДОЙ рекомпозиции
    SideEffect {
        endTimeRef.longValue = timer.endTimeMs
        remainingRef.longValue = timer.remainingMs
        overtimeRef.value = timer.config.enableOvertime
    }

    var displayMs by remember { mutableLongStateOf(timer.remainingMs) }
    var projectedEndTime by remember { mutableLongStateOf(0L) }
    var elapsedMs by remember { mutableLongStateOf(0L) }

    // Тик-цикл: ключи ТОЛЬКО id + state → adjust не трогает корутину
    LaunchedEffect(timer.id, timer.state) {
        when (timer.state) {
            TimerState.RUNNING -> {
                while (isActive) {
                    val now = System.currentTimeMillis()
                    val end = endTimeRef.longValue
                    if (end > 0L) {
                        val raw = end - now
                        displayMs = if (!overtimeRef.value) raw.coerceAtLeast(0L) else raw
                        projectedEndTime = end
                    }
                    // Elapsed: независимо от adjust, считаем реальное время с момента старта
                    if (timer.startedAtMs > 0L) {
                        elapsedMs = (now - timer.startedAtMs - timer.accumulatedPauseMs).coerceAtLeast(0L)
                    }
                    delay(200L)
                }
            }
            TimerState.PAUSED, TimerState.IDLE -> {
                while (isActive) {
                    val rem = remainingRef.longValue
                    displayMs = rem
                    projectedEndTime = System.currentTimeMillis() + rem
                    // Elapsed при паузе: заморозить на моменте паузы
                    if (timer.state == TimerState.PAUSED && timer.pausedAtMs > 0L && timer.startedAtMs > 0L) {
                        elapsedMs = (timer.pausedAtMs - timer.startedAtMs - timer.accumulatedPauseMs).coerceAtLeast(0L)
                    } else {
                        elapsedMs = 0L
                    }
                    delay(500L)
                }
            }
            TimerState.FINISHED -> {
                displayMs = 0L
                elapsedMs = 0L
            }
        }
    }

    // Animated accent color based on state
    val accentColor by animateColorAsState(
        targetValue = when (timer.state) {
            TimerState.RUNNING -> Color(0xFF4CAF50) // Зелёный
            TimerState.PAUSED -> Color(0xFFFFC107) // Янтарный
            TimerState.FINISHED -> Color(0xFFF44336) // Красный
            TimerState.IDLE -> TitaniumPrimary
        },
        animationSpec = tween(300),
        label = "timerAccent",
    )

    // Мигание для состояния PAUSED
    val alpha = if (timer.state == TimerState.PAUSED) {
        val infiniteTransition = rememberInfiniteTransition(label = "blink")
        val blinkAlpha by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(600),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "blinkAlpha",
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
                .padding(Spacing.md),
        ) {
            // ============================================================
            // Header: TIMER label + name + menu
            // ============================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TIMER",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = timer.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    // Linked tasks chips
                    val linkedTaskIds = remember(timer.linkedTaskIdsJson) {
                        try {
                            val jsonArray = org.json.JSONArray(timer.linkedTaskIdsJson)
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

                    if (tags.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        com.timetask.pro.v2.presentation.components.TagPillList(tags = tags)
                    }
                }

                // ⋮ Меню
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
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Edit, null, Modifier.size(18.dp))
                                    Text("  Переименовать")
                                }
                            },
                            onClick = {
                                showMenu = false
                                onRename()
                            },
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Settings, null, Modifier.size(18.dp))
                                    Text("  Настройки")
                                }
                            },
                            onClick = {
                                showMenu = false
                                onSettings()
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

            Spacer(modifier = Modifier.height(Spacing.sm))

            // ============================================================
            // Основной дисплей (MM:SS) — крупно, моноширинный
            // Поддерживает отрицательное время (Overtime)
            // ============================================================
            com.timetask.pro.v2.presentation.components.AutoSizeText(
                text = formatTimerDisplay(displayMs),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = if (displayMs < 0) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.sm)
                    .align(Alignment.CenterHorizontally)
                    .alpha(alpha),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            // ============================================================
            // Кнопки ±adjust (динамический шаг из config)
            // ============================================================
            val stepSec = timer.config.adjustStepSec
            val stepMs = stepSec * 1000L
            val stepLabel = if (stepSec >= 60) "${stepSec / 60}m" else "${stepSec}s"

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = { onAdjust(-stepMs) },
                    modifier = Modifier.height(32.dp),
                    enabled = timer.state != TimerState.FINISHED,
                ) {
                    Text("-$stepLabel", fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.size(Spacing.xs))
                OutlinedButton(
                    onClick = { onAdjust(stepMs) },
                    modifier = Modifier.height(32.dp),
                    enabled = timer.state != TimerState.FINISHED,
                ) {
                    Text("+$stepLabel", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            // ============================================================
            // Прогресс-бар
            // ============================================================
            val progress = if (timer.totalDurationMs > 0) {
                (displayMs.toFloat() / timer.totalDurationMs).coerceIn(0f, 1f)
            } else {
                0f
            }

            LinearProgressIndicator(
                progress = { 1f - progress }, // Заполняется по мере прохождения времени
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                color = accentColor,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            )

            // ============================================================
            // Прогресс (сколько прошло)
            // ============================================================
            Text(
                text = formatElapsed(elapsedMs),
                style = MaterialTheme.typography.bodySmall,
                color = accentColor,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            // Время окончания — всегда видно
            val endTimeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
            if (timer.state != TimerState.FINISHED && projectedEndTime > 0L) {
                val prefix = if (timer.state == TimerState.RUNNING) "Окончание" else "≈ Окончание"
                Text(
                    text = "$prefix: ${endTimeFormat.format(Date(projectedEndTime))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            } else {
                Text(
                    text = "—:—:—",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            // ============================================================
            // Кнопки управления: Reset (X) + Play/Pause
            // ============================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Reset / Стоп
                FilledIconButton(
                    onClick = onStop,
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    enabled = timer.state != TimerState.IDLE,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Стоп", Modifier.size(20.dp))
                }

                // Play / Пауза
                FilledIconButton(
                    onClick = onStartPause,
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = accentColor,
                        contentColor = Color.Black,
                    ),
                ) {
                    Icon(
                        imageVector = if (timer.state == TimerState.RUNNING) {
                            Icons.Filled.Pause
                        } else {
                            Icons.Filled.PlayArrow
                        },
                        contentDescription = if (timer.state == TimerState.RUNNING) "Пауза" else "Старт",
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        }
    }
}

// ============================================================
// Форматирование
// ============================================================

/**
 * Форматирует миллисекунды в MM:SS (или -MM:SS для Overtime).
 */
internal fun formatTimerDisplay(ms: Long): String {
    // Guard: значения в зоне (-1с..0] → 00:00, не показывать "-00:01"
    if (ms in -999L..0L) return "00:00"
    val sign = if (ms < 0) "-" else ""
    val abs = kotlin.math.abs(ms)
    // Ceiling division — 299999ms → 300s (5:00), а не 299s (4:59)
    val totalSeconds = (abs + 999) / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$sign%02d:%02d".format(minutes, seconds)
}

/**
 * Форматирует прогресс в HH:MM:SS.
 */
private fun formatElapsed(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}
