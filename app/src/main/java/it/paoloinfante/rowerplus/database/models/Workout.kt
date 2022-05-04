package it.paoloinfante.rowerplus.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Workout(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    val name: String,
    val time: Date
)
