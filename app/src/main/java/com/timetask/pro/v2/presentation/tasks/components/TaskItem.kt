package com.timetask.pro.v2.presentation.tasks.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timetask.pro.v2.data.local.db.entity.Priority
import com.timetask.pro.v2.data.local.db.entity.TagEntity
import com.timetask.pro.v2.data.local.db.entity.TaskEntity
import com.timetask.pro.v2.data.local.db.entity.TaskStatus
import com.timetask.pro.v2.domain.model.tasks.TaskCheckboxColorMode
import com.timetask.pro.v2.ui.theme.Spacing
import com.timetask.pro.v2.ui.theme.TitaniumError
import com.timetask.pro.v2.ui.theme.TitaniumPrimary
import com.timetask.pro.v2.ui.theme.TitaniumSuccess
import com.timetask.pro.v2.ui.theme.TitaniumWarning
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TaskItem(
    task: TaskEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onPin: () -> Unit,
    onMove: () -> Unit,
    onDateClick: () -> Unit,
    onToolsClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDragging: Boolean = false,
    isDropTarget: Boolean = false,
    subtaskCount: Int = 0,
    completedSubtaskCount: Int = 0,
    isExpanded: Boolean = false,
    onExpandToggle: (() -> Unit)? = null,
    isSubtask: Boolean = false,
    hideDetails: Boolean = false,
    hideSubtasks: Boolean = false,
    groupName: String? = null,
    groupColor: String? = null,
    tags: List<TagEntity> = emptyList(),
    checkboxColorMode: TaskCheckboxColorMode = TaskCheckboxColorMode.DEFAULT,
    isMultiSelectMode: Boolean = false,
    isSelected: Boolean = false,
    isArchiveMode: Boolean = false,
) {
    val isDone = task.status == TaskStatus.DONE

    // Effective priority: either explicit priority or derived from Eisenhower quadrant
    val effectivePriority = remember(task.priority, task.quadrant) {
        if (task.priority != Priority.NONE) {
            task.priority
        } else when (task.quadrant) {
            1 -> Priority.HIGH    // Срочно и Важно
            2 -> Priority.MEDIUM  // Важно, Не срочно
            3 -> Priority.LOW     // Срочно, Неважно
            else -> Priority.NONE
        }
    }
    val priorityColor = remember(effectivePriority) { effectivePriority.toColor() }

    // Drop-target border animation
    val borderColor by animateColorAsState(
        targetValue = if (isDropTarget) TitaniumPrimary else Color.Transparent,
        animationSpec = tween(200),
        label = "dropTargetBorder"
    )

    // Expand/collapse arrow animation
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 90f else 0f,
        animationSpec = tween(200),
        label = "expandArrow"
    )

    CustomSwipeToReveal(
        modifier = modifier,
        leftMenuWidth = if (isArchiveMode) 0.dp else 120.dp,
        rightMenuWidth = if (isArchiveMode) 0.dp else 180.dp,
        leftMenuContent = {
            IconButton(
                onClick = onPin,
                modifier = Modifier
                    .width(60.dp)
                    .fillMaxHeight()
                    .background(TitaniumPrimary.copy(alpha = 0.2f))
            ) {
                Icon(Icons.Default.PushPin, "Закрепить", tint = TitaniumPrimary)
            }
            IconButton(
                onClick = onToolsClick,
                modifier = Modifier
                    .width(60.dp)
                    .fillMaxHeight()
                    .background(TitaniumWarning.copy(alpha = 0.2f))
            ) {
                Icon(Icons.Default.MoreTime, "Инструменты", tint = TitaniumWarning)
            }
        },
        rightMenuContent = {
            IconButton(
                onClick = onDateClick,
                modifier = Modifier
                    .width(60.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(Icons.Default.CalendarMonth, "Дата", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            IconButton(
                onClick = onMove,
                modifier = Modifier
                    .width(60.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(Icons.Default.DriveFileMove, "Переместить", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .width(60.dp)
                    .fillMaxHeight()
                    .background(TitaniumError.copy(alpha = 0.2f))
            ) {
                Icon(Icons.Default.Delete, "Удалить", tint = TitaniumError)
            }
        }
    ) {
        val hasFolderColor = groupColor != null && groupName != "Входящие"
        val folderColor = if (hasFolderColor) {
            try { Color(android.graphics.Color.parseColor(groupColor)) } catch (_: Exception) { Color.Transparent }
        } else Color.Transparent
        val showStripe = hasFolderColor && !isSubtask
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .then(
                    if (isDropTarget) Modifier.border(2.dp, borderColor, RoundedCornerShape(12.dp))
                    else Modifier
                )
                .background(
                    if (isSubtask) MaterialTheme.colorScheme.surfaceContainerLow
                    else MaterialTheme.colorScheme.surfaceContainer
                )
                .then(
                    if (showStripe) {
                        Modifier.drawBehind {
                            val stripePx = 3.dp.toPx()
                            val padPx = 8.dp.toPx()
                            drawRoundRect(
                                color = folderColor,
                                topLeft = androidx.compose.ui.geometry.Offset(0f, padPx),
                                size = androidx.compose.ui.geometry.Size(stripePx, size.height - padPx * 2),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
                            )
                        }
                    } else Modifier
                )
                .clickable(onClick = onClick)
                .padding(vertical = Spacing.xs),
            verticalAlignment = Alignment.Top,
        ) {

            // ≡ Drag Handle
            if (!isSubtask && !isMultiSelectMode) {
                Icon(
                    imageVector = Icons.Default.DragIndicator,
                    contentDescription = "Перетащить",
                    tint = if (isDragging) {
                        TitaniumPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                    },
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(20.dp)
                        .align(Alignment.CenterVertically)
                )
            } else if (!isMultiSelectMode) {
                Spacer(Modifier.width(12.dp))
            }

            // Checkbox / Restore / Multi-select
            if (isArchiveMode) {
                IconButton(
                    onClick = { onToggle() },
                    modifier = Modifier.padding(start = if (isSubtask) 8.dp else 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = "Восстановить",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else if (isMultiSelectMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = null,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    modifier = Modifier.padding(start = if (isSubtask) 12.dp else 4.dp)
                )
            } else {
                val uncheckedCurrentColor = if (effectivePriority != Priority.NONE) {
                    priorityColor
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
                Checkbox(
                    checked = isDone,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = TitaniumSuccess,
                        uncheckedColor = uncheckedCurrentColor,
                        checkmarkColor = MaterialTheme.colorScheme.surface,
                    ),
                )
            }

            // ══════════════════════════════════════
            // Content Column (4 Layers)
            // ══════════════════════════════════════
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = Spacing.xs),
                verticalArrangement = Arrangement.Center,
            ) {
                // ──────────────────────────────────
                // Layer 1: Title
                // ──────────────────────────────────
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isDone) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onBackground
                    },
                    textDecoration = if (isDone) TextDecoration.LineThrough else null,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                // ──────────────────────────────────
                // Layer 2: Description (if any)
                // ──────────────────────────────────
                if (!hideDetails && task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // ──────────────────────────────────
                // Layer 3: Tags (FlowRow)
                // ──────────────────────────────────
                if (tags.isNotEmpty() && !isDone) {
                    Spacer(Modifier.height(4.dp))
                    com.timetask.pro.v2.presentation.components.TagPillList(
                        tags = tags,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // ──────────────────────────────────
                // Layer 4: Meta row (date, tools, time, folder)
                // ──────────────────────────────────
                val hasMetaContent = (task.dueDate != null && !isDone) ||
                    (task.totalSpentTimeMs > 0) ||
                    (task.progressPercent > 0) ||
                    (groupName != null && groupName != "Входящие" && !isDone)

                if (hasMetaContent) {
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Due date + reminder icon
                        if (task.dueDate != null && !isDone) {
                            val dateColor = if (task.dueDate < System.currentTimeMillis()) {
                                TitaniumError
                            } else {
                                TitaniumPrimary
                            }
                            Text(
                                text = formatSmartDate(task.dueDate),
                                style = MaterialTheme.typography.labelSmall,
                                color = dateColor,
                                fontSize = 11.sp
                            )
                            // Reminder icon (temporary: show if dueDate exists)
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = "Напоминание",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        // Spent time
                        if (task.totalSpentTimeMs > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "⏱",
                                    fontSize = 10.sp,
                                )
                                Text(
                                    text = formatSpentTime(task.totalSpentTimeMs),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Progress %
                        if (task.progressPercent > 0) {
                            Text(
                                text = "${task.progressPercent}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = TitaniumPrimary,
                                fontSize = 11.sp
                            )
                        }

                        // Spacer to push folder to the right
                        Spacer(Modifier.weight(1f))

                        // Group/Folder indicator (right-aligned)
                        if (groupName != null && groupName != "Входящие" && !isDone) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                val gColor = groupColor?.let { Color(android.graphics.Color.parseColor(it)) }
                                    ?: MaterialTheme.colorScheme.onSurfaceVariant
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(gColor)
                                )
                                Text(
                                    text = groupName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }

                // Progress bar (if percent > 0)
                if (task.progressPercent > 0) {
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { task.progressPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = TitaniumPrimary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }

            // ══════════════════════════════════════
            // Right side: Pin + Subtask expand
            // ══════════════════════════════════════

            // Pin indicator
            if (task.isPinned) {
                Icon(
                    imageVector = Icons.Filled.PushPin,
                    contentDescription = "Закреплено",
                    tint = TitaniumPrimary,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(end = Spacing.xs),
                )
            }

            // Expand/collapse arrow + subtask count (done/total)
            if (!hideSubtasks && subtaskCount > 0 && onExpandToggle != null) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onExpandToggle() }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "$completedSubtaskCount/$subtaskCount",
                        style = MaterialTheme.typography.labelSmall,
                        color = TitaniumPrimary,
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = if (isExpanded) "Свернуть" else "Развернуть",
                        tint = TitaniumPrimary,
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(arrowRotation),
                    )
                }
            }

            Spacer(Modifier.width(4.dp))
        } // Row (card content)
    } // CustomSwipeToReveal
} // TaskItem

// ============================================================
// Priority → Color
// ============================================================
private fun Priority.toColor() = when (this) {
    Priority.HIGH -> TitaniumError
    Priority.MEDIUM -> TitaniumWarning
    Priority.LOW -> TitaniumPrimary
    Priority.NONE -> TitaniumPrimary
}

// ============================================================
// Smart Date Formatting ("Сегодня", "Завтра", "Вчера", + time)
// ============================================================
private fun formatSmartDate(timestampMs: Long): String {
    val now = Calendar.getInstance()
    val date = Calendar.getInstance().apply { timeInMillis = timestampMs }

    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val taskDay = Calendar.getInstance().apply {
        timeInMillis = timestampMs
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val dayDiff = ((taskDay.timeInMillis - today.timeInMillis) / (24 * 60 * 60 * 1000)).toInt()

    val dayLabel = when (dayDiff) {
        0 -> "Сегодня"
        1 -> "Завтра"
        -1 -> "Вчера"
        else -> SimpleDateFormat("dd MMM", Locale("ru")).format(Date(timestampMs))
    }

    // If the task has a specific time (not midnight / 00:00), show it
    val hour = date.get(Calendar.HOUR_OF_DAY)
    val minute = date.get(Calendar.MINUTE)
    return if (hour != 0 || minute != 0) {
        "$dayLabel, ${String.format("%02d:%02d", hour, minute)}"
    } else {
        dayLabel
    }
}

// ============================================================
// Spent Time Formatting
// ============================================================
private fun formatSpentTime(ms: Long): String {
    if (ms <= 0) return "0м"
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60

    return when {
        hours > 0 && minutes > 0 -> "${hours}h${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}
