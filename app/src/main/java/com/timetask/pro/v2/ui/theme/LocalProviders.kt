package com.timetask.pro.v2.ui.theme

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.compositionLocalOf

/**
 * CompositionLocal для WindowSizeClass — доступ к размеру окна из любого Composable.
 * Устанавливается в MainActivity через CompositionLocalProvider.
 * Используется для адаптивной верстки (телефон / планшет / складной).
 */
val LocalWindowSizeClass = compositionLocalOf<WindowSizeClass> {
    error("WindowSizeClass не предоставлен. Оберните контент в CompositionLocalProvider.")
}
