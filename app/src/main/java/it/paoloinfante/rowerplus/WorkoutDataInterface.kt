package it.paoloinfante.rowerplus

import androidx.lifecycle.LiveData
import it.paoloinfante.rowerplus.database.models.WorkoutStatus

interface WorkoutDataInterface {
    val workoutData: LiveData<WorkoutStatus>
}