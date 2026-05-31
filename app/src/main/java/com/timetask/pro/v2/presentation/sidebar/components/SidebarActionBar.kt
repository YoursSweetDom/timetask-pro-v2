package com.timetask.pro.v2.presentation.sidebar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.timetask.pro.v2.ui.theme.Spacing

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun SidebarActionBar(
    onAddClick: () -> Unit,
    onAddTemplate: () -> Unit,
    onAddTag: () -> Unit,
    onAddCategory: () -> Unit,
    onAddFilter: () -> Unit,
    onManageSidebarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var addMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Кнопка + с круглой заливкой и выпадающим меню
        Box {
            IconButton(
                onClick = { addMenuExpanded = true },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Добавить",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }

            SidebarAddMenu(
                expanded = addMenuExpanded,
                onDismissRequest = { addMenuExpanded = false },
                onAddCategory = { onAddCategory(); addMenuExpanded = false },
                onAddList = { onAddClick(); addMenuExpanded = false },
                onAddFilter = { onAddFilter(); addMenuExpanded = false },
                onAddTag = { onAddTag(); addMenuExpanded = false },
                onAddTemplate = { onAddTemplate(); addMenuExpanded = false }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Настройки сайдбара
        IconButton(onClick = onManageSidebarClick, modifier = Modifier.size(36.dp)) {
            Icon(
                Icons.Default.Settings, // Заменим на гамбургер с шестеренкой позже, если будет SVG
                contentDescription = "Настройка бокового меню",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

