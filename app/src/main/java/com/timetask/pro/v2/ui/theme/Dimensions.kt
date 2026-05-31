package com.timetask.pro.v2.ui.theme

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing system — единообразные отступы по всему приложению.
 * Используется вместо хардкод-значений.
 */
object Spacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
    val xxxl = 64.dp
}

/**
 * Размеры иконок.
 */
object IconSize {
    val xs = 16.dp
    val sm = 20.dp
    val md = 24.dp
    val lg = 32.dp
    val xl = 48.dp
}

/**
 * Углы скругления (corner radius).
 */
object Corners {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val full = 999.dp  // Полный круг
}

/**
 * Высоты стандартных компонентов.
 */
object ComponentSize {
    val bottomNavHeight = 80.dp
    val topBarHeight = 56.dp
    val taskItemHeight = 56.dp
    val fabSize = 56.dp
    val tabHeight = 48.dp
}

/**
 * Адаптивные отступы на основе WindowSizeClass.
 * Compact (телефон) → стандартные отступы.
 * Medium (складной/маленький планшет) → увеличенные.
 * Expanded (планшет/десктоп) → максимальные.
 */
data class AdaptiveSpacing(
    val screenPadding: Dp,
    val contentPadding: Dp,
    val itemSpacing: Dp,
    val sectionSpacing: Dp,
)

val compactSpacing = AdaptiveSpacing(
    screenPadding = 8.dp,
    contentPadding = 16.dp,
    itemSpacing = 4.dp,
    sectionSpacing = 16.dp,
)

val mediumSpacing = AdaptiveSpacing(
    screenPadding = 24.dp,
    contentPadding = 24.dp,
    itemSpacing = 8.dp,
    sectionSpacing = 24.dp,
)

val expandedSpacing = AdaptiveSpacing(
    screenPadding = 48.dp,
    contentPadding = 32.dp,
    itemSpacing = 12.dp,
    sectionSpacing = 32.dp,
)

/**
 * Возвращает адаптивные отступы на основе текущего WindowSizeClass.
 */
@Composable
@ReadOnlyComposable
fun adaptiveSpacing(): AdaptiveSpacing {
    val windowSizeClass = LocalWindowSizeClass.current
    return when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> compactSpacing
        WindowWidthSizeClass.Medium -> mediumSpacing
        else -> expandedSpacing
    }
}
