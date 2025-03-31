package com.example.delta.data


import com.example.delta.data.entity.Buildings
import com.example.delta.data.model.BuildingsDatabase
import kotlinx.coroutines.flow.Flow

class BuildingRepository (private val db: BuildingsDatabase) {
    private val dao = db.buildingsDao()

    suspend fun insertBuildings(buildings: Buildings) {
        dao.insertBuildings(buildings)
    }

    suspend fun deleteBuildings(buildings: Buildings) {
        dao.deleteBuildings(buildings)
    }

    fun getAllBuildings(): Flow<List<Buildings>> {
        return dao.getAllBuildings()
    }
}

