package com.timetask.pro.v2.presentation.sidebar.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SidebarAddMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onAddCategory: () -> Unit,
    onAddList: () -> Unit,
    onAddFilter: () -> Unit,
    onAddTag: () -> Unit,
    onAddTemplate: () -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        DropdownMenuItem(
            text = { Text("Категория") },
            onClick = {
                onAddCategory()
                onDismissRequest()
            },
            leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) }
        )
        DropdownMenuItem(
            text = { Text("Список") },
            onClick = {
                onAddList()
                onDismissRequest()
            },
            leadingIcon = { Icon(Icons.Default.List, contentDescription = null) }
        )
        DropdownMenuItem(
            text = { Text("Фильтр") },
            onClick = {
                onAddFilter()
                onDismissRequest()
            },
            leadingIcon = { Icon(Icons.Default.FilterAlt, contentDescription = null) }
        )
        DropdownMenuItem(
            text = { Text("Метка") },
            onClick = {
                onAddTag()
                onDismissRequest()
            },
            leadingIcon = { Icon(Icons.Default.Label, contentDescription = null) }
        )
        DropdownMenuItem(
            text = { Text("Шаблон") },
            onClick = {
                onAddTemplate()
                onDismissRequest()
            },
            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) }
        )
    }
}
