package com.timetask.pro.v2.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "task_tag_cross_ref",
    primaryKeys = ["taskId", "tagId"],
    indices = [
        Index("taskId"),
        Index("tagId")
    ]
)
data class TaskTagCrossRef(
    val taskId: Long,
    val tagId: Long
)

@Entity(
    tableName = "timer_tag_cross_ref",
    primaryKeys = ["timerId", "tagId"],
    indices = [
        Index("timerId"),
        Index("tagId")
    ]
)
data class TimerTagCrossRef(
    val timerId: String,
    val tagId: Long
)

@Entity(
    tableName = "alarm_tag_cross_ref",
    primaryKeys = ["alarmId", "tagId"],
    indices = [
        Index("alarmId"),
        Index("tagId")
    ]
)
data class AlarmTagCrossRef(
    val alarmId: String,
    val tagId: Long
)

@Entity(
    tableName = "stopwatch_tag_cross_ref",
    primaryKeys = ["stopwatchId", "tagId"],
    indices = [
        Index("stopwatchId"),
        Index("tagId")
    ]
)
data class StopwatchTagCrossRef(
    val stopwatchId: String,
    val tagId: Long
)
