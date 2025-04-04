package com.example.delta.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Debts
import com.example.delta.data.entity.Earnings
import com.example.delta.data.entity.Units
import com.example.delta.data.model.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

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

    fun insertBuildings(buildings: Buildings) {
        viewModelScope.launch {
            buildingsDao.insertBuildings(buildings)
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



    fun insertCost(costs: Costs) = viewModelScope.launch {
        costsDao.insertCost(costs)
    }

    fun insertDebt(debt: Debts) = viewModelScope.launch {
        debtsDao.insertDebt(debt)
    }


    fun insertUnits(units: Units) = viewModelScope.launch {
        unitsDao.insertUnits(units)
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
