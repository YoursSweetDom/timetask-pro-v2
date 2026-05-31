package com.timetask.pro.v2.presentation.tasks.completed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.timetask.pro.v2.presentation.tasks.components.TaskItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletedScreen(
    onTaskClick: (Long) -> Unit,
    viewModel: CompletedViewModel = viewModel()
) {
    val tasks by viewModel.completedTasks.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Выполнено") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Список пуст", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(tasks, key = { it.id }) { task ->
                        TaskItem(
                            task = task,
                            onToggle = { viewModel.restoreTask(task) },
                            onDelete = { viewModel.moveToTrash(task) },
                            onPin = { /* TODO if needed */ },
                            onMove = { /* TODO if needed */ },
                            onDateClick = { /* TODO if needed */ },
                            onToolsClick = { /* TODO if needed */ },
                            onClick = { onTaskClick(task.id) }
                        )
                }
            }
        }
    }
}
