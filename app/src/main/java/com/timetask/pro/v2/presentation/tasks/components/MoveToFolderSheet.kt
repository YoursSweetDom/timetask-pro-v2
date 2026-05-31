package com.timetask.pro.v2.presentation.tasks.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.timetask.pro.v2.data.local.db.entity.FolderEntity
import com.timetask.pro.v2.ui.theme.Spacing
import kotlinx.collections.immutable.ImmutableList

/**
 * BottomSheet для перемещения задачи в другую папку.
 * Используется из свайп-действия «Переместить».
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveToFolderSheet(
    folders: ImmutableList<FolderEntity>,
    currentFolderId: Long?,
    onDismiss: () -> Unit,
    onFolderSelected: (Long?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Text(
                text = "Переместить в…",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)
            )

            // Inbox (без папки)
            ListItem(
                headlineContent = { Text("Входящие") },
                leadingContent = { Icon(Icons.Default.Inbox, contentDescription = null) },
                trailingContent = {
                    if (currentFolderId == null) {
                        Text("✓", color = MaterialTheme.colorScheme.primary)
                    }
                },
                modifier = Modifier.clickable {
                    onFolderSelected(null)
                    onDismiss()
                }
            )

            // Folders
            folders.forEach { folder ->
                ListItem(
                    headlineContent = { Text(folder.name) },
                    leadingContent = {
                        if (folder.emoji != null) {
                            Text(folder.emoji, style = MaterialTheme.typography.titleMedium)
                        } else {
                            Icon(Icons.Default.Folder, contentDescription = null)
                        }
                    },
                    trailingContent = {
                        if (currentFolderId == folder.id) {
                            Text("✓", color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    modifier = Modifier.clickable {
                        onFolderSelected(folder.id)
                        onDismiss()
                    }
                )
            }

            Spacer(Modifier.height(Spacing.md))
        }
    }
}
