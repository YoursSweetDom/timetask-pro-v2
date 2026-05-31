package com.timetask.pro.v2.presentation.sidebar.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.timetask.pro.v2.ui.theme.Spacing

/**
 * Temporary model for pinned items. In reality, these come from different lists (Folders, Tags, Categories, Filters).
 */
data class PinnedItemState(
    val id: String,
    val name: String,
    val icon: @Composable () -> Unit,
    val color: Color? = null
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun SidebarPinnedSection(
    pinnedItems: List<PinnedItemState>,
    onItemClick: (String) -> Unit,
    onItemLongPress: (PinnedItemState) -> Unit,
    modifier: Modifier = Modifier
) {
    if (pinnedItems.isEmpty()) return

    // Show up to 10 items in max 2 rows. If more, display an expand arrow.
    var isExpanded by remember { mutableStateOf(false) }
    
    val maxItemsPreview = 9
    val hasMore = pinnedItems.size > maxItemsPreview
    val itemsToShow = if (isExpanded || !hasMore) pinnedItems else pinnedItems.take(maxItemsPreview)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.sm)
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            maxLines = if (isExpanded) Int.MAX_VALUE else 2
        ) {
            itemsToShow.forEach { item ->
                PinnedSquareItem(
                    item = item,
                    onClick = { onItemClick(item.id) },
                    onLongPress = { onItemLongPress(item) }
                )
            }

            if (hasMore && !isExpanded) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable { isExpanded = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Показать больше",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PinnedSquareItem(
    item: PinnedItemState,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            )
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(12.dp)
                )
                .background(item.color?.copy(alpha = 0.1f) ?: MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            item.icon()
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            // Simple truncation if the name is too long for the square.
        )
    }
}
