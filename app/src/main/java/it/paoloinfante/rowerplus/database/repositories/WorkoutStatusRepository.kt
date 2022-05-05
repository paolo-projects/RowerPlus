package it.paoloinfante.rowerplus.database.repositories

import it.paoloinfante.rowerplus.database.dao.WorkoutDao
import it.paoloinfante.rowerplus.database.dao.WorkoutStatusDao
import it.paoloinfante.rowerplus.database.models.WorkoutStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WorkoutStatusRepository @Inject constructor(private val workoutStatusDao: WorkoutStatusDao) {
    fun getLastStatusForLastWorkout(): Flow<WorkoutStatus?> {
        return workoutStatusDao.getLastStatusForLastWorkout()
    }

    suspend fun pushStatus(workoutStatus: WorkoutStatus) {
        workoutStatusDao.addAll(workoutStatus)
    }
}