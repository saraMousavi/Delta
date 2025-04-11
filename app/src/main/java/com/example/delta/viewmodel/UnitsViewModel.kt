package com.example.delta.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.delta.data.entity.Units
import com.example.delta.data.model.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UnitsViewModel(application: Application) : AndroidViewModel(application) {
    private val unitDao = AppDatabase.getDatabase(application).unitsDao()



    suspend fun insertUnit(unit: Units): Long {
        return withContext(Dispatchers.IO) {
            unitDao.insertUnit(unit)
        }
    }


    fun updateUnit(unit: Units) {
        viewModelScope.launch(Dispatchers.IO) {
            unitDao.updateUnit(unit)
        }
    }

    fun deleteUnit(unit: Units) {
        viewModelScope.launch(Dispatchers.IO) {
            unitDao.deleteUnit(unit)
        }
    }

    fun getUnitsByBuildingId(buildingId: Int): LiveData<List<Units>> {
        return liveData(Dispatchers.IO) {
            emit(unitDao.getUnitsByBuildingId(buildingId))
        }
    }
}
