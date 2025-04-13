package com.example.delta.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.delta.data.entity.*
import com.example.delta.data.model.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SharedViewModel(application: Application) : AndroidViewModel(application) {

    private val buildingDao = AppDatabase.getDatabase(application).buildingsDao()
    private val ownersDao = AppDatabase.getDatabase(application).ownersDao()
    private val tenantsDao = AppDatabase.getDatabase(application).tenantDao()
    private val unitsDao = AppDatabase.getDatabase(application).unitsDao()
    private val costsDao = AppDatabase.getDatabase(application).costDao()
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
    // In SharedViewModel
    var sameArea by mutableStateOf(false)
    var numberOfUnits by mutableStateOf("")
    var unitArea by mutableStateOf("")
    var sharedUtilities by mutableStateOf(listOf<String>())
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
    var unitsList = mutableStateListOf<Units>()

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

    init {
        loadOwners()
        loadTenants()
        loadCosts()
        loadBuildingsWithTypesAndUsages()
        loadDefaultCosts()
    }

    private fun loadDefaultCosts() {
        viewModelScope.launch(Dispatchers.IO) {
            val defaultCostsFromDb = costsDao.getCosts().filter { it.buildingId == 0L }
            withContext(Dispatchers.Main) {
                defaultCosts.clear()
                defaultCosts.addAll(defaultCostsFromDb)
                tempCosts.clear()
                tempCosts.addAll(defaultCostsFromDb)
            }
        }
    }

    fun getUnitsForBuilding(buildingId: Long): Flow<List<Units>> = flow {
        val units = unitsDao.getUnitsByBuildingId(buildingId)
        emit(units)
    }.flowOn(Dispatchers.IO)

    fun getOwnersForBuilding(buildingId: Long): Flow<List<Owners>> = flow {
        val owners = ownersDao.getOwnersForBuilding(buildingId)
        emit(owners)
    }.flowOn(Dispatchers.IO)

    suspend fun getBuildingTypeAndUsage(building: Buildings): Pair<String, String> {
        val buildingTypeName = buildingTypesDao.getBuildingTypeName(building.buildingTypeId)
        val buildingUsageName = buildingUsagesDao.getBuildingUsageName(building.buildingUsageId)
        return Pair(buildingTypeName, buildingUsageName)
    }


    fun loadBuildingsWithTypesAndUsages() {
        viewModelScope.launch(Dispatchers.IO) {
            val buildings = buildingDao.getBuildings()
            val buildingTypes = mutableMapOf<Long?, String>()
            val buildingUsages = mutableMapOf<Long?, String>()

            // Fetch building types and usages
            buildings.forEach { building ->
                if (!buildingTypes.containsKey(building.buildingTypeId)) {
                    buildingTypes[building.buildingTypeId] = buildingTypesDao.getBuildingTypeName(building.buildingTypeId)
                    Log.d("building type", buildingTypesDao.getBuildingTypeName(building.buildingTypeId))
                }
                if (!buildingUsages.containsKey(building.buildingUsageId)) {
                    buildingUsages[building.buildingUsageId] = buildingUsagesDao.getBuildingUsageName(building.buildingUsageId)
                    Log.d("building usage", buildingUsagesDao.getBuildingUsageName(building.buildingUsageId))
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


    fun loadTenants() {
        viewModelScope.launch(Dispatchers.IO) {
            val tenantsFromDb = tenantsDao.getTenants()
            withContext(Dispatchers.Main) {
                tenantsList.value = tenantsFromDb
            }
        }
    }


    fun loadOwners() {
        viewModelScope.launch(Dispatchers.IO) {
            val ownersFromDb = ownersDao.getOwners()
            withContext(Dispatchers.Main) {
                ownersList.clear()
                ownersList.addAll(ownersFromDb)
            }
        }
    }


    fun loadUnitsForOwner(ownerId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val unitsForOwner = ownersDao.getUnitsForOwner(ownerId)
            withContext(Dispatchers.Main) {
                unitsList.clear()
                unitsList.addAll(unitsForOwner)
            }
        }
    }



    fun saveOwnerWithUnits(owner: Owners, units: List<Units>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Save owner
                val ownerId = ownersDao.insertOwners(owner)

                // Save units and get their IDs
                val savedUnits = units.map { unit ->
                    val unitId = unitsDao.insertUnit(unit)
                    unit.copy(unitId = unitId)
                }

                // Create cross-references
                savedUnits.forEach { unit ->
                    ownersDao.insertOwnerUnitCrossRef(OwnersUnitsCrossRef(ownerId, unit.unitId))
                }

                // Load owners
                loadOwners()
            } catch (e: Exception) {
                Log.e("SaveError", "Failed to save owner with units: ${e.message}")
            }
        }
    }

    fun saveTenantWithUnit(tenant: Tenants, unit: Units) {
        viewModelScope.launch(Dispatchers.IO) {
            val tenantId = tenantsDao.insertTenants(tenant)
            tenantsDao.insertTenantUnitCrossRef(TenantsUnitsCrossRef(tenantId, unit.unitId, tenant.startDate, tenant.endDate))
        }
    }

    fun saveBuildingWithUnitsAndOwnersAndTenants(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("Insert", "building")
                Log.d("selectedBuildingTypes?.buildingTypeId", selectedBuildingTypes?.buildingTypeId.toString())
                Log.d("selectedBuildingUsages?.buildingUsageId", selectedBuildingUsages?.buildingUsageId.toString())
                var fund = 0.0
                tempCosts.forEach { cost ->
                    Log.d("cost.amount", cost.amount.toString())
                    fund += cost.amount * unitsList.size
                }
                val buildingId = buildingDao.insertBuilding(
                    Buildings(
                        name = name, phone = phone, email = email, postCode = postCode,
                        buildingTypeId = selectedBuildingTypes?.buildingTypeId ?: 0,
                        buildingUsageId = selectedBuildingUsages?.buildingUsageId ?: 0,
                        street = street, province = province, state = state, fund = fund
                    )
                )

                // Save units
                unitsList.forEach { unit ->
                    unit.buildingId = buildingId // Update buildingId for each unit
                    unitsDao.insertUnit(unit)
                }

                // Save owners and their unit relationships
                ownersList.forEach { owner ->
                    val ownerId = ownersDao.insertOwners(owner)
                    val ownerUnits = ownerUnitMap[owner] ?: emptyList()
                    ownerUnits.forEach { unit ->
                        ownersDao.insertOwnerUnitCrossRef(OwnersUnitsCrossRef(ownerId, unit.unitId))
                    }
                }

                // Save costs
                tempCosts.forEach { cost ->
                    costsDao.insertCost(cost.copy(buildingId = buildingId))
                }

                // Save tenants and their unit relationships
                tenantsList.value.forEach { tenant ->
                    val tenantId = tenantsDao.insertTenants(tenant)
                    val tenantUnit = tenantUnitMap[tenant] ?: return@forEach
                    tenantsDao.insertTenantUnitCrossRef(TenantsUnitsCrossRef(tenantId, tenantUnit.unitId, tenant.startDate, tenant.endDate))
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

    // Update these functions to add units to the maps
    fun addOwnerUnits(owner: Owners, units: List<Units>) {
        ownerUnitMap[owner] = units
    }

    fun addTenantUnits(tenant: Tenants, unit: Units) {
        tenantUnitMap[tenant] = unit
    }

    fun addTenant(tenant: Tenants) {
        viewModelScope.launch(Dispatchers.IO) {
            tenantsDao.insertTenants(tenant)
            withContext(Dispatchers.Main) {
                tenantsList.value = tenantsList.value + tenant
            }
        }
    }

    fun addOwner(owner: Owners) {
        viewModelScope.launch(Dispatchers.IO) {
            ownersDao.insertOwners(owner)
            withContext(Dispatchers.Main) {
                ownersList.add(owner)
            }
        }
    }
    fun loadCosts() {
        viewModelScope.launch(Dispatchers.IO) {
            val costsFromDb = costsDao.getCosts()
            withContext(Dispatchers.Main) {
                costsList.value = costsFromDb
            }
        }
    }

    fun updateCostAmount(cost: Costs, newAmount: Double) {
        val index = tempCosts.indexOf(cost)
        if (index != -1) {
            val updatedCost = cost.copy(amount = newAmount)
            tempCosts[index] = updatedCost
        } else {
            val newCost = cost.copy(amount = newAmount)
            tempCosts.add(newCost)
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
        buildingTypeId = 0
        buildingUsageId = 0
        ownersList.clear()
        tenantsList = mutableStateOf(emptyList())
        selectedBuildingTypes = null
        selectedBuildingUsages = null
        unitsList.clear()
        selectedOwnerForUnit = null
    }
}
