package com.example.delta.data

import com.example.delta.data.entity.BuildingUsage
import com.example.delta.data.model.BuildingUsageDatabase
import kotlinx.coroutines.flow.Flow


class BuildingUsageRepository (private val db: BuildingUsageDatabase) {
    private val dao = db.buildingUsageDao()

    suspend fun insertbuildingUsage(buildingUsage: BuildingUsage) {
        dao.insertBuildingUsage(buildingUsage)
    }

    suspend fun deletebuildingUsage(buildingUsage: BuildingUsage) {
        dao.deleteBuildingUsage(buildingUsage)
    }

    fun getAllbuildingUsage(): Flow<List<BuildingUsage>> {
        return dao.getAllBuildingUsages()
    }
}
