package it.paoloinfante.rowerplus.database.models

import androidx.room.Embedded
import androidx.room.Relation

data class WorkoutWithStatuses(
    @Embedded val workout: Workout,
    @Relation(
        parentColumn = "id",
        entityColumn = "workoutId",
    )
    val workoutStatuses: List<WorkoutStatus>
)
