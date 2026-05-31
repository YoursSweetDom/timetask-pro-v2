package com.timetask.pro.v2.presentation.sidebar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.timetask.pro.v2.data.local.db.entity.CategoryEntity
import com.timetask.pro.v2.data.local.db.entity.FilterEntity
import com.timetask.pro.v2.data.local.db.entity.FolderEntity
import com.timetask.pro.v2.data.local.db.entity.TagEntity
import com.timetask.pro.v2.domain.model.TaskFilter
import com.timetask.pro.v2.presentation.sidebar.components.PinnedItemState
import com.timetask.pro.v2.presentation.sidebar.components.sidebarCollections
import com.timetask.pro.v2.presentation.sidebar.components.SidebarActionBar
import com.timetask.pro.v2.presentation.sidebar.components.SidebarFooter
import com.timetask.pro.v2.presentation.sidebar.components.SidebarHeader
import com.timetask.pro.v2.presentation.sidebar.components.SidebarPinnedSection
import com.timetask.pro.v2.presentation.sidebar.components.SidebarSmartLists
import com.timetask.pro.v2.ui.theme.Spacing
import kotlinx.collections.immutable.ImmutableList

/**
 * Главный контейнер бокового меню (Sidebar).
 * Содержит 6 функциональных зон, разделенных 1px линиями.
 */
@Composable
fun SidebarSheet(
    folders: ImmutableList<FolderEntity>,
    tags: ImmutableList<TagEntity>,
    filters: ImmutableList<FilterEntity>,
    categories: ImmutableList<CategoryEntity>,
    quickListItems: List<String>,
    currentFilter: TaskFilter?,
    selectedFooterItem: String? = null,
    onFilterSelected: (TaskFilter) -> Unit,
    onCreateFolder: () -> Unit,
    onAddTemplate: () -> Unit,
    onAddTag: () -> Unit = {},
    onAddCategory: () -> Unit = {},
    onAddFilter: () -> Unit = {},
    // CRUD callbacks for context menu
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
    onQuickListLongClick: () -> Unit = {},
    onReorderTags: ((List<Pair<Long, Int>>) -> Unit)? = null,
    onReorderCategories: ((List<Pair<Long, Int>>) -> Unit)? = null,
    onReorderFilters: ((List<Pair<Long, Int>>) -> Unit)? = null,
    onReorderFolders: ((List<Pair<Long, Int>>) -> Unit)? = null,
    onTrashClick: () -> Unit = {},
    onCompletedClick: () -> Unit = {},
    onWontDoClick: () -> Unit = {},
    onTemplatesClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Dynamic Pinned Items
    val dynamicPinnedItems = androidx.compose.runtime.remember(folders, tags, filters) {
        val items = mutableListOf<com.timetask.pro.v2.presentation.sidebar.components.PinnedItemState>()
        
        folders.filter { it.isPinned }.forEach { folder ->
            items.add(
                com.timetask.pro.v2.presentation.sidebar.components.PinnedItemState(
                    id = "folder_${folder.id}",
                    name = folder.name,
                    icon = {
                        if (folder.emoji != null) {
                            Text(folder.emoji, style = MaterialTheme.typography.titleMedium)
                        } else {
                            Icon(Icons.Filled.Folder, contentDescription = null)
                        }
                    },
                    color = folder.color?.let { 
                        try { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(it)) } catch (e: Exception) { null } 
                    }
                )
            )
        }
        
        tags.filter { it.isPinned }.forEach { tag ->
            items.add(
                com.timetask.pro.v2.presentation.sidebar.components.PinnedItemState(
                    id = "tag_${tag.id}",
                    name = tag.name,
                    icon = {
                        if (tag.emoji != null) {
                            Text(tag.emoji, style = MaterialTheme.typography.titleMedium)
                        } else {
                            Icon(Icons.Filled.Folder, contentDescription = null)
                        }
                    },
                    color = tag.color?.let { 
                        try { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(it)) } catch (e: Exception) { null } 
                    }
                )
            )
        }
        
        filters.filter { it.isPinned }.forEach { filter ->
            items.add(
                com.timetask.pro.v2.presentation.sidebar.components.PinnedItemState(
                    id = "filter_${filter.id}",
                    name = filter.name,
                    icon = {
                        if (filter.icon != null) {
                            Text(filter.icon, style = MaterialTheme.typography.titleMedium)
                        } else {
                            Icon(Icons.Filled.Folder, contentDescription = null)
                        }
                    },
                    color = null
                )
            )
        }
        
        items
    }

    // Context menu state for pinned items
    var pinnedContextTarget by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<com.timetask.pro.v2.presentation.sidebar.components.PinnedItemState?>(null) }
    var pinnedShowRenameDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    // Context menu state for collections (folders, tags, categories, filters)
    var collectionsContextTarget by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<com.timetask.pro.v2.presentation.sidebar.components.ContextTarget?>(null) }
    var collectionsShowRenameDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    // Flat Folders for Collections
    val flatFolders = androidx.compose.runtime.remember(folders) {
        com.timetask.pro.v2.domain.model.FolderTreeNode.flatten(
            com.timetask.pro.v2.domain.model.FolderTreeNode.buildTree(folders)
        )
    }

    ModalDrawerSheet(
        modifier = modifier.width(300.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.fillMaxHeight()) {
            // Zone 1: Header (Fixed)
            Spacer(modifier = Modifier.height(Spacing.md))
            SidebarHeader(
                onSearchClick = { /* TODO */ },
                onNotificationsClick = { /* TODO */ },
                onSettingsClick = { /* TODO */ },
                modifier = Modifier.padding(bottom = Spacing.sm)
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                thickness = 1.dp
            )

            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Zone 2: Pinned (Горизонтальный список закрепов)
                if (dynamicPinnedItems.isNotEmpty()) {
                    item {
                        androidx.compose.foundation.layout.Box {
                            SidebarPinnedSection(
                                pinnedItems = dynamicPinnedItems,
                                onItemClick = { itemId ->
                                    val parts = itemId.split("_")
                                    val type = parts[0]
                                    val id = parts[1].toLongOrNull() ?: return@SidebarPinnedSection
                                    
                                    val newFilter = when (type) {
                                        "folder" -> TaskFilter.Folder(id)
                                        "tag" -> TaskFilter.Tag(id)
                                        "filter" -> TaskFilter.Custom(id)
                                        else -> TaskFilter.Inbox
                                    }
                                    onFilterSelected(newFilter)
                                },
                                onItemLongPress = { item ->
                                    pinnedContextTarget = item
                                }
                            )
                            
                            // Context Menu for pinned items
                            val currentPinnedTarget = pinnedContextTarget
                            if (currentPinnedTarget != null) {
                                val item = currentPinnedTarget
                                val parts = item.id.split("_")
                                val type = parts[0]
                                val entityId = parts[1].toLongOrNull() ?: 0L
                                
                                com.timetask.pro.v2.presentation.sidebar.components.SidebarContextMenu(
                                    expanded = true,
                                    onDismissRequest = { pinnedContextTarget = null },
                                    onEdit = { 
                                        when (type) {
                                            "folder" -> folders.find { it.id == entityId }?.let { onEditFolder?.invoke(it) }
                                            "tag" -> tags.find { it.id == entityId }?.let { onEditTag?.invoke(it) }
                                            "filter" -> filters.find { it.id == entityId }?.let { onEditFilter?.invoke(it) }
                                        }
                                        pinnedContextTarget = null
                                    },
                                    onPin = {
                                        when (type) {
                                            "folder" -> folders.find { it.id == entityId }?.let { onPinFolder?.invoke(it) }
                                            "tag" -> tags.find { it.id == entityId }?.let { onPinTag?.invoke(it) }
                                            "filter" -> filters.find { it.id == entityId }?.let { onPinFilter?.invoke(it) }
                                        }
                                        pinnedContextTarget = null
                                    },
                                    onAddSubfolder = if (type == "folder") { {
                                        folders.find { it.id == entityId }?.let { onAddSubfolder?.invoke(it) }
                                        pinnedContextTarget = null
                                    } } else null,
                                    onDelete = {
                                        when (type) {
                                            "folder" -> folders.find { it.id == entityId }?.let { onDeleteFolder?.invoke(it) }
                                            "tag" -> tags.find { it.id == entityId }?.let { onDeleteTag?.invoke(it) }
                                            "filter" -> filters.find { it.id == entityId }?.let { onDeleteFilter?.invoke(it) }
                                        }
                                        pinnedContextTarget = null
                                    },
                                    isPinned = true
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    // Zone 3: Smart Lists
                    SidebarSmartLists(
                        quickListItems = quickListItems,
                        currentFilter = currentFilter,
                        onFilterSelected = onFilterSelected,
                        onItemLongClick = onQuickListLongClick
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), thickness = 1.dp)
                }

                item {
                    Spacer(modifier = Modifier.height(Spacing.sm))
                }

                // Zone 4: Collections (Метки, Фильтры, Категории, Папки)
                sidebarCollections(
                    folders = folders,
                    flatFolders = flatFolders,
                    tags = tags,
                        filters = filters,
                        categories = categories,
                        currentFilter = currentFilter,
                        onFilterSelected = onFilterSelected,
                        contextTarget = collectionsContextTarget,
                        onContextTargetChange = { collectionsContextTarget = it },
                        showRenameDialog = collectionsShowRenameDialog,
                        onShowRenameDialogChange = { collectionsShowRenameDialog = it },
                        onEditFolder = onEditFolder,
                        onDeleteFolder = onDeleteFolder,
                        onPinFolder = onPinFolder,
                        onAddSubfolder = onAddSubfolder,
                        onEditTag = onEditTag,
                        onDeleteTag = onDeleteTag,
                        onPinTag = onPinTag,
                        onEditCategory = onEditCategory,
                        onDeleteCategory = onDeleteCategory,
                        onEditFilter = onEditFilter,
                        onDeleteFilter = onDeleteFilter,
                        onPinFilter = onPinFilter,
                        onReorderTags = onReorderTags,
                        onReorderCategories = onReorderCategories,
                        onReorderFilters = onReorderFilters,
                )

                item {
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), thickness = 1.dp)
                }

                item {
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    // Zone 5: Footer Lists
                    SidebarFooter(
                        selectedItemId = selectedFooterItem,
                        onItemSelected = { itemId ->
                            when (itemId) {
                                "completed" -> onCompletedClick()
                                "wont_do" -> onWontDoClick()
                                "trash" -> onTrashClick()
                                "templates" -> onTemplatesClick()
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                }
            }

            // Zone 6: Action Bar (Fixed Bottom)
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                thickness = 1.dp
            )
            SidebarActionBar(
                onAddClick = { onCreateFolder() },
                onManageSidebarClick = { /* TODO */ },
                onAddTemplate = onAddTemplate,
                onAddTag = onAddTag,
                onAddCategory = onAddCategory,
                onAddFilter = onAddFilter,
            )
        } // End of Column
    } // End of ModalDrawerSheet
}
