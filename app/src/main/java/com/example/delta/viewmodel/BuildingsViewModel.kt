package com.example.delta.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.delta.data.dao.CostDao
import kotlinx.coroutines.launch
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Debts
import com.example.delta.data.entity.Earnings
import com.example.delta.data.entity.Units
import com.example.delta.data.model.AppDatabase
import com.example.delta.init.IranianLocations
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class BuildingsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val earningsDao = database.earningsDao()
    private val costsDao = database.costDao()
    private val unitsDao = database.unitsDao()
    private val debtsDao = database.debtsDao()

    val showEarningsDialog = mutableStateOf(false)
    val showCostDialog = mutableStateOf(false)
    val showUnitsDialog = mutableStateOf(false)

    private val buildingsDao = database.buildingsDao()

    private val _province = MutableStateFlow("تهران")
    val province: StateFlow<String> = _province.asStateFlow()

    private val _state = MutableStateFlow("مرکزی")
    val state: StateFlow<String> = _state.asStateFlow()

    val availableStates: StateFlow<List<String>> = _province
        .map { province ->
            IranianLocations.provinces[province] ?: emptyList()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = IranianLocations.provinces["تهران"] ?: emptyList()
        )

    fun onProvinceSelected(province: String) {
        _province.value = province
        _state.value = IranianLocations.provinces[province]?.firstOrNull() ?: ""
    }


    fun onStateSelected(state: String) {
        _state.value = state
        Log.d("StateSelected", "Selected State: $state")
    }

    fun insertBuildings(buildings: Buildings) {
        viewModelScope.launch {
            buildingsDao.insertBuilding(buildings)
        }
    }

    fun deleteBuildings(buildings: Buildings) = viewModelScope.launch {
        buildingsDao.deleteBuildings(buildings)
    }

    fun getAllBuildings(): Flow<List<Buildings>> {
        return buildingsDao.getAllBuildings()
    }

    private val _selectedBuildingId = MutableStateFlow<Long?>(null)

    fun selectBuilding(buildingId: Long) {
        _selectedBuildingId.value = buildingId
    }

    fun insertEarnings(earnings: Earnings) = viewModelScope.launch {
        earningsDao.insertEarnings(earnings)
    }


    suspend fun insertCost(cost: Costs): Long {
        return costsDao.insertCost(cost)
    }


    fun insertDebt(debt: Debts) = viewModelScope.launch {
        debtsDao.insertDebt(debt)
    }


    suspend fun insertUnits(units: Units) : Long {
        return unitsDao.insertUnits(units)
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


}
