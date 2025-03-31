package com.example.delta

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.delta.data.entity.BuildingType
import com.example.delta.data.entity.Buildings
import com.example.delta.data.model.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class BuildingTypeViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)

    private val buildingTypeDao = database.buildingTypeDao()

    fun insertBuildingType(buildingType: BuildingType) {
        viewModelScope.launch {
            buildingTypeDao.insertBuildingType(buildingType)
        }
    }


    fun deleteBuildingType(buildingType: BuildingType) = viewModelScope.launch {
        buildingTypeDao.deleteBuildingType(buildingType)
    }

    fun getAllBuildingType(): Flow<List<BuildingType>> {
        return buildingTypeDao.getAllBuildingTypes()
    }
}
