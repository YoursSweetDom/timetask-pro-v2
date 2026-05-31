package com.timetask.pro.v2.presentation.sidebar.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Контекстное меню (long-press) для элементов сайдбара.
 * Доступные действия зависят от типа: папка, метка, категория, фильтр.
 */
@Composable
fun SidebarContextMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onPin: (() -> Unit)? = null,
    onAddSubfolder: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    isPinned: Boolean = false,
    modifier: Modifier = Modifier,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        if (onEdit != null) {
            DropdownMenuItem(
                text = { Text("Редактировать") },
                onClick = { onEdit(); onDismissRequest() },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
            )
        }

        if (onPin != null) {
            DropdownMenuItem(
                text = { Text(if (isPinned) "Открепить" else "Закрепить") },
                onClick = { onPin(); onDismissRequest() },
                leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null) }
            )
        }

        if (onAddSubfolder != null) {
            DropdownMenuItem(
                text = { Text("Создать подпапку") },
                onClick = { onAddSubfolder(); onDismissRequest() },
                leadingIcon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null) }
            )
        }

        if (onDelete != null) {
            DropdownMenuItem(
                text = { Text("Удалить") },
                onClick = { onDelete(); onDismissRequest() },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
            )
        }
    }
}
