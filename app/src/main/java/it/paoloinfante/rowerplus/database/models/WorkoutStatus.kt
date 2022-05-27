package it.paoloinfante.rowerplus.database.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = CASCADE,
            onUpdate = CASCADE
        )
    ],
    indices = [
        Index("workoutId")
    ]
)
data class WorkoutStatus(
    @PrimaryKey(autoGenerate = true) var id: Int?,
    var workoutId: Int,
    var timeElapsed: Int = 0, // sec
    var calories: Float = 0f, // cal
    var distance: Float = 0f,// meters
    var rowsCount: Int = 0,
    var currentRPM: Float = 0f,
    var currentSecsFor500M: Float = 0f,
    var heartRateBpm: Float?
)