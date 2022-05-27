package it.paoloinfante.rowerplus.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import it.paoloinfante.rowerplus.database.converters.Converters
import it.paoloinfante.rowerplus.database.dao.*
import it.paoloinfante.rowerplus.database.models.*

@Database(
    entities = [Workout::class, WorkoutStatus::class, Scheme::class, SchemeStep::class, SchemeStepVariable::class],
    version = 4,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4)
    ]
)
@TypeConverters(Converters::class)
abstract class RowerPlusDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun workoutStatusDao(): WorkoutStatusDao
    abstract fun schemeDao(): SchemeDao
    abstract fun schemeStepDao(): SchemeStepDao
    abstract fun schemeStepVariableDao(): SchemeStepVariableDao
}