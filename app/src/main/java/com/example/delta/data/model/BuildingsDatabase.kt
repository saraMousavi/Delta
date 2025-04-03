package com.example.delta.data.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.delta.data.dao.BuildingsDao
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.BuildingTypes
import com.example.delta.data.entity.BuildingUsages

@Database(
    entities = [Buildings::class, BuildingTypes::class, BuildingUsages::class],
    version = 1,
    exportSchema = false
)
abstract class BuildingsDatabase : RoomDatabase() {
    abstract fun buildingsDao(): BuildingsDao

    companion object {
        @Volatile
        private var INSTANCE: BuildingsDatabase? = null

        fun getDatabase(context: Context): BuildingsDatabase {
            synchronized(this) {
                return INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    BuildingsDatabase::class.java,
                    "buildings_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
