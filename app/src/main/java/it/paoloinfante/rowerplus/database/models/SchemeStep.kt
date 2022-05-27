package it.paoloinfante.rowerplus.database.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Duration

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Scheme::class,
            parentColumns = ["id"],
            childColumns = ["schemeId"],
            onDelete = CASCADE
        )
    ],
    indices = [
        Index("schemeId")
    ]
)
data class SchemeStep(
    @PrimaryKey(autoGenerate = true)
    var id: Int?,
    var schemeId: Int,
    var duration: Duration,
)
