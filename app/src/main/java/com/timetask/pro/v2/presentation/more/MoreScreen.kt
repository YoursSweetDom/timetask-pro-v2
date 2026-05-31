package com.timetask.pro.v2.presentation.more

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.timetask.pro.v2.ui.theme.Spacing
import com.timetask.pro.v2.ui.theme.TitaniumPrimary

/**
 * Экран «Ещё» — хаб для настроек и дополнительных функций.
 * Доступен через нижнюю навигацию (⋯).
 */
@Composable
fun MoreScreen(
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = remember {
        listOf(
            MoreItem(
                icon = Icons.Outlined.Settings,
                title = "Настройки",
                subtitle = "Тема, язык, уведомления",
                tint = TitaniumPrimary,
                onClick = onNavigateToSettings,
            ),
            MoreItem(
                icon = Icons.Outlined.Palette,
                title = "Внешний вид",
                subtitle = "Тема, акцентный цвет",
                tint = Color(0xFF9C27B0),
                onClick = onNavigateToSettings, // TODO: separate appearance screen
            ),
            MoreItem(
                icon = Icons.Outlined.BarChart,
                title = "Статистика",
                subtitle = "Продуктивность, отчёты",
                tint = Color(0xFF4CAF50),
                onClick = {}, // TODO: navigate to stats
            ),
            MoreItem(
                icon = Icons.AutoMirrored.Outlined.HelpOutline,
                title = "Помощь",
                subtitle = "FAQ, обратная связь",
                tint = Color(0xFFFF9800),
                onClick = {},
            ),
            MoreItem(
                icon = Icons.Outlined.Info,
                title = "О приложении",
                subtitle = "TimeTask Pro v2.0",
                tint = Color(0xFF607D8B),
                onClick = {},
            ),
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        item {
            Spacer(modifier = Modifier.height(Spacing.sm))
        }

        items.forEach { item ->
            item {
                MoreCard(item = item)
            }
        }

        item {
            Spacer(modifier = Modifier.height(Spacing.md))
        }
    }
}

// ============================================================
// Internal
// ============================================================

private data class MoreItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val tint: Color,
    val onClick: () -> Unit,
)

@Composable
private fun MoreCard(item: MoreItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = item.onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = item.tint,
                modifier = Modifier.size(28.dp),
            )

            Spacer(modifier = Modifier.width(Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
