package com.timetask.pro.v2.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AllInbox
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.NextWeek
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.timetask.pro.v2.domain.model.TaskFilter
import com.timetask.pro.v2.ui.theme.Spacing
import com.timetask.pro.v2.data.local.db.entity.FolderEntity
import com.timetask.pro.v2.data.local.db.entity.TagEntity
import com.timetask.pro.v2.data.local.db.entity.CategoryEntity
import com.timetask.pro.v2.data.local.db.entity.FilterEntity

data class TaskQuickAccessItem(
    val id: String,
    val icon: ImageVector,
    val label: String,
    val filter: TaskFilter
)

fun getTaskQuickAccessItem(
    id: String,
    folders: List<FolderEntity> = emptyList(),
    tags: List<TagEntity> = emptyList(),
    categories: List<CategoryEntity> = emptyList(),
    filters: List<FilterEntity> = emptyList(),
): TaskQuickAccessItem {
    return when {
        id == "ALL" -> TaskQuickAccessItem("ALL", Icons.Outlined.AllInbox, "Все", TaskFilter.All)
        id == "TODAY" -> TaskQuickAccessItem("TODAY", Icons.Outlined.CalendarToday, "Сегодня", TaskFilter.Today)
        id == "TOMORROW" -> TaskQuickAccessItem("TOMORROW", Icons.Outlined.DateRange, "Завтра", TaskFilter.Tomorrow)
        id == "NEXT_7_DAYS" -> TaskQuickAccessItem("NEXT_7_DAYS", Icons.Outlined.NextWeek, "7 Дней", TaskFilter.Next7Days)
        id == "INBOX" -> TaskQuickAccessItem("INBOX", Icons.Outlined.Inbox, "Входящие", TaskFilter.Inbox)
        id.startsWith("FOLDER_") -> {
            val folderId = id.removePrefix("FOLDER_").toLongOrNull() ?: 0L
            val folder = folders.find { it.id == folderId }
            TaskQuickAccessItem(id, Icons.Outlined.Folder, folder?.name ?: "Папка", TaskFilter.Folder(folderId))
        }
        id.startsWith("TAG_") -> {
            val tagId = id.removePrefix("TAG_").toLongOrNull() ?: 0L
            val tag = tags.find { it.id == tagId }
            TaskQuickAccessItem(id, Icons.Outlined.Label, tag?.name ?: "Метка", TaskFilter.Tag(tagId))
        }
        id.startsWith("CATEGORY_") -> {
            val catId = id.removePrefix("CATEGORY_").toLongOrNull() ?: 0L
            val cat = categories.find { it.id == catId }
            TaskQuickAccessItem(id, Icons.Outlined.Category, cat?.name ?: "Категория", TaskFilter.Category(catId))
        }
        id.startsWith("FILTER_") -> {
            val filterId = id.removePrefix("FILTER_").toLongOrNull() ?: 0L
            val filter = filters.find { it.id == filterId }
            TaskQuickAccessItem(id, Icons.Outlined.FilterList, filter?.name ?: "Фильтр", TaskFilter.Custom(filterId))
        }
        else -> TaskQuickAccessItem("INBOX", Icons.Outlined.Inbox, "Входящие", TaskFilter.Inbox)
    }
}

@Composable
fun TasksQuickAccessPopup(
    visible: Boolean,
    highlightedIndex: Int,
    quickListItems: List<String>,
    folders: List<FolderEntity> = emptyList(),
    tags: List<TagEntity> = emptyList(),
    categories: List<CategoryEntity> = emptyList(),
    filters: List<FilterEntity> = emptyList(),
    modifier: Modifier = Modifier,
    onItemClick: ((Int) -> Unit)? = null,
) {
    val items = quickListItems.map { getTaskQuickAccessItem(it, folders, tags, categories, filters) }

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            animationSpec = tween(150),
            transformOrigin = TransformOrigin(0.5f, 1f), // Bottom-left or center? Usually bottom-left for this popup
        ) + fadeIn(tween(150)),
        exit = scaleOut(
            animationSpec = tween(100),
            transformOrigin = TransformOrigin(0.5f, 1f),
        ) + fadeOut(tween(100)),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .width(200.dp) // Fixed width for vertical list
                .heightIn(max = 400.dp) // Max height to allow scrolling (> 10 items)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                )
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(16.dp),
                )
                .padding(vertical = 8.dp) // Vertical padding for list
                .verticalScroll(rememberScrollState()),
        ) {
            items.forEachIndexed { index, item ->
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = backgroundColor)
                        .clickable(enabled = onItemClick != null) {
                            onItemClick?.invoke(index)
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp),
                        tint = contentColor,
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor,
                    )
                }
            }
        }
    }
}
