package com.example.delta

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.delta.data.entity.BuildingType
import com.example.delta.data.entity.BuildingWithCosts
import com.example.delta.data.entity.BuildingWithIncomes
import kotlinx.coroutines.launch
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.Cost
import com.example.delta.data.entity.Income
import com.example.delta.data.model.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class BuildingsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val incomesDao = database.incomeDao()
    private val costsDao = database.costDao()

    val showIncomeDialog = mutableStateOf(false)
    val showCostDialog = mutableStateOf(false)

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

//    fun getIncomesForBuilding(buildingId: Long): Flow<List<Income>> {
//        return incomesDao.getIncomesForBuilding(buildingId)
//    }

    // Current selected building for relationships
    private val _selectedBuildingId = MutableStateFlow<Long?>(null)

    val buildingWithIncomes: Flow<BuildingWithIncomes?> = _selectedBuildingId.flatMapLatest { id ->
        id?.let { buildingsDao.getBuildingWithIncomes(it) } ?: flowOf(null)
    }

    val buildingWithCosts: Flow<BuildingWithCosts?> = _selectedBuildingId.flatMapLatest { id ->
        id?.let { buildingsDao.getBuildingWithCosts(it) } ?: flowOf(null)
    }

    fun selectBuilding(buildingId: Long) {
        _selectedBuildingId.value = buildingId
    }

    fun insertIncome(income: Income) = viewModelScope.launch {
        incomesDao.insertIncome(income)
    }

    fun insertCost(cost: Cost) = viewModelScope.launch {
        costsDao.insertCost(cost)
    }

    fun showIncomeDialog(buildingId: Long) {
        _selectedBuildingId.value = buildingId
        showIncomeDialog.value = true
    }

    fun showCostDialog(buildingId: Long) {
        _selectedBuildingId.value = buildingId
        showCostDialog.value = true
    }

    fun hideDialogs() {
        showIncomeDialog.value = false
        showCostDialog.value = false
    }


}
