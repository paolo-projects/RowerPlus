package it.paoloinfante.rowerplus.database.repositories

import it.paoloinfante.rowerplus.database.dao.WorkoutDao
import it.paoloinfante.rowerplus.database.models.Workout
import it.paoloinfante.rowerplus.database.models.WorkoutWithStatuses
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WorkoutRepository @Inject constructor(private val workoutDao: WorkoutDao) {
    suspend fun getLastWorkoutId(): Int? {
        return workoutDao.getLastWorkoutId()
    }

    suspend fun insert(workout: Workout) {
        workoutDao.insertAll(workout)
    }

    fun getAllWorkoutsWithStatuses(): Flow<List<WorkoutWithStatuses>> {
        return workoutDao.getWorkoutsWithStatuses()
    }

    suspend fun deleteById(id: Int) {
        workoutDao.deleteById(id)
    }

    suspend fun deleteAll() {
        workoutDao.deleteAll()
    }
}