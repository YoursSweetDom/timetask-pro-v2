package com.timetask.pro.v2.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ============================================================
// Titanium Blue — Dark Color Scheme
// ============================================================
private val TitaniumDarkColorScheme = darkColorScheme(
    primary = TitaniumPrimary,
    onPrimary = TitaniumOnPrimary,
    primaryContainer = TitaniumPrimaryVariant,
    onPrimaryContainer = TitaniumOnBackground,

    secondary = TitaniumPrimary,
    onSecondary = TitaniumOnPrimary,

    background = TitaniumBackground,
    onBackground = TitaniumOnBackground,

    surface = TitaniumSurface,
    onSurface = TitaniumOnBackground,
    surfaceVariant = TitaniumSurfaceVariant,
    onSurfaceVariant = TitaniumOnSurfaceVariant,
    surfaceContainerLow = TitaniumSurfaceContainer,
    surfaceContainer = TitaniumSurfaceContainer,
    surfaceContainerHigh = TitaniumSurfaceVariant,

    error = TitaniumError,
    onError = TitaniumOnError,

    outline = TitaniumOutline,
    outlineVariant = TitaniumOutlineVariant,

    inverseSurface = TitaniumOnBackground,
    inverseOnSurface = TitaniumBackground,
    inversePrimary = TitaniumPrimaryVariant,
)

// ============================================================
// Titanium Blue — Light Color Scheme (для режима "Тема устройства")
// ============================================================
private val TitaniumLightColorScheme = lightColorScheme(
    primary = TitaniumPrimaryVariant,
    onPrimary = TitaniumOnBackground,
    primaryContainer = TitaniumPrimary,

    background = TitaniumOnBackground,
    onBackground = TitaniumBackground,

    surface = TitaniumOnBackground,
    onSurface = TitaniumBackground,
    surfaceVariant = TitaniumOnSurface,
    onSurfaceVariant = TitaniumSurfaceVariant,

    error = TitaniumError,
    onError = TitaniumOnError,

    outline = TitaniumOutline,
    outlineVariant = TitaniumOnSurface,
)

/**
 * Режим темы приложения.
 *  TITANIUM_BLUE — всегда тёмная Titanium Blue тема (по умолчанию)
 *  SYSTEM — следует за темой устройства (светлая/тёмная)
 */
enum class AppThemeMode {
    TITANIUM_BLUE,
    SYSTEM,
}

/**
 * Перечисление цветовых тем (для будущего расширения — Deep Forest и т.д.)
 */
enum class AppTheme {
    TITANIUM_BLUE,
    // DEEP_FOREST  — будет добавлена позже
}

@Composable
fun TimeTaskProV2Theme(
    themeMode: AppThemeMode = AppThemeMode.TITANIUM_BLUE,
    appTheme: AppTheme = AppTheme.TITANIUM_BLUE,
    content: @Composable () -> Unit
) {
    val useDarkTheme = when (themeMode) {
        AppThemeMode.TITANIUM_BLUE -> true  // Всегда тёмная
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when (appTheme) {
        AppTheme.TITANIUM_BLUE -> {
            if (useDarkTheme) TitaniumDarkColorScheme else TitaniumLightColorScheme
        }
    }

    // Status bar & navigation bar appearance (light/dark icons)
    // Цвет фона задаётся через enableEdgeToEdge() в MainActivity
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !useDarkTheme
                isAppearanceLightNavigationBars = !useDarkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}