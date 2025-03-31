package com.example.delta

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.delta.data.entity.Cost
import com.example.delta.data.model.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CostViewModel (application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val costsDao = database.costDao()


    fun insertCost(cost: Cost) = viewModelScope.launch {
        costsDao.insertCost(cost)
    }

    fun deleteCost(cost: Cost) = viewModelScope.launch {
        costsDao.deleteCost(cost)
    }

    fun getAllCost(): Flow<List<Cost>> {
        return costsDao.getAllCost()
    }
}
