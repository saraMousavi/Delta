package com.example.delta.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.delta.data.entity.BuildingUsages
import com.example.delta.data.model.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


class BuildingUsageViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)

    private val buildingUsageDao = database.buildingUsageDao()


    fun insertBuildingUsage(buildingUsages: BuildingUsages) {
        viewModelScope.launch {
            buildingUsageDao.insertBuildingUsage(buildingUsages)
        }
    }


    fun deleteBuildingUsage(buildingUsages: BuildingUsages) = viewModelScope.launch {
        buildingUsageDao.deleteBuildingUsage(buildingUsages)
    }

    fun getAllBuildingUsage(): Flow<List<BuildingUsages>> {
        return buildingUsageDao.getAllBuildingUsages()
    }
}
