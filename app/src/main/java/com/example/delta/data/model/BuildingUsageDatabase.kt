package com.example.delta.data.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.delta.data.dao.BuildingUsageDao
import com.example.delta.data.entity.BuildingUsage

@Database(entities = [BuildingUsage::class], version = 1, exportSchema = false)
abstract class BuildingUsageDatabase : RoomDatabase() {
    abstract fun buildingUsageDao(): BuildingUsageDao

    companion object {
        @Volatile
        private var INSTANCE: BuildingUsageDatabase? = null

        fun getDatabase(context: Context): BuildingUsageDatabase {
            synchronized(this) {
                return INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    BuildingUsageDatabase::class.java,
                    "building_usage_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
