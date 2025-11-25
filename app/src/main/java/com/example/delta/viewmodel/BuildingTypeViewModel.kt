package com.example.delta.viewmodel
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.delta.data.entity.BuildingTypes
import com.example.delta.data.model.AppDatabase
import com.example.delta.volley.BuildingType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BuildingTypeViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)

    private val buildingTypeDao = database.buildingTypeDao()
    private val _list = MutableStateFlow<List<BuildingTypes>>(emptyList())
    val list: StateFlow<List<BuildingTypes>> = _list
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

    fun replaceAll(list: List<BuildingTypes>) {
        viewModelScope.launch(Dispatchers.IO) {
            buildingTypeDao.deleteALL()
            buildingTypeDao.insertALL(list)
        }
    }

    fun loadFromServer(context: Context) {
        BuildingType().fetchBuildingTypes(
            context = context,
            onSuccess = { serverList ->
                _list.value = serverList
            },
            onError = {
                // Optional logging
            }
        )
    }
}
