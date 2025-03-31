package com.example.delta.data.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.delta.data.dao.BuildingTypeDao
import com.example.delta.data.dao.BuildingUsageDao
import com.example.delta.data.dao.BuildingsDao
import com.example.delta.data.dao.CostDao
import com.example.delta.data.dao.IncomeDao
import com.example.delta.data.entity.BuildingType
import com.example.delta.data.entity.BuildingUsage
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.Cost
import com.example.delta.data.entity.Income

@Database(entities = [BuildingType::class, BuildingUsage::class,Buildings::class, Cost::class, Income::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun buildingTypeDao(): BuildingTypeDao
    abstract fun buildingUsageDao(): BuildingUsageDao
    abstract fun buildingsDao(): BuildingsDao
    abstract fun costDao(): CostDao
    abstract fun incomeDao(): IncomeDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}

