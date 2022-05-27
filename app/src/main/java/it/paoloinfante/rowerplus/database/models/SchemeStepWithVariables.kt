package it.paoloinfante.rowerplus.database.models

import androidx.room.Embedded
import androidx.room.Relation

data class SchemeStepWithVariables(
    @Embedded
    val schemeStep: SchemeStep,
    @Relation(
        parentColumn = "id", entityColumn = "schemeStepId"
    )
    val schemeStepVariables: List<SchemeStepVariable>
)