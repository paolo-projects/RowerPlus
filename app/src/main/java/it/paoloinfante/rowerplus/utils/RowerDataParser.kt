package it.paoloinfante.rowerplus.utils

import it.paoloinfante.rowerplus.database.models.WorkoutStatus
import it.paoloinfante.rowerplus.models.RowerPull
import kotlin.math.floor

class RowerDataParser(private val ROWS_PER_CALORIE: Float, private val METERS_PER_ROW: Float) {
    private var previousElapsedMillis: Long = 0

    /**
     * Parse data received from the rowing machine.
     */
    fun parseData(
        data: RowerPull,
        existingRowerStatus: WorkoutStatus,
        elapsedTimeStopwatch: Stopwatch
    ) {
        val elapsedTimeMs = elapsedTimeStopwatch.elapsedMilliseconds

        existingRowerStatus.timeElapsed = floor(elapsedTimeMs / 1000f).toInt()
        existingRowerStatus.calories += getScaledCalories(data)
        existingRowerStatus.distance += getScaledDistance(data)
        existingRowerStatus.rowsCount++

        val timeElapsedDelta = elapsedTimeMs - previousElapsedMillis

        if (existingRowerStatus.rowsCount > 0 && timeElapsedDelta > 1) {
            existingRowerStatus.currentRPM = 60000f / timeElapsedDelta
            existingRowerStatus.currentSecsFor500M =
                timeElapsedDelta.toFloat() / 1000 * (500f / METERS_PER_ROW)
        } else {
            existingRowerStatus.currentRPM = 0f
            existingRowerStatus.currentSecsFor500M = 0f
        }

        previousElapsedMillis = elapsedTimeMs
    }

    private fun getScaledDistance(data: RowerPull): Float {
        return data.distance
    }

    private fun getScaledCalories(data: RowerPull): Float {
        return data.energy
    }
}