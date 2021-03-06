package it.paoloinfante.rowerplus.database.dao

import androidx.room.*
import it.paoloinfante.rowerplus.database.models.Workout
import it.paoloinfante.rowerplus.database.models.WorkoutWithStatuses
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout")
    suspend fun getAll(): List<Workout>

    @Query("SELECT EXISTS(SELECT * FROM workout WHERE id = :id)")
    suspend fun exists(id: Int): Boolean

    @Transaction
    @Query("SELECT * FROM workout ORDER BY id DESC")
    fun getWorkoutsWithStatuses(): Flow<List<WorkoutWithStatuses>>

    @Query("SELECT id FROM workout ORDER BY id DESC LIMIT 1")
    suspend fun getLastWorkoutId(): Int?

    @Query("SELECT * FROM workout WHERE id = :id")
    suspend fun findById(id: Int): Workout

    @Transaction
    @Query("SELECT * FROM workout WHERE id = :id")
    fun getWorkout(id: Int): Flow<WorkoutWithStatuses?>

    @Insert
    suspend fun insertAll(vararg workouts: Workout)

    @Transaction
    @Query("SELECT * FROM workout ORDER BY id DESC LIMIT 1")
    fun getLastWorkoutWithStatuses(): Flow<WorkoutWithStatuses>

    @Delete
    suspend fun delete(workout: Workout)

    @Query("DELETE FROM workout WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Int>)

    @Query("DELETE FROM workout WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM workout WHERE 1")
    suspend fun deleteAll()
}