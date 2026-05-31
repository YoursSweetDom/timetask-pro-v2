package com.timetask.pro.v2.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.timetask.pro.v2.ui.theme.Spacing

data class DurationOption(val label: String, val durationSec: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DurationPickerDropdown(
    title: String,
    description: String? = null,
    options: List<DurationOption>,
    currentDurationSec: Int,
    onDurationSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    allowOffOption: Boolean = false // e.g. for Auto-Snooze
) {
    var expanded by remember { mutableStateOf(false) }
    var showCustomDialog by remember { mutableStateOf(false) }

    // Find label for current duration, or default to "Свой вариант" / "Неизвестно"
    val currentLabel = remember(currentDurationSec, options) {
        if (currentDurationSec == -1) {
            if (allowOffOption) "Выключено" else "Бесконечно"
        } else {
            options.find { it.durationSec == currentDurationSec }?.label
                ?: "${currentDurationSec / 60} мин ${currentDurationSec % 60} сек"
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        
        Spacer(modifier = Modifier.width(Spacing.md))
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = currentLabel,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier.menuAnchor()
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            onDurationSelected(option.durationSec)
                            expanded = false
                        }
                    )
                }
                
                DropdownMenuItem(
                    text = { Text("Свой вариант...") },
                    onClick = {
                        expanded = false
                        showCustomDialog = true
                    }
                )
            }
        }
    }

    if (showCustomDialog) {
        CustomDurationDialog(
            initialMinutes = if (currentDurationSec > 0) currentDurationSec / 60 else 5,
            initialSeconds = if (currentDurationSec > 0) currentDurationSec % 60 else 0,
            onDismiss = { showCustomDialog = false },
            onSave = { minutes, seconds ->
                onDurationSelected(minutes * 60 + seconds)
                showCustomDialog = false
            }
        )
    }
}

@Composable
fun CustomDurationDialog(
    initialMinutes: Int,
    initialSeconds: Int,
    onDismiss: () -> Unit,
    onSave: (Int, Int) -> Unit
) {
    var minText by remember { mutableStateOf(initialMinutes.toString()) }
    var secText by remember { mutableStateOf(initialSeconds.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Своя длительность") },
        text = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = minText,
                    onValueChange = { minText = it.filter { char -> char.isDigit() } },
                    label = { Text("Минуты") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = secText,
                    onValueChange = { secText = it.filter { char -> char.isDigit() } },
                    label = { Text("Секунды") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val m = minText.toIntOrNull() ?: 0
                    val s = secText.toIntOrNull() ?: 0
                    if (m > 0 || s > 0) {
                        onSave(m, s)
                    }
                }
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
