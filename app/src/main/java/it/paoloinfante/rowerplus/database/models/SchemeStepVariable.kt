package it.paoloinfante.rowerplus.database.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = SchemeStep::class,
            parentColumns = ["id"],
            childColumns = ["schemeStepId"],
            onDelete = CASCADE
        )
    ],
    indices = [
        Index("schemeStepId")
    ]
)
data class SchemeStepVariable(
    @PrimaryKey(autoGenerate = true)
    var id: Int?,
    var schemeStepId: Int,
    var parameter: String,
    var value: Float
)
