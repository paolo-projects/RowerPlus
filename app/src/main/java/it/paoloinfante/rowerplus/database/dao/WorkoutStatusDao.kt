package it.paoloinfante.rowerplus.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import it.paoloinfante.rowerplus.database.models.WorkoutStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutStatusDao {
    @Query("SELECT * FROM workoutstatus")
    suspend fun getAll(): List<WorkoutStatus>

    @Query("SELECT * FROM workoutstatus WHERE workoutId = :workoutId")
    suspend fun getAllForWorkout(workoutId: Int): List<WorkoutStatus>

    @Insert
    suspend fun addAll(vararg workoutStatuses: WorkoutStatus)

    @Delete
    suspend fun delete(workoutStatus: WorkoutStatus)

    @Query("DELETE FROM workoutstatus WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM workoutstatus ORDER BY id DESC LIMIT 1")
    fun getLastStatus(): Flow<WorkoutStatus>

    @Query("SELECT * FROM WorkoutStatus WHERE workoutId IN (SELECT id FROM workout ORDER BY id DESC LIMIT 1) ORDER BY id DESC LIMIT 1")
    fun getLastStatusForLastWorkout(): Flow<WorkoutStatus>

    @Query("SELECT * FROM WorkoutStatus WHERE workoutId = :workoutId ORDER BY id DESC LIMIT 1")
    fun getLastStatusFor(workoutId: Int): Flow<WorkoutStatus>
}