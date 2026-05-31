package com.timetask.pro.v2.presentation.calendar.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.timetask.pro.v2.data.local.db.entity.TaskEntity
import com.timetask.pro.v2.data.local.db.entity.TaskStatus
import com.timetask.pro.v2.ui.theme.Spacing
import com.timetask.pro.v2.ui.theme.TitaniumPrimary
import com.timetask.pro.v2.ui.theme.TitaniumWarning
import com.timetask.pro.v2.ui.theme.TitaniumError
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Delete
import com.timetask.pro.v2.presentation.tasks.components.CustomSwipeToReveal
import androidx.compose.foundation.clickable

/**
 * Карточка задачи в списке дня (Calendar).
 */
@Composable
fun DayTaskItem(
    task: TaskEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit = {},
    onPin: () -> Unit = {},
    onMove: () -> Unit = {},
    onDateClick: () -> Unit = {},
    onToolsClick: () -> Unit = {},
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDone = task.status == TaskStatus.DONE
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        CustomSwipeToReveal(
            leftMenuWidth = 120.dp,
            rightMenuWidth = 180.dp,
            leftMenuContent = {
                IconButton(
                    onClick = onPin,
                    modifier = Modifier.width(60.dp).fillMaxHeight().background(TitaniumPrimary.copy(alpha = 0.2f))
                ) { Icon(Icons.Default.PushPin, "Закрепить", tint = TitaniumPrimary) }
                IconButton(
                    onClick = onToolsClick,
                    modifier = Modifier.width(60.dp).fillMaxHeight().background(TitaniumWarning.copy(alpha = 0.2f))
                ) { Icon(Icons.Default.MoreTime, "Инструменты", tint = TitaniumWarning) }
            },
            rightMenuContent = {
                IconButton(
                    onClick = onDateClick,
                    modifier = Modifier.width(60.dp).fillMaxHeight().background(MaterialTheme.colorScheme.primaryContainer)
                ) { Icon(Icons.Default.CalendarMonth, "Дата", tint = MaterialTheme.colorScheme.onPrimaryContainer) }
                IconButton(
                    onClick = onMove,
                    modifier = Modifier.width(60.dp).fillMaxHeight().background(MaterialTheme.colorScheme.surfaceVariant)
                ) { Icon(Icons.Default.DriveFileMove, "Переместить", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.width(60.dp).fillMaxHeight().background(TitaniumError.copy(alpha = 0.2f))
                ) { Icon(Icons.Default.Delete, "Удалить", tint = TitaniumError) }
            }
        ) {
            Row(
                modifier = Modifier
                    .padding(Spacing.sm)
                    .fillMaxWidth()
                    .clickable { onClick() },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onToggle,
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        imageVector = if (isDone) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                        contentDescription = null,
                        tint = if (isDone) {
                            TitaniumPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(22.dp),
                    )
                }

                Spacer(Modifier.width(Spacing.sm))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDone) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        textDecoration = if (isDone) TextDecoration.LineThrough else null,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    if (task.dueDate != null) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = timeFormat.format(java.util.Date(task.dueDate)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }
                }
            }
        }
    }
}
