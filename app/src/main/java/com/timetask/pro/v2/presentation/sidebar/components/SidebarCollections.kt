package com.timetask.pro.v2.presentation.sidebar.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.timetask.pro.v2.data.local.db.entity.CategoryEntity
import com.timetask.pro.v2.data.local.db.entity.FilterEntity
import com.timetask.pro.v2.data.local.db.entity.FolderEntity
import com.timetask.pro.v2.data.local.db.entity.TagEntity
import com.timetask.pro.v2.domain.model.FolderTreeNode
import com.timetask.pro.v2.domain.model.TaskFilter
import kotlinx.collections.immutable.ImmutableList

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items

fun LazyListScope.sidebarCollections(
    folders: ImmutableList<FolderEntity>,
    flatFolders: List<FolderTreeNode>,
    tags: ImmutableList<TagEntity>,
    filters: ImmutableList<FilterEntity>,
    categories: ImmutableList<CategoryEntity>,
    currentFilter: TaskFilter?,
    onFilterSelected: (TaskFilter) -> Unit,
    // Context Menu State
    contextTarget: ContextTarget?,
    onContextTargetChange: (ContextTarget?) -> Unit,
    showRenameDialog: Boolean,
    onShowRenameDialogChange: (Boolean) -> Unit,
    // CRUD callbacks
    onEditFolder: ((FolderEntity) -> Unit)? = null,
    onDeleteFolder: ((FolderEntity) -> Unit)? = null,
    onPinFolder: ((FolderEntity) -> Unit)? = null,
    onAddSubfolder: ((FolderEntity) -> Unit)? = null,
    onEditTag: ((TagEntity) -> Unit)? = null,
    onDeleteTag: ((TagEntity) -> Unit)? = null,
    onPinTag: ((TagEntity) -> Unit)? = null,
    onEditCategory: ((CategoryEntity) -> Unit)? = null,
    onDeleteCategory: ((CategoryEntity) -> Unit)? = null,
    onEditFilter: ((FilterEntity) -> Unit)? = null,
    onDeleteFilter: ((FilterEntity) -> Unit)? = null,
    onPinFilter: ((FilterEntity) -> Unit)? = null,
    // Reorder callbacks
    onReorderTags: ((List<Pair<Long, Int>>) -> Unit)? = null,
    onReorderCategories: ((List<Pair<Long, Int>>) -> Unit)? = null,
    onReorderFilters: ((List<Pair<Long, Int>>) -> Unit)? = null,
    onReorderFolders: ((List<Pair<Long, Int>>) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    item {
        // 1. Подписки на календари (Заглушка)
        SidebarAccordion(title = "Подписки на календари", initiallyExpanded = false, modifier = modifier) {
            SidebarItem(
                label = "Добавить календарь",
                icon = { Icon(Icons.Filled.Folder, contentDescription = null) },
                selected = false,
                onClick = { /* TODO */ }
            )
        }
    }

    item {
        // 2. Метки
        if (tags.isNotEmpty()) {
            var localTags by remember(tags) { mutableStateOf(tags.toList()) }
            SidebarAccordion(title = "Метки", modifier = modifier) {
                com.timetask.pro.v2.presentation.util.ReorderableColumn(
                    items = localTags,
                    onReorder = { from, to ->
                        val newList = localTags.toMutableList()
                        val item = newList.removeAt(from)
                        newList.add(to, item)
                        localTags = newList
                    },
                    onDragEnd = {
                        val reordered = localTags.mapIndexed { index, tag -> tag.id to index }
                        onReorderTags?.invoke(reordered)
                    },
                    key = { it.id }
                ) { tag, isDragging, dragModifier ->
                    Box {
                            SidebarItem(
                                label = tag.name,
                                icon = {
                                    if (tag.emoji != null) {
                                        Text(tag.emoji, style = MaterialTheme.typography.titleMedium)
                                    } else {
                                        Icon(Icons.Filled.Folder, contentDescription = null)
                                    }
                                },
                                selected = currentFilter is TaskFilter.Tag && currentFilter.id == tag.id,
                                dragModifier = dragModifier,
                                onClick = { onFilterSelected(TaskFilter.Tag(tag.id)) },
                                onLongPress = { onContextTargetChange(ContextTarget.TagTarget(tag)) }
                            )
                            if (contextTarget is ContextTarget.TagTarget && (contextTarget as ContextTarget.TagTarget).tag.id == tag.id) {
                                SidebarContextMenu(
                                    expanded = true,
                                    onDismissRequest = { onContextTargetChange(null) },
                                    onEdit = if (onEditTag != null) {{ onEditTag(tag); onContextTargetChange(null) }} else null,
                                    onPin = if (onPinTag != null) {{ onPinTag(tag); onContextTargetChange(null) }} else null,
                                    onDelete = if (onDeleteTag != null) {{ onDeleteTag(tag); onContextTargetChange(null) }} else null,
                                    isPinned = tag.isPinned,
                                )
                        }
                    }
                }
            }
        }
    }

    item {
        // 3. Фильтры
        if (filters.isNotEmpty()) {
            var localFilters by remember(filters) { mutableStateOf(filters.toList()) }
            SidebarAccordion(title = "Фильтры", modifier = modifier) {
                com.timetask.pro.v2.presentation.util.ReorderableColumn(
                    items = localFilters,
                    onReorder = { from, to ->
                        val newList = localFilters.toMutableList()
                        val item = newList.removeAt(from)
                        newList.add(to, item)
                        localFilters = newList
                    },
                    onDragEnd = {
                        val reordered = localFilters.mapIndexed { index, filter -> filter.id to index }
                        onReorderFilters?.invoke(reordered)
                    },
                    key = { it.id }
                ) { filter, isDragging, dragModifier ->
                    Box {
                            SidebarItem(
                                label = filter.name,
                                icon = {
                                    if (filter.icon != null) {
                                        Text(filter.icon, style = MaterialTheme.typography.titleMedium)
                                    } else {
                                        Icon(Icons.Filled.Folder, contentDescription = null)
                                    }
                                },
                                selected = currentFilter is TaskFilter.Custom && currentFilter.id == filter.id,
                                dragModifier = dragModifier,
                                onClick = { onFilterSelected(TaskFilter.Custom(filter.id)) },
                                onLongPress = { onContextTargetChange(ContextTarget.FilterTarget(filter)) }
                            )
                            if (contextTarget is ContextTarget.FilterTarget && (contextTarget as ContextTarget.FilterTarget).filter.id == filter.id) {
                                SidebarContextMenu(
                                    expanded = true,
                                    onDismissRequest = { onContextTargetChange(null) },
                                    onEdit = if (onEditFilter != null) {{ onEditFilter(filter); onContextTargetChange(null) }} else null,
                                    onPin = if (onPinFilter != null) {{ onPinFilter(filter); onContextTargetChange(null) }} else null,
                                    onDelete = if (onDeleteFilter != null) {{ onDeleteFilter(filter); onContextTargetChange(null) }} else null,
                                    isPinned = filter.isPinned,
                                )
                        }
                    }
                }
            }
        }
    }

    item {
        // 4. Категории
        if (categories.isNotEmpty()) {
            var localCategories by remember(categories) { mutableStateOf(categories.toList()) }
            SidebarAccordion(title = "Категории", modifier = modifier) {
                com.timetask.pro.v2.presentation.util.ReorderableColumn(
                    items = localCategories,
                    onReorder = { from, to ->
                        val newList = localCategories.toMutableList()
                        val item = newList.removeAt(from)
                        newList.add(to, item)
                        localCategories = newList
                    },
                    onDragEnd = {
                        val reordered = localCategories.mapIndexed { index, category -> category.id to index }
                        onReorderCategories?.invoke(reordered)
                    },
                    key = { it.id }
                ) { category, isDragging, dragModifier ->
                    Box {
                            SidebarItem(
                                label = category.name,
                                icon = {
                                    if (category.icon != null) {
                                        Text(category.icon, style = MaterialTheme.typography.titleMedium)
                                    } else {
                                        Icon(Icons.Filled.Folder, contentDescription = null)
                                    }
                                },
                                selected = currentFilter is TaskFilter.Category && currentFilter.id == category.id,
                                dragModifier = dragModifier,
                                onClick = { onFilterSelected(TaskFilter.Category(category.id)) },
                                onLongPress = { onContextTargetChange(ContextTarget.CategoryTarget(category)) }
                            )
                            if (contextTarget is ContextTarget.CategoryTarget && (contextTarget as ContextTarget.CategoryTarget).category.id == category.id) {
                                SidebarContextMenu(
                                    expanded = true,
                                    onDismissRequest = { onContextTargetChange(null) },
                                    onEdit = if (onEditCategory != null) {{ onEditCategory(category); onContextTargetChange(null) }} else null,
                                    onDelete = if (onDeleteCategory != null) {{ onDeleteCategory(category); onContextTargetChange(null) }} else null,
                                )
                        }
                    }
                }
            }
        }
    }

    item {
        // 5. Папки (с рекурсивной вложенностью)
        if (flatFolders.isNotEmpty()) {
            var localFolders by remember(flatFolders) { mutableStateOf(flatFolders.toList()) }
            SidebarAccordion(title = "Папки", modifier = modifier) {
                com.timetask.pro.v2.presentation.util.ReorderableColumn(
                    items = localFolders,
                    onReorder = { from, to ->
                        val newList = localFolders.toMutableList()
                        val item = newList.removeAt(from)
                        newList.add(to, item)
                        localFolders = newList
                    },
                    onDragEnd = {
                        val reordered = localFolders.mapIndexed { index, node -> node.folder.id to index }
                        onReorderFolders?.invoke(reordered)
                    },
                    key = { it.folder.id }
                ) { node, isDragging, dragModifier ->
                    Box {
                            SidebarItem(
                                label = node.folder.name,
                                icon = {
                                    if (node.folder.emoji != null) {
                                        Text(node.folder.emoji, style = MaterialTheme.typography.titleMedium)
                                    } else {
                                        Icon(Icons.Filled.Folder, contentDescription = null)
                                    }
                                },
                                selected = currentFilter is TaskFilter.Folder && currentFilter.id == node.folder.id,
                                dragModifier = dragModifier,
                                onClick = { onFilterSelected(TaskFilter.Folder(node.folder.id)) },
                                onLongPress = { onContextTargetChange(ContextTarget.FolderTarget(node.folder, node)) },
                                modifier = Modifier.padding(start = (node.level * 16).dp)
                            )
                            if (contextTarget is ContextTarget.FolderTarget && (contextTarget as ContextTarget.FolderTarget).folder.id == node.folder.id) {
                                SidebarContextMenu(
                                    expanded = true,
                                    onDismissRequest = { onContextTargetChange(null) },
                                    onEdit = if (onEditFolder != null) {{ onEditFolder(node.folder); onContextTargetChange(null) }} else null,
                                    onPin = if (onPinFolder != null) {{ onPinFolder(node.folder); onContextTargetChange(null) }} else null,
                                    onAddSubfolder = if (onAddSubfolder != null) {{ onAddSubfolder(node.folder); onContextTargetChange(null) }} else null,
                                    onDelete = if (onDeleteFolder != null) {{ onDeleteFolder(node.folder); onContextTargetChange(null) }} else null,
                                    isPinned = node.folder.isPinned,
                                )
                        }
                    }
                }
            }
        }
    }

    // Dialogs should be rendered inside item block or moved to SidebarSheet.
    // I will extract Dialog to SidebarSheet since it's a global overlay.
}
