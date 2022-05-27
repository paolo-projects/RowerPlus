package it.paoloinfante.rowerplus.fragments.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.paoloinfante.rowerplus.database.models.WorkoutStatus
import it.paoloinfante.rowerplus.database.models.WorkoutWithStatuses
import it.paoloinfante.rowerplus.database.repositories.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class WorkoutDataViewViewModel @Inject constructor(
    private val application: Application,
    private val workoutRepository: WorkoutRepository
) :
    ViewModel() {
    companion object {
        private const val TAG = "WorkoutDataViewViewModel"
    }

    fun getAllWorkoutsWithStatuses(): Flow<List<WorkoutWithStatuses>> {
        return workoutRepository.getAllWorkoutsWithStatuses()
    }

    suspend fun deleteWorkoutById(id: Int) {
        workoutRepository.deleteWorkoutById(id)
    }

    suspend fun deleteWorkoutsById(ids: List<Int>) {
        workoutRepository.deleteWorkoutsByIds(ids)
    }

    suspend fun deleteAll() {
        workoutRepository.deleteAllWorkouts()
    }

    fun getWorkout(id: Int): Flow<WorkoutWithStatuses?> {
        return workoutRepository.getWorkout(id)
    }

    fun getLastWorkoutStatus(): Flow<WorkoutStatus?> {
        return workoutRepository.getLastStatusForLastWorkout()
    }
}