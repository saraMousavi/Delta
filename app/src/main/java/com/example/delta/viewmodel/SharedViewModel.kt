package com.example.delta.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.delta.data.dao.UnitsDao.UnitDangSum
import com.example.delta.data.entity.*
import com.example.delta.data.model.AppDatabase
import com.example.delta.enums.CalculateMethod
import com.example.delta.enums.FundFlag
import com.example.delta.enums.PaymentLevel
import com.example.delta.enums.Period
import com.example.delta.enums.Responsible
import ir.hamsaa.persiandatepicker.util.PersianCalendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private val authorizationDoa = AppDatabase.getDatabase(application).authorizationDao()
    private val buildingTypesDao = AppDatabase.getDatabase(application).buildingTypeDao()
    private val buildingUsagesDao = AppDatabase.getDatabase(application).buildingUsageDao()
    private val uploadedFileDao = AppDatabase.getDatabase(application).uploadedFileDao()
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


    var selectedChargeType by mutableStateOf(listOf<String>())

    var unitsAdded by mutableStateOf(false)
    var buildingTypeId by mutableIntStateOf(0)
    var buildingUsageId by mutableIntStateOf(0)
        // State for Owners Page
        private set
    var ownersList = mutableStateListOf<Owners>()
        //    var ownersUnitsList = mutableStateListOf<OwnersUnitsCrossRef>()
        private set
    var tenantsList = mutableStateOf(listOf<Tenants>())
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



    var automaticCharge by mutableStateOf(false)
    var fillCostsByBuilding by mutableStateOf(false)
    var chargeFundFlagChecked by mutableStateOf(false)
    var rentMortgageFundFlagChecked by mutableStateOf(false)
    var sameCosts by mutableStateOf(true)
    var currentRoleId by mutableStateOf(0L)

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


    fun getDebtsOfCostForUnit(unitId: Long, costId: Long): Flow<Debts?> = flow {
        val costs = debtsDao.getDebtsOfCostForUnit(unitId, costId)
        emit(costs)
    }.flowOn(Dispatchers.IO)

    fun getAllAuthorizationObjects(): Flow<List<AuthorizationObject>> = flow {
        val obj = authorizationDoa.getAllAuthorizationObjects()
        emit(obj)
    }.flowOn(Dispatchers.IO)


    fun getFieldsForAuthorizationObject(objId: Long): Flow<List<AuthorizationField>> = flow {
        val obj = authorizationDoa.getFieldsForObject(objId)
        emit(obj)
    }.flowOn(Dispatchers.IO)

    fun getDebtsOneUnit(unitId: Long): Flow<List<Debts>> = flow {
        val debts = debtsDao.getDebtsOneUnit(unitId)
        emit(debts)
    }.flowOn(Dispatchers.IO)

    // New function to get debts for a specific unit and month
    fun getDebtsForUnitAndMonth(unitId: Long, year: String, month: String): Flow<List<Debts>> =
        flow {
            val debts = debtsDao.getDebtsForUnitAndMonth(unitId, year, month)
            emit(debts)
        }.flowOn(Dispatchers.IO)

    fun getDebtsForOwner(ownerId: Long, year: String, month: String): Flow<List<Debts>> =
        flow {
            val debts = debtsDao.getDebtsForOwner(ownerId, year, month)
            emit(debts)
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

    fun getDebtsForBuilding(buildingId: Long): Flow<List<Debts>> = flow {
        val debts = debtsDao.getDebtsOfBuilding(buildingId)
        emit(debts)
    }.flowOn(Dispatchers.IO)


    fun getAllDebts(): Flow<List<Debts>> = flow {
        val debts = debtsDao.getAllDebts()
        emit(debts)
    }.flowOn(Dispatchers.IO)

    fun sumPaidFundFlagPositive(buildingId: Long): Flow<Double> = flow {
        val sum = debtsDao.sumPaidFundFlagPositive(buildingId)
        Log.d("sum paid", sum.toString())
        emit(sum)
    }.flowOn(Dispatchers.IO)

    fun sumPaidEarnings(buildingId: Long): Flow<Double> = flow {
        val sum = earningsDao.sumPaidEarning(buildingId)

        Log.d("sum unpaid", sum.toString())
        emit(sum)
    }.flowOn(Dispatchers.IO)

    fun sumUnpaidFundFlagNegative(buildingId: Long): Flow<Double> = flow {
        val sum = debtsDao.sumUnpaidFundFlagNegative(buildingId)

        Log.d("sum unpaid", sum.toString())
        emit(sum)
    }.flowOn(Dispatchers.IO)

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
        Log.d("unit count", units.toString())
        emit(units)
    }.flowOn(Dispatchers.IO)

    fun getUnitsForBuilding(buildingId: Long): Flow<List<Units>> = flow {
        val units = unitsDao.getUnitsByBuildingId(buildingId)
        emit(units)
    }.flowOn(Dispatchers.IO)

    fun getOwnersForBuilding(buildingId: Long): Flow<List<Owners>> = flow {
        val owners = ownersDao.getOwnersForBuilding(buildingId)
        emit(owners)
    }.flowOn(Dispatchers.IO)

    fun getOwner(ownerId: Long): Flow<Owners> = flow {
        val owners = ownersDao.getOwnerById(ownerId)
        emit(owners)
    }.flowOn(Dispatchers.IO)


    fun getUnit(unitId: Long): Flow<Units> = flow {
        val unit = unitsDao.getUnit(unitId)
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


    fun getDangSumsForAllUnits(): Flow<List<UnitDangSum>> = flow {
        val owners = unitsDao.getDangSumsForAllUnits()
        emit(owners)
    }.flowOn(Dispatchers.IO)


    fun getUsers(): Flow<List<User>> = flow {
        val user = userDao.getUsers()
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

    fun getTenantsForBuilding(buildingId: Long): Flow<List<Tenants>> = flow {
        val tenants = tenantsDao.getTenantsForBuilding(buildingId)
        emit(tenants)
    }.flowOn(Dispatchers.IO)

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



    fun getAllUnits(): Flow<List<Units>> = flow {
        val units = unitsDao.getUnits()
        emit(units)
    }.flowOn(Dispatchers.IO)

    fun getUnitsForOwners(ownerId: Long): Flow<List<UnitWithDang>> = flow {
        val owners = ownersDao.getUnitsWithDangForOwner(ownerId)
        emit(owners)
    }.flowOn(Dispatchers.IO)


    fun loadBuildingsWithTypesAndUsages() {
        viewModelScope.launch(Dispatchers.IO) {
            val buildings = buildingDao.getBuildings()
            val buildingTypes = mutableMapOf<Long?, String>()
            val buildingUsages = mutableMapOf<Long?, String>()

            // Fetch building types and usages
            buildings.forEach { building ->
                if (!buildingTypes.containsKey(building.buildingTypeId)) {
                    buildingTypes[building.buildingTypeId] =
                        buildingTypesDao.getBuildingTypeName(building.buildingTypeId)
                }
                if (!buildingUsages.containsKey(building.buildingUsageId)) {
                    buildingUsages[building.buildingUsageId] =
                        buildingUsagesDao.getBuildingUsageName(building.buildingUsageId)
                }
            }

            // Map building types and usages to buildings
            val buildingsWithTypesAndUsages = buildings.map { building ->
                BuildingWithTypesAndUsages(
                    building = building,
                    buildingTypeName = buildingTypes[building.buildingTypeId] ?: "",
                    buildingUsageName = buildingUsages[building.buildingUsageId] ?: ""
                )
            }

            withContext(Dispatchers.Main) {
                // Update your UI with the new data
                buildingsWithTypesAndUsagesList.value = buildingsWithTypesAndUsages
            }
        }
    }



    val buildingsWithTypesAndUsagesList = mutableStateOf(listOf<BuildingWithTypesAndUsages>())
    fun insertRoleAuthorizationObjectCrossRef(roleId: Long, objectId: Long, permissionLevel: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val crossRef = RoleAuthorizationObjectCrossRef(
                roleId = roleId,
                objectId = objectId,
                permissionLevel = permissionLevel
            )
            authorizationDoa.insertRoleAuthorizationObjectCrossRef(crossRef)
        }

    }

    fun insertUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            userDao.insertUser(user)
        }

    }

    fun insertDebtPerNewCost(
        buildingId: Long,
        amount: String,
        name: String,
        period: Period,
        dueDate: String,
        paymentLevel: PaymentLevel,
        fundFlag: FundFlag,
        responsible: Responsible,
        selectedUnitIds: List<Long> = emptyList(),
        selectedOwnerIds: List<Long> = emptyList()
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val parsedAmount = amount.persianToEnglishDigits().toDoubleOrNull() ?: 0.0

            // Check if cost with the same name exists for this building
            val existingCost = costsDao.getCostByBuildingIdAndName(buildingId, name)

            val costId: Long = if (existingCost != null) {
                // Update existing cost
                val updatedCost = existingCost.copy(
                    tempAmount = parsedAmount,
                    period = period,
                    paymentLevel = paymentLevel,
                    responsible = responsible,
                    fundFlag = fundFlag
                )
                costsDao.updateCost(updatedCost)
                updatedCost.id
            } else {
                // Insert new cost
                val newCost = Costs(
                    buildingId = buildingId,
                    costName = name,
                    tempAmount = parsedAmount,
                    period = period,
                    calculateMethod = CalculateMethod.FIXED,
                    paymentLevel = paymentLevel,
                    responsible = responsible,
                    fundFlag = fundFlag
                )
                costsDao.insertCost(newCost)
            }

            // Get units depending on responsible and selected units
            val units = when {
                responsible == Responsible.TENANT && selectedUnitIds.isNotEmpty() -> {
                    unitsDao.getUnitsByIds(selectedUnitIds)
                }

                responsible == Responsible.OWNER && selectedOwnerIds.isNotEmpty() -> {
                    unitsDao.getUnitsByOwnerIds(selectedOwnerIds)
                }

                else -> {
                    unitsDao.getUnitsByBuildingId(buildingId)
                }
            }

            var newDueDate = ""
            Log.d("period", period.toString())
            Log.d("Period.NONE", Period.NONE.toString())
            if (period == Period.NONE) {
                newDueDate = dueDate
                Log.d("newDueDate1", newDueDate)
            } else {
                //@TODO loop of period
                newDueDate = getNextMonthSameDaySafe().persianShortDate
                Log.d("newDueDate2", newDueDate)
            }
            Log.d("selectedOwnerIds", selectedOwnerIds.toString())

            Log.d("units", units.toString())

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
                    paymentFlag = false
                )
                debtsDao.insertDebt(debt)
            }
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
            Log.d("amount", amount.toString())
            val parsedAmount = amount.toString().persianToEnglishDigits().toDoubleOrNull() ?: 0.0
            Log.d("parsedAmount", parsedAmount.toString())

            var units = unitsDao.getUnitsByBuildingId(buildingId)
            // Calculate debt per unit
            val numberOfUnits = units.size
            Log.d("numberOfUnits", numberOfUnits.toString())
            val amountPerUnit = if (numberOfUnits > 0) parsedAmount / numberOfUnits else 0.0
            Log.d("amountPerUnit", amountPerUnit.toString())
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

            Log.d("new cost", cost.toString())
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
                Log.d("new debt", debt.toString())
                debtsDao.insertDebt(debt)
            }
        }
    }


    fun saveOwnerWithUnits(owner: Owners, ownerUnits: List<OwnersUnitsCrossRef>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Insert owner and get the new ownerId
                val newOwnerId = ownersDao.insertOwners(owner)
                Log.d("newOwnerId", newOwnerId.toString())

                // For each OwnersUnitsCrossRef, insert the cross-reference with the correct dang value
                Log.d("ownerUnits", ownerUnits.toString())

                ownerUnits.forEach { oUnit ->
                    // Make sure dang is clamped between 0.0 and 6.0
//                    val clampedDang = oUnit.dang.coerceIn(0.0, 6.0)
                    if (oUnit.dang != 0.0) {
                        ownersDao.insertOwnerUnitCrossRef(
                            OwnersUnitsCrossRef(
                                ownerId = newOwnerId,
                                unitId = oUnit.unitId,
                                dang = oUnit.dang
                            )
                        )
                    }
                }

                val updatedOwner = owner.copy(ownerId = newOwnerId)
                ownersList.add(updatedOwner)
                addOwnerUnits(owner, ownerUnits)
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
                    tenantsList.value = tenantsList.value + updatedTenant
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
                withContext(Dispatchers.Main) { Log.d("Unknown error", e.message.toString()) }
            }

        }
    }

    @SuppressLint("DefaultLocale")
    @RequiresApi(Build.VERSION_CODES.O)
    fun saveBuildingWithUnitsAndOwnersAndTenants(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        tenantsUnitsCrossRef: List<TenantsUnitsCrossRef>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Insert Building
                val buildingId = buildingDao.insertBuilding(
                    Buildings(
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
                        utilities = sharedUtilities
                    )
                )

                fileList.forEach { file ->
                    var fileID = uploadedFileDao.insertUploadedFile(file)
                    buildingDao.insertCrossRef(BuildingUploadedFileCrossRef(buildingId = buildingId, fileId = fileID))
                }

                Log.d("buildingIdd", buildingId.toString())

                // Prepare Charges
                val updatedCharges = mutableListOf<Costs>()
                val updatedCosts = mutableListOf<Costs>()
                Log.d("costsList.value 1", costsList.value.toString())
                // Prepare Costs

                if (sameCosts) {
                    costsList.value.filter { it.buildingId == null }.forEach { cost ->
                        val newCost = cost.copy(buildingId = buildingId, id = 0L)
                        Log.d("newCost", newCost.toString())
                        costsList.value = costsList.value + newCost
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
                                costsList.value = costsList.value + newCost
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
                    Log.d("new cost", cost.toString())
                    var insertedId = costsDao.insertCost(cost)

                    updatedCosts.add(cost.copy(id = insertedId))
                }


                Log.d("charge.value", charge.value.toString())
                charge.value.filter { it.buildingId == null }.forEach { cost ->
                    val newCost = cost.copy(buildingId = buildingId, id = 0L)
                    Log.d("newCost", newCost.toString())
                    charge.value = charge.value + newCost
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

                Log.d("costs", costsDao.getCosts().toString())

                // Prepare units
                unitsList.forEach { unit ->
                    val unitWithBuildingId = unit.copy(buildingId = buildingId)
                    Log.d("unitWithBuildingId", unitWithBuildingId.toString())
                    Log.d("c buildingId", buildingId.toString())
                    val insertedUnit = unitsDao.insertUnit(unitWithBuildingId)
                    Log.d("units", unitsDao.getUnits().toString())

                    // **Loop Through Both costsList.value AND charge.value**
                    Log.d("costsList.value", costsList.value.toString())
                    Log.d("charge.value", charge.value.toString())
                    (updatedCosts + updatedCharges).filter { it.buildingId == buildingId }
                        .forEach { cost ->

                            Log.d("automaticCharge ", automaticCharge.toString())
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

                                else -> cost.tempAmount.toString().persianToEnglishDigits()
                                    .toDoubleOrNull()
                                    ?: 0.0 //If not from charge get from cost amount
                            }

                            Log.d("amount", amount.toString())

                            // Retrieve Tenant information
                            val tenant = tenantsList.value.firstOrNull { tenant ->
                                val tenantUnit = tenantUnitMap[tenant]
                                tenantUnit?.unitId == insertedUnit
                            }
                            Log.d("tenant", tenant.toString())
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
                                                it.persianMonth + 1,
                                                it.persianDay
                                            )
                                        } ?: ""

                                        // Insert debt with dueDate
                                        Log.d("insertedUnit", insertedUnit.toString())
                                        Log.d("cost.id", cost.id.toString())
                                        Log.d("buildingIdd", buildingId.toString())
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
                                        Log.d("debt", debtsDao.getAllDebts().toString())

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
                                    dueDate = getNextMonthSameDaySafe(PersianCalendar()).persianShortDate,
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
                    ownersDao.insertOwnerWithBuild(OwnerWithBuildings(ownerId, buildingId))
                    ownerUnits.forEach { unit ->
                        // Get the dang value for this owner-unit pair, default to 0.0 if not present
                        ownersDao.insertOwnerUnitCrossRef(
                            OwnersUnitsCrossRef(
                                ownerId = ownerId,
                                unitId = unit.unitId,
                                dang = unit.dang
                            )
                        )
                    }
                }

                // Save Tenants
                tenantsList.value.forEach { tenant ->
                    val tenantId = tenantsDao.insertTenants(tenant)
                    val tenantUnit: Units? = tenantUnitMap[tenant]
                    if (tenantUnit == null) {
                        return@forEach
                    }
                    tenantsDao.insertTenantUnitCrossRef(
                        TenantsUnitsCrossRef(
                            tenantId,
                            tenantUnit.unitId,
                            tenant.startDate,
                            tenant.endDate,
                            tenant.status
                        )
                    )
                }

                withContext(Dispatchers.IO) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("Insert Building", e.message.toString())
                withContext(Dispatchers.Main) {
                    onError("Failed to save building: ${e.message}")
                }
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


    fun getUnitForTenant(tenant: Tenants): Units? {
        return tenantUnitMap[tenant]
    }

    fun getUnitForTenant(tenantId: Long): Flow<Units> = flow {
        val tenant = tenantsDao.getUnitForTenant(tenantId)
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
                tenantsList.value.map {
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
                    tenantsList.value =
                        tenantsList.value.filter { it.tenantId != tenant.tenantId }
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
    fun addOwnerUnits(owner: Owners, ownerUnits: List<OwnersUnitsCrossRef>) {
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
            Log.d("temp debt", debt.toString())

        }
    }

    fun addUnitDebtsList(debts: List<Debts>) {
        unitDebtsList.clear()
        debts.forEach { debt ->
            unitDebtsList.add(debt)
        }
    }

    fun addUnpaidDebtListList(debts: List<Debts>) {
        unpaidDebtList.value = emptyList()
        debts.forEach { debt ->
            unpaidDebtList.value = unpaidDebtList.value + debt
        }
    }

    fun addFileList(uploadedFileEntity : UploadedFileEntity){
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
        tenantsList.value = emptyList()
        costsList.value = emptyList()
        charge.value = emptyList()
        loadCosts() // Reload the costs
        selectedBuildingTypes = null
        selectedBuildingUsages = null
        unitsList.clear()
        fileList.clear()
        Log.d("unitsList.clear()", "yes")
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
        Log.d("inputDate", "${date.persianYear}/${date.persianMonth + 1}/${date.persianDay}")

        val newYear = date.persianYear + 1
        val newMonth = date.persianMonth // zero-based month

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

        Log.d("resultDate", "${result.persianYear}/${result.persianMonth + 1}/${result.persianDay}")
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
        Log.d("chargeAmount", chargeAmount)
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

    fun getBuildingFiles(buildingId: Long): Flow<List<UploadedFileEntity>> = flow {
        val obj = uploadedFileDao.getFileUrlsForBuilding(buildingId)
        emit(obj)
    }.flowOn(Dispatchers.IO)

}



