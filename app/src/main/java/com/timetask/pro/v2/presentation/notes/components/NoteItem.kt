package com.timetask.pro.v2.presentation.notes.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.timetask.pro.v2.data.local.db.entity.NoteColor
import com.timetask.pro.v2.data.local.db.entity.NoteEntity
import com.timetask.pro.v2.ui.theme.Spacing
import com.timetask.pro.v2.ui.theme.TitaniumPrimary

/**
 * Карточка заметки в стиле Google Keep.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteItem(
    note: NoteEntity,
    onPin: () -> Unit,
    onDelete: () -> Unit,
    onChangeColor: (NoteColor) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    val cardColor = remember(note.color) { noteColorToComposeColor(note.color) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { /* TODO: открыть редактор заметки */ },
                onLongClick = { showMenu = true },
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md),
        ) {
            // Header: title + pin
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (note.title.isNotBlank()) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }

                if (note.isPinned) {
                    Spacer(Modifier.width(Spacing.xs))
                    Icon(
                        imageVector = Icons.Filled.PushPin,
                        contentDescription = "Закреплено",
                        modifier = Modifier.size(16.dp),
                        tint = TitaniumPrimary,
                    )
                }
            }

            // Content preview
            if (note.content.isNotBlank()) {
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.alpha(0.85f),
                )
            }
        }

        // Context menu
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
        ) {
            DropdownMenuItem(
                text = { Text(if (note.isPinned) "Открепить" else "Закрепить") },
                onClick = {
                    onPin()
                    showMenu = false
                },
            )
            DropdownMenuItem(
                text = { Text("Цвет") },
                onClick = {
                    showMenu = false
                    showColorPicker = true
                },
            )
            DropdownMenuItem(
                text = { Text("Удалить", color = MaterialTheme.colorScheme.error) },
                onClick = {
                    onDelete()
                    showMenu = false
                },
            )
        }

        // Color picker menu
        DropdownMenu(
            expanded = showColorPicker,
            onDismissRequest = { showColorPicker = false },
        ) {
            NoteColor.entries.forEach { color ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Card(
                                modifier = Modifier.size(20.dp),
                                shape = RoundedCornerShape(4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = noteColorToComposeColor(color),
                                ),
                            ) {}
                            Spacer(Modifier.width(Spacing.sm))
                            Text(noteColorLabel(color))
                        }
                    },
                    onClick = {
                        onChangeColor(color)
                        showColorPicker = false
                    },
                )
            }
        }
    }
}

/**
 * Маппинг NoteColor → Compose Color (тёмная тема).
 */
fun noteColorToComposeColor(color: NoteColor): Color = when (color) {
    NoteColor.DEFAULT -> Color(0xFF1E293B) // surfaceContainer
    NoteColor.BLUE    -> Color(0xFF1E3A5F)
    NoteColor.GREEN   -> Color(0xFF1A3B2A)
    NoteColor.YELLOW  -> Color(0xFF3B3520)
    NoteColor.PINK    -> Color(0xFF3B1E2E)
    NoteColor.PURPLE  -> Color(0xFF2D1F3D)
    NoteColor.GRAY    -> Color(0xFF2A2D32)
}

private fun noteColorLabel(color: NoteColor): String = when (color) {
    NoteColor.DEFAULT -> "По умолчанию"
    NoteColor.BLUE    -> "Синий"
    NoteColor.GREEN   -> "Зелёный"
    NoteColor.YELLOW  -> "Жёлтый"
    NoteColor.PINK    -> "Розовый"
    NoteColor.PURPLE  -> "Фиолетовый"
    NoteColor.GRAY    -> "Серый"
}
