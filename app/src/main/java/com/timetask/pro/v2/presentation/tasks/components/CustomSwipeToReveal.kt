package com.timetask.pro.v2.presentation.tasks.components

import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Глобальный менеджер свайпов.
 * Позволяет закрывать открытый свайп при нажатии в любое место.
 */
object SwipeManager {
    var openItem by mutableStateOf<String?>(null)

    /** Закрыть все открытые свайпы */
    fun closeAll() {
        openItem = null
    }
}

/**
 * Возможные состояния свайпа.
 */
enum class SwipeState {
    CLOSED,
    LEFT_OPEN,
    RIGHT_OPEN
}

/**
 * Кастомный компонент свайпа (TickTick style).
 *
 * КЛЮЧЕВОЙ ПРИНЦИП: Кнопки ПРИВЯЗАНЫ к краю карточки.
 * - Когда свайп закрыт, кнопки полностью скрыты за краем карточки.
 * - Когда свайп открывается, кнопки «вытягиваются» вместе с краем.
 * - Через скругленные углы карточки ничего не просвечивает.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomSwipeToReveal(
    leftMenuWidth: Dp,
    rightMenuWidth: Dp,
    modifier: Modifier = Modifier,
    leftMenuContent: @Composable () -> Unit = {},
    rightMenuContent: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val itemKey = remember { java.util.UUID.randomUUID().toString() }

    val leftWidthPx = with(density) { leftMenuWidth.toPx() }
    val rightWidthPx = with(density) { rightMenuWidth.toPx() }

    val anchors = DraggableAnchors {
        SwipeState.CLOSED at 0f
        if (leftWidthPx > 0f) {
            SwipeState.LEFT_OPEN at leftWidthPx
        }
        if (rightWidthPx > 0f) {
            SwipeState.RIGHT_OPEN at -rightWidthPx
        }
    }

    val state = remember {
        AnchoredDraggableState<SwipeState>(
            initialValue = SwipeState.CLOSED,
            anchors = anchors,
            positionalThreshold = { distance: Float -> distance * 0.4f },
            velocityThreshold = { with(density) { 400.dp.toPx() } }, // Менее чувствительный порог
            snapAnimationSpec = spring(
                dampingRatio = 0.8f,  // Плавная пружина без дерганья
                stiffness = 400f      // Средняя жесткость
            ),
            decayAnimationSpec = androidx.compose.animation.core.exponentialDecay()
        )
    }

    SideEffect {
        state.updateAnchors(anchors)
    }

    // Haptic feedback при открытии
    LaunchedEffect(state.targetValue) {
        if (state.targetValue != SwipeState.CLOSED) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            SwipeManager.openItem = itemKey
        }
    }

    // Закрыть этот свайп, если открылся другой или вызван closeAll()
    LaunchedEffect(SwipeManager.openItem) {
        if (SwipeManager.openItem != itemKey && state.currentValue != SwipeState.CLOSED) {
            state.animateTo(SwipeState.CLOSED)
        }
    }

    // Внешний контейнер: clipToBounds скрывает всё за пределами
    // + clip(RoundedCornerShape) + фон, чтобы через скругленные углы ничего не просвечивало
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clipToBounds()
            .background(MaterialTheme.colorScheme.surfaceContainer) // Тот же цвет что и у карточки
    ) {
        val currentOffset = state.requireOffset()
        val isOpen = state.currentValue != SwipeState.CLOSED

        // ============================================================
        // ЛЕВЫЕ КНОПКИ — привязаны к левому краю карточки
        // offset = currentOffset - leftWidthPx
        // Closed (offset=0): x = -leftWidthPx (скрыты за левым краем)
        // Open (offset=+leftWidthPx): x = 0 (видны)
        // ============================================================
        if (leftWidthPx > 0f) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .offset {
                        IntOffset(
                            x = (currentOffset - leftWidthPx).roundToInt().coerceAtMost(0),
                            y = 0
                        )
                    }
            ) {
                leftMenuContent()
            }
        }

        // ============================================================
        // ПРАВЫЕ КНОПКИ — привязаны к правому краю карточки
        // offset = currentOffset + rightWidthPx
        // Closed (offset=0): x = +rightWidthPx (скрыты за правым краем)
        // Open (offset=-rightWidthPx): x = 0 (видны)
        // ============================================================
        if (rightWidthPx > 0f) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .offset {
                        IntOffset(
                            x = (currentOffset + rightWidthPx).roundToInt().coerceAtLeast(0),
                            y = 0
                        )
                    }
            ) {
                rightMenuContent()
            }
        }

        // ============================================================
        // КАРТОЧКА — сдвигается по горизонтали
        // ============================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset {
                    IntOffset(
                        x = currentOffset.roundToInt(),
                        y = 0
                    )
                }
                .anchoredDraggable(
                    state = state,
                    orientation = Orientation.Horizontal
                )
        ) {
            content()

            // Невидимый оверлей поверх карточки, ТОЛЬКО когда свайп открыт.
            // Перехватывает ВСЕ тапы → закрывает свайп → НЕ пропускает клик в content.
            if (isOpen) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .pointerInput(Unit) {
                            detectTapGestures {
                                // Закрываем свайп, клик НЕ идёт к карточке
                                scope.launch { state.animateTo(SwipeState.CLOSED) }
                            }
                        }
                )
            }
        }
    }
}
