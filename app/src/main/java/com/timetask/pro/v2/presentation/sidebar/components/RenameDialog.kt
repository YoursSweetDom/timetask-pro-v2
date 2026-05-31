package com.timetask.pro.v2.presentation.sidebar.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.timetask.pro.v2.ui.theme.TitaniumPrimary

/**
 * Универсальный диалог переименования (папки, метки, категории, фильтра).
 */
@Composable
fun RenameDialog(
    currentName: String,
    title: String = "Переименовать",
    onDismiss: () -> Unit,
    onRename: (String) -> Unit,
) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
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
        },
        confirmButton = {
            Button(
                onClick = { onRename(name.trim()) },
                enabled = name.isNotBlank() && name.trim() != currentName,
                colors = ButtonDefaults.buttonColors(containerColor = TitaniumPrimary),
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        },
    )
}
