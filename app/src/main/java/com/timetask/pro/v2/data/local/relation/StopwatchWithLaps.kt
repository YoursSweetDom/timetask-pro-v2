package com.timetask.pro.v2.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.timetask.pro.v2.data.local.entity.StopwatchEntity
import com.timetask.pro.v2.data.local.entity.StopwatchLapEntity

data class StopwatchWithLaps(
    @Embedded val stopwatch: StopwatchEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "stopwatchId"
    )
    val laps: List<StopwatchLapEntity>
)
