package com.timetask.pro.v2.domain.model

import com.timetask.pro.v2.data.local.db.entity.FolderEntity

/**
 * Узел дерева папок.
 * Позволяет рекурсивно отображать иерархию папок в сайдбаре.
 */
data class FolderTreeNode(
    val folder: FolderEntity,
    val children: List<FolderTreeNode>,
    val level: Int = 0
) {
    companion object {
        /**
         * Строит дерево из плоского списка папок.
         * @param folders все пользовательские папки
         * @param parentId id родительской папки (null = корневые)
         * @param level текущий уровень вложенности
         */
        fun buildTree(
            folders: List<FolderEntity>,
            parentId: Long? = null,
            level: Int = 0
        ): List<FolderTreeNode> {
            return folders
                .filter { it.parentId == parentId }
                .map { folder ->
                    FolderTreeNode(
                        folder = folder,
                        children = buildTree(folders, folder.id, level + 1),
                        level = level
                    )
                }
        }

        /**
         * Разворачивает дерево обратно в плоский список (depth-first)
         * для отображения в LazyColumn с корректными отступами.
         */
        fun flatten(nodes: List<FolderTreeNode>): List<FolderTreeNode> {
            val result = mutableListOf<FolderTreeNode>()
            for (node in nodes) {
                result.add(node)
                result.addAll(flatten(node.children))
            }
            return result
        }
    }
}
