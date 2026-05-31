package com.timetask.pro.v2.presentation.tools.chrono

import androidx.compose.runtime.Immutable
import java.util.UUID

/**
 * Состояние одного секундомера.
 * Timestamp-based: elapsed = now - startTimeMs + accumulatedMs
 */
enum class StopwatchState {
    IDLE,
    RUNNING,
    PAUSED,
}

@Immutable
data class StopwatchInstance(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Секундомер",
    val elapsedMs: Long = 0L,
    val state: StopwatchState = StopwatchState.IDLE,
    /** Timestamp when this stopwatch was last started/resumed. */
    val startTimeMs: Long = 0L,
    /** Accumulated time from previous runs (before pauses). */
    val accumulatedMs: Long = 0L,
    /** Lap times (list of elapsed ms at moment of each lap). */
    val laps: List<Long> = emptyList(),
    val tagIds: List<Long> = emptyList(),
)
