package com.example.delta.init

import android.app.Application
import android.content.Context
import android.util.Log
import com.example.delta.R
import com.example.delta.data.dao.BuildingsDao
import com.example.delta.data.dao.CostDao
import com.example.delta.data.dao.EarningsDao
import com.example.delta.data.dao.UsersDao
import com.example.delta.data.entity.BuildingTypes
import com.example.delta.data.entity.BuildingUsages
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Earnings
import com.example.delta.data.entity.User
import com.example.delta.data.model.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyApplication : Application() {

    companion object {
        private var appContext: Context? = null
    }

    private lateinit var buildingDao: BuildingsDao
    private lateinit var earningsDao: EarningsDao
    private lateinit var costsDao: CostDao
    private lateinit var usersDao: UsersDao // Assuming you have a UsersDao

    override fun onCreate() {
        super.onCreate()
        appContext = this

        // Initialize buildingDao here
        buildingDao = AppDatabase.getDatabase(this).buildingsDao()
        earningsDao = AppDatabase.getDatabase(this).earningsDao()
        costsDao = AppDatabase.getDatabase(this).costDao()
        usersDao = AppDatabase.getDatabase(this).usersDao()


        // Insert Default Values
        insertDefaultValues()
    }

    private fun insertDefaultValues() {
        CoroutineScope(Dispatchers.IO).launch {
            // Insert Default Building Types
            val buildingTypes = buildingDao.getAllBuildingTypes().firstOrNull()
            if (buildingTypes == null) {
                val defaultBuildingTypes = listOf(
                    BuildingTypes(buildingTypeName = getString(R.string.villa)),
                    BuildingTypes(buildingTypeName = getString(R.string.apartment))
                )
                defaultBuildingTypes.forEach {
                    buildingDao.insertBuildingType(it)
                }
            } else {
                Log.d("MyApplication", "Building Types already exist")
            }

            // Insert Default Building Usages
            val buildingUsages = buildingDao.getAllBuildingUsages().firstOrNull()
            if (buildingUsages == null) {
                val defaultBuildingUsages = listOf(
                    BuildingUsages(buildingUsageName = getString(R.string.residential) ),
                    BuildingUsages(buildingUsageName = getString(R.string.commercial) ),
                    BuildingUsages(buildingUsageName = getString(R.string.industrial))
                )
                defaultBuildingUsages.forEach {
                    buildingDao.insertBuildingUsage(it)
                }
            } else {
                Log.d("MyApplication", "Building Usages already exist")
            }

            // Insert Default Costs
            val costs = costsDao.getCosts().firstOrNull()
            if (costs == null) {
                val defaultCosts = listOf(
                    Costs(costName = getString(R.string.charge), buildingId = 0, period = listOf(getString(R.string.monthly)), amountUnit = listOf(getString(R.string.milion_toman)), paymentLevel = listOf(getString(R.string.unit)) , fundFlag = true, calculateMethod = listOf(getString(R.string.area)), responsible = listOf(getString(R.string.tenant))),
                    Costs(costName = getString(R.string.mortgage), buildingId = 0, period = listOf(getString(R.string.yearly)), amountUnit = listOf(getString(R.string.milion_toman)), paymentLevel = listOf(getString(R.string.unit)) , fundFlag = false, calculateMethod = listOf(getString(R.string.fixed)), responsible = listOf(getString(R.string.tenant))),
                    Costs(costName = getString(R.string.rent), buildingId = 0, period = listOf(getString(R.string.monthly)), amountUnit = listOf(getString(R.string.milion_toman)), paymentLevel = listOf(getString(R.string.unit)) , fundFlag = true, calculateMethod = listOf(getString(R.string.fixed)), responsible = listOf(getString(R.string.tenant))),
                 )
                defaultCosts.forEach {
                    costsDao.insertCost(it)
                }
            } else {
                Log.d("MyApplication", "Costs already exist")
            }

            val earnings = earningsDao.getEarnings().firstOrNull()
            if (earnings == null) {
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

            val users = usersDao.getUsers().firstOrNull()
            if (users == null) {
                val phoneNumber = "09103009458"
                val persianPhoneNumber = convertToPersianDigits(phoneNumber)

                val password = "1234"
                val persianPassword = convertToPersianDigits(password)

                val defaultUser = User(
                    mobileNumber = persianPhoneNumber,
                    password = persianPassword,
                    role = "owner"
                )
                usersDao.insertUser(defaultUser)
            } else {
                Log.d("MyApplication", "Users already exist")
            }
        }
    }

    fun convertToPersianDigits(input: String): String {
        val persianDigits = listOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val builder = StringBuilder()
        for (char in input) {
            if (char.isDigit()) {
                val digit = char.toString().toInt()
                builder.append(persianDigits[digit])
            } else {
                builder.append(char) // keep non-digit characters as is
            }
        }
        return builder.toString()
    }


}
