package com.timetask.pro.v2.presentation.tools.chrono

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.timetask.pro.v2.domain.model.chrono.Lap

@Composable
fun EditLapDialog(
    lap: Lap,
    onDismiss: () -> Unit,
    onSave: (newName: String, newColorArgb: Int?) -> Unit
) {
    var title by remember { mutableStateOf(lap.title ?: "") }
    var selectedColor by remember { mutableStateOf(lap.colorARGB) }

    // Pre-defined color palette
    val colors = listOf(
        null, // Default
        0xFFF44336.toInt(), // Red
        0xFFE91E63.toInt(), // Pink
        0xFF9C27B0.toInt(), // Purple
        0xFF2196F3.toInt(), // Blue
        0xFF00BCD4.toInt(), // Cyan
        0xFF4CAF50.toInt(), // Green
        0xFFFFC107.toInt(), // Amber
        0xFFFF9800.toInt()  // Orange
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Настройки круга #${lap.lapNumber}") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Цвет выделения", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))

                // Color Palette Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    colors.take(4).forEach { colorInt ->
                        ColorItem(
                            colorInt = colorInt,
                            isSelected = selectedColor == colorInt,
                            onClick = { selectedColor = colorInt }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    colors.takeLast(colors.size - 4).forEach { colorInt ->
                        ColorItem(
                            colorInt = colorInt,
                            isSelected = selectedColor == colorInt,
                            onClick = { selectedColor = colorInt }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(title.trim(), selectedColor) }
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

@Composable
private fun ColorItem(
    colorInt: Int?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val displayColor = colorInt?.let { Color(it) } ?: MaterialTheme.colorScheme.onSurfaceVariant
    val modifier = Modifier
        .size(40.dp)
        .clip(CircleShape)
        .background(displayColor)
        .clickable { onClick() }

    Box(
        modifier = if (isSelected) {
            modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
        } else {
            modifier
        },
        contentAlignment = Alignment.Center
    ) {
        if (colorInt == null) {
            // Unset marker
            Text("X", color = MaterialTheme.colorScheme.surface)
        }
    }
}
