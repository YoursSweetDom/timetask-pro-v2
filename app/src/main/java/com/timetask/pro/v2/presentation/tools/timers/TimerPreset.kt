package com.timetask.pro.v2.presentation.tools.timers

import androidx.compose.runtime.Immutable

@Immutable
data class TimerPreset(
    val name: String,
    val durationMs: Long,
    val emoji: String = "⏱️"
)

val defaultTimerPresets = listOf(
    TimerPreset("Фокус", 25 * 60 * 1000L, "🍅"),
    TimerPreset("Короткий перерыв", 5 * 60 * 1000L, "☕"),
    TimerPreset("Длинный перерыв", 15 * 60 * 1000L, "🧘"),
    TimerPreset("Яйца всмятку", 3 * 60 * 1000L, "🥚"),
    TimerPreset("Спагетти", 9 * 60 * 1000L, "🍝"),
    TimerPreset("Чай", 3 * 60 * 1000L, "🍵"),
)

/**
 * Стандартные пресеты длительности (без имени) — для быстрого выбора времени.
 */
data class DurationPreset(val label: String, val durationMs: Long)

val standardDurationPresets = listOf(
    DurationPreset("30s", 30 * 1000L),
    DurationPreset("1m", 60 * 1000L),
    DurationPreset("5m", 5 * 60 * 1000L),
    DurationPreset("10m", 10 * 60 * 1000L),
    DurationPreset("15m", 15 * 60 * 1000L),
    DurationPreset("30m", 30 * 60 * 1000L),
    DurationPreset("1h", 60 * 60 * 1000L),
    DurationPreset("2h", 2 * 60 * 60 * 1000L),
)
