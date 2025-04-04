package com.example.delta.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.delta.data.entity.Costs
import com.example.delta.data.model.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class CostViewModel (application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val costsDao = database.costDao()


    fun insertCost(costs: Costs) = viewModelScope.launch {
        costsDao.insertCost(costs)
    }

    fun deleteCost(costs: Costs) = viewModelScope.launch {
        costsDao.deleteCost(costs)
    }

    fun getAllCost(): Flow<List<Costs>> {
        return costsDao.getAllCost()
    }

    fun getCost(costId : Long): Costs {
        return costsDao.getCost(costId)
    }

    fun getAllMenuCost(): Flow<List<Costs>> {
        return costsDao.getAllMenuCost()
    }

    fun getLastCostId(): Long{
        return costsDao.getLastCostId()
    }

    fun fetchAndProcessCosts(buildingId: Long) : Flow<List<Costs>> {
             return costsDao.getCostsForBuilding(buildingId)
    }
}
