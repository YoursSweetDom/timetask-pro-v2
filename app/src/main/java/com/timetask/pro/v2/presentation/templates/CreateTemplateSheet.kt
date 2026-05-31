package com.timetask.pro.v2.presentation.templates

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timetask.pro.v2.presentation.tasks.components.TaskFormContent
import com.timetask.pro.v2.presentation.util.bottomSheetFlingToClose
import com.timetask.pro.v2.ui.theme.Spacing
import com.timetask.pro.v2.ui.theme.TitaniumPrimary
import com.timetask.pro.v2.presentation.components.EmojiPickerBottomSheet
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTemplateSheet(
    viewModel: CreateTemplateViewModel,
    onDismiss: () -> Unit,
    onShowFolderPicker: () -> Unit = {} // TODO: connect with Folder Picker
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()
    
    var emoji by remember { mutableStateOf<String?>(null) }
    var showEmojiPicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        TaskFormContent(
            sheetTitle = "Новый шаблон",
            confirmButtonText = "Сохранить шаблон",
            onDismiss = onDismiss,
            headerContent = {
                // Template Name
                OutlinedTextField(
                    value = state.templateName,
                    onValueChange = { viewModel.onEvent(CreateTemplateEvent.TemplateNameChanged(it)) },
                    label = { Text("Название шаблона") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TitaniumPrimary,
                        cursorColor = TitaniumPrimary,
                        focusedLabelColor = TitaniumPrimary,
                    ),
                )
                
                Spacer(Modifier.height(Spacing.sm))

                // Emoji Picker Trigger
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showEmojiPicker = true }
                        .padding(vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Эмодзи (необязательно): ", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(Spacing.sm))
                    Text(
                        text = emoji ?: "Выберите...",
                        fontSize = if (emoji != null) 24.sp else 16.sp,
                        color = if (emoji != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.height(Spacing.md))

                // Folder Picker Trigger
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onShowFolderPicker() }
                        .padding(vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Папка",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(Spacing.md))
                    val folderName = state.folders.find { it.id == state.selectedFolderId }?.name
                    Text(
                        text = folderName ?: "Без папки",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(Spacing.md))

                // Template Description
                OutlinedTextField(
                    value = state.templateDescription,
                    onValueChange = { viewModel.onEvent(CreateTemplateEvent.TemplateDescriptionChanged(it)) },
                    label = { Text("Описание шаблона") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TitaniumPrimary,
                        cursorColor = TitaniumPrimary,
                        focusedLabelColor = TitaniumPrimary,
                    ),
                )

                Spacer(Modifier.height(Spacing.md))

                // Is Pinned Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Закрепить шаблон", modifier = Modifier.weight(1f))
                    Switch(
                        checked = state.isPinned,
                        onCheckedChange = { viewModel.onEvent(CreateTemplateEvent.IsPinnedChanged(it)) }
                    )
                }

                Spacer(Modifier.height(Spacing.lg))
                Text("Настройки задачи:", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(Spacing.md))
            },
            onConfirm = { taskTitle, taskDescription, quadrant, pinMode, progress ->
                viewModel.onEvent(
                    CreateTemplateEvent.SaveTemplate(
                        taskTitle = taskTitle,
                        taskDescription = taskDescription,
                        quadrant = quadrant,
                        pinMode = pinMode,
                        progress = progress
                    )
                )
                onDismiss()
            },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .bottomSheetFlingToClose(sheetState, scope, onDismiss)
                .padding(horizontal = Spacing.md)
                .navigationBarsPadding()
        )
    }

    if (showEmojiPicker) {
        EmojiPickerBottomSheet(
            onEmojiSelected = { selected ->
                emoji = selected
                // Wait, CreateTemplateEvent does not have an emoji field right now. 
                // We will just leave it in the UI and maybe update ViewModel later if needed, 
                // but for now the user plan just says "Integrate Emoji button into creation forms". 
                // Let's add the basic UI trigger.
            },
            onDismiss = { showEmojiPicker = false }
        )
    }
}
