package com.timetask.pro.v2.domain.model.chrono

import com.timetask.pro.v2.presentation.tools.chrono.StopwatchState
import com.timetask.pro.v2.data.local.entity.StopwatchNotification

/**
 * Domain model representing a running Lap.
 */
data class Lap(
    val id: String,
    val lapNumber: Int,
    val lapTimeMs: Long,
    val totalTimeMs: Long,
    val isBest: Boolean = false,
    val isWorst: Boolean = false,
    val title: String? = null,
    val categoryText: String = "",
    val tagsText: String = "",
    val colorARGB: Int? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)

/**
 * Domain model representing a Stopwatch with all its Laps.
 */
data class Stopwatch(
    val id: String,
    val name: String,
    val state: StopwatchState,
    val startTimeMs: Long,
    val accumulatedMs: Long,
    val categoryId: Long? = null,
    val linkedTaskIdsJson: String = "[]",
    val categoryText: String = "",
    val tagsText: String = "",
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val notification: StopwatchNotification = StopwatchNotification(),
    val laps: List<Lap>
) {
    /**
     * Helper to compute elapsed time dynamically if needed.
     * Note: UI should typically compute this actively on a tick.
     */
    fun computeElapsedMs(nowMs: Long): Long {
        return if (state == StopwatchState.RUNNING) {
            nowMs - startTimeMs + accumulatedMs
        } else {
            accumulatedMs
        }
    }
}
