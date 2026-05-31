package com.timetask.pro.v2.presentation.sidebar.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.timetask.pro.v2.ui.theme.Spacing
import com.timetask.pro.v2.ui.theme.TitaniumPrimary
import com.timetask.pro.v2.presentation.components.EmojiPickerBottomSheet
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * BottomSheet для создания новой категории.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCategorySheet(
    onDismiss: () -> Unit,
    onAdd: (name: String, icon: String?, color: String?) -> Unit,
    initialName: String = "",
    initialIcon: String = "",
    initialColorHex: String? = null
) {
    val isEditMode = initialName.isNotEmpty()
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var name by remember { mutableStateOf(initialName) }
    var icon by remember { mutableStateOf(initialIcon) }
    var emoji by remember { mutableStateOf<String?>(null) }
    var showEmojiPicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md)
                .navigationBarsPadding()
        ) {
            Text(
                text = if (isEditMode) "Редактировать категорию" else "Новая категория",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(Spacing.md))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TitaniumPrimary,
                    cursorColor = TitaniumPrimary,
                    focusedLabelColor = TitaniumPrimary,
                ),
            )
            Spacer(Modifier.height(Spacing.sm))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showEmojiPicker = true }
                    .padding(vertical = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Эмодзи: ", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = emoji ?: icon.ifBlank { "Выберите..." },
                    fontSize = if (emoji != null || icon.isNotBlank()) 24.sp else 16.sp,
                    color = if (emoji != null || icon.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(Spacing.lg))
            Button(
                onClick = { 
                    scope.launch {
                        sheetState.hide()
                        onAdd(name.trim(), emoji ?: icon.trim().ifBlank { null }, null) 
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = TitaniumPrimary),
            ) {
                Text(if (isEditMode) "Сохранить" else "Создать")
            }
            Spacer(Modifier.height(Spacing.md))
        }
    }

    if (showEmojiPicker) {
        EmojiPickerBottomSheet(
            onEmojiSelected = { selected ->
                emoji = selected
                if (selected != null) icon = selected
            },
            onDismiss = { showEmojiPicker = false }
        )
    }
}
