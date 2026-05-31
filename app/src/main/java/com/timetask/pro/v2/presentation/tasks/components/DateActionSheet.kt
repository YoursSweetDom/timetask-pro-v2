package com.timetask.pro.v2.presentation.tasks.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.timetask.pro.v2.ui.theme.Spacing
import com.timetask.pro.v2.util.DateUtils

/**
 * BottomSheet для быстрого выбора даты из свайп-действия «Календарь».
 * Опции: Сегодня, Завтра, Следующий понедельник, Выбрать дату, Очистить, Пропустить рекурринг.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateActionSheet(
    onDismiss: () -> Unit,
    onDateSelected: (Long?) -> Unit,
    onPickCustomDate: () -> Unit = {},
    showSkipRecurring: Boolean = false,
    onSkipRecurring: () -> Unit = {},
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Text(
                text = "Выбрать дату",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)
            )

            // Сегодня
            ListItem(
                headlineContent = { Text("Сегодня") },
                leadingContent = { Icon(Icons.Default.Today, contentDescription = null) },
                modifier = Modifier.clickable {
                    onDateSelected(DateUtils.endOfToday())
                    onDismiss()
                }
            )

            // Завтра
            ListItem(
                headlineContent = { Text("Завтра") },
                leadingContent = { Icon(Icons.Default.WbSunny, contentDescription = null) },
                modifier = Modifier.clickable {
                    onDateSelected(DateUtils.endOfTomorrow())
                    onDismiss()
                }
            )

            // Следующий понедельник
            ListItem(
                headlineContent = { Text("Следующий понедельник") },
                leadingContent = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                modifier = Modifier.clickable {
                    onDateSelected(DateUtils.startOfNextMonday())
                    onDismiss()
                }
            )

            HorizontalDivider()

            // Выбрать дату (открывает DatePicker)
            ListItem(
                headlineContent = { Text("Выбрать дату…") },
                leadingContent = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                modifier = Modifier.clickable {
                    onPickCustomDate()
                    onDismiss()
                }
            )

            // Очистить дату / напоминания
            ListItem(
                headlineContent = { Text("Очистить дату") },
                leadingContent = { Icon(Icons.Default.Clear, contentDescription = null) },
                modifier = Modifier.clickable {
                    onDateSelected(null)
                    onDismiss()
                }
            )

            // Пропустить рекуррентное (если задача рекуррентная)
            if (showSkipRecurring) {
                ListItem(
                    headlineContent = { Text("Пропустить повторение") },
                    leadingContent = { Icon(Icons.Default.SkipNext, contentDescription = null) },
                    modifier = Modifier.clickable {
                        onSkipRecurring()
                        onDismiss()
                    }
                )
            }

            Spacer(Modifier.height(Spacing.md))
        }
    }
}
