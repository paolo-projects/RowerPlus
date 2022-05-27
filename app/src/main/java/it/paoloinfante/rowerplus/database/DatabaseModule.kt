package it.paoloinfante.rowerplus.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import it.paoloinfante.rowerplus.R
import it.paoloinfante.rowerplus.database.dao.*
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {
    @Provides
    fun provideWorkoutDao(rowerPlusDatabase: RowerPlusDatabase): WorkoutDao =
        rowerPlusDatabase.workoutDao()

    @Provides
    fun provideWorkoutStatusDao(rowerPlusDatabase: RowerPlusDatabase): WorkoutStatusDao =
        rowerPlusDatabase.workoutStatusDao()

    @Provides
    fun provideSchemeDao(rowerPlusDatabase: RowerPlusDatabase): SchemeDao =
        rowerPlusDatabase.schemeDao()

    @Provides
    fun provideSchemeStepDao(rowerPlusDatabase: RowerPlusDatabase): SchemeStepDao =
        rowerPlusDatabase.schemeStepDao()

    @Provides
    fun provideSchemeStepVariableDao(rowerPlusDatabase: RowerPlusDatabase): SchemeStepVariableDao =
        rowerPlusDatabase.schemeStepVariableDao()

    @Provides
    @Singleton
    fun provideRowerPlusDatabase(@ApplicationContext appContext: Context): RowerPlusDatabase {
        val dbName = appContext.getString(R.string.database_name)
        return Room.databaseBuilder(appContext, RowerPlusDatabase::class.java, dbName).build()
    }
}