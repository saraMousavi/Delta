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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SharedViewModel(application: Application) : AndroidViewModel(application) {

    private val buildingDao = AppDatabase.getDatabase(application).buildingsDao()
    private val ownersDao = AppDatabase.getDatabase(application).ownersDao()
    private val tenantsDao = AppDatabase.getDatabase(application).tenantDao()
    private val unitsDao = AppDatabase.getDatabase(application).unitsDao()

    // State for Building Info Page
    var name by mutableStateOf("")
    var phone by mutableStateOf("")
    var email by mutableStateOf("")
    var postCode by mutableStateOf("")
    var street by mutableStateOf("")
    var province by mutableStateOf("")
    var state by mutableStateOf("")
    var buildingTypeId by mutableIntStateOf(0)
    var buildingUsageId by mutableIntStateOf(0)

    // State for Owners Page
    var ownersList = mutableStateListOf<Owners>()
        private set
    var tenantsList = mutableStateOf(listOf<Tenants>())
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

    init {
        loadOwners()
        loadTenants()
    }

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

    // Add callbacks to saveBuildingWithOwners function
    fun saveBuildingWithOwners(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                // Insert the building and get its ID
                val buildingId = buildingDao.insertBuilding(
                    Buildings(
                        name = name, phone = phone, email = email, postCode = postCode,
                        buildingTypeId = selectedBuildingTypes?.buildingTypeId ?: 0,
                        buildingUsageId = selectedBuildingUsages?.buildingUsageId ?: 0,
                        street = street, province = province, state = state
                    )
                )
                // Insert owners
                ownersList.forEach { owner ->
                    val ownerId = ownersDao.insertOwners(owner)
                    buildingDao.insertBuildingOwnerCrossRef(BuildingOwnerCrossRef(buildingId, ownerId))
                }
                // Insert tenants
                tenantsList.value.forEach { tenant ->
                    val tenantId = tenantsDao.insertTenants(tenant)
                    buildingDao.insertBuildingTenantCrossRef(
                        BuildingTenantCrossRef(
                            buildingId,
                            tenantId
                        )
                    )
                }

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to save building with owners")
                }
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
                val buildingId = buildingDao.insertBuilding(
                    Buildings(
                        name = name, phone = phone, email = email, postCode = postCode,
                        buildingTypeId = selectedBuildingTypes?.buildingTypeId ?: 0,
                        buildingUsageId = selectedBuildingUsages?.buildingUsageId ?: 0,
                        street = street, province = province, state = state
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
