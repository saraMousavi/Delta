package com.example.delta.init

import android.app.Application
import android.content.Context
import android.util.Log
import com.example.delta.R
import com.example.delta.data.dao.BuildingsDao
import com.example.delta.data.dao.CostDao
import com.example.delta.data.dao.EarningsDao
import com.example.delta.data.entity.BuildingTypes
import com.example.delta.data.entity.BuildingUsages
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Earnings
import com.example.delta.data.model.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MyApplication : Application() {

    companion object {
        private var appContext: Context? = null
    }

    private lateinit var buildingDao: BuildingsDao
    private lateinit var earningsDao: EarningsDao
    private lateinit var costsDao: CostDao

    override fun onCreate() {
        super.onCreate()
        appContext = this
        Log.d("MyApplication", "Application started")

        // Initialize buildingDao here
        buildingDao = AppDatabase.getDatabase(this).buildingsDao()
        earningsDao = AppDatabase.getDatabase(this).earningsDao()
        costsDao = AppDatabase.getDatabase(this).costDao()

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
                    BuildingTypes(buildingTypeName = getString(R.string.villa)),
                    BuildingTypes(buildingTypeName = getString(R.string.apartment))
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
                    BuildingUsages(buildingUsageName = getString(R.string.residential) ),
                    BuildingUsages(buildingUsageName = getString(R.string.commercial) ),
                    BuildingUsages(buildingUsageName = getString(R.string.industrial))
                )
                defaultBuildingUsages.forEach {
                    Log.d("MyApplication", "Inserting Building Usage: ${it.buildingUsageName}")
                    buildingDao.insertBuildingUsage(it)
                }
            } else {
                Log.d("MyApplication", "Building Usages already exist")
            }

            // Insert Default Costs
            Log.d("MyApplication", "Checking Costs")
            val costs = costsDao.getCosts().firstOrNull()
            if (costs == null) {
                Log.d("MyApplication", "Costs are empty, inserting defaults")
                val defaultCosts = listOf(
                    Costs(costName = getString(R.string.rent), buildingId = 0, amount = 0.0, currency = "USD"),
                    Costs(costName = getString(R.string.utilities), buildingId = 0, amount = 0.0, currency = "USD"),
                    Costs(costName = getString(R.string.maintenance), buildingId = 0, amount = 0.0, currency = "USD")
                )
                defaultCosts.forEach {
                    Log.d("MyApplication", "Inserting Cost: ${it.costName}")
                    costsDao.insertCost(it)
                }
            } else {
                Log.d("MyApplication", "Costs already exist")
            }

            // Insert Default Earnings
            Log.d("MyApplication", "Checking Earnings")
            val earnings = earningsDao.getEarnings().firstOrNull()
            if (earnings == null) {
                Log.d("MyApplication", "Earnings are empty, inserting defaults")
                val defaultEarnings = listOf(
                    Earnings(earningsName = getString(R.string.parking), buildingId = 0, amount = 0.0, currency = "USD"),
                    Earnings(earningsName = getString(R.string.co_working_space), buildingId = 0, amount = 0.0, currency = "USD"),
                    Earnings(earningsName = getString(R.string.pool), buildingId = 0, amount = 0.0, currency = "USD")
                )
                defaultEarnings.forEach {
                    Log.d("MyApplication", "Inserting Earnings: ${it.earningsName}")
                    earningsDao.insertEarnings(it)
                }
            } else {
                Log.d("MyApplication", "Earnings already exist")
            }
        }
    }

}
