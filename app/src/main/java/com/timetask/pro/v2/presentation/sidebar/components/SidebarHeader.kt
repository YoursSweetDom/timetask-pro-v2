package com.timetask.pro.v2.presentation.sidebar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.timetask.pro.v2.ui.theme.Spacing

@Composable
fun SidebarHeader(
    onSearchClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Аватар (Заглушка)
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "D",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(Spacing.sm))

        // Имя профиля
        Text(
            text = "Dom",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        // Иконки действий
        IconButton(onClick = onSearchClick, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Search, contentDescription = "Поиск", modifier = Modifier.size(22.dp))
        }
        IconButton(onClick = onNotificationsClick, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.NotificationsNone, contentDescription = "Уведомления", modifier = Modifier.size(22.dp))
        }
        IconButton(onClick = onSettingsClick, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Settings, contentDescription = "Настройки", modifier = Modifier.size(22.dp))
        }
    }
}
