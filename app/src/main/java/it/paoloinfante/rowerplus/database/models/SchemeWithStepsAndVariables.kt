package it.paoloinfante.rowerplus.database.models

import androidx.room.Embedded
import androidx.room.Relation

data class SchemeWithStepsAndVariables(
    @Embedded
    val scheme: Scheme,
    @Relation(
        entity = SchemeStep::class,
        parentColumn = "id",
        entityColumn = "schemeId"
    )
    val schemeSteps: List<SchemeStepWithVariables>
)
