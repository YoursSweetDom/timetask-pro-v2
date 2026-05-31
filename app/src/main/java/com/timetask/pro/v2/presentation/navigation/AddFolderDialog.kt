package com.timetask.pro.v2.presentation.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.timetask.pro.v2.presentation.components.ColorSelectionRow
import com.timetask.pro.v2.ui.theme.Spacing
import com.timetask.pro.v2.ui.theme.TitaniumPrimary
import com.timetask.pro.v2.ui.theme.ItemColors
import com.timetask.pro.v2.presentation.components.EmojiPickerBottomSheet
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp

/**
 * Диалог создания новой папки или подпапки.
 * @param parentFolderName если не null, диалог создаёт подпапку внутри указанной папки
 */
@Composable
fun AddFolderDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, emoji: String?, color: String?) -> Unit,
    parentFolderName: String? = null,
    initialName: String = "",
    initialEmoji: String = "",
    initialColorHex: String? = null
) {
    val isEditMode = initialName.isNotEmpty()
    var name by remember { mutableStateOf(initialName) }
    var emoji by remember { mutableStateOf(initialEmoji) }
    var selectedColor by remember { mutableStateOf(initialColorHex ?: ItemColors.first()) }
    var showEmojiPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                if (isEditMode) "Редактировать папку" 
                else if (parentFolderName != null) "Подпапка в \"$parentFolderName\"" 
                else "Новая папка"
            ) 
        },
        text = {
            Column {
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
                        text = emoji.ifBlank { "Выберите..." },
                        fontSize = if (emoji.isNotBlank()) 24.sp else 16.sp,
                        color = if (emoji.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(Spacing.md))
                Text(
                    text = "Цвет",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Spacing.sm))
                ColorSelectionRow(
                    selectedColorHex = selectedColor,
                    onColorSelected = { selectedColor = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(name.trim(), emoji.trim().ifBlank { null }, selectedColor) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = TitaniumPrimary),
            ) {
                Text(if (isEditMode) "Сохранить" else "Создать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
    )

    if (showEmojiPicker) {
        EmojiPickerBottomSheet(
            onEmojiSelected = { selected ->
                emoji = selected ?: ""
            },
            onDismiss = { showEmojiPicker = false }
        )
    }
}
