package com.example.delta.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.delta.data.dao.AuthorizationDao.FieldWithPermission
import com.example.delta.data.dao.UnitsDao.UnitDangSum
import com.example.delta.data.entity.AuthorizationField
import com.example.delta.data.entity.AuthorizationObject
import com.example.delta.data.entity.BuildingOwnerCrossRef
import com.example.delta.data.entity.BuildingTypes
import com.example.delta.data.entity.BuildingUploadedFileCrossRef
import com.example.delta.data.entity.BuildingUsages
import com.example.delta.data.entity.BuildingWithTypesAndUsages
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Debts
import com.example.delta.data.entity.Earnings
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
import com.example.delta.data.model.AppDatabase
import com.example.delta.enums.CalculateMethod
import com.example.delta.enums.FundFlag
import com.example.delta.enums.PaymentLevel
import com.example.delta.enums.Period
import com.example.delta.enums.PermissionLevel
import com.example.delta.enums.Responsible
import com.example.delta.init.Calculation
import com.example.delta.init.Preference
import com.example.delta.volley.Building
import com.example.delta.volley.Users
import ir.hamsaa.persiandatepicker.util.PersianCalendar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import kotlin.math.min

class SharedViewModel(application: Application) : AndroidViewModel(application) {

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
    var sharedUtilities by mutableStateOf(listOf<String>())
    var savedFilePaths = mutableStateListOf<String>()
    var isLoading by mutableStateOf(false)


    var selectedChargeType by mutableStateOf(listOf<String>())

    var unitsAdded by mutableStateOf(false)
    private val _buildingList = MutableStateFlow<List<BuildingWithTypesAndUsages>>(emptyList())
    val buildingList: StateFlow<List<BuildingWithTypesAndUsages>> = _buildingList
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
    var unpaidDebtList = mutableStateOf(listOf<Debts>())
    var charge = mutableStateOf(listOf<Costs>())
    var unitsList = mutableStateListOf<Units>()
    var debtsList = mutableStateListOf<Debts>()
    var unitDebtsList = mutableStateListOf<Debts>()
    var newOwnerId: Long by mutableLongStateOf(0L)
    var newTenantId: Long by mutableLongStateOf(0L)
    var selectedOwnerForUnit by mutableStateOf<Owners?>(null)
    var fileList = mutableStateListOf<UploadedFileEntity>()

    // These represent the selected items from your dropdowns
    var selectedBuildingTypes by mutableStateOf<BuildingTypes?>(null)
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
    var fillCostsByBuilding by mutableStateOf(false)
    var chargeFundFlagChecked by mutableStateOf(false)
    var rentMortgageFundFlagChecked by mutableStateOf(false)
    var sameCosts by mutableStateOf(true)
    var fundMinus by mutableStateOf(false)
    var currentRoleId by mutableLongStateOf(0L)

    var fixedAmount by mutableStateOf("")
        private set // Prevent external direct modification


    var chargeAmount by mutableStateOf("")


    // Options Lists
    val periods = Period.entries


    init {
//        loadOwners()
//        loadTenants()
        loadCosts()
        loadBuildingsWithTypesAndUsages()
//        loadDefaultCosts()
    }


    // Function to insert new cost name into DB
    suspend fun insertNewCostName(buildingId: Long, costName: String): Costs {
        val cost = Costs(
            buildingId = buildingId,
            costName = costName,
            tempAmount = 0.0,
            period = Period.NONE,
            calculateMethod = CalculateMethod.FIXED,
            paymentLevel = PaymentLevel.BUILDING,
            responsible = Responsible.OWNER,
            fundFlag = FundFlag.NO_EFFECT
        )
        val id = costsDao.insertCost(cost)
        return costsDao.getCostById(id) // You need this DAO method
    }


    fun getAllAuthorizationObjects(): Flow<List<AuthorizationObject>> = flow {
        val obj = authorizationDao.getAllAuthorizationObjects()
        emit(obj)
    }.flowOn(Dispatchers.IO)


    fun getFieldsForAuthorizationObject(objId: Long): Flow<List<AuthorizationField>> = flow {
        val obj = authorizationDao.getFieldsForObject(objId)
        emit(obj)
    }.flowOn(Dispatchers.IO)

    fun getDebtsOneUnit(unitId: Long): Flow<List<Debts>> = flow {
        val debts = debtsDao.getDebtsOneUnit(unitId)
        emit(debts)
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

    fun updateDebt(debt: Debts) {
        viewModelScope.launch(Dispatchers.IO) {
            debtsDao.updateDebt(debt)
        }
    }

    fun getUnitsOfBuildingForCost(costId: Long, buildingId: Long): Flow<List<Units>> = flow {
        val units = costsDao.getUnitsOfBuildingFromCost(costId, buildingId)
        emit(units)
    }.flowOn(Dispatchers.IO)


    fun getBuildingsWithUserRole(userId: Long): Flow<List<BuildingWithTypesAndUsages>> = flow {
        val units = buildingDao.getBuildingsWithUserRole(userId)
        emit(units)
    }.flowOn(Dispatchers.IO)

    fun canShowTab(
        roleId: Long,
        objectId: Long,
        fieldIds: List<Long>,
        minPermissionLevel: Int = 0
    ): Flow<Boolean> {
        return authorizationDao.hasAuthorizationForFields(roleId, objectId, fieldIds, minPermissionLevel)
    }

    fun hasFieldPermission(
        roleId: Long,
        objectId: Long,
        fieldId: Long,
        required: PermissionLevel
    ): Flow<Boolean> = flow {
        val level = authorizationDao.getPermissionLevel(roleId, objectId, fieldId)
        emit(level != null && level >= required.value)
    }.flowOn(Dispatchers.IO)

    fun getFieldPermissionFlow(
        roleId: Long,
        objectId: Long,
        fieldNameRes: Int,
        required: PermissionLevel = PermissionLevel.READ
    ): Flow<Boolean> = flow {
        val fieldId = authorizationDao.getFieldIdByName(objectId, fieldNameRes)
        if (fieldId == null || fieldId == 0L) {
            emit(false)
        } else {
            // Collect permission level from DAO
            val hasPermission = authorizationDao.getPermissionLevel(roleId, objectId, fieldId)?.let {
                it >= required.value
            } ?: false
            emit(hasPermission)
        }
    }.flowOn(Dispatchers.IO)



    fun getOwnersOfBuildingForCost(costId: Long, buildingId: Long): Flow<List<Owners>> = flow {
        val owners = costsDao.getOwnersOfBuildingFromCost(costId, buildingId)
        emit(owners)
    }.flowOn(Dispatchers.IO)


    fun getAllBuildings(): Flow<List<Buildings>> = flow {
        val buildings = buildingDao.getBuildings()
        emit(buildings)
    }.flowOn(Dispatchers.IO)

    fun getPaysForUnit(unitId: Long): Flow<List<Debts>> = flow {
        val debts = debtsDao.getPaysForUnit(unitId)
        emit(debts)
    }.flowOn(Dispatchers.IO)

    fun getDebtsForUnitCostCurrentAndPreviousUnpaid(
        costId: Long,
        buildingId: Long,
        unitId: Long,
        yearStr: String,
        monthStr: String
    ): Flow<List<Debts>> = flow {
        val units = debtsDao.getDebtsCurrentMonthAndPastUnpaid(
            buildingId = buildingId,
            costId = costId,
            unitId = unitId,
            yearStr = yearStr,
            monthStr = monthStr
//            ,            getCurrentYearMonth()
        )
        emit(units)
    }.flowOn(Dispatchers.IO)

    fun getDebtsFundMinus(
        costId: Long,
        buildingId: Long,
        yearStr: String,
        monthStr: String
    ): Flow<List<Debts>> = flow {
        val units = debtsDao.getDebtsFundMinus(
            buildingId = buildingId,
            costId = costId,
            yearStr = yearStr,
            monthStr = monthStr
        )
        emit(units)
    }.flowOn(Dispatchers.IO)


    fun getDebtsForOwnerCostCurrentAndPreviousUnpaid(
        costId: Long,
        buildingId: Long,
        ownerId: Long,
        yearStr: String,
        monthStr: String
    ): Flow<List<Debts>> = flow {
        val units = debtsDao.getDebtsForOwnerCostCurrentAndPreviousUnpaid(
            buildingId = buildingId,
            costId = costId,
            ownerId = ownerId,
            yearStr = yearStr,
            monthStr = monthStr
//            ,            getCurrentYearMonth()
        )
        emit(units)
    }.flowOn(Dispatchers.IO)


    fun sumPaidFundFlagPositive(buildingId: Long): Flow<Double> =
        debtsDao.sumPaidFundFlagPositive(buildingId)

    fun sumPaidEarnings(buildingId: Long): Flow<Double> =
        earningsDao.sumPaidEarning(buildingId)

    fun sumUnpaidFundFlagNegative(buildingId: Long): Flow<Double> =
        debtsDao.sumUnpaidFundFlagNegative(buildingId)

    fun sumFundMinus(buildingId: Long): Flow<Double> =
        debtsDao.sumFundMinus(buildingId, Responsible.ALL)


    fun getAllCosts(): Flow<List<Costs>> = flow {
        val costs = costsDao.getCosts()
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

    fun getBuildingsForUnit(unitId: Long): Flow<Buildings> = flow {
        val owners = unitsDao.getBuildingForUnit(unitId)
        emit(owners)
    }.flowOn(Dispatchers.IO)

    fun getCostsForBuilding(buildingId: Long): Flow<List<Costs>> = flow {
        val costs = costsDao.getCostsForBuildingWithFundFlag(
            buildingId, FundFlag.NO_EFFECT
        )
        emit(costs)
    }.flowOn(Dispatchers.IO)

    fun getEarningForBuilding(buildingId: Long): Flow<List<Earnings>> = flow {
        val earnings = earningsDao.getEarningsForBuilding(
            buildingId
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

    fun getCost(costId: Long): Flow<Costs> = flow {
        val cost = costsDao.getCostById(costId)
        emit(cost)
    }.flowOn(Dispatchers.IO)


    fun checkCostNameExists(buildingId: Long, costName: String): Flow<Boolean> = flow {
        val costs = costsDao.costNameExists(buildingId, costName)
        emit(costs)
    }.flowOn(Dispatchers.IO)

    fun countUnits(buildingId: Long): Flow<Int> = flow {
        val units = unitsDao.countUnits(buildingId)
        emit(units)
    }.flowOn(Dispatchers.IO)

    fun getUnitsForBuilding(buildingId: Long?): Flow<List<Units>> = flow {
        val units = unitsDao.getUnitsByBuildingId(buildingId)
        emit(units)
    }.flowOn(Dispatchers.IO)

    fun getOwnersForBuilding(buildingId: Long?): Flow<List<Owners>> = flow {
        val owners = ownersDao.getOwnersForBuilding(buildingId)
        emit(owners)
    }.flowOn(Dispatchers.IO)

    fun getOwner(ownerId: Long): Flow<Owners> = flow {
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

//    fun getAllOwnerUnitsCrossRefs(): Flow<List<OwnersUnitsCrossRef>> {
//        return ownersDao.getOwnersUnitsCrossRef()
//    }


    fun getCostsByIds(ownerIds: List<Long>): Flow<List<Costs>> = flow {
        val owners = costsDao.getCostsByIds(ownerIds)
        emit(owners)
    }.flowOn(Dispatchers.IO)


    fun getDangSumsForAllUnits(): Flow<List<UnitDangSum>> = flow {
        val owners = unitsDao.getDangSumsForAllUnits()
        emit(owners)
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


    fun getUserByRoleId(roleId: Long): Flow<User> = flow {
        emit(userDao.getUserByRoleId(roleId))
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

//    fun getTenantUnitRelationshipsOfUnit(unitId: Long): Flow<List<TenantWithRelation>> = flow {
//        val tenantsUnitsCrossRef = tenantsDao.getTenantsWithRelationForUnit(unitId)
//        emit(tenantsUnitsCrossRef)
//    }.flowOn(Dispatchers.IO)

    fun getTenantUnitRelationshipsOfUnit(unitId: Long): Flow<List<TenantWithRelation>> =
        tenantsDao.getTenantsWithRelationForUnit(unitId)
            .flowOn(Dispatchers.IO)


    fun getAllUnitsForBuilding(buildingId: Long): Flow<List<Units>> = flow {
        val units = unitsDao.getUnits(buildingId)
        emit(units)
    }.flowOn(Dispatchers.IO)

    fun getAllUnits(): Flow<List<Units>> = flow {
        val units = tenantsDao.getAllUnits()
        emit(units)
    }.flowOn(Dispatchers.IO)


    fun getAllUsers(): Flow<List<User>> = flow {
        val users = userDao.getUsers()
        emit(users)
    }.flowOn(Dispatchers.IO)

    fun getUnitsForOwners(ownerId: Long): Flow<List<UnitWithDang>> = flow {
        val owners = ownersDao.getUnitsWithDangForOwner(ownerId)
        emit(owners)
    }.flowOn(Dispatchers.IO)

    fun getOwnersForUnit(unitId: Long): Flow<List<Owners>> = flow {
        val owners = ownersDao.getOwnersForUnit(unitId)
        emit(owners)
    }.flowOn(Dispatchers.IO)

    fun getBuildingOwnerCrossRef(): Flow<List<BuildingOwnerCrossRef>> = flow {
        val owners = ownersDao.getAllBuildingsOwnerCrossRef()
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

    fun insertDebtPerNewCost(
        buildingId: Long,
        amount: String,
        name: String,
        period: Period,
        dueDate: String,
        paymentLevel: PaymentLevel,
        calculateMethod: CalculateMethod,
        calculatedUnitMethod: CalculateMethod,
        fundFlag: FundFlag,
        responsible: Responsible,
        selectedUnitIds: List<Long> = emptyList(),
        selectedOwnerIds: List<Long> = emptyList(),
        fundMinus: FundFlag
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val parsedAmount = amount.persianToEnglishDigits().toDoubleOrNull() ?: 0.0

            // Check if cost with the same name exists for this building
            val existingCost = costsDao.getCostByBuildingIdAndName(buildingId, name)
            val cost: Costs
            var newResponsible = responsible
            var paymentFlag = false
            var newPaymentLevel = paymentLevel
            if (fundMinus == FundFlag.NEGATIVE_EFFECT) {
                newResponsible = Responsible.ALL
                paymentFlag = true
                newPaymentLevel = PaymentLevel.BUILDING

            }
            var costId: Long = 0
            if (existingCost != null) {
                // Update existing cost
                if (newResponsible == Responsible.TENANT) {
                    cost = existingCost.copy(
                        tempAmount = parsedAmount,
                        period = period,
                        paymentLevel = newPaymentLevel,
                        calculateMethod = calculatedUnitMethod,
                        responsible = newResponsible,
                        fundFlag = fundFlag
                    )
                    costsDao.updateCost(cost)
                    costId = cost.id
                } else {
                    cost = existingCost.copy(
                        tempAmount = parsedAmount,
                        period = period,
                        paymentLevel = newPaymentLevel,
                        calculateMethod = calculateMethod,
                        responsible = newResponsible,
                        fundFlag = fundFlag
                    )
                    costsDao.updateCost(cost)
                    costId = cost.id
                }

            } else {
                // Insert new cost
                if (newResponsible == Responsible.TENANT) {
                    cost = Costs(
                        buildingId = buildingId,
                        costName = name,
                        tempAmount = parsedAmount,
                        period = period,
                        calculateMethod = calculatedUnitMethod,
                        paymentLevel = newPaymentLevel,
                        responsible = newResponsible,
                        fundFlag = fundFlag
                    )
                    costsDao.insertCost(cost)
                } else {
                    cost = Costs(
                        buildingId = buildingId,
                        costName = name,
                        tempAmount = parsedAmount,
                        period = period,
                        calculateMethod = calculateMethod,
                        paymentLevel = newPaymentLevel,
                        responsible = newResponsible,
                        fundFlag = fundFlag
                    )
                    costsDao.insertCost(cost)
                }

            }

            // Get units depending on responsible and selected units
            when {
                newResponsible == Responsible.TENANT && selectedUnitIds.isNotEmpty() -> {
                    val units = unitsDao.getUnitsByIds(selectedUnitIds)
                    var newDueDate = ""
                    if (period == Period.NONE) {
                        newDueDate = dueDate
                    } else {
                        //@TODO loop of period
                        newDueDate = getNextMonthSameDaySafe().persianShortDate
                    }
                    //@todo amount per unit based on people , area, fixed

                    val numberOfUnits = units.size
                    val amountPerUnit = if (numberOfUnits > 0) parsedAmount / numberOfUnits else 0.0

                    // Insert debts for each unit
                    units.forEach { unit ->
                        val debt = Debts(
                            unitId = unit.unitId,
                            costId = costId,
                            buildingId = buildingId,
                            description = name,
                            dueDate = newDueDate,
                            amount = amountPerUnit,
                            paymentFlag = paymentFlag
                        )
                        debtsDao.insertDebt(debt)
                    }

                }

                newResponsible == Responsible.OWNER && selectedOwnerIds.isNotEmpty() -> {
                    var newDueDate = ""
                    if (period == Period.NONE) {
                        newDueDate = dueDate
                    } else {
                        //@TODO loop of period
                        newDueDate = getNextMonthSameDaySafe().persianShortDate
                    }
                    val owners = ownersDao.getOwnersForBuilding(buildingId)
                    val units = unitsDao.getUnits(buildingId)
                    val ownersUnitsCrossRefs = ownersDao.getOwnersWithUnitsList(selectedOwnerIds)
                    val areaByOwner =
                        Calculation().calculateAreaByOwners(owners, units, ownersUnitsCrossRefs)
                    // Calculate amount per owner
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
                            costId = cost.id,
                            buildingId = buildingId,
                            description = cost.costName,
                            dueDate = newDueDate, // set appropriately
                            amount = amount,
                            paymentFlag = paymentFlag
                        )
                        debtsDao.insertDebt(debt)
                    }
                }

                newResponsible == Responsible.ALL -> {
                    var newDueDate: String
                    if (period == Period.NONE) {
                        newDueDate = dueDate
                    } else {
                        //@TODO loop of period
                        newDueDate = getNextMonthSameDaySafe().persianShortDate
                    }
                    val debt = Debts(
                        unitId = null,
                        ownerId = null,
                        costId = cost.id,
                        buildingId = buildingId,
                        description = cost.costName,
                        dueDate = newDueDate, // set appropriately
                        amount = parsedAmount,
                        paymentFlag = true
                    )
                    debtsDao.insertDebt(debt)
                }

            }

//            var newDueDate = ""
//            if (period == Period.NONE) {
//                newDueDate = dueDate
//            } else {
//                //@TODO loop of period
//                newDueDate = getNextMonthSameDaySafe().persianShortDate
//            }
//            val units = unitsDao.getUnitsByIds(selectedUnitIds)
//            val numberOfUnits = units.size
//            val amountPerUnit = if (numberOfUnits > 0) parsedAmount / numberOfUnits else 0.0
//
//            // Insert debts for each unit
//            units.forEach { unit ->
//                val debt = Debts(
//                    unitId = unit.unitId,
//                    costId = costId,
//                    buildingId = buildingId,
//                    description = name,
//                    dueDate = newDueDate,
//                    amount = amountPerUnit,
//                    paymentFlag = false
//                )
//                debtsDao.insertDebt(debt)
//            }
        }
    }


    fun insertDebtPerNewEarnings(
        buildingId: Long,
        amount: String,
        name: String,
        period: Period,
        paymentLevel: PaymentLevel,
        fundFlag: FundFlag
    ) {

        viewModelScope.launch(Dispatchers.IO) {
            // Convert amount to Double
            val parsedAmount = amount.toString().persianToEnglishDigits().toDoubleOrNull() ?: 0.0

            var units = unitsDao.getUnitsByBuildingId(buildingId)
            // Calculate debt per unit
            val numberOfUnits = units.size
            val amountPerUnit = if (numberOfUnits > 0) parsedAmount / numberOfUnits else 0.0
            // Insert Cost
            val cost = Costs(
                buildingId = buildingId,
                costName = name,
                tempAmount = parsedAmount.toString().persianToEnglishDigits().toDoubleOrNull()
                    ?: 0.0,
                period = period,
                calculateMethod = CalculateMethod.FIXED,
                paymentLevel = paymentLevel,
                responsible = Responsible.TENANT,
                fundFlag = fundFlag
            )

            val costId = costsDao.insertCost(cost)

            // Insert Debts
            units.forEach { unit ->
                val debt = Debts(
                    unitId = unit.unitId,
                    costId = costId,
                    buildingId = buildingId,
                    description = name, //Customizing description
                    dueDate = getNextMonthSameDaySafe().persianShortDate,
                    amount = amountPerUnit.toString().persianToEnglishDigits()
                        .toDoubleOrNull() ?: 0.0,
                    paymentFlag = false
                )
                debtsDao.insertDebt(debt)
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
        tenantsUnitsCrossRef: List<TenantsUnitsCrossRef>,
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
                        utilities = sharedUtilities,
                        userId = userId
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

                    // Prepare Charges
                    val updatedCharges = mutableListOf<Costs>()
                    val updatedCosts = mutableListOf<Costs>()
                    // Prepare Costs

                    if (sameCosts) {
                        costsList.value.filter { it.buildingId == null }.forEach { cost ->
                            val newCost = cost.copy(buildingId = buildingId, id = 0L)
                            costsList.value += newCost
                        }
                    } else {
                        unitDebtsList.forEach { debt ->
                            val originalCost = costsList.value.find { it.id == debt.costId }
                            if (originalCost != null) {
                                // Check if costsList already contains a cost with the same name and buildingId
                                val exists = costsList.value.any {
                                    it.buildingId == buildingId && it.costName == originalCost.costName
                                }
                                if (!exists) {
                                    val newCost = originalCost.copy(
                                        id = 0L,
                                        buildingId = buildingId,
                                        tempAmount = debt.amount
                                    )
                                    costsList.value += newCost
                                }
                            }
                        }

                    }

                    costsList.value.filter { it.buildingId == buildingId }.map {
                        it.copy(
                            id = 0, // Reset for new building
                            buildingId = buildingId
                        )
                    }.forEach { cost ->
                        var insertedId = costsDao.insertCost(cost)

                        updatedCosts.add(cost.copy(id = insertedId))
                    }

                    charge.value.filter { it.buildingId == null }.forEach { cost ->
                        val newCost = cost.copy(buildingId = buildingId, id = 0L)
                        charge.value += newCost
                    }
                    charge.value.filter { it.buildingId == buildingId }.map {
                        it.copy(
                            id = 0, // Reset for new building
                            buildingId = buildingId
                        )
                    }.forEach { charge ->
                        var insertedId = costsDao.insertCost(charge)
                        updatedCharges.add(charge.copy(id = insertedId))
                    }

                    // Prepare units
                    unitsList.forEachIndexed { index, unit ->
                        val unitWithBuildingId = unit.copy(buildingId = buildingId)
                        val insertedUnit = unitsDao.insertUnit(unitWithBuildingId)
                        unitsList[index] = unit.copy(unitId = insertedUnit)
                        (updatedCosts + updatedCharges).filter { it.buildingId == buildingId }
                            .forEach { cost ->
                                // Determine Amount for Charge
                                val amount = when {
                                    automaticCharge && cost in updatedCharges -> { //Check if amount comes from charge
                                        when {
                                            selectedChargeType.contains("متراژ") -> {
                                                val area = unitsDao.getUnit(insertedUnit).area
                                                area.toInt() * (chargeAmount.persianToEnglishDigits()
                                                    .toDoubleOrNull() ?: 0.0)
                                            }

                                            selectedChargeType.contains("نفرات") -> {
                                                try {
                                                    val relationship =
                                                        tenantsUnitsCrossRef.firstOrNull {
                                                            it.unitId == insertedUnit && it.status == "فعال"
                                                        }
                                                    if (relationship != null) {
                                                        val tenant =
                                                            tenantsDao.getTenant(relationship.tenantId)
                                                        tenant.numberOfTenants.toInt() *
                                                                (chargeAmount.persianToEnglishDigits()
                                                                    .toDoubleOrNull() ?: 0.0)
                                                    } else {
                                                        0.0
                                                    }
                                                } catch (e: Exception) {
                                                    0.0
                                                }
                                            }

                                            else -> 0.0
                                        }
                                    }

                                    else -> {
                                        val matchedCost =
                                            costsList.value.find { it.costName == cost.costName }

// If found, get its id; else handle null case
                                        val costIdToUse = matchedCost?.id
                                            ?: -1L // or some invalid ID to indicate not found
// Then find the matching debt in unitDebtsList by unitId and costId
                                        val matchingDebt = if (costIdToUse != -1L) {
                                            unitDebtsList.find { debt ->
                                                debt.unitId == insertedUnit && debt.costId == costIdToUse
                                            }
                                        } else {
                                            null
                                        }

                                        if (!sameCosts) {
                                            matchingDebt?.amount
                                                ?: 0.0  // Default to 0 if not found
                                        } else {
                                            cost.tempAmount.toString().persianToEnglishDigits()
                                                .toDoubleOrNull()
                                                ?: 0.0 //If not from charge get from cost amount
                                        }
                                    }
                                }

                                // Retrieve Tenant information
                                val tenant = tenantsList.firstOrNull { tenant ->
                                    val tenantUnit = tenantUnitMap[tenant]
                                    tenantUnit?.unitId == insertedUnit
                                }
                                val startDate = parsePersianDate(tenant?.startDate ?: "")
                                val endDate = parsePersianDate(tenant?.endDate ?: "")
                                // Create debt for monthly period
                                if (cost.period == Period.MONTHLY) {
                                    if (startDate != null && endDate != null) {
                                        var dueDate = startDate

                                        while (isDateLessOrEqual(dueDate, endDate)) {
                                            val formattedDueDate = dueDate?.let {
                                                String.format(
                                                    "%04d/%02d/%02d",
                                                    it.persianYear,
                                                    it.persianMonth,
                                                    it.persianDay
                                                )
                                            } ?: ""

                                            val debt = Debts(
                                                unitId = insertedUnit,
                                                costId = cost.id,
                                                buildingId = buildingId,
                                                description = cost.costName,
                                                dueDate = formattedDueDate,
                                                amount = amount,
                                                paymentFlag = false
                                            )
                                            debtsDao.insertDebt(debt)

                                            // Move to next month safely
                                            dueDate = getNextMonthSameDaySafe(dueDate)
                                        }
                                    }
                                } else {
                                    // Other periods
                                    val debt = Debts(
                                        unitId = insertedUnit,
                                        costId = cost.id,
                                        buildingId = buildingId,
                                        description = cost.costName,
                                        dueDate = PersianCalendar().persianShortDate,// getNextMonthSameDaySafe(PersianCalendar()).persianShortDate,
                                        amount = amount,
                                        paymentFlag = false
                                    )
                                    debtsDao.insertDebt(debt)
                                }
                            }
                    }

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


                    // Save Tenants
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
                Log.d("SaveBuilding", "Before onSuccess")
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
                    utilities = sharedUtilities,
                    userId = userId
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
                    Log.d("allBuilding",allBuilding.toString())
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

    fun getUnitForTenant(tenantId: Long): Flow<Units> = flow {
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


    // In SharedViewModel
    fun addOwner(owner: Owners) {
        viewModelScope.launch(Dispatchers.IO) {
//            ownersDao.insertOwners(owner) // Get the generated ID

            // Update the owner with the new ID
            val updatedOwner = owner.copy(ownerId = newOwnerId)

            withContext(Dispatchers.Main) {
                ownersList.add(updatedOwner)
            }
        }
    }

    fun addTempDebt(unitId: Long) {

        costsList.value.filter { it.buildingId == null }.forEach { cost ->
            val existingDebtInDebts = debtsList.find { it.unitId == unitId && it.costId == cost.id }
            val existingDebtInUnitDebts =
                unitDebtsList.find { it.unitId == unitId && it.costId == cost.id }
            val debt = Debts(
                unitId = unitId, costId = cost.id,
                description = cost.costName, amount = cost.tempAmount,
                buildingId = 0, dueDate = "", paymentFlag = false
            )
            if (existingDebtInDebts != null) {
                // Update existing debt in debtsList
                val index = debtsList.indexOf(existingDebtInDebts)
                debtsList[index] = existingDebtInDebts.copy(
                    amount = cost.tempAmount,
                    paymentFlag = false,
                    dueDate = "" // or keep existing dueDate if preferred
                )
            } else {
                debtsList.add(debt)
            }

            if (existingDebtInUnitDebts != null) {
                // Update existing debt in debtsList
                val index = unitDebtsList.indexOf(existingDebtInUnitDebts)
                unitDebtsList[index] = existingDebtInUnitDebts.copy(
                    amount = cost.tempAmount,
                    paymentFlag = false,
                    dueDate = "" // or keep existing dueDate if preferred
                )
            } else {
                unitDebtsList.add(debt)
            }

        }
    }

    fun addUnitDebtsList(debts: List<Debts>) {
        if (debts.isEmpty()) return

        val unitIdToReplace = debts.first().unitId

        // Remove debts with the same unitId
        unitDebtsList.removeAll { it.unitId == unitIdToReplace }

        // Add new debts
        unitDebtsList.addAll(debts)
    }


    fun addUnpaidDebtListList(debts: List<Debts>) {
        unpaidDebtList.value = emptyList()
        debts.forEach { debt ->
            unpaidDebtList.value = unpaidDebtList.value + debt
        }
    }

    fun addFileList(uploadedFileEntity: UploadedFileEntity) {
        fileList.add(uploadedFileEntity)
    }

    fun loadCosts() {
        viewModelScope.launch(Dispatchers.IO) {
            val costsFromDb = costsDao.getCostsWithNullBuildingId()
            withContext(Dispatchers.Main) {
                val fixedCosts = costsFromDb.filter { cost ->
                    cost.calculateMethod == CalculateMethod.FIXED
                }.map {
                    it.copy(
                        tempAmount = 0.0,
                        period = Period.YEARLY,
                        responsible = Responsible.TENANT // or appropriate value
                    )
                }

                costsList.value = fixedCosts
                val chargeCost = costsFromDb.filter { cost ->
                    cost.calculateMethod == CalculateMethod.AREA
                }.map {
                    it.copy(
                        tempAmount = 0.0,
                        period = Period.MONTHLY,
                        responsible = Responsible.TENANT
                    )
                }
                charge.value = chargeCost
            }
        }
    }


    fun updateCostAmount(cost: Costs, newAmount: Double) {
        costsList.value = costsList.value.filter { it.buildingId == null }.map {
            if (it.id == cost.id) {
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


    fun updateCostTempAmount(newAmount: Double) {
        costsList.value = costsList.value.filter { it.buildingId == null }.map {
            it.copy(tempAmount = newAmount)
        }
    }

    fun updateCostFundFlag(newValue: FundFlag) {
        costsList.value = costsList.value.filter { it.buildingId == null }.map {
            it.copy(fundFlag = newValue)
        }
    }

    fun clearAllCostAmount() {
        costsList.value = costsList.value.map { cost ->
            cost.copy(tempAmount = 0.0) // Reset to 0.0 or your preferred default
        }
    }

    fun clearDebtList() {
        debtsList.clear()
    }

    fun clearUnitDebtList() {
        unitDebtsList.clear()
    }


    fun updateCostPeriod(cost: Costs, newPeriod: Period) {
        costsList.value = costsList.value.filter { it.buildingId == null }.map {
            if (it.id == cost.id) it.copy(period = newPeriod) else it
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

    // Parses a date string "yyyy/MM/dd" into PersianCalendar
    fun parsePersianDate(dateStr: String): PersianCalendar? {
        val parts = dateStr.split("/")
        if (parts.size != 3) return null
        val year = parts[0].toIntOrNull() ?: return null
        val month = parts[1].toIntOrNull() ?: return null
        val day = parts[2].toIntOrNull() ?: return null

        return PersianCalendar().apply {
            setPersianDate(year, month, day) // month is zero-based
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
    val maxDay = calendar.getMaxDaysInMonth() // 31

    fun updateFixedAmountFormat(newValue: String) {
        // Remove non-digit characters before storing
        val cleanValue = newValue.filter { it.isDigit() }
        fixedAmount = cleanValue.persianToEnglishDigits()
        charge.value = charge.value.filter { it.buildingId == null }.map {
            it.copy(tempAmount = fixedAmount.toDoubleOrNull() ?: 0.0)
        }
    }

    fun updateFundFlagCharge(newValue: FundFlag) {
        charge.value = charge.value.filter { it.buildingId == null }.map {
            it.copy(fundFlag = newValue)
        }
    }

    fun updateChargeAmountFormat(newValue: String) {
        // Remove non-digit characters before storing
        val cleanValue = newValue.filter { it.isDigit() }
        chargeAmount = cleanValue
        charge.value = charge.value.filter { it.buildingId == null }.map {
            it.copy(tempAmount = chargeAmount.toDoubleOrNull() ?: 0.0)
        }
    }

    // In your ViewModel
    fun addAuthorization(objectId: Long, fieldId: Long, role: String) {
        viewModelScope.launch {
            // Insert into RoleAuthorizationObjectCrossRef
            // Update authObjects list
        }
    }

    private val _userState = mutableStateListOf<User>()

    private val _authObjects = MutableStateFlow(listOf<AuthorizationObject>())
    val authObjects: StateFlow<List<AuthorizationObject>> = _authObjects

    fun selectRole(role: String) {
        // Load user data and authorizations
        // _userState.update { it.copy(role = role) } // Uncomment and adjust if needed
        loadAuthorizations(role)
    }

    fun getUserRole(mobile: String): String? {
        // API call or database query
        return null // Replace with actual implementation
    }

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

    fun getUsersBuilding(context: Context) {
        Building().fetchBuildings(
            context,
            phone = Preference().getUserMobile(context = context) ?: "",
            userId = Preference().getUserId(context = context).toString(),
            onSuccess = { buildings ->
                // Handle the userList (e.g., update your ViewModel, display in UI)
                //Example:
                //  userViewModel.updateUsers(userList)
                _buildingList.value = buildings
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


}



