package com.example.delta

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.delta.data.entity.BuildingType
import com.example.delta.data.entity.BuildingUsage
import com.example.delta.data.entity.Buildings
import com.example.delta.data.model.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


class BuildingUsageViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)

    private val buildingUsageDao = database.buildingUsageDao()


    fun insertBuildingUsage(buildingUsage: BuildingUsage) {
        viewModelScope.launch {
            buildingUsageDao.insertBuildingUsage(buildingUsage)
        }
    }


    fun deleteBuildingUsage(buildingUsage: BuildingUsage) = viewModelScope.launch {
        buildingUsageDao.deleteBuildingUsage(buildingUsage)
    }

    fun getAllBuildingUsage(): Flow<List<BuildingUsage>> {
        return buildingUsageDao.getAllBuildingUsages()
    }
}
