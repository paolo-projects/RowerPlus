package it.paoloinfante.rowerplus.models

import java.util.*

data class RowerPull (
    var time: Date,
    var energy: Float,
    var power: Float,
    var distance: Float
)