package it.paoloinfante.rowerplus.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Scheme(
    @PrimaryKey(autoGenerate = true)
    var id: Int?,
    var name: String,
    var time: Date
)
