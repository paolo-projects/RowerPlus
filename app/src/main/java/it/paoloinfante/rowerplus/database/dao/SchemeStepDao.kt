package it.paoloinfante.rowerplus.database.dao

import androidx.room.Dao
import androidx.room.Insert
import it.paoloinfante.rowerplus.database.models.SchemeStep

@Dao
interface SchemeStepDao {

    @Insert
    suspend fun insert(schemeStep: SchemeStep): Long
}