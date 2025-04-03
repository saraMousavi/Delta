package com.example.delta.viewmodel
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.delta.data.entity.BuildingTypes
import com.example.delta.data.model.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class BuildingTypeViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)

    private val buildingTypeDao = database.buildingTypeDao()

    fun insertBuildingType(buildingTypes: BuildingTypes) {
        viewModelScope.launch {
            buildingTypeDao.insertBuildingType(buildingTypes)
        }
    }


    fun deleteBuildingType(buildingTypes: BuildingTypes) = viewModelScope.launch {
        buildingTypeDao.deleteBuildingType(buildingTypes)
    }

    fun getAllBuildingType(): Flow<List<BuildingTypes>> {
        return buildingTypeDao.getAllBuildingTypes()
    }
}
