package com.timetask.pro.v2.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.timetask.pro.v2.data.local.db.entity.CategoryEntity
import com.timetask.pro.v2.data.local.db.entity.FilterEntity
import com.timetask.pro.v2.data.local.db.entity.FolderEntity
import com.timetask.pro.v2.data.local.db.entity.TagEntity
import com.timetask.pro.v2.domain.model.TaskFilter
import com.timetask.pro.v2.presentation.sidebar.SidebarSheet
import com.timetask.pro.v2.presentation.sidebar.components.PinnedItemState
import com.timetask.pro.v2.presentation.sidebar.components.QuickListMenuSheet
import kotlinx.collections.immutable.ImmutableList

/**
 * Содержимое бокового меню (Drawer).
 * Выступает оберткой для нового модульного SidebarSheet.
 */
@Composable
fun DrawerContent(
    folders: ImmutableList<FolderEntity>,
    tags: ImmutableList<TagEntity> = kotlinx.collections.immutable.persistentListOf(),
    filters: ImmutableList<FilterEntity> = kotlinx.collections.immutable.persistentListOf(),
    categories: ImmutableList<CategoryEntity> = kotlinx.collections.immutable.persistentListOf(),
    quickListItems: List<String> = listOf("ALL", "TODAY", "TOMORROW", "NEXT_7_DAYS", "INBOX"),
    currentFilter: TaskFilter?,
    selectedFooterItem: String? = null,
    onFilterSelected: (TaskFilter) -> Unit,
    onCreateFolder: () -> Unit,
    onAddTemplate: () -> Unit = {},
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
    onSaveQuickListItems: (List<String>) -> Unit = {},
    onReorderTags: ((List<Pair<Long, Int>>) -> Unit)? = null,
    onReorderCategories: ((List<Pair<Long, Int>>) -> Unit)? = null,
    onReorderFilters: ((List<Pair<Long, Int>>) -> Unit)? = null,
    onReorderFolders: ((List<Pair<Long, Int>>) -> Unit)? = null,
    onTrashClick: () -> Unit = {},
    onCompletedClick: () -> Unit = {},
    onWontDoClick: () -> Unit = {},
    onTemplatesClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var showQuickListMenu by remember { mutableStateOf(false) }
    SidebarSheet(
        folders = folders,
        tags = tags,
        filters = filters,
        categories = categories,
        quickListItems = quickListItems,
        currentFilter = currentFilter,
        selectedFooterItem = selectedFooterItem,
        onFilterSelected = onFilterSelected,
        onCreateFolder = onCreateFolder,
        onAddTemplate = onAddTemplate,
        onAddTag = onAddTag,
        onAddCategory = onAddCategory,
        onAddFilter = onAddFilter,
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
        onQuickListLongClick = { showQuickListMenu = true },
        onReorderTags = onReorderTags,
        onReorderCategories = onReorderCategories,
        onReorderFilters = onReorderFilters,
        onReorderFolders = onReorderFolders,
        onTrashClick = onTrashClick,
        onCompletedClick = onCompletedClick,
        onWontDoClick = onWontDoClick,
        onTemplatesClick = onTemplatesClick,
        modifier = modifier
    )

    if (showQuickListMenu) {
        QuickListMenuSheet(
            currentItems = quickListItems,
            folders = folders,
            tags = tags,
            categories = categories,
            filters = filters,
            onDismiss = { showQuickListMenu = false },
            onSave = { items ->
                onSaveQuickListItems(items)
                showQuickListMenu = false
            }
        )
    }
}
