package it.paoloinfante.rowerplus.utils

import com.github.mikephil.charting.data.Entry
import it.paoloinfante.rowerplus.database.models.WorkoutStatus

class WorkoutPerformanceDataBuilder(private val workoutData: List<WorkoutStatus>) {
    fun buildDistanceChart(): List<Entry> =
        workoutData.map {
            Entry(it.timeElapsed.toFloat(), it.distance)
        }

    fun buildRPMChart(): List<Entry> =
        workoutData.map {
            Entry(it.timeElapsed.toFloat(), it.currentRPM)
        }

    fun buildCaloriesChart(): List<Entry> =
        workoutData.map {
            Entry(it.timeElapsed.toFloat(), it.calories)
        }

    fun buildBPMChart(): List<Entry> =
        workoutData.map {
            Entry(it.timeElapsed.toFloat(), it.heartRateBpm ?: 0f)
        }
}