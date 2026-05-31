package com.timetask.pro.v2.data.local.db.entity

import kotlinx.serialization.Serializable
import androidx.compose.runtime.Immutable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.UUID

@Entity(tableName = "timers")
@Serializable
data class TimerEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Таймер",
    
    /** Total duration in milliseconds (e.g. 5 mins = 300_000) */
    val totalDurationMs: Long = 5 * 60 * 1000L,
    
    /** 
     * Snapshot of remaining time when PAUSED/IDLE. 
     * When RUNNING, this is ignored in favor of `endTimeMs - now`.
     */
    val remainingMs: Long = totalDurationMs,
    
    val state: TimerState = TimerState.IDLE,
    
    /** 
     * Timestamp (System.currentTimeMillis()) when this timer should finish. 
     * Only valid if state == RUNNING. 
     * If 0, not running.
     */
    val endTimeMs: Long = 0L,
    
    /** Timestamp when last started/resumed (для elapsed counter) */
    val startedAtMs: Long = 0L,

    /** Timestamp момента паузы (для корректного расчёта elapsed) */
    val pausedAtMs: Long = 0L,

    /** Накопленное время пауз (вычитается из elapsed) */
    val accumulatedPauseMs: Long = 0L,

    @Embedded(prefix = "cfg_")
    val config: TimerConfig = TimerConfig(),

    @Embedded(prefix = "notif_")
    val notification: TimerNotification = TimerNotification(),

    // Metadata links
    val categoryId: String? = null,
    @ColumnInfo(defaultValue = "'[]'")
    val tagIdsJson: String = "[]", // Stored as JSON string
    
    /** Привязка к конкретным задачам для учета времени */
    val linkedTaskIdsJson: String = "[]",
    
    val createdAt: Long = System.currentTimeMillis(),
)

@Serializable
enum class TimerState {
    IDLE,
    RUNNING,
    PAUSED,
    FINISHED
}

@Immutable
@Serializable
data class TimerConfig(
    val adjustStepSec: Int = 60, // 1 min default
    val isCountDown: Boolean = true,
    val enableOvertime: Boolean = false,
    val autoRepeat: Boolean = false,
    val autoReset: Boolean = false,
    val quickAddDurationSec: Int = 60, // 1 min default
)

@Immutable
@Serializable
data class TimerNotification(
    /** Показывать этот таймер в уведомлениях шторки */
    val showInNotifications: Boolean = true,
    val soundUri: String? = null,
    val volume: Float = 1.0f,
    val isSilent: Boolean = false,
    val isLooping: Boolean = true,
    val vibrationPatternJson: String? = null,
    val snoozeDurationSec: Int = 300, // 5 min
    val ringingDurationSec: Int = 180, // Duration of ringing (in sec). -1 = infinite
)
