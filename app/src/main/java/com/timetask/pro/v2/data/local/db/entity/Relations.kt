package com.timetask.pro.v2.data.local.db.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.timetask.pro.v2.data.local.entity.StopwatchEntity

data class TaskWithTags(
    @Embedded val task: TaskEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TaskTagCrossRef::class,
            parentColumn = "taskId",
            entityColumn = "tagId"
        )
    )
    val tags: List<TagEntity>
)

data class TimerWithTags(
    @Embedded val timer: TimerEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TimerTagCrossRef::class,
            parentColumn = "timerId",
            entityColumn = "tagId"
        )
    )
    val tags: List<TagEntity>
)

data class AlarmWithTags(
    @Embedded val alarm: AlarmEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = AlarmTagCrossRef::class,
            parentColumn = "alarmId",
            entityColumn = "tagId"
        )
    )
    val tags: List<TagEntity>
)

data class StopwatchWithTags(
    @Embedded val stopwatch: StopwatchEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = StopwatchTagCrossRef::class,
            parentColumn = "stopwatchId",
            entityColumn = "tagId"
        )
    )
    val tags: List<TagEntity>
)
