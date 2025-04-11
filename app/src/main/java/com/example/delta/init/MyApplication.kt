package com.example.delta.init

import android.app.Application
import android.content.Context
import android.util.Log
import com.example.delta.R
import com.example.delta.data.dao.BuildingsDao
import com.example.delta.data.entity.BuildingTypes
import com.example.delta.data.entity.BuildingUsages
import com.example.delta.data.model.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyApplication : Application() {

    companion object {
        private var appContext: Context? = null

        fun getAppContext(): Context? {
            return appContext
        }
    }

    private lateinit var buildingDao: BuildingsDao

    override fun onCreate() {
        super.onCreate()
        appContext = this
        Log.d("MyApplication", "Application started")

        // Initialize buildingDao here
        buildingDao = AppDatabase.getDatabase(this).buildingsDao()

        // Insert Default Values
        insertDefaultValues()
    }

    private fun insertDefaultValues() {
        Log.d("MyApplication", "Inserting default values")
        CoroutineScope(Dispatchers.IO).launch {
            // Insert Default Building Types
            Log.d("MyApplication", "Checking Building Types")
            val buildingTypes = buildingDao.getAllBuildingTypes().firstOrNull()
            if (buildingTypes == null) {
                Log.d("MyApplication", "Building Types are empty, inserting defaults")
                val defaultBuildingTypes = listOf(
                    BuildingTypes(buildingTypeName = getString(R.string.villa) ?: "Villa"),
                    BuildingTypes(buildingTypeName = getString(R.string.apartment) ?: "Apartment")
                )
                defaultBuildingTypes.forEach {
                    Log.d("MyApplication", "Inserting Building Type: ${it.buildingTypeName}")
                    buildingDao.insertBuildingType(it)
                }
            } else {
                Log.d("MyApplication", "Building Types already exist")
            }

            // Insert Default Building Usages
            Log.d("MyApplication", "Checking Building Usages")
            val buildingUsages = buildingDao.getAllBuildingUsages().firstOrNull()
            if (buildingUsages == null) {
                Log.d("MyApplication", "Building Usages are empty, inserting defaults")
                val defaultBuildingUsages = listOf(
                    BuildingUsages(buildingUsageName = getString(R.string.residential) ?: "Residential"),
                    BuildingUsages(buildingUsageName = getString(R.string.commercial) ?: "Commercial"),
                    BuildingUsages(buildingUsageName = getString(R.string.industrial) ?: "Industrial")
                )
                defaultBuildingUsages.forEach {
                    Log.d("MyApplication", "Inserting Building Usage: ${it.buildingUsageName}")
                    buildingDao.insertBuildingUsage(it)
                }
            } else {
                Log.d("MyApplication", "Building Usages already exist")
            }
        }
    }
}
