package com.example.delta.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.delta.data.entity.BuildingOwnerCrossRef
import com.example.delta.data.entity.BuildingTenantCrossRef
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
    var state by mutableStateOf("")

    // State for Owners Page
    var ownersList by mutableStateOf(listOf<Owners>())
    var tenantsList by mutableStateOf(listOf<Tenants>())

    // Save Building with Owners Relationship
    fun saveBuildingWithOwners() {
        viewModelScope.launch {
            // Insert the building and get its ID
            val buildingId = buildingDao.insertBuilding(
                Buildings(
                    name = name, phone = phone, email = email, postCode = postCode,
                    street = street, province = province, state = state
                )
            )

            // Insert owners and their cross-references
            ownersList.forEach { owner ->
                val ownerId = buildingDao.insertOwner(owner)
                buildingDao.insertBuildingOwnerCrossRef(BuildingOwnerCrossRef(buildingId, ownerId))
            }
            // Insert tenants and their cross-references
            tenantsList.forEach { tenant ->
                val tenantId = buildingDao.insertTenant(tenant)
                buildingDao.insertBuildingTenantCrossRef(
                    BuildingTenantCrossRef(
                        buildingId,
                        tenantId
                    )
                )
            }
        }
    }
}
