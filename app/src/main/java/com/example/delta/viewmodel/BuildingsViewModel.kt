package com.example.delta.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.Earnings
import com.example.delta.data.model.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BuildingsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val earningsDao = database.earningsDao()

    val showEarningsDialog = mutableStateOf(false)
    val showCostDialog = mutableStateOf(false)
    val showUnitsDialog = mutableStateOf(false)

    private val buildingsDao = database.buildingsDao()

    private val _province = MutableStateFlow("تهران")
    val province: StateFlow<String> = _province.asStateFlow()

    private val _state = MutableStateFlow("تهران")
    val state: StateFlow<String> = _state.asStateFlow()


    fun getAllBuildingsList(): List<Buildings> {
        return buildingsDao.getAllBuildingsList()
    }
    private val _selectedBuildingId = MutableStateFlow<Long?>(null)


    fun insertEarnings(earnings: Earnings) = viewModelScope.launch {
        earningsDao.insertEarnings(earnings)
    }

    fun showEarningsDialog(buildingId: Long) {
        _selectedBuildingId.value = buildingId
        showEarningsDialog.value = true
    }

    fun showCostDialog(buildingId: Long) {
        _selectedBuildingId.value = buildingId
        showCostDialog.value = true
    }

    fun showUnitsDialog(buildingId: Long) {
        _selectedBuildingId.value = buildingId
        showUnitsDialog.value = true
    }

    fun hideDialogs() {
        showEarningsDialog.value = false
        showCostDialog.value = false
        showUnitsDialog.value = false
    }

    // Function to get building type name
    fun getBuildingTypeName(buildingTypeId: Long?, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val typeName = buildingsDao.getBuildingTypeName(buildingTypeId)
            onResult(typeName) // Pass the result back to the caller
        }
    }

    // Function to get building usage name
    fun getBuildingUsageName(buildingUsageId: Long?, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val usageName = buildingsDao.getBuildingUsageName(buildingUsageId)
            onResult(usageName) // Pass the result back to the caller
        }
    }


}
