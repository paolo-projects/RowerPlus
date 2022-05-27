package it.paoloinfante.rowerplus.database.dao

import androidx.room.Dao
import androidx.room.Insert
import it.paoloinfante.rowerplus.database.models.SchemeStepVariable

@Dao
interface SchemeStepVariableDao {

    @Insert
    suspend fun insert(schemeStepVariable: SchemeStepVariable): Long
}