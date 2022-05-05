package it.paoloinfante.rowerplus.utils

import it.paoloinfante.rowerplus.database.models.WorkoutStatus
import it.paoloinfante.rowerplus.models.RowerPull
import java.util.*
import kotlin.math.floor

class RowerDataParser(private val ROWS_PER_CALORIE: Float, private val METERS_PER_ROW: Float) {
    private var previousElapsedMillis: Long = 0

    /**
     * Parse data received from the rowing machine.
     * TODO: This can be further improved by taking into account the parameters sent by the MCU (avg time + count of switch triggers in the pull phase)
     */
    fun parseData(data: RowerPull, existingRowerStatus: WorkoutStatus, elapsedTimeStopwatch: Stopwatch) {
        val elapsedTimeMs = elapsedTimeStopwatch.elapsedMilliseconds

        existingRowerStatus.timeElapsed = floor(elapsedTimeMs / 1000f).toInt()
        existingRowerStatus.calories += 1f / ROWS_PER_CALORIE
        existingRowerStatus.distance += METERS_PER_ROW
        existingRowerStatus.rowsCount++

        val timeElapsedDelta = elapsedTimeMs - previousElapsedMillis

        if (existingRowerStatus.rowsCount > 0 && timeElapsedDelta > 0) {
            existingRowerStatus.currentRPM = 60000f / timeElapsedDelta
            existingRowerStatus.currentSecsFor500M =
                timeElapsedDelta.toFloat() / 1000 * (500f / METERS_PER_ROW)
        } else {
            existingRowerStatus.currentRPM = 0f
            existingRowerStatus.currentSecsFor500M = 0f
        }

        previousElapsedMillis = elapsedTimeMs
    }
}