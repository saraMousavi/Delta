package com.example.delta.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.delta.data.dao.AuthorizationDao.FieldWithPermission
import com.example.delta.data.dao.NotificationDao
import com.example.delta.data.dao.UnitsDao.UnitDangSum
import com.example.delta.data.entity.AuthorizationField
import com.example.delta.data.entity.AuthorizationObject
import com.example.delta.data.entity.BuildingOwnerCrossRef
import com.example.delta.data.entity.BuildingTypes
import com.example.delta.data.entity.BuildingUploadedFileCrossRef
import com.example.delta.data.entity.BuildingUsages
import com.example.delta.data.entity.BuildingWithTypesAndUsages
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.CityComplex
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Credits
import com.example.delta.data.entity.Debts
import com.example.delta.data.entity.Earnings
import com.example.delta.data.entity.Funds
import com.example.delta.data.entity.Notification
import com.example.delta.data.entity.Owners
import com.example.delta.data.entity.OwnersUnitsCrossRef
import com.example.delta.data.entity.PhonebookEntry
import com.example.delta.data.entity.Role
import com.example.delta.data.entity.RoleAuthorizationObjectFieldCrossRef
import com.example.delta.data.entity.TenantWithRelation
import com.example.delta.data.entity.Tenants
import com.example.delta.data.entity.TenantsUnitsCrossRef
import com.example.delta.data.entity.UnitWithDang
import com.example.delta.data.entity.Units
import com.example.delta.data.entity.UploadedFileEntity
import com.example.delta.data.entity.User
import com.example.delta.data.entity.UserRoleCrossRef
import com.example.delta.data.entity.UsersBuildingsCrossRef
import com.example.delta.data.entity.UsersNotificationCrossRef
import com.example.delta.data.model.AppDatabase
import com.example.delta.enums.CalculateMethod
import com.example.delta.enums.FundType
import com.example.delta.enums.NotificationType
import com.example.delta.enums.PaymentLevel
import com.example.delta.enums.Period
import com.example.delta.enums.Responsible
import com.example.delta.enums.UserType
import com.example.delta.enums.UserWithUnit
import com.example.delta.init.Calculation
import com.example.delta.init.Preference
import com.example.delta.volley.Building
import com.example.delta.volley.Users
import ir.hamsaa.persiandatepicker.util.PersianCalendar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import kotlin.math.min
import kotlinx.coroutines.flow.combine



class SharedViewModel(application: Application) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val context = getApplication<Application>().applicationContext
    val userId = Preference().getUserId(context)

    private val buildingDao = AppDatabase.getDatabase(application).buildingsDao()
    private val ownersDao = AppDatabase.getDatabase(application).ownersDao()
    private val tenantsDao = AppDatabase.getDatabase(application).tenantDao()
    private val userDao = AppDatabase.getDatabase(application).usersDao()
    private val roleDao = AppDatabase.getDatabase(application).roleDao()
    private val unitsDao = AppDatabase.getDatabase(application).unitsDao()
    private val costsDao = AppDatabase.getDatabase(application).costDao()
    private val debtsDao = AppDatabase.getDatabase(application).debtsDao()
    private val earningsDao = AppDatabase.getDatabase(application).earningsDao()
    private val authorizationDao = AppDatabase.getDatabase(application).authorizationDao()
    private val buildingTypesDao = AppDatabase.getDatabase(application).buildingTypeDao()
    private val buildingUsagesDao = AppDatabase.getDatabase(application).buildingUsageDao()
    private val uploadedFileDao = AppDatabase.getDatabase(application).uploadedFileDao()
    private val phonebookDao = AppDatabase.getDatabase(application).phonebookDao()
    private val notificationDao = AppDatabase.getDatabase(application).notificationDao()
    private val fundsDao = AppDatabase.getDatabase(application).fundsDao()
    private val cityComplexDao = AppDatabase.getDatabase(application).cityComplexDao()
    private val creditsDao = AppDatabase.getDatabase(application).creditsDao()

    private val _invoiceResult = MutableSharedFlow<Boolean>() // true=success, false=failure
    val invoiceResult = _invoiceResult.asSharedFlow()


    // State for Building Info Page
    var name by mutableStateOf("")
    var phone by mutableStateOf("")
    var email by mutableStateOf("")
    var postCode by mutableStateOf("")
    var street by mutableStateOf("")
    var province by mutableStateOf("")
    var state by mutableStateOf("")
    var sameArea by mutableStateOf(false)
    var numberOfUnits by mutableStateOf("")
    var unitArea by mutableStateOf("")
    var savedFilePaths = mutableStateListOf<String>()
    var isLoading by mutableStateOf(false)


    var unitsAdded by mutableStateOf(false)
    var buildingTypeId by mutableIntStateOf(0)
    var buildingUsageId by mutableIntStateOf(0)
        // State for Owners Page
        private set
    var ownersList = mutableStateListOf<Owners>()
        //    var ownersUnitsList = mutableStateListOf<OwnersUnitsCrossRef>()
        private set
    var tenantsList = mutableStateListOf<Tenants>()
        private set
    var costsList = mutableStateOf(listOf<Costs>())
    var chargeCostsList = mutableStateOf(listOf<Costs>())
    var unpaidDebtList = mutableStateOf(listOf<Debts>())
    var charge = mutableStateOf(listOf<Costs>())
    var unitsList = mutableStateListOf<Units>()
    var debtsList = mutableStateListOf<Debts>()
    var unitDebtsList = mutableStateListOf<Debts>()
    var newTenantId: Long by mutableLongStateOf(0L)
    var selectedOwnerForUnit by mutableStateOf<Owners?>(null)
    var fileList = mutableStateListOf<UploadedFileEntity>()

    // These represent the selected items from your dropdowns
    private val _selectedCredits = MutableStateFlow<Set<Long>>(emptySet())
    val selectedCredits: StateFlow<Set<Long>> = _selectedCredits
    private val _creditsForEarning = MutableStateFlow<List<Credits>>(emptyList())

    var selectedBuildingTypes by mutableStateOf<BuildingTypes?>(null)
    var selectedCityComplex by mutableStateOf<CityComplex?>(null)
    var selectedBuildingUsages by mutableStateOf<BuildingUsages?>(null)
    var selectedEarnings by mutableStateOf<Earnings?>(null)

    // Unit selection state
    var selectedUnits = mutableStateListOf<Units>()
    var selectedOwners = mutableStateListOf<Owners>()

    // Maps to store unit associations for owners and tenants
    val ownerUnitMap = mutableMapOf<Owners, List<OwnersUnitsCrossRef>>()
    val tenantUnitMap = mutableMapOf<Tenants, Units>()
    val ownerManagerMap = mutableMapOf<Owners, Boolean>()


    var automaticCharge by mutableStateOf(false)
    // State to hold current balance of operational fund
    private val _operationalFundBalance = mutableDoubleStateOf(0.0)
    // State to hold current balance of capital fund
    private val _capitalFundBalance = mutableDoubleStateOf(0.0)

    var sameCosts by mutableStateOf(true)
    var currentRoleId by mutableLongStateOf(0L)

    var fixedAmount by mutableStateOf("")
        private set // Prevent external direct modification


    val tenantRentDebtMap = mutableStateMapOf<Long, Double>()
    val tenantMortgageDebtMap = mutableStateMapOf<Long, Double>()



    // Options Lists
    val periods = Period.entries

    // Hold the flow of all notifications with read statuses
    private val _allNotifications = notificationDao.getNotificationsWithReadStatusByUser()

    // Filter to system and manager notifications as StateFlows
    val systemNotifications = _allNotifications
        .map { list -> list.filter { it.notification.type == NotificationType.SYSTEM } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val managerNotifications = _allNotifications
        .map { list -> list.filter { it.notification.type == NotificationType.MANAGER } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    init {
//        loadOwners()
//        loadTenants()
//        loadNotification()
        loadCosts()
        loadBuildingsWithTypesAndUsages()
//        loadDefaultCosts()
    }

    /**
     * Loads the current balances of operational and capital funds for the given building.
     * Updates the public states that UI observes.
     */
    fun loadFundBalances(buildingId: Long) {
        viewModelScope.launch {
            val opFund = fundsDao.getFundByType(buildingId, FundType.OPERATIONAL)
            val capFund = fundsDao.getFundByType(buildingId, FundType.CAPITAL)

            _operationalFundBalance.doubleValue = opFund?.balance ?: 0.0
            _capitalFundBalance.doubleValue = capFund?.balance ?: 0.0
        }
    }


    // Function to insert new cost name into DB
   fun insertNewCost(cost: Costs) {
        viewModelScope.launch(Dispatchers.IO) {
            costsDao.insertCost(cost)
        }
    }
    fun insertNewEarnings(earning: Earnings) {
        viewModelScope.launch(Dispatchers.IO) {
            earningsDao.insertEarnings(earning)
        }
    }


    fun getAllAuthorizationObjects(): Flow<List<AuthorizationObject>> = flow {
        val obj = authorizationDao.getAllAuthorizationObjects()
        emit(obj)
    }.flowOn(Dispatchers.IO)


    fun getCreditFromEarning(earningId: Long): Flow<List<Credits>> = flow {
        val credit = creditsDao.getCreditFromEarning(earningId)
        emit(credit)
    }.flowOn(Dispatchers.IO)


    // New function to get debts for a specific unit and month
    fun getDebtsForUnitAndMonth(unitId: Long, year: String?, month: String?): Flow<List<Debts>> = flow {
        if ((year == null || year == "null") && month == "00") {
            val debts = debtsDao.getAllDebtsForUnits(unitId)
            emit(debts)
        } else {
            val debts = debtsDao.getDebtsForUnits(
                unitId = unitId,
                yearStr = year,
                monthStr = month
            )
            emit(debts)
        }
    }.flowOn(Dispatchers.IO)

    fun getDebtsForEachCost(costId: Long): Flow<List<Debts>> = flow {
        val debts = debtsDao.getDebtsForEachCost(
            costId = costId
        )
        emit(debts)
    }.flowOn(Dispatchers.IO)


    fun getDebtsForOwner(ownerId: Long, year: String?, month: String?): Flow<List<Debts>> =
        flow {
            if ((year == null || year == "null") && month == "00") {
                val debts = debtsDao.getAllDebtsForOwner(ownerId)
                emit(debts)
            } else {
                val debts = debtsDao.getDebtsForOwner(
                    ownerId = ownerId,
                    yearStr = year,
                    monthStr = month
                )
                emit(debts)
            }
        }.flowOn(Dispatchers.IO)

    fun getPaysForOwner(ownerId: Long): Flow<List<Debts>> =
        flow {
            val debts = debtsDao.getPaysForOwner(ownerId)
            emit(debts)
        }.flowOn(Dispatchers.IO)

    suspend fun getDebtById(id: Long): Debts? = debtsDao.getDebt(id) // suspend function in DAO

    fun updateDebt(debt: Debts) {
        viewModelScope.launch(Dispatchers.IO) {
            debtsDao.updateDebt(debt)
        }
    }

    fun updateBuilding(building: Buildings) {
        viewModelScope.launch(Dispatchers.IO) {
            buildingDao.updateBuilding(building)
        }
    }


    fun getBuildingsWithUserRole(userId: Long): Flow<List<BuildingWithTypesAndUsages>> = flow {
        val units = buildingDao.getBuildingsWithUserRole(userId)
        emit(units)
    }.flowOn(Dispatchers.IO)


    fun getBuildingsForUser(userId: Long): Flow<List<Buildings>> = flow {
        val units = buildingDao.getBuildingsForUser(userId)
        emit(units)
    }.flowOn(Dispatchers.IO)



    fun getAllBuildings(): Flow<List<Buildings>> = flow {
        val buildings = buildingDao.getBuildings()
        emit(buildings)
    }.flowOn(Dispatchers.IO)

    fun getAllCityComplex(): Flow<List<CityComplex>> = flow {
        val buildings = cityComplexDao.getAllCityComplexesFlow()
        emit(buildings)
    }.flowOn(Dispatchers.IO)

    // Insert city complex
    fun insertCityComplex(cityComplex: CityComplex, onInserted: (Long) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = cityComplexDao.insertCityComplex(cityComplex)
            onInserted(id)
        }
    }

    fun getPaysForUnit(unitId: Long): Flow<List<Debts>> = flow {
        val debts = debtsDao.getPaysForUnit(unitId)
        emit(debts)
    }.flowOn(Dispatchers.IO)


    fun getAllCostsOfCharges(): Flow<List<String>> = flow {
        val costs = costsDao.getCostsOfCharges()
        emit(costs)
    }.flowOn(Dispatchers.IO)

    fun getAllOwners(): Flow<List<Owners>> = flow {
        val owners = ownersDao.getOwners()
        emit(owners)
    }.flowOn(Dispatchers.IO)


    fun getAllTenants(): Flow<List<TenantsUnitsCrossRef>> = flow {
        val tenants = tenantsDao.getAllTenantUnitRelationships()
        emit(tenants)
    }.flowOn(Dispatchers.IO)

    suspend fun getNumberOfUnitsTenantForUnit(unitId: Long): String? = tenantsDao.getNumberOfUnitsTenantForUnit(unitId) // suspend function in DAO

    fun getBuildingsForUnit(unitId: Long): Flow<Buildings> = flow {
        val owners = unitsDao.getBuildingForUnit(unitId)
        emit(owners)
    }.flowOn(Dispatchers.IO)


    fun getRawChargesCostsWithBuildingId(buildingId: Long): Flow<List<Costs>> = flow {
        val costs = costsDao.getRawChargesCostsWithBuildingId(
            buildingId
        )
        emit(costs)
    }.flowOn(Dispatchers.IO)

    fun getCostsForBuildingWithChargeFlag(buildingId: Long): Flow<List<Costs>> = flow {
        val costs = costsDao.getCostsForBuildingWithChargeFlag(
            buildingId
        )
        emit(costs)
    }.flowOn(Dispatchers.IO)

    fun getCostsForBuildingWithChargeFlagAndFiscalYear(buildingId: Long, fiscalYear: String): Flow<List<Costs>> = flow {
        val costs = costsDao.getCostsForBuildingWithChargeFlagAndFiscalYear(
            buildingId, fiscalYear
        )
        emit(costs)
    }.flowOn(Dispatchers.IO)

    fun getChargesCostsNotInBuilding(buildingId: Long): Flow<List<Costs>> = flow {
        val costs = costsDao.getChargesCostsNotInBuilding(
            buildingId
        )
        emit(costs)
    }.flowOn(Dispatchers.IO)


    fun getChargesCostsWithNullBuildingId(): Flow<List<Costs>> = flow {
        val costs = costsDao.getChargesCostsWithNullBuildingId()
        emit(costs)
    }.flowOn(Dispatchers.IO)


    fun getEarning(earningId: Long): Flow<Earnings?> = flow {
        val earnings = earningsDao.getEarning(
            earningId
        )
        emit(earnings)
    }.flowOn(Dispatchers.IO)


    fun getDebtsForBuilding(buildingId: Long): Flow<List<Debts>> = flow {
        val debts = debtsDao.getDebtsForBuilding(buildingId)
        emit(debts)
    }.flowOn(Dispatchers.IO)


    fun getPaysForBuilding(buildingId: Long): Flow<List<Debts>> = flow {
        val debts = debtsDao.getPaysForBuilding(buildingId)
        emit(debts)
    }.flowOn(Dispatchers.IO)


    suspend fun getCostById(id: Long): Costs? = costsDao.getCostById(id) // suspend function in DAO

    fun getCost(costId: Long): Flow<Costs> = flow {
        val cost = costsDao.getCostById(costId)
        emit(cost)
    }.flowOn(Dispatchers.IO)

    fun getCosts(): Flow<List<Costs>> = flow {
        val cost = costsDao.getCosts()
        emit(cost)
    }.flowOn(Dispatchers.IO)

    fun getCostByBuildingIdAndName(buildingId: Long, costName: String): Flow<Costs?> = flow {
        val cost = costsDao.getDefaultCostByBuildingIdAndName(buildingId, costName)
        emit(cost)
    }.flowOn(Dispatchers.IO)


    fun checkCostNameExists(buildingId: Long, costName: String): Flow<Boolean> = flow {
        val costs = costsDao.costNameExists(buildingId, costName)
        emit(costs)
    }.flowOn(Dispatchers.IO)

    fun earningNameExists(buildingId: Long, earningName: String): Flow<Boolean> = flow {
        val costs = earningsDao.earningNameExists(buildingId, earningName)
        emit(costs)
    }.flowOn(Dispatchers.IO)

    fun getPendingCostsByFundType(buildingId: Long, fundType: FundType): Flow<List<Costs>> = flow {
        val costs = costsDao.getPendingCostsByFundType(buildingId, fundType)
        emit(costs)
    }.flowOn(Dispatchers.IO)


    fun getInvoicedCostsByFundType(buildingId: Long, fundType: FundType): Flow<List<Costs>> = flow {
        val costs = costsDao.getInvoicedCostsByFundType(buildingId, fundType)
        emit(costs)
    }.flowOn(Dispatchers.IO)


    fun getUnitsForBuilding(buildingId: Long?): Flow<List<Units>> = flow {
        val units = unitsDao.getUnitsByBuildingId(buildingId)
        emit(units)
    }.flowOn(Dispatchers.IO)

    fun getOwnersForBuilding(buildingId: Long?): Flow<List<Owners>> = flow {
        val owners = ownersDao.getOwnersForBuilding(buildingId)
        emit(owners)
    }.flowOn(Dispatchers.IO)

    fun getOwner(ownerId: Long): Flow<Owners?> = flow {
        val owners = ownersDao.getOwnerById(ownerId)
        emit(owners)
    }.flowOn(Dispatchers.IO)


    fun getUnit(unitId: Long): Flow<Units> = flow {
        val unit = tenantsDao.getUnit(unitId)
        emit(unit)
    }.flowOn(Dispatchers.IO)


    fun getTenant(tenantId: Long): Flow<Tenants> = flow {
        val tenant = tenantsDao.getTenant(tenantId)
        emit(tenant)
    }.flowOn(Dispatchers.IO)


    fun getBuilding(buildingId: Long): Flow<Buildings> = flow {
        val building = buildingDao.getBuilding(buildingId)
        emit(building)
    }.flowOn(Dispatchers.IO)

    fun getOwnerUnitsCrossRefs(ownerId: Long): Flow<List<OwnersUnitsCrossRef>> = flow {
        val owners = ownersDao.getOwnersWithUnits(ownerId)
        emit(owners)
    }.flowOn(Dispatchers.IO)

    fun getAllOwnerUnitsCrossRefs(): Flow<List<OwnersUnitsCrossRef>> = flow {
        val owners = ownersDao.getOwnersUnitsCrossRef()
        emit(owners)
    }.flowOn(Dispatchers.IO)


    fun getDangSumsForAllUnits(): Flow<List<UnitDangSum>> = flow {
        val owners = unitsDao.getDangSumsForAllUnits()
        emit(owners)
    }.flowOn(Dispatchers.IO)


    fun getNotificationsWithReadStatus(userId: Long = this.userId): Flow<List<NotificationDao.NotificationWithRead>> {
        return notificationDao.getNotificationsWithReadStatusByUser()
    }


    fun getUsersNotificationsByNotification(notificationId: Long, userId: Long): Flow<UsersNotificationCrossRef?> = flow {
        val notification = notificationDao.getUsersNotificationsByNotification(notificationId)//, userId
        emit(notification)
    }.flowOn(Dispatchers.IO)

    fun deleteUserNotificationCrossRef(userId: Long, notificationId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val crossRef = notificationDao.getUsersNotificationsByNotification(notificationId)//, userId
            if (crossRef != null) {
                notificationDao.deleteUserNotificationCrossRef(crossRef)
            }
        }
    }



    fun getUsersNotificationsById(notificationId: Long): Flow<UsersNotificationCrossRef> = flow {
        val notification = notificationDao.getUsersNotificationsById(notificationId)
        emit(notification)
    }.flowOn(Dispatchers.IO)

    fun updateUserNotificationCrossRef(notification: UsersNotificationCrossRef){
        viewModelScope.launch(Dispatchers.IO) {
            notificationDao.updateUserNotificationCrossRef(notification)
        }
    }
    fun insertNotification(
        notification: Notification,
        targetUserIds: List<Long> // null for all users
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            // Insert notification and get its id
            val notificationId = notificationDao.insertNotification(notification)

            // Insert a UsersNotificationCrossRef for each target user
            targetUserIds.forEach { userId ->
                notificationDao.insertUsersNotificationCrossRef(UsersNotificationCrossRef(userId, notificationId, isRead = false))
            }
        }
    }



    fun getOperationalOrCapitalFundBalance(buildingId: Long, fundType: FundType): Flow<Double?> =
        flow {
            val balance = fundsDao.getOperationalOrCapitalFundBalance(buildingId, fundType)
            emit(balance)
        }.flowOn(Dispatchers.IO)


    fun getUsers(): Flow<List<User>> = flow {
        val user = userDao.getUsers()
        emit(user)
    }.flowOn(Dispatchers.IO)


    fun getUserByMobile(mobile: String): Flow<User?> = flow {
        emit(userDao.getUserByMobile(mobile))
    }.flowOn(Dispatchers.IO)


    fun getUserById(userId: Long): Flow<User?> = flow {
        emit(userDao.getUserById(userId))
    }.flowOn(Dispatchers.IO)



    fun getRoleByUserId(userId: Long): Flow<Role> = flow {
        val user = userDao.getRoleByUserId(userId)
        emit(user)
    }.flowOn(Dispatchers.IO)

    fun getRoles(): Flow<List<Role>> = flow {
        val role = roleDao.getRoles()
        emit(role)
    }.flowOn(Dispatchers.IO)


    fun getUserWithRoleByMobile(mobileNumber: String): Flow<Role?> = flow {
        val role = roleDao.getRoleNameByMobileNumber(mobileNumber = mobileNumber)
        emit(role)
    }.flowOn(Dispatchers.IO)

//    fun getTenantsForBuilding(buildingId: Long): Flow<List<Tenants>> = flow {
//        val tenants = tenantsDao.getTenantsForBuilding(buildingId)
//        emit(tenants)
//    }.flowOn(Dispatchers.IO)

    fun getTenantsForBuilding(buildingId: Long): Flow<List<Tenants>> =
        tenantsDao.getTenantsForBuilding(buildingId)

    fun getActiveUnits(buildingId: Long): Flow<List<Units>> = flow {
        val units = unitsDao.getActiveUnits(buildingId)
        emit(units)
    }.flowOn(Dispatchers.IO)


    fun getFixedEarnings(): Flow<List<Earnings>> = flow {
        val earnings = earningsDao.getAllMenuEarnings()
        emit(earnings)
    }.flowOn(Dispatchers.IO)


    fun getActiveTenantsWithRelationForUnit(unitId: Long): Flow<TenantWithRelation?> = flow {
        emit(tenantsDao.getActiveTenantsWithRelationForUnit(unitId))
    }.flowOn(Dispatchers.IO)


    fun getAllUnits(): Flow<List<Units>> = flow {
        val units = tenantsDao.getAllUnits()
        emit(units)
    }.flowOn(Dispatchers.IO)



    fun getUnitsForOwners(ownerId: Long): Flow<List<UnitWithDang>> = flow {
        val owners = ownersDao.getUnitsWithDangForOwner(ownerId)
        emit(owners)
    }.flowOn(Dispatchers.IO)

    fun getOwnersForUnit(unitId: Long): Flow<List<Owners>> = flow {
        val owners = ownersDao.getOwnersForUnit(unitId)
        emit(owners)
    }.flowOn(Dispatchers.IO)


    fun isOwnerManager(ownerId: Long, buildingId: Long): Flow<Boolean> = flow {
        val crossRef = ownersDao.getBuildingOwnerCrossRef(ownerId, buildingId)
        if (crossRef.isManager) {
            emit(true)
        } else {
            emit(false)
        }
    }


    fun loadBuildingsWithTypesAndUsages() {
        viewModelScope.launch(Dispatchers.IO) {
            val buildings = buildingDao.getBuildings()
            val buildingTypes = mutableMapOf<Long?, String>()
            val buildingUsages = mutableMapOf<Long?, String>()

            // Fetch building types and usages
            // Fetch building types and usages
            buildings.forEach { building ->

                // Handle possible null buildingTypeId
                val typeId = building.buildingTypeId
                if (typeId != null && !buildingTypes.containsKey(typeId)) {
                    // DAO method should return String? to avoid crash if not found
                    val typeName: String? = buildingTypesDao.getBuildingTypeName(typeId)
                    buildingTypes[typeId] = typeName ?: "Unknown Type"
                }

                // Similarly for buildingUsageId
                val usageId = building.buildingUsageId
                if (usageId != null && !buildingUsages.containsKey(usageId)) {
                    val usageName: String? = buildingUsagesDao.getBuildingUsageName(usageId)
                    buildingUsages[usageId] = usageName ?: "Unknown Usage"
                }
            }


            // Map building types and usages to buildings
            val buildingsWithTypesAndUsages = buildings.map { building ->
                BuildingWithTypesAndUsages(
                    building = building,
                    buildingTypeName = buildingTypes[building.buildingTypeId] ?: "",
                    buildingUsageName = buildingUsages[building.buildingUsageId] ?: "",
                    roleName = ""
                )
            }

            withContext(Dispatchers.Main) {
                // Update your UI with the new data
                buildingsWithTypesAndUsagesList.value = buildingsWithTypesAndUsages
            }
        }
    }


    private val buildingsWithTypesAndUsagesList =
        mutableStateOf(listOf<BuildingWithTypesAndUsages>())

    fun insertRoleAuthorizationFieldCrossRef(roleId: Long, objectId: Long, fields: List<AuthorizationField>, permissionLevel: Int) {
        viewModelScope.launch {
            fields.forEach { field ->
                authorizationDao.insertRoleAuthorizationFieldCrossRef(
                    RoleAuthorizationObjectFieldCrossRef(
                        roleId = roleId,
                        objectId = objectId,
                        fieldId = field.fieldId,
                        permissionLevel = permissionLevel
                    )
                )
            }
        }
    }


    fun getAuthorizationDetailsForUser(userId: Long): Flow<List<FieldWithPermission>> =
        authorizationDao.getFieldsWithPermissionsForUser(userId)

    fun deleteFieldAuthorization(crossRef: RoleAuthorizationObjectFieldCrossRef) {
        viewModelScope.launch {
            authorizationDao.deleteRoleAuthorizationFieldCrossRef(crossRef)
        }
    }

    fun deleteObjectAuthorization(roleId: Long, objectId: Long) {
        viewModelScope.launch {
            authorizationDao.deleteRoleAuthorizationObjectCrossRefs(roleId, objectId)
        }
    }



    fun insertUser(context: Context, user: User, onSuccess: (Long) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val userJson = JSONObject().apply {
                put("mobileNumber", user.mobileNumber)
                put("passwordHash", user.password)
                put("roleId", user.roleId)
            }
            val userId = userDao.insertUser(user)
            userJson.put("userId", userId)
            userDao.insertUserRoleCrossRef(UserRoleCrossRef(roleId = user.roleId, userId = userId))
            Users().insertUser(context, userJson)
            onSuccess(userId)
        }

    }

    fun insertDebtForCharge(
        buildingId: Long,
        fiscalYear: String,
        chargeUnitMap: Map<Units, Double>?,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        if (chargeUnitMap.isNullOrEmpty()) return

        val fyInt = fiscalYear.toIntOrNull() ?: return

        try {
            viewModelScope.launch(Dispatchers.IO) {
                // --- insert or update cost ---
                val costsToInsertOrUpdate = costsDao.getRawChargesCostsWithBuildingId(buildingId)
                val fiscalYearDueDate = "$fiscalYear/01/01"

                costsToInsertOrUpdate.forEach { cost ->
                    val existingCost = costsDao.getCostsForBuildingWithChargeFlagAndFiscalYearAndCostName(buildingId,
                        fiscalYear,cost.costName)
                    val costWithNewValues = cost.copy(
                        buildingId = buildingId,
                        dueDate = fiscalYearDueDate
                    )
                    if (existingCost != null) {
                        costsDao.updateCost(costWithNewValues.copy(costId = existingCost.costId, calculateMethod = existingCost.calculateMethod))
                    } else {
                        costsDao.insertCost(costWithNewValues.copy(costId = 0))
                    }
                }
                // ----------------------------------------
                chargeUnitMap.forEach { (unit, amount) ->
                    if (amount <= 0.0) return@forEach

                    val dueDateObj = parsePersianDate("$fiscalYear/01/01")
                    val formattedDueDate = dueDateObj?.let {
                        String.format("%04d/%02d/%02d", it.persianYear, it.persianMonth, it.persianDay)
                    } ?: ""

                    val tenantUnitRelations = tenantsDao.getTenantsWithRelationForUnit(unit.unitId)

                    fun getYearFromDateStr(dateStr: String?): Int? {
                        if (dateStr.isNullOrEmpty()) return null
                        val pc = parsePersianDate(dateStr) ?: return null
                        return pc.persianYear
                    }

                    val activeTenantsForFiscalYear = tenantUnitRelations.filter { ref ->
                        val startYear = getYearFromDateStr(ref.crossRef.startDate) ?: Int.MIN_VALUE
                        val endYear = getYearFromDateStr(ref.crossRef.endDate) ?: Int.MAX_VALUE
                        fyInt in startYear..endYear
                    }

                    val responsible = if (activeTenantsForFiscalYear.isNotEmpty())
                        Responsible.TENANT else Responsible.OWNER

                    val charge = costsDao.getDefaultCostByBuildingIdAndName(buildingId = buildingId)
                        ?: run {
                            Log.e("insertDebtForCharge", "No cost found for buildingId $buildingId")
                            return@forEach
                        }

                    val newCharge = charge.copy(responsible = responsible)
                    costsDao.updateCost(newCharge)

                    for (month in 1..12) {
                        val monthDueDate = String.format("%s/%02d/%02d", fiscalYear, month, 1)

                        if (responsible == Responsible.OWNER) {
                            val ownersList = ownersDao.getOwnersWithUnitId(unit.unitId)
                            if (ownersList.isEmpty()) return@forEach

                            val totalDang = ownersList.sumOf { it.dang }.takeIf { it > 0 } ?: 6.0

                            for (owner in ownersList) {
                                val ownerShare = (amount / totalDang) * owner.dang

                                // Check if debt already exists
                                val existingDebt = debtsDao.getDebtByKeys(
                                    buildingId = buildingId,
                                    costId = charge.costId,
                                    unitId = unit.unitId,
                                    dueDate = monthDueDate,
                                    ownerId = owner.ownerId
                                )
                                if (existingDebt != null) {
                                    // Update existing debt
                                    val updatedDebt = existingDebt.copy(
                                        amount = ownerShare,
                                        paymentFlag = false,
                                        description = "شارژ"
                                    )
                                    debtsDao.updateDebt(updatedDebt)
                                } else {
                                    // Insert new debt
                                    val debt = Debts(
                                        unitId = unit.unitId,
                                        costId = charge.costId,
                                        buildingId = buildingId,
                                        ownerId = owner.ownerId,
                                        description = "شارژ",
                                        dueDate = monthDueDate,
                                        amount = ownerShare,
                                        paymentFlag = false
                                    )
                                    debtsDao.insertDebt(debt)
                                }
                            }
                        } else {
                            // Tenant responsible; ownerId=null
                            val existingDebt = debtsDao.getDebtByKeys(
                                buildingId = buildingId,
                                costId = charge.costId,
                                unitId = unit.unitId,
                                dueDate = monthDueDate,
                                ownerId = null
                            )

                            if (existingDebt != null) {
                                val updatedDebt = existingDebt.copy(
                                    amount = amount,
                                    paymentFlag = false,
                                    description = "شارژ"
                                )
                                debtsDao.updateDebt(updatedDebt)
                            } else {
                                val debt = Debts(
                                    unitId = unit.unitId,
                                    costId = charge.costId,
                                    buildingId = buildingId,
                                    description = "شارژ",
                                    dueDate = monthDueDate,
                                    amount = amount,
                                    paymentFlag = false
                                )
                                debtsDao.insertDebt(debt)
                            }
                        }
                    }
                }
                withContext(Dispatchers.Main) { onSuccess() }
            }
        } catch (e: Exception) {
            Log.e("insertDebtForCharge", e.toString())
            onError()
        }
    }

    fun insertDebtPerNewCost(
        buildingId: Long,
        amount: String,
        name: String,
        period: Period,
        dueDate: String,
        paymentLevel: PaymentLevel,
        calculateMethod: CalculateMethod,
        calculatedUnitMethod: CalculateMethod,
        fundType: FundType,
        responsible: Responsible,
        selectedUnitIds: List<Long> = emptyList(),
        selectedOwnerIds: List<Long> = emptyList()
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val parsedAmount = amount.persianToEnglishDigits().toDoubleOrNull() ?: 0.0

            // Try find existing cost - optional update logic commented out
            val existingCost = costsDao.getCostByBuildingIdAndName(buildingId, name)
            val cost: Costs
            var newResponsible = responsible
            var newPaymentLevel = paymentLevel
            var costId: Long = 0

            Log.d("existingCost", existingCost.toString())

            // Special case: if responsible is TENANT and fundType is OPERATIONAL, check for active tenant per unit
            if (fundType == FundType.OPERATIONAL && newResponsible == Responsible.TENANT && selectedUnitIds.isNotEmpty()) {
                var tenantFoundForAllUnits = true

                // Check each selected unit if it has an active tenant
                selectedUnitIds.forEach { unitId ->
                    val activeTenant = tenantsDao.getActiveTenantForUnit(unitId)
                    if (activeTenant == null) {
                        tenantFoundForAllUnits = false
                        return@forEach
                    }
                }

                if (!tenantFoundForAllUnits) {
                    Log.d("insertDebtPerNewCost", "No active tenant for all units, switching responsible to OWNER")
                    newResponsible = Responsible.OWNER
                }
            }

            // Insert the cost row with updated responsible
            cost = Costs(
                buildingId = buildingId,
                costName = name,
                tempAmount = parsedAmount,
                period = period,
                calculateMethod = if (newResponsible == Responsible.TENANT) calculatedUnitMethod else calculateMethod,
                paymentLevel = newPaymentLevel,
                responsible = newResponsible,
                fundType = fundType,
                dueDate = dueDate
            )
            costId = costsDao.insertCost(cost)

            // Insert debts according to responsible and units/owners selected
            when {
                newResponsible == Responsible.TENANT && selectedUnitIds.isNotEmpty() -> {
                    val units = unitsDao.getUnitsByIds(selectedUnitIds)
                    for (unit in units) {
                        val activeTenant = tenantsDao.getActiveTenantForUnit(unit.unitId)

                        if (activeTenant != null) {
                            // Unit is active, insert debt for unit

                            // Calculate amount for this unit by calculation method
                            val getTenantCountForUnit: (Long) -> Int = { unitId ->
                                tenantsDao.getActiveTenantForUnit(unitId)?.numberOfTenants?.toIntOrNull() ?: 0 // Implement this DAO method if needed
                            }
                            val amountForUnit = Calculation().calculateAmountByMethod(
                                costAmount = parsedAmount,
                                unit = unit,
                                allUnits = unitsDao.getUnits(buildingId),
                                getTenantCountForUnit = getTenantCountForUnit,
                                calculationMethod = calculatedUnitMethod  // use your method for per-unit calculation
                            )
                            val debt = Debts(
                                unitId = unit.unitId,
                                costId = costId,
                                buildingId = buildingId,
                                description = name,
                                dueDate = dueDate,
                                amount = amountForUnit,
                                paymentFlag = false
                            )
                            Log.d("new debt (unit)", debt.toString())
                            debtsDao.insertDebt(debt)
                        } else {
// Unit inactive, find owners of this unit and insert debt for owners

                            val ownersForUnit = ownersDao.getOwnersForUnits(listOf(unit.unitId))
                            val ownersUnitsCrossRefs = ownersDao.getOwnersWithUnitsList(ownersForUnit.map { it.ownerId })
                            val areaByOwner = Calculation().calculateAreaByOwners(
                                owners = ownersForUnit,
                                units = listOf(unit),
                                ownersUnitsCrossRefs = ownersUnitsCrossRefs
                            )
                            val ownerPayments = Calculation().calculateOwnerPaymentsPerCost(
                                cost = cost,
                                totalAmount = parsedAmount, // You may want to split amount here by units or pass full amount for each owner? Adjust business rule.
                                areaByOwner = areaByOwner,
                                ownersUnitsCrossRefs = ownersUnitsCrossRefs
                            )

                            ownerPayments.forEach { (ownerId, amount) ->
                                val debt = Debts(
                                    unitId = null,
                                    ownerId = ownerId,
                                    costId = costId,
                                    buildingId = buildingId,
                                    description = name,
                                    dueDate = dueDate,
                                    amount = amount,
                                    paymentFlag = false
                                )
                                Log.d("new debt (owner for inactive unit)", debt.toString())
                                debtsDao.insertDebt(debt)
                            }
                        }
                    }

                }

                newResponsible == Responsible.OWNER && selectedOwnerIds.isNotEmpty() -> {
                    val owners = ownersDao.getOwnersForBuilding(buildingId)
                    val units = unitsDao.getUnits(buildingId)
                    val ownersUnitsCrossRefs = ownersDao.getOwnersWithUnitsList(selectedOwnerIds)
                    val areaByOwner =
                        Calculation().calculateAreaByOwners(owners, units, ownersUnitsCrossRefs)

                    val ownerPayments = Calculation().calculateOwnerPaymentsPerCost(
                        cost = cost,
                        totalAmount = cost.tempAmount,
                        areaByOwner = areaByOwner,
                        ownersUnitsCrossRefs = ownersUnitsCrossRefs
                    )
                    ownerPayments.forEach { (ownerId, amount) ->
                        val debt = Debts(
                            unitId = null,
                            ownerId = ownerId,
                            costId = costId,
                            buildingId = buildingId,
                            description = cost.costName,
                            dueDate = dueDate,
                            amount = amount,
                            paymentFlag = false
                        )
                        Log.d("owner debt", debt.toString())
                        debtsDao.insertDebt(debt)
                    }
                }

                newResponsible == Responsible.ALL -> {
                    val newDueDate = if (period == Period.NONE) dueDate else getNextMonthSameDaySafe().persianShortDate

                    val debt = Debts(
                        unitId = null,
                        ownerId = null,
                        costId = costId,
                        buildingId = buildingId,
                        description = cost.costName,
                        dueDate = newDueDate,
                        amount = parsedAmount,
                        paymentFlag = true
                    )
                    debtsDao.insertDebt(debt)
                }
            }
        }
    }


    fun saveOwnerWithUnits(
        owner: Owners,
        ownerUnits: List<OwnersUnitsCrossRef>,
        isManager: Boolean,
        isNotForm: Boolean,
        buildingId: Long
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Insert owner and get the new ownerId
                val newOwnerId = ownersDao.insertOwners(owner)
                val updatedOwner = owner.copy(ownerId = newOwnerId)
                ownerManagerMap[updatedOwner] = isManager


                ownerUnits.forEach { oUnit ->
                    // Make sure dang is clamped between 0.0 and 6.0
//                    val clampedDang = oUnit.dang.coerceIn(0.0, 6.0)
                    if (oUnit.dang != 0.0) {
                        if (isNotForm) {
                            ownersDao.insertBuildingOwner(
                                BuildingOwnerCrossRef(
                                    ownerId = newOwnerId,
                                    buildingId = buildingId,
                                    isManager = isManager
                                )
                            )
                        }
                        ownersDao.insertOwnerUnitCrossRef(
                            OwnersUnitsCrossRef(
                                ownerId = newOwnerId,
                                unitId = oUnit.unitId,
                                dang = oUnit.dang
                            )
                        )
                    }
                }

                ownersList.add(updatedOwner)
                addOwnerUnits(updatedOwner, ownerUnits)
            } catch (e: Exception) {
                Log.e("SaveError", "Failed to save owner with units: ${e.message}")
            }
        }
    }


    fun saveTenantWithUnit(tenant: Tenants, unit: Units) {
        viewModelScope.launch(Dispatchers.IO) {
            try { // Ensure in IO thread
//                newTenantId = tenantsDao.insertTenants(tenant)
                newTenantId = tenantsDao.insertTenants(tenant)
                val updatedTenant = tenant.copy(tenantId = newTenantId)
                tenantUnitMap[updatedTenant] = unit // Use tenantId as the key
                withContext(Dispatchers.Main) {
                    tenantsList.add(updatedTenant)
                }
                val tenantCrossRef = TenantsUnitsCrossRef(
                    newTenantId,
                    unit.unitId,
                    tenant.startDate,
                    tenant.endDate,
                    tenant.status
                )
                tenantsDao.insertTenantUnitCrossRef(tenantCrossRef)

                // Retrieve active relationship immediately
                tenantsDao.getActiveTenantUnitRelationships(unit.unitId, "فعال")

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("Unknown error", e.message.toString())
                }
            }

        }
    }

    fun insertBuildingToServer(
        context: Context,
        building: Buildings,
        tenantsUnitsCrossRef: List<TenantsUnitsCrossRef>,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val buildingHelper = Building()

        CoroutineScope(Dispatchers.IO).launch {
            // Convert all lists to JSONArray using the helper's toJson functions
            val unitsJsonArray =
                buildingHelper.listToJsonArray(unitsList, buildingHelper::unitToJson)
            val ownersJsonArray =
                buildingHelper.listToJsonArray(ownersList, buildingHelper::ownerToJson)
            val tenantsJsonArray =
                buildingHelper.listToJsonArray(tenantsList, buildingHelper::tenantToJson)
            val costsJsonArray =
                buildingHelper.listToJsonArray(costsList.value, buildingHelper::costToJson)
            val ownerUnitsJsonArray =
                buildingHelper.buildOwnerUnitsJsonArray(ownersList, ownerUnitMap, ownersDao)
            val tenantUnitsJsonArray =
                buildingHelper.tenantUnitListToJsonArray(tenantsUnitsCrossRef)

            // Convert building object to JSONObject
            val buildingJson = buildingHelper.buildingToJson(
                building,
                selectedBuildingTypes,
                selectedBuildingUsages
            )

            // Call your Volley helper to insert the building and related data
            buildingHelper.insertBuilding(
                context,
                buildingJson,
                unitsJsonArray,
                ownersJsonArray,
                tenantsJsonArray,
                costsJsonArray,
                ownerUnitsJsonArray,
                tenantUnitsJsonArray,
                onSuccess = { response ->
                    onSuccess(response)
                },
                onError = { error ->
                    Log.e("InsertBuildingServer", "Error: ${error.message}")
                    onError(error)
                }
            )
        }
    }

    @SuppressLint("DefaultLocale")
    fun saveBuildingWithUnitsAndOwnersAndTenants(
        onSuccess: (Buildings) -> Unit,
        onError: (String) -> Unit,
        context: Context
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.IO) {
                    // Insert Building
                    val userId = Preference().getUserId(context = context)
                    val role = userDao.getRoleByUserId(userId)
                    var roleName: String
                    if (role == null) {
                        roleName = "nothing"
                    } else {
                        roleName = role.roleName.getDisplayName(context)
                    }
                    val building = Buildings(
                        name = name,
                        phone = phone,
                        email = email,
                        postCode = postCode,
                        buildingTypeId = selectedBuildingTypes?.buildingTypeId ?: 0,
                        buildingUsageId = selectedBuildingUsages?.buildingUsageId ?: 0,
                        street = street,
                        province = province,
                        state = state,
                        fund = 0.0,
                        userId = userId,
                        complexId = selectedCityComplex?.complexId
                    )

                    val buildingId = buildingDao.insertBuilding(building)
                    userDao.insertUserBuildingCrossRef(
                        UsersBuildingsCrossRef(
                            userId = userId,
                            buildingId = buildingId,
                            roleName = roleName
                        )
                    )

                    fileList.forEach { file ->
                        var fileID = uploadedFileDao.insertUploadedFile(file)
                        buildingDao.insertCrossRef(
                            BuildingUploadedFileCrossRef(
                                buildingId = buildingId,
                                fileId = fileID
                            )
                        )
                    }

                    // Prepare units
                    unitsList.forEachIndexed { index, unit ->
                        val unitWithBuildingId = unit.copy(buildingId = buildingId)
                        val insertedUnit = unitsDao.insertUnit(unitWithBuildingId)
                    }

                    // Filter costs with tempAmount == 1.0 (selected ones)
                    val selectedCostsToInsert = costsList.value.filter { it.tempAmount == 1.0 }
                        .map { cost ->
                            // Reset tempAmount to 0.0 before insert, assign new buildingId
                            cost.copy(
                                costId = 0,
                                tempAmount = 0.0,
                                buildingId = buildingId
                            )
                        }
                    selectedCostsToInsert.forEach { cost ->
                        // Insert all selected costs into the DB
                        costsDao.insertCost(cost)
                    }
                    //insert charge
                    val charge = costsDao.getCost(1)
                    val newCharge = charge.copy(
                        costId = 0,
                        tempAmount = 0.0,
                        buildingId = buildingId,
                    )
                    costsDao.insertCost(newCharge)
                    // Save Owners
                    ownersList.forEach { owner ->
                        val ownerId = ownersDao.insertOwners(owner)

                        val ownerUnits = ownerUnitMap[owner] ?: emptyList()
                        val isManager: Boolean = ownerManagerMap[owner] ?: false
                        ownersDao.insertBuildingOwner(
                            BuildingOwnerCrossRef(
                                ownerId = ownerId,
                                buildingId = buildingId,
                                isManager = isManager
                            )
                        )
                        if (isManager) {
                            val userOwnerID = userDao.insertUser(
                                User(
                                    mobileNumber = owner.mobileNumber,
                                    password = "123456",
                                    roleId = 3L
                                )
                            )
                            userDao.insertUserBuildingCrossRef(
                                UsersBuildingsCrossRef(
                                    userId = userOwnerID,
                                    buildingId = buildingId,
                                    roleName = "Manager"
                                )
                            )
                            userDao.insertUserRoleCrossRef(
                                UserRoleCrossRef(
                                    roleId = 3L,
                                    userId = userOwnerID
                                )
                            )
                        } else {
                            val userOwnerID = userDao.insertUser(
                                User(
                                    mobileNumber = owner.mobileNumber,
                                    password = "123456",
                                    roleId = 2L
                                )
                            )
                            userDao.insertUserBuildingCrossRef(
                                UsersBuildingsCrossRef(
                                    userId = userOwnerID,
                                    buildingId = buildingId,
                                    roleName = "Owner"
                                )
                            )
                            userDao.insertUserRoleCrossRef(
                                UserRoleCrossRef(
                                    roleId = 2L,
                                    userId = userOwnerID
                                )
                            )
                        }
                        ownerUnits.forEach { unit ->
                            // Get the dang value for this owner-unit pair, default to 0.0 if not present
                            if(unit.dang != 0.0) {
                                val cross = OwnersUnitsCrossRef(
                                    ownerId = ownerId,
                                    unitId = unit.unitId,
                                    dang = unit.dang
                                )
                                ownersDao.insertOwnerUnitCrossRef(cross)
                            }
                        }
                    }


                    // Save Tenant
                    tenantsList.forEachIndexed { index, tenant ->
                        val tenantId = tenantsDao.insertTenants(tenant)
                        val tenantUnit: Units = tenantUnitMap[tenant] ?: return@forEachIndexed
                        tenantsList[index] = tenant.copy(tenantId = tenantId)
                        tenantsDao.insertTenantUnitCrossRef(
                            TenantsUnitsCrossRef(
                                tenantId,
                                tenantUnit.unitId,
                                tenant.startDate,
                                tenant.endDate,
                                tenant.status
                            )
                        )
                        val userTenantID = userDao.insertUser(
                            User(
                                mobileNumber = tenant.mobileNumber,
                                password = "123456",
                                roleId = 4L
                            )
                        )
                        userDao.insertUserBuildingCrossRef(
                            UsersBuildingsCrossRef(
                                userId = userTenantID,
                                buildingId = buildingId,
                                roleName = "Tenant"
                            )
                        )
                        userDao.insertUserRoleCrossRef(
                            UserRoleCrossRef(
                                roleId = 4L,
                                userId = userTenantID
                            )
                        )

                    }
                }
// Insert Building
                val userId = Preference().getUserId(context = context)
                val building = Buildings(
                    name = name,
                    phone = phone,
                    email = email,
                    postCode = postCode,
                    buildingTypeId = selectedBuildingTypes?.buildingTypeId ?: 0,
                    buildingUsageId = selectedBuildingUsages?.buildingUsageId ?: 0,
                    street = street,
                    province = province,
                    state = state,
                    fund = 0.0,
                    userId = userId,
                    complexId = selectedCityComplex?.complexId
                )
                onSuccess(building)
            } catch (e: Exception) {
                onError("Failed to save building: ${e.message}")
            }
        }
    }


    fun saveBuildingsList(
        buildings: List<Buildings>,
        units: List<Units>,
        owners: List<Owners>,
        tenants: List<Tenants>,
        onComplete: (successCount: Int, errorMessages: List<String>) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val errors = mutableListOf<String>()
            var successCount = 0
            try {
                for ((index, building) in buildings.withIndex()) {

                    val buildingId = buildingDao.insertBuilding(building)
                    val role = userDao.getRoleByUserId(building.userId)
                    val roleName = role.roleName
                    userDao.insertUserBuildingCrossRef(
                        UsersBuildingsCrossRef(
                            userId = building.userId,
                            buildingId = buildingId,
                            roleName = "مدیر ساختمان"
                        )
                    )
                    successCount++
                }
                units.forEach { unit ->
                    val tmpBuilding = buildingDao.getBuilding(unit.excelBuildingName ?: "-1")
                    val allBuilding = buildingDao.getAllBuildingsList()
                    unit.buildingId = tmpBuilding.buildingId
                    unitsDao.insertUnit(unit)
                }

                owners.forEach { owner ->
                    //@todo check if the owner inserted once not to be inserted again
                    val ownerId = ownersDao.insertOwners(owner)
                    val tmpBuilding = buildingDao.getBuilding(owner.excelBuildingName ?: "-1")
                    val ownerUnit = unitsDao.getUnitByUnitNumber(
                        owner.excelUnitsNumber?.toLong() ?: 0L,
                        owner.excelBuildingName ?: ""
                    )
                    val isManager: Boolean = owner.excelIsManager ?: false
                    ownersDao.insertBuildingOwner(
                        BuildingOwnerCrossRef(
                            ownerId = ownerId,
                            buildingId = tmpBuilding.buildingId,
                            isManager = isManager
                        )
                    )
                    if (isManager) {
                        val userOwnerID = userDao.insertUser(
                            User(
                                mobileNumber = owner.mobileNumber,
                                password = "123456",
                                roleId = 3L
                            )
                        )
                        userDao.insertUserBuildingCrossRef(
                            UsersBuildingsCrossRef(
                                userId = userOwnerID,
                                buildingId = tmpBuilding.buildingId,
                                roleName = "Manager"
                            )
                        )
                        userDao.insertUserRoleCrossRef(
                            UserRoleCrossRef(
                                roleId = 3L,
                                userId = userOwnerID
                            )
                        )
                    } else {
                        val userOwnerID = userDao.insertUser(
                            User(
                                mobileNumber = owner.mobileNumber,
                                password = "123456",
                                roleId = 2L
                            )
                        )
                        userDao.insertUserBuildingCrossRef(
                            UsersBuildingsCrossRef(
                                userId = userOwnerID,
                                buildingId = tmpBuilding.buildingId,
                                roleName = "Owner"
                            )
                        )
                        userDao.insertUserRoleCrossRef(
                            UserRoleCrossRef(
                                roleId = 2L,
                                userId = userOwnerID
                            )
                        )
                    }
//                ownerUnits.forEach { unit ->
                    // Get the dang value for this owner-unit pair, default to 0.0 if not present
                    if (owner.excelDang != 0.0) {
                        val cross = OwnersUnitsCrossRef(
                            ownerId = ownerId,
                            unitId = ownerUnit.unitId,
                            dang = owner.excelDang ?: 6.0
                        )
                        ownersDao.insertOwnerUnitCrossRef(cross)
                    }
//                }
                }

                // Save Tenants
                tenants.forEach { tenant ->
                    val tenantId = tenantsDao.insertTenants(tenant)
                    val tenantUnit: Units = unitsDao.getUnitByUnitNumber(
                        tenant.excelUnitsNumber?.toLong() ?: 0L,
                        tenant.excelBuildingName ?: ""
                    )
                    val tmpBuilding = buildingDao.getBuilding(tenant.excelBuildingName ?: "-1")
                    tenantsDao.insertTenantUnitCrossRef(
                        TenantsUnitsCrossRef(
                            tenantId,
                            tenantUnit.unitId,
                            tenant.startDate,
                            tenant.endDate,
                            tenant.status
                        )
                    )
                    val userTenantID = userDao.insertUser(
                        User(
                            mobileNumber = tenant.mobileNumber,
                            password = "123456",
                            roleId = 4L
                        )
                    )
                    userDao.insertUserBuildingCrossRef(
                        UsersBuildingsCrossRef(
                            userId = userTenantID,
                            buildingId = tmpBuilding.buildingId,
                            roleName = "Tenant"
                        )
                    )
                    userDao.insertUserRoleCrossRef(
                        UserRoleCrossRef(
                            roleId = 4L,
                            userId = userTenantID
                        )
                    )

                }
            } catch (e: Exception) {
                errors.add("${e.message}")
            }
            withContext(Dispatchers.Main) {
                onComplete(successCount, errors)
            }
        }
    }



    fun deleteOwner(
        owner: Owners,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                ownersDao.deleteOwners(owner)
                ownersDao.deleteOwnersWithUnits(owner.ownerId)
                withContext(Dispatchers.Main) {
                    ownersList.remove(owner)
                    onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(e.message ?: "Unknown error") }
            }
        }
    }

    fun updateOwner(
        owner: Owners,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                ownersDao.updateOwner(owner)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(e.message ?: "Unknown error") }
            }
        }
    }


    fun updateUser(
        user: User,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                userDao.updateUser(user)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(e.message ?: "Unknown error") }
            }
        }
    }


    fun updateOwnerWithUnits(owner: Owners, units: List<OwnersUnitsCrossRef>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Update Owner
                ownersDao.updateOwner(owner)

                // Delete Existing Relationships
                ownersDao.deleteOwnersWithUnits(owner.ownerId)

                // Insert New Relationships with dang from OwnersUnitsCrossRef
                units.forEach { unitCrossRef ->
                    if (unitCrossRef.dang != 0.0) {
                        ownersDao.insertOwnerUnitCrossRef(
                            OwnersUnitsCrossRef(
                                ownerId = owner.ownerId,
                                unitId = unitCrossRef.unitId,
                                dang = unitCrossRef.dang.coerceIn(0.0, 6.0) // Clamp between 0 and 6
                            )
                        )
                    }
                }

                ownersList.replaceAll { if (it.ownerId == owner.ownerId) owner else it }

            } catch (e: Exception) {
                Log.e("UpdateError", "Failed to update owner with units: ${e.message}")
            }
        }
    }


    fun getFieldsForObject(objectId: Long): Flow<List<AuthorizationField>> = flow {
        val auth = authorizationDao.getFieldsForObject(objectId)
        emit(auth)
    }.flowOn(Dispatchers.IO)

    fun getUnitForTenant(tenantId: Long): Flow<Units?> = flow {
        val tenant = tenantsDao.getUnitForTenant(tenantId)
        emit(tenant)
    }.flowOn(Dispatchers.IO)

    fun getTenantForUserMobileNumber(mobileNumber: String): Flow<Tenants?> = flow {
        val tenant = tenantsDao.getTenantForUserMobileNumber(mobileNumber)
        emit(tenant)
    }.flowOn(Dispatchers.IO)

    fun updateTenantWithUnit(tenant: Tenants, unit: Units?) {
        viewModelScope.launch(Dispatchers.IO) {
            tenantsDao.updateTenant(tenant)

            if (unit != null) {
                updateTenantUnitCrossRef(tenant, unit)
            } else {
                // Handle case where tenant is disassociated from unit
                tenantsDao.deleteTenantUnitCrossRef(tenant.tenantId)
            }

            // Update StateFlows
            withContext(Dispatchers.Main) {
                tenantsList.map {
                    if (it.tenantId == tenant.tenantId) tenant else it
                }
                // Update tenantUnitMap as well
                if (unit != null) {
                    tenantUnitMap[tenant] = unit
                } else {
                    tenantUnitMap.remove(tenant)
                }
            }
        }


    }

    fun updateTenantWithCostsAndDebts(
        tenantWithRelation: TenantWithRelation,
        updatedTenant: Tenants,
        rentAmount: Double,
        mortgageAmount: Double
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val tenant = updatedTenant
            val crossRef = tenantWithRelation.crossRef
            val unitId = crossRef.unitId ?: return@launch
            val buildingId =
                unitsDao.getBuildingIdFromUnit(unitId) ?: return@launch  // Implement this method

            // 1. Update tenant info
            tenantsDao.updateTenant(tenant)

            // 2. Ensure Rent cost exists or insert it
            val rentCost = costsDao.getDefaultCostByBuildingIdAndName(buildingId, "اجاره") ?: let {
                val newCost = Costs(
                    buildingId = buildingId,
                    costName = "اجاره",
                    tempAmount = rentAmount,
                    period = Period.MONTHLY,
                    calculateMethod = CalculateMethod.EQUAL,
                    paymentLevel = PaymentLevel.UNIT,
                    responsible = Responsible.TENANT,
                    fundType = FundType.OPERATIONAL,
                    chargeFlag = false,
                    dueDate = ""
                )
                val id = costsDao.insertCost(newCost)
                newCost.copy(costId = id)
            }

            // If rentCost already exists, update amount if changed
            if (rentCost.tempAmount != rentAmount) {
                costsDao.updateCost(rentCost.copy(tempAmount = rentAmount))
            }

            // 3. Ensure Mortgage cost exists or insert it
            val mortgageCost =
                costsDao.getDefaultCostByBuildingIdAndName(buildingId, "رهن") ?: let {
                val newCost = Costs(
                    buildingId = buildingId,
                    costName = "رهن",
                    tempAmount = mortgageAmount,
                    period = Period.YEARLY,
                    calculateMethod = CalculateMethod.EQUAL,
                    paymentLevel = PaymentLevel.UNIT,
                    responsible = Responsible.TENANT,
                    fundType = FundType.OPERATIONAL,
                    chargeFlag = false,
                    dueDate = ""
                )
                val id = costsDao.insertCost(newCost)
                newCost.copy(costId = id)
            }

            // If mortgageCost already exists, update amount if changed
            if (mortgageCost.tempAmount != mortgageAmount) {
                costsDao.updateCost(mortgageCost.copy(tempAmount = mortgageAmount))
            }


            val startDateCal = parsePersianDate(crossRef.startDate)
            val endDateCal = parsePersianDate(crossRef.endDate)



            if (startDateCal != null && endDateCal != null) {
                // 4. Insert or update Rent debts - one debt per month within contract period
                var currentCal = startDateCal
                val currDate = String.format(
                    "%04d/%02d/%02d",
                    currentCal.persianYear,
                    currentCal.persianMonth,
                    1
                )

                // Upsert mortgage debt
                val existingMortgageDebt = debtsDao.getDebtForUnitCostAndDueDate(
                    unitId,
                    mortgageCost.costId,
                    currDate
                )
                if(mortgageAmount > 0) {
                    if (existingMortgageDebt == null) {
                        debtsDao.insertDebt(
                            Debts(
                                unitId = unitId,
                                costId = mortgageCost.costId,
                                buildingId = buildingId,
                                description = "رهن",
                                dueDate = currDate,
                                amount = mortgageAmount,
                                paymentFlag = false
                            )
                        )
                    } else {
                        if (existingMortgageDebt.amount != mortgageAmount) {
                            debtsDao.updateDebt(existingMortgageDebt.copy(amount = mortgageAmount))
                        }
                    }
                }

                while (isDateLessOrEqual(currentCal, endDateCal)) {
                    val dueDate = String.format(
                        "%04d/%02d/%02d",
                        currentCal!!.persianYear,
                        currentCal!!.persianMonth,
                        1
                    )

                    // Upsert rent debt
                    val existingRentDebt =
                        debtsDao.getDebtForUnitCostAndDueDate(unitId, rentCost.costId, dueDate)
                    if (rentAmount > 0) {
                        if (existingRentDebt == null) {
                            debtsDao.insertDebt(
                                Debts(
                                    unitId = unitId,
                                    costId = rentCost.costId,
                                    buildingId = buildingId,
                                    description = "اجاره",
                                    dueDate = dueDate,
                                    amount = rentAmount,
                                    paymentFlag = false
                                )
                            )
                        } else {
                            if (existingRentDebt.amount != rentAmount) {
                                debtsDao.updateDebt(existingRentDebt.copy(amount = rentAmount))
                            }
                        }
                    }


                    // Increase month by 1 using your existing function
                    currentCal = getNextMonthSameDaySafe(currentCal)
                }
            }
        }
    }


    private suspend fun updateTenantUnitCrossRef(tenant: Tenants, unit: Units) {
        // Check if relationship exists
        val existingCrossRef = tenantsDao.getTenantUnitCrossRef(tenant.tenantId)

        if (existingCrossRef != null) {
            // Update relationship
            val updatedCrossRef = existingCrossRef.copy(unitId = unit.unitId)
            tenantsDao.updateTenantUnitCrossRef(updatedCrossRef)
        } else {
            // Create relationship
            tenantsDao.insertTenantUnitCrossRef(
                TenantsUnitsCrossRef(
                    tenantId = tenant.tenantId,
                    unitId = unit.unitId,
                    startDate = tenant.startDate,
                    endDate = tenant.endDate,
                    status = tenant.status
                )
            )
        }
    }

    fun deleteTenant(
        tenant: Tenants,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        viewModelScope.launch(Dispatchers.IO) {
            try {
                tenantsDao.deleteTenants(tenant)
                withContext(Dispatchers.Main) {
                    tenantsList.remove(tenant)
                    onSuccess()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(e.message ?: "Unknown error") }
            }
        }

    }


    fun deleteBuildingWithRelations(
        buildingId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                debtsDao.deleteDebtsForBuilding(buildingId)
                // Delete tenants linked to building's units
                tenantsDao.deleteTenantsForBuilding(buildingId)

// Delete owners linked to building's units
                ownersDao.deleteOwnersForBuilding(buildingId)

// Delete units
                unitsDao.deleteUnitsForBuilding(buildingId)

// Delete costs
                costsDao.deleteCostsForBuilding(buildingId)

// Delete the building itself
                buildingDao.deleteBuildingById(buildingId)


                withContext(Dispatchers.Main) { onSuccess() }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(e.message ?: "Unknown error") }
            }
        }
    }


    // Update these functions to add units to the maps
    private fun addOwnerUnits(owner: Owners, ownerUnits: List<OwnersUnitsCrossRef>) {
        ownerUnitMap[owner] = ownerUnits
    }


    fun addFileList(uploadedFileEntity: UploadedFileEntity) {
        fileList.add(uploadedFileEntity)
    }



    fun loadCosts() {
        viewModelScope.launch(Dispatchers.IO) {
            val costsFromDb = costsDao.getChargesCostsWithNullBuildingId()
            withContext(Dispatchers.Main) {
//                val fixedCosts = costsFromDb.filter { cost ->
//                    cost.calculateMethod == CalculateMethod.EQUAL
//                }.map {
//                    it.copy(
//                        tempAmount = 0.0,
//                        period = Period.YEARLY,
//                        responsible = Responsible.TENANT // or appropriate value
//                    )
//                }

                costsList.value = costsFromDb
//                val chargeCost = costsFromDb.filter { cost ->
//                    cost.calculateMethod == CalculateMethod.AREA
//                }.map {
//                    it.copy(
//                        tempAmount = 0.0,
//                        period = Period.MONTHLY,
//                        responsible = Responsible.TENANT
//                    )
//                }
//                charge.value = chargeCost
            }
        }
    }


    fun updateCostAmount(cost: Costs, newAmount: Double) {
        costsList.value = costsList.value.filter { it.buildingId == null }.map {
            if (it.costId == cost.costId) {
                it.copy(tempAmount = newAmount)
            } else it
        }
    }

    fun updateDebtPaymentFlag(debt: Debts, newAmount: Boolean) {
        unpaidDebtList.value = unpaidDebtList.value.map {
            if (it.debtId == debt.debtId) {
                it.copy(paymentFlag = newAmount)
            } else it
        }
    }

    fun updateCostPeriod(cost: Costs, newPeriod: Period) {
        costsList.value = costsList.value.filter { it.buildingId == null }.map {
            if (it.costId == cost.costId) it.copy(period = newPeriod) else it
        }
    }


    fun resetState() {
        name = ""
        phone = ""
        email = ""
        postCode = ""
        street = ""
        province = ""
        state = ""
        fixedAmount = ""
        automaticCharge = false
        sameArea = false
        sameCosts = true
        buildingTypeId = 0
        buildingUsageId = 0
        ownersList.clear()
        ownersList = mutableStateListOf()
        unitDebtsList.clear()
        unitDebtsList = mutableStateListOf()
        tenantsList = mutableStateListOf()
        costsList.value = emptyList()
        charge.value = emptyList()
        loadCosts() // Reload the costs
        selectedBuildingTypes = null
        selectedBuildingUsages = null
        unitsList.clear()
        fileList.clear()
        debtsList.clear()
        selectedOwnerForUnit = null
    }

    // Add to your utilities
    fun String.persianToEnglishDigits(): String {
        return this.map { char ->
            when (char) {
                '۰' -> '0'
                '۱' -> '1'
                '۲' -> '2'
                '۳' -> '3'
                '۴' -> '4'
                '۵' -> '5'
                '۶' -> '6'
                '۷' -> '7'
                '۸' -> '8'
                '۹' -> '9'
                else -> char
            }
        }.joinToString("")
    }

    // Get current year/month in "yyyy/MM" format (e.g. "1403/03")
    fun getCurrentYearMonth(): String {
        val today = PersianCalendar()
        return "${today.persianYear}/${"%02d".format(today.persianMonth + 1)}"
    }

//    // Parses a date string "yyyy/MM/dd" into PersianCalendar
//    fun parsePersianDate(dateStr: String): PersianCalendar? {
//        val parts = dateStr.split("/")
//        if (parts.size != 3) return null
//        val year = parts[0].toIntOrNull() ?: return null
//        val month = parts[1].toIntOrNull() ?: return null
//        val day = parts[2].toIntOrNull() ?: return null
//
//        return PersianCalendar().apply {
//            setPersianDate(year, month, day) // month is zero-based
//        }
//    }

    // Parses a date string "yyyy/MM/dd" into PersianCalendar
    fun parsePersianDate(dateStr: String): PersianCalendar? {
        val parts = dateStr.split("/")
        if (parts.size != 3) return null
        val year = parts[0].toIntOrNull() ?: return null
        val month = parts[1].toIntOrNull()?.minus(1) ?: return null // zero-based month in PersianCalendar
        val day = parts[2].toIntOrNull() ?: return null

        return PersianCalendar().apply {
            setPersianDate(year, month, day)
        }
    }

    // Compare two PersianCalendar dates: returns true if date1 <= date2
    fun isDateLessOrEqual(date1: PersianCalendar?, date2: PersianCalendar): Boolean {
        if (date1 == null) return false

        return when {
            date1.persianYear < date2.persianYear -> true
            date1.persianYear > date2.persianYear -> false
            else -> {
                // Same year, compare month
                if (date1.persianMonth < date2.persianMonth) true
                else if (date1.persianMonth > date2.persianMonth) false
                else {
                    // Same month, compare day
                    date1.persianDay <= date2.persianDay
                }
            }
        }
    }


    fun getNextMonthSameDaySafe(inputDate: PersianCalendar?): PersianCalendar {
        val date = inputDate ?: PersianCalendar() // Use current date if null

        val newYear = if (date.persianMonth == 11) date.persianYear + 1 else date.persianYear
        val newMonth = (date.persianMonth + 1) % 12

        // Temporary PersianCalendar for max days in new month
        val temp = PersianCalendar().apply {
            setPersianDate(newYear, newMonth, 1)
        }
        val maxDay = temp.getMaxDaysInMonth()

        return PersianCalendar().apply {
            setPersianDate(
                newYear,
                newMonth,
                min(date.persianDay, maxDay)
            )
        }
    }


    fun getNextYearSameDaySafe(inputDate: PersianCalendar?): PersianCalendar {
        val date = inputDate ?: PersianCalendar() // Use current date if null
        val newYear = date.persianYear + 1
        val newMonth = date.persianMonth// zero-based month

        // Temporary PersianCalendar for max days in the same month next year
        val temp = PersianCalendar().apply {
            setPersianDate(newYear, newMonth, 1)
        }
        val maxDay = temp.getMaxDaysInMonth()

        val result = PersianCalendar().apply {
            setPersianDate(
                newYear,
                newMonth,
                min(date.persianDay, maxDay)
            )
        }
        return result
    }


    fun getNextMonthSameDaySafe(): PersianCalendar {
        val today = PersianCalendar()
        val newYear = if (today.persianMonth == 11) today.persianYear + 1 else today.persianYear
        val newMonth = (today.persianMonth + 1) % 12

        val temp = PersianCalendar().apply {
            setPersianDate(newYear, newMonth, 1)
        }
        val maxDay = temp.getMaxDaysInMonth()

        return PersianCalendar().apply {
            setPersianDate(
                newYear,
                newMonth,
                minOf(today.persianDay, maxDay)
            )
        }
    }


    fun PersianCalendar.getMaxDaysInMonth(): Int {
        val temp = PersianCalendar().apply {
            setPersianDate(
                this@getMaxDaysInMonth.persianYear,
                this@getMaxDaysInMonth.persianMonth,
                1
            )
        }

        // Find last day of month
        while (temp.persianMonth == this.persianMonth) {
            temp.addPersianDate(PersianCalendar.DAY_OF_MONTH, 1)
        }
        temp.addPersianDate(PersianCalendar.DAY_OF_MONTH, -1)

        return temp.persianDay
    }

    // Usage
    val calendar = PersianCalendar().apply {
        setPersianDate(1402, 5, 1) // Shahrivar (month 6)
    }
    private val _authObjects = MutableStateFlow(listOf<AuthorizationObject>())

    private fun loadAuthorizations(role: String) {
        // Load from database
        _authObjects.value = listOf(
//            AuthorizationObject(
//                objectId = 1,
//                name = "Building Profile",
//                fields = listOf(
//                    AuthorizationField(fieldId = 1, objectId = 1, name = "buildingName", fieldType = "READ"),
//                    AuthorizationField(fieldId = 2, objectId = 1, name = "address", fieldType = "WRITE")
//                )
//            )
        )
    }

    fun saveUploadedFileUrl(buildingId: Long, fileUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val fileId = uploadedFileDao.insertUploadedFile(UploadedFileEntity(fileUrl = fileUrl))
            buildingDao.insertCrossRef(BuildingUploadedFileCrossRef(buildingId, fileId))
        }
    }

    fun getResidents(buildingId: Long) = phonebookDao.getResidents(buildingId)
    fun getEmergencyNumbers(buildingId: Long) = phonebookDao.getEmergencyNumbers(buildingId)
    fun deletePhonebookEntry(entry: PhonebookEntry) = viewModelScope.launch {
        phonebookDao.deletePhonebookEntry(entry)
    }

    fun addPhonebookEntry(entry: PhonebookEntry) = viewModelScope.launch {
        phonebookDao.insertEntry(entry)
    }

    fun getBuildingFiles(buildingId: Long): Flow<List<UploadedFileEntity>> = flow {
        val obj = uploadedFileDao.getFileUrlsForBuilding(buildingId)
        emit(obj)
    }.flowOn(Dispatchers.IO)

    fun allUsers(context: Context) {
        Users().fetchUsers(
            context,
            onSuccess = { userList ->
                // Handle the userList (e.g., update your ViewModel, display in UI)
                //Example:
                //  userViewModel.updateUsers(userList)
            },
            onError = { error ->
                Log.e("error", error.toString())
                // Handle the error (e.g., show a toast, log the error)
                // Example:
                // Toast.makeText(context, "Error fetching users: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

    }



    fun deleteFile(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                }
                // Remove from savedFilePaths list safely on main thread
                withContext(Dispatchers.Main) {
                    savedFilePaths.remove(path)
                }
            } catch (e: Exception) {
                Log.e("SharedViewModel", "Error deleting file", e)
            }
        }
    }


    /**
     * Returns charges map for units for the specified building and fiscal year,
     * for the cost named "شارژ".
     */
    suspend fun getLastCalculatedCharges(
        buildingId: Long,
        fiscalYear: String
    ): Map<Units, Double>? {
        // Get aggregated amount per unitId from debts associated with charge cost
        val aggregates = debtsDao.getTotalChargesByUnitForChargeCost(buildingId, fiscalYear)

        if (aggregates.isEmpty()) return null

        // Extract unitIds (filter nulls just in case)
        val unitIds = aggregates.mapNotNull { it.unitId }

        // Fetch Units for these ids
        val units = unitsDao.getUnitsByIds(unitIds)

        // Make map unitId -> Unit for quick lookup
        val unitMap = units.associateBy { it.unitId }

        // Build Map<Units, Double> only for units found
        val result = aggregates.mapNotNull { agg ->
            val unit = agg.unitId?.let { unitMap[it] }
            if (unit != null) {
                unit to agg.totalAmount
            } else null
        }.toMap()

        return if (result.isEmpty()) null else result
    }

    // Function to load current rent & mortgage debt amounts for a given tenant + unit + costs
    fun loadTenantDebtAmounts(unitId: Long, tenantId: Long, rentCostId: Long, mortgageCostId: Long) {
        viewModelScope.launch {
            val rentDebt = debtsDao.getDebtForTenantAndCost(unitId, tenantId, rentCostId)
            tenantRentDebtMap[tenantId] = rentDebt?.amount ?: 0.0
            Log.d("tenantRentDebtMap[tenantId]", tenantRentDebtMap[tenantId].toString())

            val mortgageDebt = debtsDao.getDebtForTenantAndCost(unitId, tenantId, mortgageCostId)
            tenantMortgageDebtMap[tenantId] = mortgageDebt?.amount ?: 0.0
            Log.d("tenantMortgageDebtMap[tenantId]", tenantMortgageDebtMap[tenantId].toString())
        }
    }


    fun invoiceCostIfEnoughFund(cost: Costs) {
        viewModelScope.launch {
            val buildingId = cost.buildingId ?: return@launch
            val fundType = cost.fundType
            val costAmount = cost.tempAmount
            Log.d("cost", cost.toString())
            Log.d("fundType", fundType.toString())
            // Get current fund record
            val fund = fundsDao.getFundByType(buildingId, fundType)

            if (fund == null) {
                // No fund record found - treat as no funds available
                _invoiceResult.emit(false)
                return@launch
            }
            Log.d("fund.balance", fund.balance.toString())
            Log.d("costAmount", costAmount.toString())
            if (fund.balance >= costAmount) {
                // Enough fund - update cost and fund

                // Mark cost as invoiced
                costsDao.markCostAsInvoiced(cost.costId)

                // Decrease fund balance
                val updatedFund = fund.copy(balance = fund.balance - costAmount)
                fundsDao.updateFunds(updatedFund)

                _invoiceResult.emit(true) // Success
            } else {
                // Not enough funds
                _invoiceResult.emit(false)
            }
        }
    }

    fun getNotInvoicedEarnings(buildingId: Long) = earningsDao.getNotInvoicedEarnings(buildingId)



    suspend fun insertEarningsWithCredits(
        earnings: Earnings
    ) {
        val conflictCount = creditsDao.countConflictingCredits(
            buildingId = earnings.buildingId ?: return,
            earningsName = earnings.earningsName,
            startDate = earnings.startDate,
            endDate = earnings.endDate
        )

        if (conflictCount > 0) {
            // Signal conflict (e.g., throw exception or return failure status)
            throw IllegalStateException("Earnings conflict with existing credits")
        } else {
            // Insert the Earnings
            val earningsId = earningsDao.insertEarnings(earnings)
            val startLocalDate = parsePersianDate(earnings.startDate)
            val endLocalDate = parsePersianDate(earnings.endDate)

            if (earnings.period == Period.MONTHLY) {
                // Insert a Credit row for each month between start and end dates
                var current = startLocalDate
                if (current != null && endLocalDate != null) {
                    while (isDateLessOrEqual(current, endLocalDate)) {
                        val dueDate = String.format(
                            "%04d/%02d/%02d",
                            current!!.persianYear,
                            current.persianMonth,
                            1
                        )
                        val credit = Credits(
                            earningsId = earningsId,
                            buildingId = earnings.buildingId,
                            description = earnings.earningsName,
                            dueDate = dueDate,
                            amount = earnings.amount,
                            receiptFlag = false
                        )
                        creditsDao.insertCredit(credit)
                        current = getNextMonthSameDaySafe(current)
                    }
                }
            } else {
                // Insert a single Credit covering the whole period or whichever logic you want
                creditsDao.insertCredit(
                    Credits(
                        earningsId = earningsId,
                        buildingId = earnings.buildingId!!,
                        description = earnings.earningsName,
                        dueDate = earnings.startDate,
                        amount = earnings.amount,
                        receiptFlag = false
                    )
                )
            }
        }
    }

    private fun calculateNextDueDate(currentDueDate: String, period: Period): String {
        // Implement date calculation based on your date format and Period enum
        // e.g. parse currentDueDate, add period duration, format back to string
        return "" // implement this!
    }

    suspend fun decreaseOperationalFund(buildingId: Long, amount: Double, fundType:FundType): Boolean {
        var fund = fundsDao.getFundByType(buildingId, fundType)
        if (fund == null) {
            // Insert new fund with 0 balance
            val newFund = Funds(
                buildingId = buildingId,
                fundType = fundType,
                balance = 0.0
            )
            fundsDao.insertFunds(newFund)
            fund = newFund
        }
        return if (fund.balance >= amount) {
            fundsDao.updateFunds(fund.copy(balance = fund.balance - amount))
            true
        } else {
            false
        }
    }

    suspend fun increaseBalanceFund(buildingId: Long, amount: Double, fundType:FundType): Boolean {
        try {
            var fund = fundsDao.getFundByType(buildingId, fundType)
            if (fund == null) {
                // Insert new fund with initial balance = amount since we're increasing
                val newFund = Funds(
                    buildingId = buildingId,
                    fundType = fundType,
                    balance = amount
                )
                fundsDao.insertFunds(newFund)
                return true // Insert done, balance is amount
            }
            // Fund exists, update by increasing balance
            fundsDao.updateFunds(fund.copy(balance = fund.balance + amount))
            return true
        } catch (e: Exception) {
            Log.e("increaseFund", e.toString())
            return false
        }
    }

    fun updateCredits(credits: List<Credits>) {
        _creditsForEarning.value = credits
    }

    fun toggleCreditSelection(creditId: Long) {
        val credit = _creditsForEarning.value.find { it.creditsId == creditId } ?: return
        if (credit.receiptFlag == true) return  // Don't toggle if already received

        val currentSelection = _selectedCredits.value.toMutableSet()
        if (currentSelection.contains(creditId)) {
            currentSelection.remove(creditId)
        } else {
            currentSelection.add(creditId)
        }
        _selectedCredits.value = currentSelection
    }


    val sumSelectedAmount = combine(_creditsForEarning, _selectedCredits) { credits, selectedIds ->
        credits.filter { selectedIds.contains(it.creditsId) }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    // Update selected credits as receiptFlag=1 (received)
    fun markSelectedAsReceived(earningId:Long) {
            viewModelScope.launch {
                val selectedIds = _selectedCredits.value.toList()
                creditsDao.updateReceiptFlagByIds(selectedIds, true)

                // Reload credits from DB again after update
                val updatedCredits = creditsDao.getCreditsFromEarning(earningId)
                val building = earningsDao.getBuildingFromEarning(earningId)
                increaseBalanceFund(building?.buildingId ?: 0,sumSelectedAmount.value, FundType.OPERATIONAL)
                _creditsForEarning.value = updatedCredits

                // Clear selections
                _selectedCredits.value = emptySet()
            }

    }


    // Combine owners and units into UserWithUnit list
    fun getActiveOwnersForBuilding(buildingId: Long): Flow<List<UserWithUnit>> = flow {
        val owners = ownersDao.getOwnersForBuilding(buildingId)
        val userWithUnits = owners.map { owner ->
            val units = unitsDao.getUnitsForOwner(owner.ownerId)
            UserWithUnit(
                id = owner.ownerId,
                firstName = owner.firstName,
                lastName = owner.lastName,
                unitNumber = units.firstOrNull()?.unitNumber,
                userType = UserType.OWNER
            )
        }
        emit(userWithUnits)
    }.flowOn(Dispatchers.IO)

    // Combine tenants and units into UserWithUnit list
    fun getActiveTenantsForBuilding(buildingId: Long): Flow<List<UserWithUnit>> = flow {
        val tenants = tenantsDao.getActiveTenantsForBuilding(buildingId)
        val userWithUnits = tenants.map { tenant ->
            val units = tenantsDao.getUnitForTenant(tenant.tenantId)
            UserWithUnit(
                id = tenant.tenantId,
                firstName = tenant.firstName,
                lastName = tenant.lastName,
                unitNumber = units?.unitNumber ?: "",
                userType = UserType.TENANT
            )
        }
        emit(userWithUnits)
    }.flowOn(Dispatchers.IO)

    // Convenience: get all users (owners + tenants) for building combined
    fun getOwnersAndTenantsForBuilding(buildingId: Long): Flow<List<UserWithUnit>> =
        combine(getActiveOwnersForBuilding(buildingId), getActiveTenantsForBuilding(buildingId)) { owners, tenants ->
            owners + tenants
        }

    suspend fun transferDebtsToTenants(ownerId: Long, debtsToTransfer: List<Debts>): Boolean = try {
        withContext(Dispatchers.IO) {
            debtsToTransfer.forEach { debt ->
                val tenants = tenantsDao.getTenantsWithRelationForUnit(debt.unitId ?: return@forEach)
                if (tenants.isEmpty()) return@forEach
                val tenantUnitId = tenants.first().crossRef.unitId
                val updatedDebt = debt.copy(ownerId = null, unitId = tenantUnitId)
                debtsDao.updateDebt(updatedDebt)
            }
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }




}



