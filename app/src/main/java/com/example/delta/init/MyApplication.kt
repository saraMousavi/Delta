package com.example.delta.init


import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.delta.R
import com.example.delta.data.dao.AuthorizationDao
import com.example.delta.data.dao.BuildingsDao
import com.example.delta.data.dao.CostDao
import com.example.delta.data.dao.EarningsDao
import com.example.delta.data.dao.RoleDao
import com.example.delta.data.dao.UsersDao
import com.example.delta.data.entity.BuildingTypes
import com.example.delta.data.entity.BuildingUsages
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Earnings
import com.example.delta.data.entity.Role
import com.example.delta.data.entity.User
import com.example.delta.data.entity.UserRoleCrossRef
import com.example.delta.data.model.AppDatabase
import com.example.delta.enums.CalculateMethod
import com.example.delta.enums.FundFlag
import com.example.delta.enums.PaymentLevel
import com.example.delta.enums.Period
import com.example.delta.enums.Responsible
import com.example.delta.enums.Roles
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
        val database = AppDatabase.getDatabase(this)
        buildingDao = database.buildingsDao()
        earningsDao = database.earningsDao()
        costsDao = database.costDao()
        usersDao = database.usersDao()
        roleDao = database.roleDao() // Initialize Role DAO
        authorizationDao = database.authorizationDao() // Initialize Role DAO

        CoroutineScope(Dispatchers.IO).launch {
            AppDatabase.getDatabase(this@MyApplication).let { db ->
                AuthDatabaseInitializer.initialize(db)
                AuthFieldDatabaseInitializer.initializeFields(db)
                // Insert Default Values
                insertDefaultValues()
            }
        }
    }

    private fun insertDefaultValues() {
        CoroutineScope(Dispatchers.IO).launch {
            // Insert Default Roles
            val existingRoles = roleDao.getRoles().firstOrNull()
            if (existingRoles == null) {
                val defaultRoles = listOf(
                    Role(roleName = Roles.ADMIN, roleDescription = "System Administrator"),
                    Role(roleName = Roles.PROPERTY_OWNER, roleDescription = "Property Owner"),
                    Role(roleName = Roles.BUILDING_MANAGER, roleDescription = "Property Manager"),
                    Role(roleName = Roles.PROPERTY_TENANT, roleDescription = "Building Tenant"),
                    Role(roleName = Roles.INDEPENDENT_USER, roleDescription = "Independent User"),
                    Role(roleName = Roles.GUEST_BUILDING_MANAGER, roleDescription = "Guest Property Manager"),
                    Role(roleName = Roles.GUEST_PROPERTY_OWNER, roleDescription = "Guest Property Owner"),
                    Role(roleName = Roles.GUEST_PROPERTY_TENANT, roleDescription = "Guest Building Tenant"),
                    Role(roleName = Roles.GUEST_INDEPENDENT_USER, roleDescription = "Guest Independent User")
                )
                defaultRoles.forEach { role ->
                    roleDao.insertRole(role)
                }

                Log.d("MyApplication", "Default roles inserted")
            } else {
                Log.d("MyApplication", "Roles already exist")
            }

            // Insert Default Guest User
            val guestBuildingManager = User(
                mobileNumber = "01111111111",
                password = "123456",
                roleId = 6L // guest user for building manager
            )
            var userId = usersDao.insertUser(guestBuildingManager)

            usersDao.insertUserRoleCrossRef(
                UserRoleCrossRef(
                    roleId = 6L,
                    userId = userId
                )
            )

            val guestPropertyOwner = User(
                mobileNumber = "0222222222",
                password = "123456",
                roleId = 7L // guest user for owner
            )
            userId = usersDao.insertUser(guestPropertyOwner)

            usersDao.insertUserRoleCrossRef(
                UserRoleCrossRef(
                    roleId = 7L,
                    userId = userId
                )
            )


            val guestPropertyTenant = User(
                mobileNumber = "03333333333",
                password = "123456",
                roleId = 8L // guest user for building tenant
            )
            userId = usersDao.insertUser(guestPropertyTenant)

            usersDao.insertUserRoleCrossRef(
                UserRoleCrossRef(
                    roleId = 8L,
                    userId = userId
                )
            )

            val guestIndependentUser = User(
                mobileNumber = "04444444444",
                password = "123456",
                roleId = 9L // guest user for independent user
            )
            userId = usersDao.insertUser(guestIndependentUser)

            usersDao.insertUserRoleCrossRef(
                UserRoleCrossRef(
                    roleId = 9L,
                    userId = userId
                )
            )

            val allUsers = usersDao.getUsers()
            Log.d("allUsrs", allUsers.toString())

            // Insert Default Building Types
            val buildingTypes = buildingDao.getAllBuildingTypes().firstOrNull()
            if (buildingTypes == null) {
                val defaultBuildingTypes = listOf(
                    BuildingTypes(buildingTypeName = getString(R.string.villa)),
                    BuildingTypes(buildingTypeName = getString(R.string.apartment)),
                    BuildingTypes(buildingTypeName = getString(R.string.city_complex))
                )
                defaultBuildingTypes.forEach {
                    buildingDao.insertBuildingType(it)
                }
            }

            // Insert Default Building Usages
            val buildingUsages = buildingDao.getAllBuildingUsages().firstOrNull()
            if (buildingUsages == null) {
                val defaultBuildingUsages = listOf(
                    BuildingUsages(buildingUsageName = getString(R.string.residential)),
                    BuildingUsages(buildingUsageName = getString(R.string.commercial)),
                    BuildingUsages(buildingUsageName = getString(R.string.industrial))
                )
                defaultBuildingUsages.forEach {
                    buildingDao.insertBuildingUsage(it)
                }
            }

            // Insert Default Costs
            val costs = costsDao.getCosts().firstOrNull()
            if (costs == null) {
                val defaultCosts = listOf(
                    Costs(costName = getString(R.string.charge), period = Period.MONTHLY, paymentLevel = PaymentLevel.UNIT, fundFlag = FundFlag.NO_EFFECT, calculateMethod = CalculateMethod.AREA, responsible = Responsible.TENANT, tempAmount = 0.0),
                    Costs(costName = getString(R.string.mortgage), period = Period.YEARLY, paymentLevel = PaymentLevel.UNIT, fundFlag = FundFlag.NO_EFFECT, calculateMethod = CalculateMethod.FIXED, responsible = Responsible.TENANT, tempAmount = 0.0),
                    Costs(costName = getString(R.string.rent), period = Period.MONTHLY, paymentLevel = PaymentLevel.UNIT, fundFlag = FundFlag.NO_EFFECT, calculateMethod = CalculateMethod.FIXED, responsible = Responsible.TENANT, tempAmount = 0.0),
                )
                defaultCosts.forEach {
                    costsDao.insertCost(it)
                }
            }

            Log.d("My Application", costsDao.getCosts().toString())

            // Insert Default Earnings
            val earnings = earningsDao.getEarnings().firstOrNull()
            if (earnings == null) {
                val defaultEarnings = listOf(
                    Earnings(earningsName = getString(R.string.parking), amount = 0.0, startDate = "", endDate = "", period = Period.MONTHLY),
                    Earnings(earningsName = getString(R.string.co_working_space), amount = 0.0, startDate = "", endDate = "", period = Period.YEARLY),
                    Earnings(earningsName = getString(R.string.pool), amount = 0.0, startDate = "", endDate = "", period = Period.YEARLY)
                )
                defaultEarnings.forEach {
                    earningsDao.insertEarnings(it)
                }
            }

        }
    }

}
