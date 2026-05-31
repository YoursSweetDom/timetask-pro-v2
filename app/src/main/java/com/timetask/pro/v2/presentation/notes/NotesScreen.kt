package com.timetask.pro.v2.presentation.notes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.timetask.pro.v2.presentation.notes.components.AddNoteSheet
import com.timetask.pro.v2.presentation.notes.components.NoteItem
import com.timetask.pro.v2.ui.theme.Spacing
import com.timetask.pro.v2.ui.theme.TitaniumPrimary

@Composable
fun NotesScreen(viewModel: NotesViewModel) {

    val notes by viewModel.notes.collectAsState()
    val showAddSheet by viewModel.showAddSheet.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    // Разделяем на закреплённые и обычные
    val pinnedNotes = remember(notes) { notes.filter { it.isPinned } }
    val unpinnedNotes = remember(notes) { notes.filter { !it.isPinned } }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = remember { { viewModel.showAddSheet() } },
                containerColor = TitaniumPrimary,
            ) {
                Icon(Icons.Filled.Add, "Добавить заметку")
            }
        },
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Search bar
            TextField(
                value = searchQuery,
                onValueChange = remember<(String) -> Unit> { { viewModel.search(it) } },
                placeholder = { Text("Поиск заметок...") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                shape = RoundedCornerShape(16.dp),
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = "Поиск",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = remember { { viewModel.search("") } }) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Очистить",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = TitaniumPrimary,
                ),
            )

            // Content
            if (notes.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.StickyNote2,
                            contentDescription = null,
                            modifier = Modifier.size(Spacing.xxl),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        )
                        Spacer(Modifier.height(Spacing.md))
                        Text(
                            text = if (searchQuery.isBlank()) "Нет заметок" else "Ничего не найдено",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(Spacing.xs))
                        Text(
                            text = if (searchQuery.isBlank()) "Нажмите + чтобы создать" else "Попробуйте другой запрос",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        )
                    }
                }
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = Spacing.sm,
                        end = Spacing.sm,
                        bottom = Spacing.xxxl,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalItemSpacing = Spacing.sm,
                ) {
                    // Pinned section
                    if (pinnedNotes.isNotEmpty()) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            Text(
                                text = "Закреплённые",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(
                                    start = Spacing.sm,
                                    top = Spacing.xs,
                                    bottom = Spacing.xs,
                                ),
                            )
                        }

                        items(
                            items = pinnedNotes,
                            key = { it.id },
                        ) { note ->
                            val onPin = remember(note.id) { { viewModel.pinNote(note) } }
                            val onDelete = remember(note.id) { { viewModel.deleteNote(note) } }
                            val onChangeColor = remember(note.id) {
                                { color: com.timetask.pro.v2.data.local.db.entity.NoteColor ->
                                    viewModel.changeColor(note, color)
                                }
                            }
                            NoteItem(
                                note = note,
                                onPin = onPin,
                                onDelete = onDelete,
                                onChangeColor = onChangeColor,
                            )
                        }

                        // Separator
                        if (unpinnedNotes.isNotEmpty()) {
                            item(span = StaggeredGridItemSpan.FullLine) {
                                Text(
                                    text = "Другие",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(
                                        start = Spacing.sm,
                                        top = Spacing.sm,
                                        bottom = Spacing.xs,
                                    ),
                                )
                            }
                        }
                    }

                    // Regular notes
                    items(
                        items = unpinnedNotes,
                        key = { it.id },
                    ) { note ->
                        val onPin = remember(note.id) { { viewModel.pinNote(note) } }
                        val onDelete = remember(note.id) { { viewModel.deleteNote(note) } }
                        val onChangeColor = remember(note.id) {
                            { color: com.timetask.pro.v2.data.local.db.entity.NoteColor ->
                                viewModel.changeColor(note, color)
                            }
                        }
                        NoteItem(
                            note = note,
                            onPin = onPin,
                            onDelete = onDelete,
                            onChangeColor = onChangeColor,
                        )
                    }
                }
            }
        }
    }

    // Add note sheet
    AnimatedVisibility(visible = showAddSheet, enter = fadeIn(), exit = fadeOut()) {
        AddNoteSheet(
            onDismiss = remember { { viewModel.hideAddSheet() } },
            onAdd = remember {
                { title: String, content: String, color: com.timetask.pro.v2.data.local.db.entity.NoteColor ->
                    viewModel.addNote(title, content, color)
                }
            },
        )
    }
}
