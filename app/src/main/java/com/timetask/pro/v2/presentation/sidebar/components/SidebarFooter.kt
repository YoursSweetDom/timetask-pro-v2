package com.timetask.pro.v2.presentation.sidebar.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SidebarFooter(
    selectedItemId: String?,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SidebarItem(
            label = "Выполнено",
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
            selected = selectedItemId == "completed",
            onClick = { onItemSelected("completed") }
        )
        SidebarItem(
            label = "Не будет выполнено",
            icon = { Icon(Icons.Default.Cancel, contentDescription = null) },
            selected = selectedItemId == "wont_do",
            onClick = { onItemSelected("wont_do") }
        )
        SidebarItem(
            label = "Корзина",
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            selected = selectedItemId == "trash",
            onClick = { onItemSelected("trash") }
        )
        SidebarItem(
            label = "Шаблоны",
            icon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
            selected = selectedItemId == "templates",
            onClick = { onItemSelected("templates") }
        )
    }
}
