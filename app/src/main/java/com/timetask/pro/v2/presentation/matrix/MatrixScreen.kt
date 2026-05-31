package com.timetask.pro.v2.presentation.matrix

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.timetask.pro.v2.ui.theme.Spacing
import com.timetask.pro.v2.ui.theme.TitaniumPrimary

@Composable
fun MatrixScreen(viewModel: MatrixViewModel) {
    val quadrants by viewModel.quadrants.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.sm),
    ) {
        // 2×2 Grid
        if (quadrants.size >= 4) {
            // Top row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                QuadrantCard(
                    quadrant = quadrants[0],
                    onToggle = remember { { task: com.timetask.pro.v2.data.local.db.entity.TaskEntity -> viewModel.toggleTask(task) } },
                    modifier = Modifier.weight(1f),
                )
                QuadrantCard(
                    quadrant = quadrants[1],
                    onToggle = remember { { task: com.timetask.pro.v2.data.local.db.entity.TaskEntity -> viewModel.toggleTask(task) } },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(Spacing.sm))

            // Bottom row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                QuadrantCard(
                    quadrant = quadrants[2],
                    onToggle = remember { { task: com.timetask.pro.v2.data.local.db.entity.TaskEntity -> viewModel.toggleTask(task) } },
                    modifier = Modifier.weight(1f),
                )
                QuadrantCard(
                    quadrant = quadrants[3],
                    onToggle = remember { { task: com.timetask.pro.v2.data.local.db.entity.TaskEntity -> viewModel.toggleTask(task) } },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun QuadrantCard(
    quadrant: Quadrant,
    onToggle: (com.timetask.pro.v2.data.local.db.entity.TaskEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = quadrant.color.copy(alpha = 0.1f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.sm),
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = quadrant.emoji,
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.width(Spacing.xs))
                Column {
                    Text(
                        text = quadrant.title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = quadrant.color,
                    )
                    Text(
                        text = quadrant.subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
            }

            Spacer(Modifier.height(Spacing.xs))

            // Tasks list
            if (quadrant.tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Пусто",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    items(items = quadrant.tasks, key = { it.id }) { task ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            IconButton(
                                onClick = { onToggle(task) },
                                modifier = Modifier.size(24.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Circle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = quadrant.color,
                                )
                            }
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            // Task count
            Text(
                text = "${quadrant.tasks.size} задач",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
        }
    }
}
