package com.example.delta.data


import com.example.delta.data.entity.BuildingType
import com.example.delta.data.model.BuildingTypeDatabase
import kotlinx.coroutines.flow.Flow

class BuildingTypeRepository (private val db: BuildingTypeDatabase) {
    private val dao = db.buildingTypeDao()

    suspend fun insertBuildingType(buildingType: BuildingType) {
        dao.insertBuildingType(buildingType)
    }

    suspend fun deleteBuildingType(buildingType: BuildingType) {
        dao.deleteBuildingType(buildingType)
    }

    fun getAllBuildingType(): Flow<List<BuildingType>> {
        return dao.getAllBuildingTypes()
    }
}
