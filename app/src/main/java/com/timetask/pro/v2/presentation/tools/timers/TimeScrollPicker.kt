package com.timetask.pro.v2.presentation.tools.timers

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Scroll-wheel time picker (ЧЧ : ММ : СС).
 *
 * Производительность:
 * - snapshotFlow → минимальный recomposition
 * - HapticFeedback при смене значения
 * - animateScrollToItem для программной прокрутки
 */
@Composable
fun TimeScrollPicker(
    hours: Int,
    minutes: Int,
    seconds: Int,
    onTimeChange: (hours: Int, minutes: Int, seconds: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Hours
        WheelColumn(
            range = 0..23,
            selectedValue = hours,
            onValueChange = { onTimeChange(it, minutes, seconds) },
            label = "ч",
        )

        // Separator
        Text(
            text = ":",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        // Minutes
        WheelColumn(
            range = 0..59,
            selectedValue = minutes,
            onValueChange = { onTimeChange(hours, it, seconds) },
            label = "м",
        )

        // Separator
        Text(
            text = ":",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        // Seconds
        WheelColumn(
            range = 0..59,
            selectedValue = seconds,
            onValueChange = { onTimeChange(hours, minutes, it) },
            label = "с",
        )
    }
}

// ============================================================
// Внутренний компонент — одна колонка scroll-wheel
// ============================================================

private const val VISIBLE_ITEMS = 3 // Видимых элементов: 1 выше + центральный + 1 ниже
private val ITEM_HEIGHT = 48.dp
private val COLUMN_WIDTH = 72.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WheelColumn(
    range: IntRange,
    selectedValue: Int,
    onValueChange: (Int) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val itemCount = range.last - range.first + 1

    // Достаточно большое число для "бесконечного" скролла
    val virtualCount = itemCount * 1000
    val virtualMiddle = (virtualCount / 2) - ((virtualCount / 2) % itemCount)

    val initialIndex = virtualMiddle + (selectedValue - range.first)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex - 1)

    // rememberUpdatedState — предотвращает stale closure в long-lived LaunchedEffect
    val currentOnValueChange by rememberUpdatedState(onValueChange)

    var lastReportedValue by remember { mutableIntStateOf(selectedValue) }

    // snapshotFlow для минимального recomposition
    LaunchedEffect(listState) {
        snapshotFlow {
            // Определяем центральный элемент
            val layoutInfo = listState.layoutInfo
            val viewportCenter = layoutInfo.viewportStartOffset +
                (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2
            layoutInfo.visibleItemsInfo.minByOrNull {
                kotlin.math.abs((it.offset + it.size / 2) - viewportCenter)
            }?.index
        }
            .distinctUntilChanged()
            .collect { centerIndex ->
                if (centerIndex != null) {
                    val value = range.first + (centerIndex % itemCount)
                    // Guard: вызываем callback только при пользовательском скролле,
                    // не при программной прокрутке (пресеты)
                    if (value != lastReportedValue) {
                        lastReportedValue = value
                        if (listState.isScrollInProgress) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        currentOnValueChange(value)
                    }
                }
            }
    }

    // Программная прокрутка при внешнем изменении (пресет)
    LaunchedEffect(selectedValue) {
        if (selectedValue != lastReportedValue) {
            lastReportedValue = selectedValue
            val targetIndex = virtualMiddle + (selectedValue - range.first)
            listState.animateScrollToItem(targetIndex - 1)
        }
    }

    Column(
        modifier = modifier.width(COLUMN_WIDTH),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Label
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        val totalHeight = ITEM_HEIGHT * VISIBLE_ITEMS
        val surfaceColor = MaterialTheme.colorScheme.surfaceContainerLow

        Box(
            modifier = Modifier
                .height(totalHeight)
                .width(COLUMN_WIDTH)
                .drawWithContent {
                    drawContent()
                    // Gradient fade сверху и снизу
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(surfaceColor, Color.Transparent),
                            startY = 0f,
                            endY = size.height * 0.3f,
                        ),
                    )
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, surfaceColor),
                            startY = size.height * 0.7f,
                            endY = size.height,
                        ),
                    )
                },
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.height(totalHeight),
                flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
            ) {
                items(virtualCount) { virtualIndex ->
                    val value = range.first + (virtualIndex % itemCount)
                    val isSelected = value == lastReportedValue

                    Box(
                        modifier = Modifier
                            .height(ITEM_HEIGHT)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "%02d".format(value),
                            fontSize = if (isSelected) 28.sp else 18.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            },
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}
