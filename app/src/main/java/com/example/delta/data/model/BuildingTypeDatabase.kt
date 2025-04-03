package com.example.delta.data.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.delta.data.dao.BuildingTypeDao
import com.example.delta.data.entity.BuildingTypes

@Database(entities = [BuildingTypes::class], version = 1, exportSchema = false)
abstract class BuildingTypeDatabase : RoomDatabase() {
    abstract fun buildingTypeDao(): BuildingTypeDao

    companion object {
        @Volatile
        private var INSTANCE: BuildingTypeDatabase? = null

        fun getDatabase(context: Context): BuildingTypeDatabase {
            synchronized(this) {
                return INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    BuildingTypeDatabase::class.java,
                    "building_type_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
