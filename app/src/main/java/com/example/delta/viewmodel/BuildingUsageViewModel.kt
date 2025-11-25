package com.example.delta.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.delta.data.entity.BuildingUsages
import com.example.delta.data.model.AppDatabase
import com.example.delta.volley.BuildingUsage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class BuildingUsageViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)

    private val buildingUsageDao = database.buildingUsageDao()
    private val _list = MutableStateFlow<List<BuildingUsages>>(emptyList())
    val list: StateFlow<List<BuildingUsages>> = _list

    fun loadFromServer(context: Context) {
        BuildingUsage().fetchBuildingUsages(
            context = context,
            onSuccess = { serverList ->
                _list.value = serverList
            },
            onError = {
                // Optional logging
            }
        )
    }

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

    fun replaceAll(list: List<BuildingUsages>) {
        viewModelScope.launch(Dispatchers.IO) {
            buildingUsageDao.deleteALL()
            buildingUsageDao.insertALL(list)
        }
    }

}
