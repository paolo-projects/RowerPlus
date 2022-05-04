package it.paoloinfante.rowerplus.database.dao

import androidx.room.*
import it.paoloinfante.rowerplus.database.models.Workout
import it.paoloinfante.rowerplus.database.models.WorkoutWithStatuses
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout")
    suspend fun getAll(): List<Workout>

    @Transaction
    @Query("SELECT * FROM workout")
    fun getWorkoutsWithStatuses(): Flow<List<WorkoutWithStatuses>>

    @Query("SELECT id FROM workout ORDER BY id DESC LIMIT 1")
    suspend fun getLastWorkoutId(): Int?

    @Query("SELECT * FROM workout WHERE id = :id")
    suspend fun findById(id: Int): Workout

    @Insert
    suspend fun insertAll(vararg workouts: Workout)

    @Transaction
    @Query("SELECT * FROM workout ORDER BY id DESC LIMIT 1")
    fun getLastWorkoutWithStatuses(): Flow<WorkoutWithStatuses>

    @Delete
    suspend fun delete(workout: Workout)

    @Query("DELETE FROM workout WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM workout WHERE 1")
    suspend fun deleteAll()
}