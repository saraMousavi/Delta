package com.example.delta.init

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class Converter {
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time // Converts Date to timestamp (Long)
    }

    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) } // Converts timestamp back to Date
    }
}