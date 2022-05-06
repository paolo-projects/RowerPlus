package it.paoloinfante.rowerplus.fragments.viewmodels

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.paoloinfante.rowerplus.R
import it.paoloinfante.rowerplus.database.models.WorkoutStatus
import it.paoloinfante.rowerplus.database.models.WorkoutWithStatuses
import it.paoloinfante.rowerplus.database.repositories.WorkoutRepository
import it.paoloinfante.rowerplus.database.repositories.WorkoutStatusRepository
import it.paoloinfante.rowerplus.models.TimerData
import it.paoloinfante.rowerplus.utils.Stopwatch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.floor

@HiltViewModel
class WorkoutDataViewViewModel @Inject constructor(
    private val application: Application,
    private val workoutStatusRepository: WorkoutStatusRepository,
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
        workoutRepository.deleteById(id)
    }

    suspend fun deleteWorkoutsById(ids: List<Int>) {
        workoutRepository.deleteByIds(ids)
    }

    suspend fun deleteAll() {
        workoutRepository.deleteAll()
    }

    fun getWorkout(id: Int): Flow<WorkoutWithStatuses?> {
        return workoutRepository.getWorkout(id)
    }

    fun getLastWorkoutStatus(): Flow<WorkoutStatus?> {
        return workoutStatusRepository.getLastStatusForLastWorkout()
    }
}