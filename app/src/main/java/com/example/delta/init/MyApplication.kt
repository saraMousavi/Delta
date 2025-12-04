package com.example.delta.init


import android.app.Application
import android.content.Context
import android.util.Log
import com.example.delta.R
import com.example.delta.data.dao.AuthorizationDao
import com.example.delta.data.dao.BuildingsDao
import com.example.delta.data.dao.CostDao
import com.example.delta.data.dao.EarningsDao
import com.example.delta.data.dao.RoleDao
import com.example.delta.data.dao.UsersDao
import com.example.delta.data.entity.Earnings
import com.example.delta.data.entity.User
import com.example.delta.data.entity.UserRoleBuildingUnitCrossRef
import com.example.delta.data.model.AppDatabase
import com.example.delta.enums.Period
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
    private lateinit var usersDao: UsersDao
    private lateinit var roleDao: RoleDao // Add Role DAO
    private lateinit var authorizationDao: AuthorizationDao

    override fun onCreate() {
        super.onCreate()
        appContext = this

        // Initialize DAOs
//        deleteDatabase("app_database")
        val database = AppDatabase.getDatabase(this)
        buildingDao = database.buildingsDao()
        earningsDao = database.earningsDao()
        costsDao = database.costDao()
        usersDao = database.usersDao()
        roleDao = database.roleDao() // Initialize Role DAO
        authorizationDao = database.authorizationDao() // Initialize Role DAO

        CoroutineScope(Dispatchers.IO).launch {
            AppDatabase.getDatabase(this@MyApplication).let { db ->
//                AuthDatabaseInitializer.initialize(db)
//                AuthFieldDatabaseInitializer.initializeFields(db)
                // Insert Default Values
//                insertDefaultValues()
            }
        }
    }

    private fun insertDefaultValues() {
        CoroutineScope(Dispatchers.IO).launch {
            // Insert Default Roles


            // Insert Default Guest User
            val guestBuildingManager = User(
                mobileNumber = "01111111111",
                password = "123456"
            )
            var userId = usersDao.insertUser(guestBuildingManager)
            /*@TODO define valid buildingid and unitid from server*/
            usersDao.insertUserRoleCrossRef(
                UserRoleBuildingUnitCrossRef(
                    roleId = 6L,
                    userId = userId,
                    buildingId = 0L,
                    unitId = 0L
                )
            )

            val guestPropertyOwner = User(
                mobileNumber = "0222222222",
                password = "123456",
            )
            userId = usersDao.insertUser(guestPropertyOwner)

            usersDao.insertUserRoleCrossRef(
                UserRoleBuildingUnitCrossRef(
                    roleId = 7L,
                    userId = userId,
                    buildingId = 0L,
                    unitId = 0L
                )
            )


            val guestPropertyTenant = User(
                mobileNumber = "03333333333",
                password = "123456"
            )
            userId = usersDao.insertUser(guestPropertyTenant)

            usersDao.insertUserRoleCrossRef(
                UserRoleBuildingUnitCrossRef(
                    roleId = 8L,
                    userId = userId,
                    buildingId = 0L,
                    unitId = 0L
                )
            )

            val guestIndependentUser = User(
                mobileNumber = "04444444444",
                password = "123456",
            )
            userId = usersDao.insertUser(guestIndependentUser)

            usersDao.insertUserRoleCrossRef(
                UserRoleBuildingUnitCrossRef(
                    roleId = 9L,
                    userId = userId,
                    buildingId = 0L,
                    unitId = 0L
                )
            )

            Log.d("My Application", costsDao.getCosts().toString())

            // Insert Default Earnings
            val earnings = earningsDao.getEarnings().firstOrNull()
            if (earnings == null) {
                val defaultEarnings = listOf(
                    Earnings(earningsName = getString(R.string.parking), amount = 0.0, startDate =  "", endDate = "", period = Period.MONTHLY),
                    Earnings(earningsName = getString(R.string.co_working_space), amount = 0.0, startDate =  "", endDate = "", period = Period.YEARLY),
                    Earnings(earningsName = getString(R.string.pool), amount = 0.0, startDate =  "", endDate = "", period = Period.YEARLY)
                )
                defaultEarnings.forEach {
                    earningsDao.insertEarnings(it)
                }
            }

        }
    }

}
