package it.paoloinfante.rowerplus.database.repositories

import it.paoloinfante.rowerplus.database.dao.WorkoutDao
import it.paoloinfante.rowerplus.database.dao.WorkoutStatusDao
import it.paoloinfante.rowerplus.database.models.Workout
import it.paoloinfante.rowerplus.database.models.WorkoutStatus
import it.paoloinfante.rowerplus.database.models.WorkoutWithStatuses
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WorkoutRepository @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val workoutStatusDao: WorkoutStatusDao
) {
    suspend fun getLastWorkoutId(): Int? {
        return workoutDao.getLastWorkoutId()
    }

    suspend fun workoutExists(id: Int): Boolean {
        return workoutDao.exists(id)
    }

    suspend fun insertWorkout(workout: Workout) {
        workoutDao.insertAll(workout)
    }

    fun getAllWorkoutsWithStatuses(): Flow<List<WorkoutWithStatuses>> {
        return workoutDao.getWorkoutsWithStatuses()
    }

    fun getWorkout(id: Int): Flow<WorkoutWithStatuses?> {
        return workoutDao.getWorkout(id)
    }

    suspend fun deleteWorkoutById(id: Int) {
        workoutDao.deleteById(id)
    }

    suspend fun deleteWorkoutsByIds(ids: List<Int>) {
        workoutDao.deleteByIds(ids)
    }

    suspend fun deleteAllWorkouts() {
        workoutDao.deleteAll()
    }

    fun getLastStatusForLastWorkout(): Flow<WorkoutStatus?> {
        return workoutStatusDao.getLastStatusForLastWorkout()
    }

    suspend fun pushStatus(workoutStatus: WorkoutStatus) {
        workoutStatusDao.addAll(workoutStatus)
    }
}