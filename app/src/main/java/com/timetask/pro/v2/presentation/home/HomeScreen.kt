package com.timetask.pro.v2.presentation.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timetask.pro.v2.data.local.db.entity.TaskEntity
import com.timetask.pro.v2.data.local.db.entity.TaskStatus
import com.timetask.pro.v2.ui.theme.Spacing
import com.timetask.pro.v2.ui.theme.TitaniumPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Home Dashboard — главный экран приложения.
 *
 * Содержит:
 * - Приветствие с текущей датой
 * - Сводка (активные / сегодня / просроченные / выполненные)
 * - Список задач на сегодня
 * - Просроченные задачи (если есть)
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToTasks: () -> Unit = {},
) {
    val activeTasks by viewModel.activeTasks.collectAsStateWithLifecycle()
    val todayTasks by viewModel.todayTasks.collectAsStateWithLifecycle()
    val overdueTasks by viewModel.overdueTasks.collectAsStateWithLifecycle()
    val completedCount by viewModel.completedCount.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        // ========== Greeting ==========
        GreetingSection()

        // ========== Summary cards ==========
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            SummaryCard(
                icon = Icons.Outlined.Checklist,
                value = "${activeTasks.size}",
                label = "Активные",
                color = TitaniumPrimary,
                modifier = Modifier.weight(1f),
            )
            SummaryCard(
                icon = Icons.Outlined.CalendarToday,
                value = "${todayTasks.size}",
                label = "Сегодня",
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            SummaryCard(
                icon = Icons.Outlined.Warning,
                value = "${overdueTasks.size}",
                label = "Просрочено",
                color = if (overdueTasks.isNotEmpty()) Color(0xFFEF5350) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            SummaryCard(
                icon = Icons.Outlined.CheckCircleOutline,
                value = "$completedCount",
                label = "Выполнено",
                color = Color(0xFF66BB6A),
                modifier = Modifier.weight(1f),
            )
        }

        // ========== Today section ==========
        if (todayTasks.isNotEmpty()) {
            TaskSection(
                title = "📅 Задачи на сегодня",
                tasks = todayTasks,
                onNavigateToTasks = onNavigateToTasks,
            )
        }

        // ========== Overdue section ==========
        if (overdueTasks.isNotEmpty()) {
            TaskSection(
                title = "⚠️ Просроченные",
                tasks = overdueTasks,
                accentColor = Color(0xFFEF5350),
                onNavigateToTasks = onNavigateToTasks,
            )
        }

        // ========== Empty state ==========
        if (activeTasks.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Text(
                        text = "Нет активных задач",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Создай задачу во вкладке «Задачи»",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // ========== Quick navigation ==========
        if (activeTasks.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToTasks() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = TitaniumPrimary.copy(alpha = 0.1f),
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Checklist,
                        contentDescription = null,
                        tint = TitaniumPrimary,
                    )
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Text(
                        text = "Перейти к задачам →",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TitaniumPrimary,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.lg))
    }
}

// ============================================================
// Components
// ============================================================

@Composable
private fun GreetingSection() {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 6 -> "Доброй ночи 🌙"
        hour < 12 -> "Доброе утро ☀️"
        hour < 18 -> "Добрый день 🌤"
        else -> "Добрый вечер 🌆"
    }

    val dateFormat = SimpleDateFormat("EEEE, d MMMM", Locale("ru"))
    val dateString = dateFormat.format(calendar.time)
        .replaceFirstChar { it.uppercase() }

    Column {
        Text(
            text = greeting,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = dateString,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SummaryCard(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = color,
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = color,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TaskSection(
    title: String,
    tasks: List<TaskEntity>,
    accentColor: Color = TitaniumPrimary,
    onNavigateToTasks: () -> Unit = {},
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            tasks.take(5).forEachIndexed { index, task ->
                if (index > 0) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToTasks() }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = task.status == TaskStatus.DONE,
                        onCheckedChange = null,
                        modifier = Modifier.size(24.dp),
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF4CAF50),
                        ),
                    )
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (task.status == TaskStatus.DONE) {
                            TextDecoration.LineThrough
                        } else {
                            TextDecoration.None
                        },
                        color = if (task.status == TaskStatus.DONE) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                }
            }

            if (tasks.size > 5) {
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = "ещё ${tasks.size - 5}...",
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    modifier = Modifier.clickable { onNavigateToTasks() },
                )
            }
        }
    }
}
