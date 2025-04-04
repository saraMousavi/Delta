package com.example.delta.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.delta.data.entity.Units
import com.example.delta.data.model.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class UnitsViewModel (application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val unitsDao = database.unitsDao()


    fun insertUnits(units: Units) = viewModelScope.launch {
        unitsDao.insertUnits(units)
    }

    fun deleteUnits(units: Units) = viewModelScope.launch {
        unitsDao.deleteUnits(units)
    }

    fun getAllUnits(): Flow<List<Units>> {
        return unitsDao.getAllUnits()
    }

    fun getLastUnitId(): Long{
        return unitsDao.getLastUnitId()
    }
    fun fetchAndProcessUnits(buildingId: Long) : Flow<List<Units>> {
        return unitsDao.getUnitsForBuilding(buildingId)
    }
}
