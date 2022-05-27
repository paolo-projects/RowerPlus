package it.paoloinfante.rowerplus.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import it.paoloinfante.rowerplus.database.models.Scheme
import it.paoloinfante.rowerplus.database.models.SchemeWithStepsAndVariables
import kotlinx.coroutines.flow.Flow

@Dao
interface SchemeDao {

    @Transaction
    @Query("SELECT * FROM scheme ORDER BY id DESC")
    fun getAllSchemes(): Flow<List<SchemeWithStepsAndVariables>>

    @Insert
    suspend fun insert(scheme: Scheme): Long
}