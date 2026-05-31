package com.timetask.pro.v2.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================
// Titanium Blue — Default Theme (Технологичный Премиум)
// Стиль: Tesla / Linear / Vercel — строгий, холодный, металл
// ============================================================

// Background & Surface
val TitaniumBackground = Color(0xFF0F172A)       // Очень тёмный сине-серый
val TitaniumSurface = Color(0xFF1E293B)           // Глубокий сланец
val TitaniumSurfaceVariant = Color(0xFF334155)    // Светлый сланец (hover/выделения)
val TitaniumSurfaceContainer = Color(0xFF1A2332)  // Промежуточный (карточки, bottom bar)

// Primary accent
val TitaniumPrimary = Color(0xFF38BDF8)           // Электрический голубой
val TitaniumPrimaryVariant = Color(0xFF0EA5E9)    // Чуть темнее (pressed state)
val TitaniumOnPrimary = Color(0xFF0F172A)         // Текст на акцентных кнопках

// Text
val TitaniumOnBackground = Color(0xFFF1F5F9)     // Холодный белый (основной текст)
val TitaniumOnSurface = Color(0xFFCBD5E1)         // Приглушённый (вторичный текст)
val TitaniumOnSurfaceVariant = Color(0xFF94A3B8)  // Третичный текст / подсказки

// Semantic
val TitaniumError = Color(0xFFF43F5E)             // Мягкий коралловый (ошибки, удаление)
val TitaniumOnError = Color(0xFFFFFFFF)
val TitaniumSuccess = Color(0xFF22C55E)           // Зелёный (чекбоксы, завершение)
val TitaniumWarning = Color(0xFFF59E0B)           // Янтарный (предупреждения)

// Outline & Dividers
val TitaniumOutline = Color(0xFF475569)           // Границы, разделители
val TitaniumOutlineVariant = Color(0xFF334155)    // Тонкие разделители

// ============================================================
// Deep Forest — Future Theme (Эко-Минимализм)
// Будет использоваться для переключения тем в настройках
// ============================================================

val ForestBackground = Color(0xFF1A1C19)
val ForestSurface = Color(0xFF2C332B)
val ForestPrimary = Color(0xFFA3B18A)             // Шалфей
val ForestPrimaryAlt = Color(0xFFDAD7CD)          // Бежевый камень
val ForestOnBackground = Color(0xFFECEBE4)        // Тёплый белый
val ForestError = Color(0xFFCF6679)

// ============================================================
// Item Custom Colors (Tag/Folder Palette)
// ============================================================
val ItemColors = listOf(
    // Blues
    "#E0F2FE", "#BAE6FD", "#7DD3FC", "#38BDF8", "#0EA5E9", "#0284C7", "#0369A1",
    // Reds
    "#FFE4E6", "#FECDD3", "#FDA4AF", "#FB7185", "#F43F5E", "#E11D48", "#BE123C",
    // Greens
    "#DCFCE7", "#BBF7D0", "#86EFAC", "#4ADE80", "#22C55E", "#16A34A", "#15803D",
    // Ambers/Yellows
    "#FEF3C7", "#FDE68A", "#FCD34D", "#FBBF24", "#F59E0B", "#D97706", "#B45309",
    // Purples/Violets
    "#F3E8FF", "#E9D5FF", "#D8B4FE", "#C084FC", "#A855F7", "#9333EA", "#7E22CE",
    // Pinks
    "#FCE7F3", "#FBCFE8", "#F9A8D4", "#F472B6", "#EC4899", "#DB2777", "#BE185D",
    // Slates/Grays
    "#F1F5F9", "#E2E8F0", "#CBD5E1", "#94A3B8", "#64748B", "#475569", "#334155"
)