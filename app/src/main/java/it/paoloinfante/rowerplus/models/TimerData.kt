package it.paoloinfante.rowerplus.models

data class TimerData (
    var timeElapsed: Int,
    var calories: Float,
    var distance: Float,
    var rowsCount: Int,
    var currentRPM: Float,
    var currentSecsFor500M: Float
)