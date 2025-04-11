package com.example.delta.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.delta.data.entity.BuildingOwnerCrossRef
import com.example.delta.data.entity.BuildingTenantCrossRef
import com.example.delta.data.entity.BuildingTypes
import com.example.delta.data.entity.BuildingUsages
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.Owners
import com.example.delta.data.entity.Tenants
import com.example.delta.data.model.AppDatabase
import kotlinx.coroutines.launch

class SharedViewModel(application: Application) : AndroidViewModel(application) {

    private val buildingDao = AppDatabase.getDatabase(application).buildingsDao()

    // State for Building Info Page
    var name by mutableStateOf("")
    var phone by mutableStateOf("")
    var email by mutableStateOf("")
    var postCode by mutableStateOf("")
    var street by mutableStateOf("")
    var province by mutableStateOf("")
    var state by mutableStateOf("") // Default state
    var buildingTypeId by mutableIntStateOf(0) // Default state
    var buildingUsageId by mutableIntStateOf(0) // Default state
    // State for Owners Page
    var ownersList by mutableStateOf(listOf<Owners>())
    var tenantsList by mutableStateOf(listOf<Tenants>())

    // These represent the selected items from your dropdowns
    var selectedBuildingTypes by mutableStateOf<BuildingTypes?>(null)
    var selectedBuildingUsages by mutableStateOf<BuildingUsages?>(null)

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
                    val ownerId = buildingDao.insertOwner(owner)
                    buildingDao.insertBuildingOwnerCrossRef(BuildingOwnerCrossRef(buildingId, ownerId))
                }
                // Insert tenants
                tenantsList.forEach { tenant ->
                    val tenantId = buildingDao.insertTenant(tenant)
                    buildingDao.insertBuildingTenantCrossRef(
                        BuildingTenantCrossRef(
                            buildingId,
                            tenantId
                        )
                    )
                }

                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to save building with owners")
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
        ownersList = emptyList()
        tenantsList = emptyList()
        selectedBuildingTypes = null
        selectedBuildingUsages = null
    }

}
