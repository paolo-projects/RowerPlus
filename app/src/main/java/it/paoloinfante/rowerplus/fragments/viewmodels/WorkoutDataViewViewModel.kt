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

    private var ROWING_TIMER_TIMEOUT_MS: Int = 5000
    private var DATA_UPDATE_TIMER_INTERVAL_MS: Int = 100

    private val timerDataLock = Object()
    private val _timerData = MutableStateFlow(TimerData(0, 0f, 0f, 0, 0f, 0f))
    val timerData: StateFlow<TimerData> = _timerData

    private var lastWorkoutStatus: WorkoutStatus = WorkoutStatus(null, 0, 0, 0f, 0f, 0, 0f, 0f)

    private val timerCancelHandler = Handler(Looper.getMainLooper())
    private val timerThreadPool = Executors.newScheduledThreadPool(1)
    private var timerUpdater: ScheduledFuture<*>? = null
    private val stopwatch = Stopwatch()

    init {
        ROWING_TIMER_TIMEOUT_MS =
            application.resources.getInteger(R.integer.rowing_timer_timeout_ms)
        DATA_UPDATE_TIMER_INTERVAL_MS =
            application.resources.getInteger(R.integer.data_update_timer_interval_ms)
    }

    fun startDataCollection() {
        viewModelScope.launch {
            workoutStatusRepository.getLastStatusForLastWorkout().collect(workoutStatusCollector)
        }
    }

    private val workoutStatusCollector = FlowCollector<WorkoutStatus?> { value ->
        if (value != null) {
            synchronized(timerDataLock) {
                timerCancelHandler.removeCallbacks(timerCancelFutureTask)
                timerCancelHandler.postDelayed(
                    timerCancelFutureTask,
                    ROWING_TIMER_TIMEOUT_MS.toLong()
                )

                _timerData.value = TimerData(
                    value.timeElapsed,
                    value.calories,
                    value.distance,
                    value.rowsCount,
                    value.currentRPM,
                    value.currentSecsFor500M
                )

                lastWorkoutStatus = value

                stopwatch.reset()
                stopwatch.start()
            }

            if (timerUpdater == null) {
                timerUpdater =
                    timerThreadPool.scheduleAtFixedRate(
                        timerUpdaterTask,
                        DATA_UPDATE_TIMER_INTERVAL_MS.toLong(),
                        DATA_UPDATE_TIMER_INTERVAL_MS.toLong(),
                        TimeUnit.MILLISECONDS
                    )
            }
        }
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

    private val timerUpdaterTask = {
        Log.d(TAG, "timerUpdateTask: timer tick")
        synchronized(timerDataLock) {
            _timerData.value =
                _timerData.value.copy(timeElapsed = lastWorkoutStatus.timeElapsed + floor(stopwatch.elapsedSeconds).toInt())
        }
    }

    private val timerCancelFutureTask = Runnable { timerUpdater?.cancel(true) }
}