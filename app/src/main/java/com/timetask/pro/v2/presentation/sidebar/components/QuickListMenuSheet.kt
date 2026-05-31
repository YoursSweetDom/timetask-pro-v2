package com.timetask.pro.v2.presentation.sidebar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.timetask.pro.v2.ui.theme.Spacing
import com.timetask.pro.v2.ui.theme.TitaniumPrimary
import com.timetask.pro.v2.data.local.db.entity.FolderEntity
import com.timetask.pro.v2.data.local.db.entity.TagEntity
import com.timetask.pro.v2.data.local.db.entity.CategoryEntity
import com.timetask.pro.v2.data.local.db.entity.FilterEntity
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FilterList
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

val ALL_QUICK_LIST_ITEMS = listOf("ALL", "TODAY", "TOMORROW", "NEXT_7_DAYS", "INBOX")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickListMenuSheet(
    currentItems: List<String>,
    folders: List<FolderEntity> = emptyList(),
    tags: List<TagEntity> = emptyList(),
    categories: List<CategoryEntity> = emptyList(),
    filters: List<FilterEntity> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Local state for checking/unchecking items
    var selectedItems by remember { mutableStateOf(currentItems.toSet()) }

    // 1. Maintain the ordered list of ONLY selected items so reordering works cleanly
    var orderedSelectedItems by remember(currentItems) {
        mutableStateOf(currentItems.toMutableList())
    }

    // 2. Data grouping for unselected items
    val systemItems = remember {
        ALL_QUICK_LIST_ITEMS.map { id ->
            val label = when (id) {
                "ALL" -> "Все"
                "TODAY" -> "Сегодня"
                "TOMORROW" -> "Завтра"
                "NEXT_7_DAYS" -> "Следующие 7 дней"
                "INBOX" -> "Входящие"
                else -> id
            }
            id to label
        }
    }
    
    val folderItems = remember(folders) { folders.map { "FOLDER_${it.id}" to it.name } }
    val tagItems = remember(tags) { tags.map { "TAG_${it.id}" to it.name } }
    val categoryItems = remember(categories) { categories.map { "CATEGORY_${it.id}" to it.name } }
    val filterItems = remember(filters) { filters.map { "FILTER_${it.id}" to it.name } }

    // Function to get label for any ID
    fun getLabelForId(id: String): String {
        return systemItems.find { it.first == id }?.second ?:
               folderItems.find { it.first == id }?.second ?:
               tagItems.find { it.first == id }?.second ?:
               categoryItems.find { it.first == id }?.second ?:
               filterItems.find { it.first == id }?.second ?: id
    }

    val quickListLazyListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(quickListLazyListState) { from, to ->
        val fromKey = from.key as? String ?: return@rememberReorderableLazyListState
        val toKey = to.key as? String ?: return@rememberReorderableLazyListState

        val fromIndex = orderedSelectedItems.indexOf(fromKey)
        val toIndex = orderedSelectedItems.indexOf(toKey)

        if (fromIndex != -1 && toIndex != -1) {
            orderedSelectedItems = orderedSelectedItems.toMutableList().apply {
                add(toIndex, removeAt(fromIndex))
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md)
                .navigationBarsPadding(),
        ) {
            Text(
                text = "Быстрые списки",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                text = "Выберите, какие списки показывать в сайдбаре",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(Spacing.md))

            LazyColumn(
                state = quickListLazyListState,
                modifier = Modifier
                    .weight(1f, fill = false)
            ) {
                // Section 1: Selected Items (Reorderable)
                if (orderedSelectedItems.isNotEmpty()) {
                    item {
                        Text(
                            text = "Выбранные",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = Spacing.sm)
                        )
                    }
                    items(orderedSelectedItems, key = { it }) { itemId ->
                        val itemLabel = getLabelForId(itemId)
                        
                        ReorderableItem(reorderState, key = itemId) { isDragging ->
                            val elevation = if (isDragging) 8.dp else 0.dp
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .shadow(elevation, RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedItems = selectedItems - itemId
                                        orderedSelectedItems = orderedSelectedItems.toMutableList().apply { remove(itemId) }
                                    }
                                    .padding(horizontal = Spacing.sm),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = true,
                                    onCheckedChange = null,
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = TitaniumPrimary
                                    )
                                )
                                Spacer(Modifier.width(Spacing.md))
                                
                                val icon = when {
                                    itemId == "ALL" -> Icons.Default.List
                                    itemId == "TODAY" -> Icons.Default.WbSunny
                                    itemId == "TOMORROW" -> Icons.Default.CalendarToday
                                    itemId == "NEXT_7_DAYS" -> Icons.Default.DateRange
                                    itemId == "INBOX" -> Icons.Default.Inbox
                                    itemId.startsWith("FOLDER_") -> Icons.Default.Folder
                                    itemId.startsWith("TAG_") -> Icons.Default.Label
                                    itemId.startsWith("CATEGORY_") -> Icons.Default.Category
                                    itemId.startsWith("FILTER_") -> Icons.Default.FilterList
                                    else -> Icons.Default.List
                                }
                                
                                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.width(Spacing.md))
                                Text(
                                    text = itemLabel,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                
                                Spacer(Modifier.weight(1f))
                                Icon(
                                    Icons.Default.DragHandle, 
                                    contentDescription = "Перетащить", 
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.longPressDraggableHandle()
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(Spacing.md))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(Modifier.height(Spacing.md))
                }

                // Section 2: Available Items Groups
                val renderGroup = @Composable { title: String, groupItems: List<Pair<String, String>> ->
                    val unselected = groupItems.filter { !selectedItems.contains(it.first) }
                    if (unselected.isNotEmpty()) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = Spacing.sm)
                        )
                        unselected.forEach { (itemId, itemLabel) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedItems = selectedItems + itemId
                                        orderedSelectedItems = orderedSelectedItems.toMutableList().apply { add(itemId) }
                                    }
                                    .padding(horizontal = Spacing.sm),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = false,
                                    onCheckedChange = null,
                                )
                                Spacer(Modifier.width(Spacing.md))
                                
                                val icon = when {
                                    itemId == "ALL" -> Icons.Default.List
                                    itemId == "TODAY" -> Icons.Default.WbSunny
                                    itemId == "TOMORROW" -> Icons.Default.CalendarToday
                                    itemId == "NEXT_7_DAYS" -> Icons.Default.DateRange
                                    itemId == "INBOX" -> Icons.Default.Inbox
                                    itemId.startsWith("FOLDER_") -> Icons.Default.Folder
                                    itemId.startsWith("TAG_") -> Icons.Default.Label
                                    itemId.startsWith("CATEGORY_") -> Icons.Default.Category
                                    itemId.startsWith("FILTER_") -> Icons.Default.FilterList
                                    else -> Icons.Default.List
                                }
                                
                                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.width(Spacing.md))
                                Text(
                                    text = itemLabel,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                        Spacer(Modifier.height(Spacing.sm))
                    }
                }

                item { renderGroup("СИСТЕМНЫЕ", systemItems) }
                item { renderGroup("ПАПКИ", folderItems) }
                item { renderGroup("МЕТКИ", tagItems) }
                item { renderGroup("КАТЕГОРИИ", categoryItems) }
                item { renderGroup("ФИЛЬТРЫ", filterItems) }
            }

            Spacer(Modifier.height(Spacing.md))

            // Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.sm),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = {
                    val allAvail = systemItems.map { it.first } + 
                                   folderItems.map { it.first } + 
                                   tagItems.map { it.first } + 
                                   categoryItems.map { it.first } + 
                                   filterItems.map { it.first }
                    
                    val newSelection = mutableSetOf<String>()
                    val newOrder = mutableListOf<String>()
                    
                    allAvail.forEach { id ->
                        if (!selectedItems.contains(id)) {
                            newSelection.add(id)
                            newOrder.add(id)
                        }
                    }
                    
                    selectedItems = selectedItems + newSelection
                    orderedSelectedItems = (orderedSelectedItems + newOrder).toMutableList()
                }) {
                    Text("Выбрать все", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onDismiss) {
                    Text("Отмена", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.width(Spacing.sm))
                Button(
                    onClick = {
                        // Save checked items in their chosen order
                        onSave(orderedSelectedItems.toList()) 
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TitaniumPrimary,
                    ),
                ) {
                    Text("Сохранить")
                }
            }
        }
    }
}
