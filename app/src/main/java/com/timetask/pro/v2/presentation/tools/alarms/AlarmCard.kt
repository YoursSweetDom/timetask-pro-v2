package com.timetask.pro.v2.presentation.tools.alarms

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timetask.pro.v2.ui.theme.Spacing
import com.timetask.pro.v2.ui.theme.TitaniumPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AlarmCard(
    alarm: AlarmInstance,
    tags: List<com.timetask.pro.v2.data.local.db.entity.TagEntity> = emptyList(),
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }
    var showNotesDialog by remember { mutableStateOf(false) }

    val baseColor = if (alarm.colorARGB != null) Color(alarm.colorARGB) else TitaniumPrimary
    val accentColor by animateColorAsState(
        targetValue = if (alarm.isEnabled) baseColor else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(300),
        label = "alarmAccentColor"
    )

    // Live Countdown
    var remainingText by remember { mutableStateOf("") }

    LaunchedEffect(alarm.isEnabled, alarm.nextTriggerTime) {
        if (!alarm.isEnabled || alarm.nextTriggerTime == 0L) {
            remainingText = "Выключен"
        } else {
            while (isActive) {
                val diffMs = alarm.nextTriggerTime - System.currentTimeMillis()
                if (diffMs > 0) {
                    val totalSecs = diffMs / 1000
                    val d = totalSecs / (24 * 3600)
                    val h = (totalSecs % (24 * 3600)) / 3600
                    val m = (totalSecs % 3600) / 60
                    val s = totalSecs % 60
                    
                    remainingText = when {
                        d > 0 -> "Осталось: %dд %02dч %02dм %02dс".format(d, h, m, s)
                        h > 0 -> "Осталось: %dч %02dм %02dс".format(h, m, s)
                        else -> "Осталось: %02dм %02dс".format(m, s)
                    }
                } else {
                    remainingText = "Срабатывание!"
                }
                delay(1000L) // update once a second
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 220.dp),
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
                    val headerText = if (alarm.categoryText.isNotBlank()) alarm.categoryText.uppercase() else "БУДИЛЬНИК"
                    Text(
                        text = headerText,
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = alarm.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    
                    if (tags.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        com.timetask.pro.v2.presentation.components.TagPillList(tags = tags)
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
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Edit, null, Modifier.size(18.dp))
                                    Text("  Редактировать")
                                }
                            },
                            onClick = {
                                showMenu = false
                                onEdit()
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

            // HH:MM Display + Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "%02d:%02d".format(alarm.hour, alarm.minute),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = if (alarm.isEnabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    },
                )

                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = accentColor,
                        checkedTrackColor = accentColor.copy(alpha = 0.3f),
                    ),
                )
            }

            // Countdown text
            Text(
                text = remainingText,
                style = MaterialTheme.typography.bodySmall,
                color = if (alarm.isEnabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            // Metadata Chips (Tags, Category)
            val tags = alarm.tagsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val chipColor = FilterChipDefaults.filterChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Почва для меток
            if (tags.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (tags.isNotEmpty()) {
                        FilterChip(
                            selected = false,
                            onClick = { },
                            label = { Text("🏷 ${tags.first()}", fontSize = 11.sp) },
                            modifier = Modifier.height(24.dp),
                            colors = chipColor
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            } else {
                // Если нет ни меток ни категории, оставляем пустое место структурно.
                // Ничего не рендерим, как и просил пользователь ("пока меток нет, там ничего не должно быть тогда").
            }

            // Repeat status
            val repeatStr = remember(alarm.repeatDaysMask) {
                if (alarm.repeatDaysMask == 0) "1 раз"
                else if (alarm.repeatDaysMask == 127) "Каждый день"
                else {
                    val dayNames = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
                    dayNames.filterIndexed { index, _ -> (alarm.repeatDaysMask and (1 shl index)) != 0 }
                        .joinToString(", ")
                }
            }

            FilterChip(
                selected = false,
                onClick = { },
                label = { 
                    Text(text = repeatStr, fontSize = 11.sp) 
                },
                modifier = Modifier.height(24.dp),
                colors = chipColor
            )

            // Notes
            if (alarm.notesText.isNotBlank()) {
                Spacer(modifier = Modifier.height(Spacing.xs))
                // Make the text clickable to show full notes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📝 ${alarm.notesText}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showNotesDialog = true }
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }
    }

    if (showNotesDialog) {
        AlertDialog(
            onDismissRequest = { showNotesDialog = false },
            title = {
                Text(text = "Заметки будильника")
            },
            text = {
                Text(
                    text = alarm.notesText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                TextButton(onClick = { showNotesDialog = false }) {
                    Text("Закрыть")
                }
            }
        )
    }
}
