package com.timetask.pro.v2.presentation.sidebar.components

import com.timetask.pro.v2.data.local.db.entity.CategoryEntity
import com.timetask.pro.v2.data.local.db.entity.FilterEntity
import com.timetask.pro.v2.data.local.db.entity.FolderEntity
import com.timetask.pro.v2.data.local.db.entity.TagEntity
import com.timetask.pro.v2.domain.model.FolderTreeNode

/**
 * Sealed class для идентификации элемента, на котором сработал long-press.
 */
sealed class ContextTarget {
    data class FolderTarget(val folder: FolderEntity, val node: FolderTreeNode) : ContextTarget()
    data class TagTarget(val tag: TagEntity) : ContextTarget()
    data class CategoryTarget(val category: CategoryEntity) : ContextTarget()
    data class FilterTarget(val filter: FilterEntity) : ContextTarget()
}
