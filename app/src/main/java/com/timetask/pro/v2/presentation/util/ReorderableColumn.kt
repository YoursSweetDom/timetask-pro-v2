package com.timetask.pro.v2.presentation.util

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun <T> ReorderableColumn(
    items: List<T>,
    onReorder: (from: Int, to: Int) -> Unit,
    onDragEnd: () -> Unit = {},
    modifier: Modifier = Modifier,
    key: ((T) -> Any)? = null,
    itemContent: @Composable ColumnScope.(item: T, isDragging: Boolean, modifier: Modifier) -> Unit
) {
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val itemHeights = remember { mutableMapOf<Int, Int>() }
    val itemPositions = remember { mutableMapOf<Int, Float>() }
    val scope = rememberCoroutineScope()

    Column(modifier = modifier) {
        items.forEachIndexed { index, item ->
            val isDragging = draggingIndex == index
            val animatedOffset = remember { Animatable(0f) }

            // Вычисляем сдвиг для анимации других элементов
            LaunchedEffect(draggingIndex, dragOffset) {
                if (draggingIndex != null && !isDragging) {
                    val draggedIdx = draggingIndex!!
                    val draggedY = itemPositions[draggedIdx] ?: 0f
                    val currentY = itemPositions[index] ?: 0f
                    val draggedHeight = itemHeights[draggedIdx] ?: 0
                    
                    val targetY = draggedY + dragOffset
                    
                    // Элемент был ниже, а drag ушел выше нас
                    if (draggedIdx > index && targetY < currentY + (itemHeights[index] ?: 0) / 2) {
                        animatedOffset.animateTo(draggedHeight.toFloat())
                    }
                    // Элемент был выше, а drag ушел ниже нас
                    else if (draggedIdx < index && targetY > currentY - draggedHeight / 2) {
                        animatedOffset.animateTo(-draggedHeight.toFloat())
                    } else {
                        animatedOffset.animateTo(0f)
                    }
                } else {
                    animatedOffset.animateTo(0f)
                }
            }

            val dragModifier = Modifier
                .onGloballyPositioned { coordinates ->
                    itemHeights[index] = coordinates.size.height
                    itemPositions[index] = coordinates.positionInParent().y
                }
                .zIndex(if (isDragging) 1f else 0f)
                .offset {
                    if (isDragging) {
                        IntOffset(x = 0, y = dragOffset.roundToInt())
                    } else {
                        IntOffset(x = 0, y = animatedOffset.value.roundToInt())
                    }
                }
                .pointerInput(key?.invoke(item) ?: item) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            draggingIndex = index
                            dragOffset = 0f
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragOffset += dragAmount.y
                            
                            // Check for swap
                            val draggedIdx = draggingIndex ?: return@detectDragGesturesAfterLongPress
                            val draggedY = itemPositions[draggedIdx] ?: 0f
                            val targetY = draggedY + dragOffset
                            
                            var newIndex = draggedIdx
                            
                            if (dragOffset > 0) {
                                // Движемся вниз
                                for (i in draggedIdx + 1 until items.size) {
                                    val y = itemPositions[i] ?: continue
                                    val h = itemHeights[i] ?: continue
                                    if (targetY > y - h / 2) {
                                        newIndex = i
                                    }
                                }
                            } else if (dragOffset < 0) {
                                // Движемся вверх
                                for (i in draggedIdx - 1 downTo 0) {
                                    val y = itemPositions[i] ?: continue
                                    val h = itemHeights[i] ?: continue
                                    if (targetY < y + h / 2) {
                                        newIndex = i
                                    }
                                }
                            }
                            
                            if (newIndex != draggedIdx) {
                                onReorder(draggedIdx, newIndex)
                                draggingIndex = newIndex
                                // Корректируем offset, так как позиция элемента в списке изменилась
                                val oldPosY = itemPositions[draggedIdx] ?: 0f
                                val newPosY = itemPositions[newIndex] ?: 0f
                                dragOffset += (oldPosY - newPosY)
                            }
                        },
                        onDragEnd = {
                            draggingIndex = null
                            dragOffset = 0f
                            onDragEnd()
                        },
                        onDragCancel = {
                            draggingIndex = null
                            dragOffset = 0f
                        }
                    )
                }

            itemContent(item, isDragging, dragModifier)
        }
    }
}
