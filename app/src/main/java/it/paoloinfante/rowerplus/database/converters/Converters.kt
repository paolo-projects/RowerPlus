package it.paoloinfante.rowerplus.database.converters

import androidx.room.TypeConverter
import java.time.Duration
import java.util.*

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(value: Date?): Long? {
        return value?.time
    }

    @TypeConverter
    fun fromDurationSeconds(value: Long?): Duration? {
        return if (value != null) Duration.ofSeconds(value) else null
    }

    @TypeConverter
    fun durationToDurationSeconds(value: Duration?): Long? {
        return value?.seconds
    }
}