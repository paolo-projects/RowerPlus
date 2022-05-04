package it.paoloinfante.rowerplus.fragments.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.paoloinfante.rowerplus.database.models.WorkoutStatus
import it.paoloinfante.rowerplus.database.models.WorkoutWithStatuses
import it.paoloinfante.rowerplus.database.repositories.WorkoutRepository
import it.paoloinfante.rowerplus.database.repositories.WorkoutStatusRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class WorkoutDataViewViewModel @Inject constructor(private val workoutStatusRepository: WorkoutStatusRepository, private val workoutRepository: WorkoutRepository) :
    ViewModel() {

    fun getLastStatus(): Flow<WorkoutStatus> {
        return workoutStatusRepository.getLastStatusForLastWorkout()
    }

    fun getAllWorkoutsWithStatuses(): Flow<List<WorkoutWithStatuses>> {
        return workoutRepository.getAllWorkoutsWithStatuses()
    }

    suspend fun deleteWorkoutById(id: Int) {
        workoutRepository.deleteById(id)
    }

    suspend fun deleteAll() {
        workoutRepository.deleteAll()
    }
}