package com.timetask.pro.v2.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.timetask.pro.v2.ui.theme.Spacing
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip

/**
 * Описание одного элемента Quick Access.
 */
@Immutable
data class QuickAccessItem(
    val icon: ImageVector,
    val label: String,
    val tabIndex: Int,
)

/**
 * Список всех инструментов для Quick Access меню.
 */
val quickAccessItems = listOf(
    QuickAccessItem(Icons.Outlined.Timer, "Таймер", 0),
    QuickAccessItem(Icons.Outlined.Watch, "Хроно", 1),
    QuickAccessItem(Icons.Outlined.Alarm, "Будильник", 2),
    QuickAccessItem(Icons.Outlined.Notifications, "Напомин.", 3),
    QuickAccessItem(Icons.Outlined.BarChart, "Стат.", 4),
)

/**
 * Popup меню для быстрого доступа к инструментам.
 * Появляется над кнопкой «Инструменты» при Long Press.
 *
 * @param visible показывать ли popup
 * @param highlightedIndex индекс подсвеченного элемента (-1 = ничего)
 */
@Composable
fun ToolsQuickAccessPopup(
    visible: Boolean,
    highlightedIndex: Int,
    modifier: Modifier = Modifier,
    onItemClick: ((Int) -> Unit)? = null,
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            animationSpec = tween(150),
            transformOrigin = TransformOrigin(0.5f, 1f), // масштаб снизу-вверх
        ) + fadeIn(tween(150)),
        exit = scaleOut(
            animationSpec = tween(100),
            transformOrigin = TransformOrigin(0.5f, 1f),
        ) + fadeOut(tween(100)),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                )
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(16.dp),
                )
                .padding(horizontal = Spacing.sm, vertical = Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            quickAccessItems.forEachIndexed { index, item ->
                val isHighlighted = index == highlightedIndex

                val backgroundColor = if (isHighlighted) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                }

                val contentColor = if (isHighlighted) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            color = backgroundColor,
                            shape = RoundedCornerShape(12.dp),
                        )
                        .clickable(enabled = onItemClick != null) {
                            onItemClick?.invoke(index)
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp), // Reduced padding for better fit
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp),
                        tint = contentColor,
                    )
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor,
                    )
                }
            }
        }
    }
}
