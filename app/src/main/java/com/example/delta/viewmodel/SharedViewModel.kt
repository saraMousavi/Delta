package com.example.delta.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.delta.R
import com.example.delta.data.entity.*
import com.example.delta.data.model.AppDatabase
import ir.hamsaa.persiandatepicker.util.PersianCalendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.time.LocalDate
import kotlin.contracts.contract

class SharedViewModel(application: Application) : AndroidViewModel(application) {

    private val buildingDao = AppDatabase.getDatabase(application).buildingsDao()
    private val ownersDao = AppDatabase.getDatabase(application).ownersDao()
    private val tenantsDao = AppDatabase.getDatabase(application).tenantDao()
    private val unitsDao = AppDatabase.getDatabase(application).unitsDao()
    private val costsDao = AppDatabase.getDatabase(application).costDao()
    private val debtsDao = AppDatabase.getDatabase(application).debtsDao()
    private val buildingTypesDao = AppDatabase.getDatabase(application).buildingTypeDao()
    private val buildingUsagesDao = AppDatabase.getDatabase(application).buildingUsageDao()

    // State for Building Info Page
    var name by mutableStateOf("")
    var phone by mutableStateOf("")
    var email by mutableStateOf("")
    var postCode by mutableStateOf("")
    var street by mutableStateOf("")
    var province by mutableStateOf("")
    var state by mutableStateOf("")
    var tempAmount by mutableStateOf("")
    var sameArea by mutableStateOf(false)
    var numberOfUnits by mutableStateOf("")
    var unitArea by mutableStateOf("")
    var sharedUtilities by mutableStateOf(listOf<String>())

    var selectedChargeType by mutableStateOf(listOf<String>())
    var unitsAdded by mutableStateOf(false)
    var buildingTypeId by mutableIntStateOf(0)
    var buildingUsageId by mutableIntStateOf(0)
        // State for Owners Page
        private set
    var ownersList = mutableStateListOf<Owners>()
        private set
    var tenantsList = mutableStateOf(listOf<Tenants>())
        private set
    var costsList = mutableStateOf(listOf<Costs>())
    var charge = mutableStateOf(listOf<Costs>())
    var unitsList = mutableStateListOf<Units>()
    var newOwnerId: Long by mutableLongStateOf(0L)
    var newTenantId: Long by mutableLongStateOf(0L)
    var selectedOwnerForUnit by mutableStateOf<Owners?>(null)

    // These represent the selected items from your dropdowns
    var selectedBuildingTypes by mutableStateOf<BuildingTypes?>(null)
    var selectedBuildingUsages by mutableStateOf<BuildingUsages?>(null)

    // Unit selection state
    var selectedUnits = mutableStateListOf<Units>()

    // Maps to store unit associations for owners and tenants
    val ownerUnitMap = mutableMapOf<Owners, List<Units>>()
    val tenantUnitMap = mutableMapOf<Tenants, Units>()

    //        private set
    var defaultCosts = mutableStateListOf<Costs>()
        private set

    //Temporary costs list to store updated amounts
    var tempCosts = mutableStateListOf<Costs>()

    var automaticCharge by mutableStateOf(false)
    var sameCosts by mutableStateOf(true)
    var fixedAmount by mutableStateOf("")
        private set // Prevent external direct modification


    var chargeAmount by mutableStateOf("")

    fun updateFixedAmount(newValue: String) {
        Log.d("newValue fixedAmount", newValue)
        fixedAmount = newValue
    }

    // Options Lists
    val periods = listOf("هفته", "ماه", "سال")
    val amountUnitOptions = listOf("هزار تومان", "میلیون تومان")


    init {
//        loadOwners()
//        loadTenants()
        loadCosts()
        loadBuildingsWithTypesAndUsages()
//        loadDefaultCosts()
    }


    fun getUnitsForBuilding(buildingId: Long): Flow<List<Units>> = flow {
        val units = unitsDao.getUnitsByBuildingId(buildingId)
        emit(units)
    }.flowOn(Dispatchers.IO)

    fun getOwnersForBuilding(buildingId: Long): Flow<List<Owners>> = flow {
        val owners = ownersDao.getOwnersForBuilding(buildingId)
        emit(owners)
    }.flowOn(Dispatchers.IO)

    fun getTenantsForBuilding(buildingId: Long): Flow<List<Tenants>> = flow {
        val tenants = tenantsDao.getTenantsForBuilding(buildingId)
        emit(tenants)
    }.flowOn(Dispatchers.IO)

    fun getUnitsForOwners(ownerId: Long): Flow<List<Units>> = flow {
        val owners = ownersDao.getUnitsForOwner(ownerId)
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


    fun saveOwnerWithUnits(owner: Owners, units: List<Units>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Save owner
                 newOwnerId = ownersDao.insertOwners(owner)

                // Save units and get their IDs
                val savedUnits = units.map { unit ->
                    val unitId = unitsDao.insertUnit(unit)
                    unit.copy(unitId = unitId)
                }

                // Create cross-references
                savedUnits.forEach { unit ->
                    ownersDao.insertOwnerUnitCrossRef(OwnersUnitsCrossRef(newOwnerId, unit.unitId))
                }

                // Load owners
//                loadOwners()
            } catch (e: Exception) {
                Log.e("SaveError", "Failed to save owner with units: ${e.message}")
            }
        }
    }

    fun saveTenantWithUnit(tenant: Tenants, unit: Units) {
        viewModelScope.launch(Dispatchers.IO) {
            try { // Ensure in IO thread
//                newTenantId = tenantsDao.insertTenants(tenant)
                val tenantCrossRef = TenantsUnitsCrossRef(
                    newTenantId,
                    unit.unitId,
                    tenant.startDate,
                    tenant.endDate,
                    tenant.status
                )
                tenantsDao.insertTenantUnitCrossRef(tenantCrossRef)

                // Retrieve active relationship immediately
                val relationship =
                    tenantsDao.getActiveTenantUnitRelationships(unit.unitId, "فعال")
                Log.d("relation", relationship.toString())

            } catch (e: Exception) {
                withContext(Dispatchers.Main) { Log.d("Unknown error", e.message.toString()) }
            }

        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun saveBuildingWithUnitsAndOwnersAndTenants(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        tenantsUnitsCrossRef: List<TenantsUnitsCrossRef>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var fund = 0.0
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
                        fund = fund,
                        utilities = sharedUtilities
                    )
                )

                // Save units
                unitsList.forEach { unit ->
                    unit.buildingId = buildingId
                    val insertedUnit = unitsDao.insertUnit(unit)
                    //create deep copy
                    if (sameCosts) {
                        val costCopies = costsList.value.map { it.copy(buildingId = buildingId) }
                        costCopies.forEach { cost ->
                            costsDao.insertCost(cost).also { costId ->
                                insertDebtPerCostForUnit(insertedUnit, cost, costId)
                            }
                        }
                    }

                    val chargeCopies = charge.value.map { it.copy(buildingId = buildingId) }
                    chargeCopies.forEach { cost ->
                        costsDao.insertCost(cost.copy(buildingId = buildingId)).also { costId ->
                            insertDebtPerChargeForUnit(
                                insertedUnit,
                                cost,
                                tenantsUnitsCrossRef,
                                costId
                            )
                        }
                    }
                }


                // Save owners and their unit relationships
                ownersList.forEach { owner ->
                    val ownerId = ownersDao.insertOwners(owner)
                    val ownerUnits = ownerUnitMap[owner] ?: emptyList()
                    ownersDao.insertOwnerWithBuild(OwnerWithBuildings(ownerId, buildingId))
                    ownerUnits.forEach { unit ->
                        ownersDao.insertOwnerUnitCrossRef(OwnersUnitsCrossRef(ownerId, unit.unitId))
                    }
                }


                // Save tenants and their unit relationships
                tenantsList.value.forEach { tenant ->
                    val tenantId = tenantsDao.insertTenants(tenant)
                    val tenantUnit = tenantUnitMap[tenant] ?: return@forEach
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

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError("Failed to save building: ${e.message}")
                }
            }
        }
    }

    //function to insert and adding try and catch to get log if there are error
    @RequiresApi(Build.VERSION_CODES.O)
    fun insertDebtPerCostForUnit(insertedUnit: Long, cost: Costs, costId: Long) {
        try {
            val nextMonthDate = getNextMonthSameDaySafe().persianShortDate
            // Usage
            val amount = cost.tempAmount.toString().persianToEnglishDigits().toDoubleOrNull() ?: 0.0
            val debt = Debts(
                unitId = insertedUnit,
                costId = costId, // Foreign key referencing Costs
                description = cost.costName, // Description of the debt
                dueDate = nextMonthDate.toString(), // Due date of the debt
                amount = amount,
                paymentFlag = false // Indicates whether the debt has been paid
            )
            Log.d("debt", debt.toString())
            viewModelScope.launch(Dispatchers.IO) {
                debtsDao.insertDebt(debt)
            }

        } catch (e: Exception) {
            //getting the insert exception and log
            Log.e("insertdebt", "error insert ${e.message}")
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


    fun updateOwnerWithUnits(owner: Owners, units: List<Units>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Update Owner
                ownersDao.updateOwner(owner)

                // Delete Existing Relationships
                ownersDao.deleteOwnersWithUnits(owner.ownerId)

                // Insert New Relationships
                Log.d("units for rel", units.toString())
                units.forEach { unit ->
                    ownersDao.insertOwnerUnitCrossRef(
                        OwnersUnitsCrossRef(
                            ownerId = owner.ownerId,
                            unitId = unit.unitId
                        )
                    )
                }

                // Update StateFlow
                withContext(Dispatchers.Main) {
                    ownersList.replaceAll { if (it.ownerId == owner.ownerId) owner else it }
                    Log.d("new ownersList", ownersList.toString())
                }

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
            Log.d("tenant for update", tenant.toString())
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
                    tenantsList.value = tenantsList.value.filter { it.tenantId != tenant.tenantId }
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
    fun addOwnerUnits(owner: Owners, units: List<Units>) {
        ownerUnitMap[owner] = units
    }

    fun addTenantUnits(tenant: Tenants, unit: Units) {
        tenantUnitMap[tenant] = unit
    }

    fun addTenant(tenant: Tenants) {
        viewModelScope.launch(Dispatchers.IO) {
            newTenantId = tenantsDao.insertTenants(tenant)
            Log.d("newTenantId", newTenantId.toString())
            val updatedTenant = tenant.copy(tenantId = newTenantId)
            tenantsList.value = tenantsList.value + updatedTenant
            Log.d("tenantsList adtenant", tenantsList.value.toString())
        }
    }

    // In SharedViewModel
    fun addOwner(owner: Owners) {
        viewModelScope.launch(Dispatchers.IO) {
             ownersDao.insertOwners(owner) // Get the generated ID

            // Update the owner with the new ID
            val updatedOwner = owner.copy(ownerId = newOwnerId)
            Log.d("updatedOwner", updatedOwner.toString())

            withContext(Dispatchers.Main) {
                Log.d("owner save", updatedOwner.toString())
                ownersList.add(updatedOwner)
                // Log all owners in the list for debugging
                ownersList.forEach { existingOwner ->
                    Log.d("owners in list", existingOwner.toString())
                }
            }
        }
    }


    fun loadCosts() {
        viewModelScope.launch(Dispatchers.IO) {
            val costsFromDb = costsDao.getCosts()
            withContext(Dispatchers.Main) {
                val fixedCosts = costsFromDb.filter { cost ->
                    cost.calculateMethod.any { method ->
                        method.equals("ثابت", ignoreCase = true)
                    }
                }.map {
                    it.copy(
                        tempAmount = 0.0,
                        period = mutableListOf("سال"),
                        amountUnit = mutableListOf("میلیون تومان"),
                        responsible = mutableListOf("")
                    )
                }
                costsList.value = fixedCosts
                val chargeCost = costsFromDb.filter { cost ->
                    cost.calculateMethod.any { method ->
                        method.equals("متراژ", ignoreCase = true)
                    }
                }.map {
                    it.copy(
                        tempAmount = 0.0,
                        period = mutableListOf("ماه"),
                        amountUnit = mutableListOf("میلیون تومان"),
                        responsible = mutableListOf("")
                    )
                }
                charge.value = chargeCost
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun insertDebtPerChargeForUnit(
        insertedUnit: Long,
        cost: Costs,
        tenantsUnitsCrossRef: List<TenantsUnitsCrossRef>,
        costId: Long
    ) {
        try {
            // Determine amount
            val amount = when {
                automaticCharge -> {
                    when {
                        selectedChargeType.contains("متراژ") -> {
                            val area = unitsDao.getUnit(insertedUnit).area
                            val result = area.toInt() * (chargeAmount.persianToEnglishDigits()
                                .toDoubleOrNull() ?: 0.0)
                            Log.d("AREA_CALC", "Result: $result")
                            result // Explicit return
                        }

                        selectedChargeType.contains("نفرات") -> {
                            try {

                                // Find the relationship for the current unit
                                val relationship = tenantsUnitsCrossRef.firstOrNull {
                                    it.unitId == insertedUnit && it.status == "فعال"
                                }

                                if (relationship != null) {
                                    val tenant = tenantsDao.getTenant(relationship.tenantId)

                                    val result = tenant.numberOfTenants.toInt() *
                                            (chargeAmount.persianToEnglishDigits().toDoubleOrNull()
                                                ?: 0.0)
                                    result
                                } else {
                                    0.0
                                }
                            } catch (e: Exception) {
                                Log.e("insertdebt", "error insert ${e.message}")
                                0.0
                            }
                        }


                        else -> 0.0
                    }
                }

                else -> fixedAmount.persianToEnglishDigits().toDoubleOrNull() ?: 0.0
            }

            val englishAmount = amount
            Log.d("englishAmount", englishAmount.toString())
            // Usage in picker
            val nextMonthDate = getNextMonthSameDaySafe().persianShortDate
            Log.d("nextMonthDate", nextMonthDate)
//            setInitDate(nextMonthDate.persianYear, nextMonthDate.persianMonth, nextMonthDate.persianDay)
            val debt = Debts(
                unitId = insertedUnit,
                costId = costId,
                description = cost.costName,
                dueDate = nextMonthDate.toString(),
                amount = englishAmount.toString().toDouble(),
                paymentFlag = true
            )
            Log.d("debt in charge", debt.toString())
            viewModelScope.launch(Dispatchers.IO) {
                debtsDao.insertDebt(debt)
            }
        } catch (e: Exception) {
            Log.e("insertDebt", "Error: ${e.message}")
        }
    }


    // In SharedViewModel
    fun updateCostAmount(cost: Costs, newAmount: Double) {
        costsList.value = costsList.value.map {
            if (it.id == cost.id) {
                it.copy(tempAmount = newAmount)
            } else it
        }
    }


    fun updateCostPeriod(cost: Costs, newPeriod: String) {
        costsList.value = costsList.value.map {
            if (it.id == cost.id) it.copy(period = listOf(newPeriod)) else it
        }
    }

    fun updateCostAmountMoney(cost: Costs, newAmountMoney: String) {
        costsList.value = costsList.value.map {
            if (it.id == cost.id) it.copy(amountUnit = listOf(newAmountMoney)) else it
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
        tenantsList.value = emptyList()
        costsList.value = emptyList()
        charge.value = emptyList()
        loadCosts() // Reload the costs
        selectedBuildingTypes = null
        selectedBuildingUsages = null
        unitsList.clear()
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

}

