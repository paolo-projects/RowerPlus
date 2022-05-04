package it.paoloinfante.rowerplus.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import it.paoloinfante.rowerplus.database.converters.Converters
import it.paoloinfante.rowerplus.database.dao.WorkoutDao
import it.paoloinfante.rowerplus.database.dao.WorkoutStatusDao
import it.paoloinfante.rowerplus.database.models.Workout
import it.paoloinfante.rowerplus.database.models.WorkoutStatus

@Database(entities = [Workout::class, WorkoutStatus::class], version = 1)
@TypeConverters(Converters::class)
abstract class RowerPlusDatabase: RoomDatabase() {

    abstract fun workoutDao(): WorkoutDao

    abstract fun workoutStatusDao(): WorkoutStatusDao
}