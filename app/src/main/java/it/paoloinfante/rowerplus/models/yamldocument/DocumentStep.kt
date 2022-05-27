package it.paoloinfante.rowerplus.models.yamldocument

data class DocumentStep(
    var duration: Long,
    var variables: List<DocumentStepVariable>
)
